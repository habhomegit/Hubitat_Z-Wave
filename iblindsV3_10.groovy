/**
 *  Copyright 2020 HAB Home Intelligence
 *  Written by HAB Home Intelligence for iblinds increased compatibility with Hubitat hub
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *      Written by: Chance H, 6-26-20
 *      Update Eric B, 08-14-20 - Fix Configuration and Set Level/Set Position
 *      Update Chance H, 06-11-21 - Add Fingerprint for V3.10+, Add Parameters 7 - 11 for V3.06+
 *      Update Dwayne C, 07-30-21 - Cleaned up log, fixed getbBattery, report battery status, removed tabs, comman in refresh removed 
 */
metadata {
    definition (name: "iblinds V3.11", namespace: "iblinds Hubitat", author: "HAB") {
        capability "Switch Level"
        capability "Actuator"
        capability "Switch"
        capability "Window Shade"   
        capability "Refresh"
        capability "Battery"
   

    //  fingerprint inClusters: "0x26"
        fingerprint type: "1106", cc: "5E,85,59,86,72,5A,73,26,25,80"
        fingerprint mfr:"0287", prod:"0004", model:"0071", deviceJoinName: "iBlinds V3"
        fingerprint mfr:"0287", prod:"0004", model:"0072", deviceJoinName: "iBlinds V3.1"

    simulator {
        status "on":  "command: 2003, payload: FF"
        status "off": "command: 2003, payload: 00"
        status "09%": "command: 2003, payload: 09"
        status "10%": "command: 2003, payload: 0A"
        status "33%": "command: 2003, payload: 21"
        status "66%": "command: 2003, payload: 42"
        status "99%": "command: 2003, payload: 63"

        // reply messages
        reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
        reply "200100,delay 5000,2602": "command: 2603, payload: 00"
        reply "200119,delay 5000,2602": "command: 2603, payload: 19"
        reply "200132,delay 5000,2602": "command: 2603, payload: 32"
        reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
        reply "200163,delay 5000,2602": "command: 2603, payload: 63"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"blind", type: "lighting", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true){
            tileAttribute ("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label:'${name}', action:"switch.off", icon:"https://raw.githubusercontent.com/habhomegit/Smartthings_Z-Wave/master/blind.png", backgroundColor:"#00B200", nextState:"closing"
                attributeState "closed", label:'${name}', action:"switch.on", icon:"https://raw.githubusercontent.com/habhomegit/Smartthings_Z-Wave/master/blind.png", backgroundColor:"#ffffff", nextState:"opening"
                attributeState "opening", label:'${name}', action:"switch.off", icon:"https://raw.githubusercontent.com/habhomegit/Smartthings_Z-Wave/master/blind.png", backgroundColor:"#00B200", nextState:"closing"
                attributeState "closing", label:'${name}', action:"switch.on", icon:"https://raw.githubusercontent.com/habhomegit/Smartthings_Z-Wave/master/blind.png", backgroundColor:"#ffffff", nextState:"opening" 
            }
            
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
            attributeState "level", action:"switch level.setLevel"
        }
            tileAttribute("device.battery", key: "SECONDARY_CONTROL") {
            attributeState "battery", label:'Battery Level: ${currentValue}%', unit:"%"    
            }
    
        }

        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {    
            state "battery", label:'${currentValue}% Battery Level', unit:""
        }
        
        valueTile("levelval", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {    
            state "Level", label:'${currentValue}% Tilt Angle', unit:""
        }
        
        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        }

        main(["blind"])
           details(["blind", "levelval", "battery", "levelSliderControl",  "refresh"])

    }
    
      preferences {
        
        input name: "time", type: "time", title: "Check battery level every day at: ", description: "Enter time", defaultValue: "2019-01-01T12:00:00.000-0600", required: true, displayDuringSetup: true
        input name: "closePosition", type: "number", title: "Close Position", description: "Default OFF Value", defaultValue: 0, required: true
        input name: "NVM_TightLevel",type: "number",title: "Close Interval",defaultValue: 22,range:"16..32",description: "Smaller value will make the blinds close tighter",required: true, displayDuringSetup:true
        input name: "NVM_Direction",type: "bool",title: "Reverse",description: "Reverse Blind Direction", defaultValue: false
        input name: "NVM_Target_Value",type: "number", title: "Default ON Value",defaultValue: 50, range: "1..100",  description: "Used to set the default ON level when manual push button is pushed",required: true, displayDuringSetup:false
        input name: "NVM_Device_Reset_Support",type: "bool",title: "Disable Reset Button", description: "Used for situations where the buttons are being held down accidentally via a tight space, etc.", defaultValue: false
        input name: "Speed_Parameter",type: "number",title: "Open/Close Speed(seconds)",     defaultValue: 0, range:"0..100",    description: "To slow down the blinds, increase the value",required: true, displayDuringSetup: false
        input name: "Init_Calib", type: "bool", title: "Initiate Calibration", defaultValue: false, description: "Will begin calibration after the next command is sent (V3.06+). Change to false when complete", displayDuringSetup: false
        input name: "MinTilt",type: "number", title: "Lower close value",defaultValue: 0, range: "0..25",  description: "Increase if lower interval is closing too tightly.",required: true, displayDuringSetup:false
        input name: "MaxTilt",type: "number", title: "Upper close value",defaultValue: 100, range: "75..100",  description: "Increase if upper interval is closing too tightly.",required: true, displayDuringSetup:false
        input name: "ReMap",type: "bool", title: "Re-map to 0x63",defaultValue: false, description:"Not applicable to Hubitat",required: true, displayDuringSetup:false
        input name: "MultiChange",type: "bool", title: "Allow MultiLevelStopChange",defaultValue: false,  description: "Allows use of MultiLevelStopChange",required: true, displayDuringSetup:false
     }
    
}
def parse(String description) {
    def result = null
    if (description != "updated") {
        // log.info "parse() >> zwave.parse($description)"
        def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
        if (cmd) {
            result = zwaveEvent(cmd)
        }
    }
    if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
        result = [result, response(zwave.basicV1.basicGet())]
        // log.info "Was hailed: requesting state update"
    } else {
        // log.info "Parse returned ${result?.descriptionText}"
    }
    return result
}



def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
    dimmerEvents(cmd)
}

private dimmerEvents(hubitat.zwave.Command cmd) {
    def value = (cmd.value ? "on" : "off")
    def result = [createEvent(name: "switch", value: value)]
    if (cmd.value && cmd.value <= 100) {
        result << createEvent(name: "level", value: cmd.value, unit: "%")
    }
    return result
}

def zwaveEvent(hubitat.zwave.commands.hailv1.Hail cmd) {
    createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.info "manufacturerId:   ${cmd.manufacturerId}"
    log.info "manufacturerName: ${cmd.manufacturerName}"
    log.info "productId:        ${cmd.productId}"
    log.info "productTypeId:    ${cmd.productTypeId}"
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    updateDataValue("MSR", msr)
    createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
    [createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    [:]
}

def on() {
        open()
}

def off() {
        close()
}

def setPosition(value) {
    log.info "$device.displayName Set Position $value"
    setLevel(value)
}

def setLevel(value, duration=0) {
    log.info "$device.displayName Set Level $value, duration $duration"
    def level = Math.max(Math.min(value as Integer, 99), 0)

    if (level <= 0 || level >= 99) {
         sendEvent(name: "switch", value: "off")
         sendEvent(name: "windowShade", value: "closed")
    } else {
        sendEvent(name: "switch", value: "on")
        sendEvent(name: "windowShade", value: "open")
    }

    sendEvent(name: "level", value: level, unit: "%")
    def setLevel = reverse ? 99 - level : level
    def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
    zwave.switchMultilevelV2.switchMultilevelSet(value: setLevel, dimmingDuration: dimmingDuration).format()
}




def zwaveEvent(hubitat.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "${device.displayName} has a low battery"
        log.info "$device.displayName battery is low"
    } else {
        map.value = cmd.batteryLevel
        log.info "$device.displayName battery is ${cmd.batteryLevel}%"
    }
    createEvent(map)
}


def installed () {
    // When device is installed get battery level and set daily schedule for battery refresh
    log.info "$device.displayName Installed"
    runIn(15,getBattery) 
    schedule("$time",getBattery)
    
}

def updated () {
    // When device is updated get battery level and set daily schedule for battery refresh
    log.info "$device.displayName Updating"
    runIn(15,getBattery) 
    schedule("$time",getBattery)
    configureParams()
    //configureParams()
}

def open() {
    log.info "$device.displayName Open"
    // Blinds fully open at 50%
    sendEvent(name: "windowShade", value: "open")
    sendEvent(name: "switch",value: "on")
    sendEvent(name: "level", value: NVM_Target_Value, unit: "%", displayed: true)
    zwave.switchMultilevelV3.switchMultilevelSet(value: NVM_Target_Value).format()
}

def close() {
    log.info "$device.displayName Close"
    Integer level = reverse ? 99 : 0

    sendEvent(name: "windowShade", value: "closed")
    sendEvent(name: "switch",value: "off")
    sendEvent(name: "level", value: closePosition, unit: "%", displayed: true)
    zwave.switchMultilevelV3.switchMultilevelSet(value: closePosition).format()
}

def refresh() {
    log.info "$device.displayName Refresh"
    delayBetween([
        zwave.switchMultilevelV1.switchMultilevelGet().format(),
        zwave.batteryV1.batteryGet().format()
    ], 3000)
}

def getBattery() {
    delayBetween([zwave.batteryV1.batteryGet().format()],3000)
}

def configureParams() {

    /*
    Parameter No.   |   Size    |    Parameter Name      |          Desc.
    1                    1            NVM_TightLevel                Auto Calibration tightness
    2                    1            NVM_Direction                 Reverse the direction of iblinds
    3                    1            NVM_Target_Report             Not used ****
    4                    1            NVM_Target_Value              Default on position
    5                    1            NVM_Device_Reset_Support      Turns off the reset button
    6                    1            Speed_Parameter               Speed
    7                    1            Init_Calib                    Initiate Calibration 
    8                    1            MinTilt                       Minimum Tilt value
    9                    1            MaxTilt                       Maximum Tilt value
    10                   1            ReMap                         Remap 0xFF to 0x63 for certain hubs
    11                   1            MultiChange                   Allow for MultiLevelStopChange
    */
    
    // Set Boolean Values     
    def NVM_Direction_Val 
    def NVM_Device_Reset_Val 
    NVM_Direction_Val = boolToInteger(NVM_Direction)
    NVM_Device_Reset_Val = boolToInteger(NVM_Device_Reset_Support)

    log.info "$device.displayName Configuration Started"
    def cmds = []   
    
    // If paramater value has changed then add zwave configration command to cmds 
    /* exmaple: 
       secureCmd(zwave.configurationV1.configurationSet(scaledConfigurationValue: value, parameterNumber: parameterNumber, size: size))
    */
    
    
    if (state.param1 != NVM_TightLevel) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, configurationValue: [NVM_TightLevel.toInteger()]).format()  
    }
    if (state.param2 != NVM_Direction) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, configurationValue: [NVM_Direction_Val.toInteger()]).format()  
    }
    if (state.param3 != 0 ) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, configurationValue: [0]).format()  
    }
    if (state.param4 != NVM_Target_Value ) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [NVM_Target_Value.toInteger()]).format()  
    }
    if (state.param5 != NVM_Device_Reset_Support ) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, configurationValue: [NVM_Device_Reset_Val.toInteger()]).format()  
    }
    if ( state.param6 != Speed_Parameter ) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 6, size: 1, configurationValue: [Speed_Parameter.toInteger()]).format()  
    }
    if (Init_Calib != null && state.param7 != Init_Calib) {
        def Init_Calib = boolToInteger(Init_Calib)

        cmds << zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1, configurationValue: [Init_Calib.toInteger()]).format()
    }
    if ( state.param8 != MinTilt ) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1, configurationValue: [MinTilt.toInteger()]).format()  
    }
    if ( state.param9 != MaxTilt ) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, configurationValue: [MaxTilt.toInteger()]).format()  
    }
    if ( state.param10 != ReMap ) {
        def ReMap = boolToInteger(ReMap)
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, configurationValue: [ReMap.toInteger()]).format()  
    }
    if ( state.param11 != MultiChange ) {
        def MultiChange = boolToInteger(MultiChange)
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, configurationValue: [MultiChange.toInteger()]).format()  
    }    
    
        // log.info "Cmds: " + cmds
        commands(cmds) 
        storeParamState()

    log.info "$device.displayName Configuration Complete"
}

private commands(cmds) { 
    //temporarily solve the non z-wave prefernce change by adding a try catch block. 
    try {
        //Use senHubCommands to fire off Z-Wave Command
        sendHubCommand(new hubitat.device.HubMultiAction(delayBetween(cmds,500), hubitat.device.Protocol.ZWAVE))
    } catch (err) {
        log.info "$device.displayName Z-Wave Parameter was not changed."
    }
}


private storeParamState() {
    log.info "$device.displayName Storing Paramater Values"
    state.param1 = NVM_TightLevel
    state.param2 = NVM_Direction
    state.param3 = 0  // Not used at the moment 
    state.param4 = NVM_Target_Value
    state.param5 = NVM_Device_Reset_Support
    state.param6 = Speed_Parameter
    state.param7 = Init_Calib
    state.param8 = MinTilt
    state.param9 = MaxTilt
    state.param10 = ReMap
    state.param11 = MultiChange
    
}

def boolToInteger(boolValue) {
    def result = null
    if(boolValue) {
        result = 1 
    }
    else
    {
        result = 0                     
    }
    return result 
}

String secureCmd(cmd) {
    if (getDataValue("zwaveSecurePairingComplete") == "true" && getDataValue("S2") == null) {
        return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        return secure(cmd)
    }    
}
