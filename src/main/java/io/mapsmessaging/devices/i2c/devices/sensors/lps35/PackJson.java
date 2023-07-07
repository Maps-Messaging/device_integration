package io.mapsmessaging.devices.i2c.devices.sensors.lps35;

import io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers.FiFoStatus;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers.InterruptSource;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers.Status;
import org.json.JSONArray;
import org.json.JSONObject;

public class PackJson {

  public static JSONObject pack (Lps35Sensor sensor) {
    JSONObject jsonObject = new JSONObject();
    // Interrupt Config Register
    JSONObject interruptConfigObj = new JSONObject();
    interruptConfigObj.put("enableAutoRifp", sensor.isAutoRifpEnabled());
    interruptConfigObj.put("isAutoZeroEnabled", sensor.isAutoZeroEnabled());
    interruptConfigObj.put("resetAutoRifp", false); // Placeholder value
    interruptConfigObj.put("resetAutoZero", false); // Placeholder value
    interruptConfigObj.put("enableInterrupt", sensor.isInterruptEnabled());
    interruptConfigObj.put("latchInterruptToSource", sensor.isLatchInterruptToSource());
    interruptConfigObj.put("latchInterruptToPressureLow", sensor.isLatchInterruptToPressureLow());
    interruptConfigObj.put("latchInterruptToPressureHigh", sensor.isLatchInterruptToPressureHigh());
    jsonObject.put("interruptConfig", interruptConfigObj);

    // Pressure Threshold register
    JSONObject pressureThresholdObj = new JSONObject();
    pressureThresholdObj.put("thresholdPressure", sensor.getThresholdPressure());
    jsonObject.put("pressureThreshold", pressureThresholdObj);

    // Who Am I register
    JSONObject whoAmIObj = new JSONObject();
    whoAmIObj.put("whoAmI", sensor.whoAmI());
    jsonObject.put("whoAmI", whoAmIObj);

    // Control Register 1
    JSONObject controlReg1Obj = new JSONObject();
    controlReg1Obj.put("dataRate", sensor.getDataRate().toString());
    controlReg1Obj.put("lowPassFilter", sensor.isLowPassFilterSet());
    controlReg1Obj.put("lowPassFilterConfig", sensor.isLowPassFilterConfigSet());
    controlReg1Obj.put("blockUpdate", sensor.isBlockUpdateSet());
    jsonObject.put("controlReg1", controlReg1Obj);

    // Control Register 2
    JSONObject controlReg2Obj = new JSONObject();
    controlReg2Obj.put("boot", false); // Placeholder value
    controlReg2Obj.put("enableFiFo", sensor.isFiFoEnabled());
    controlReg2Obj.put("stopFiFoOnThreshold", sensor.isStopFiFoOnThresholdEnabled());
    controlReg2Obj.put("reset", false); // Placeholder value
    controlReg2Obj.put("enableOneShot", sensor.isOneShotEnabled());
    jsonObject.put("controlReg2", controlReg2Obj);

    // Control Register 3
    JSONObject controlReg3Obj = new JSONObject();
    controlReg3Obj.put("interruptActive", sensor.isInterruptActive());
    controlReg3Obj.put("pushPullDrainActive", sensor.isPushPullDrainActive());
    controlReg3Obj.put("fiFoDrainInterrupt", sensor.isFiFoDrainInterruptEnabled());
    controlReg3Obj.put("fiFoWatermarkInterrupt", sensor.isFiFoWatermarkInterruptEnabled());
    controlReg3Obj.put("fiFoOverrunInterrupt", sensor.isFiFoOverrunInterruptEnabled());
    controlReg3Obj.put("signalOnInterrupt", sensor.isSignalOnInterrupr().toString());
    jsonObject.put("controlReg3", controlReg3Obj);

    // FiFo Control Register
    JSONObject fifoCtrlObj = new JSONObject();
    fifoCtrlObj.put("fifoMode", sensor.getFifoMode().toString());
    fifoCtrlObj.put("fifoWaterMark", sensor.getFiFoWaterMark());
    jsonObject.put("fifoCtrl", fifoCtrlObj);

    // Reference Pressure Registers
    JSONObject referencePressureObj = new JSONObject();
    referencePressureObj.put("referencePressure", sensor.getReferencePressure());
    jsonObject.put("referencePressure", referencePressureObj);

    // Low Power Mode Registers
    JSONObject lowPowerModeObj = new JSONObject();
    lowPowerModeObj.put("lowPowerMode", sensor.isLowPowerModeEnabled());
    jsonObject.put("lowPowerMode", lowPowerModeObj);

    // Interrupt Source Register
    JSONObject interruptSourceObj = new JSONObject();
    InterruptSource[] interruptSources = sensor.getInterruptSource();
    JSONArray interruptSourceJsonArray = new JSONArray();
    for (InterruptSource source : interruptSources) {
      interruptSourceJsonArray.put(source.toString());
    }
    interruptSourceObj.put("interruptSource", interruptSourceJsonArray);
    jsonObject.put("interruptSource", interruptSourceObj);

    // FiFo Status Register
    JSONObject fifoStatusObj = new JSONObject();
    FiFoStatus fifoStatus = sensor.getFiFoStatus();
    fifoStatusObj.put("hitThreshold", fifoStatus.isHitThreshold());
    fifoStatusObj.put("isOverwritten", fifoStatus.isOverwritten());
    fifoStatusObj.put("size", fifoStatus.getSize());
    jsonObject.put("fifoStatus", fifoStatusObj);

    // Device Status Register
    JSONObject deviceStatusObj = new JSONObject();
    JSONArray statusJsonArray = new JSONArray();
    Status[] statusArray = sensor.getStatus();
    for (Status status : statusArray) {
      statusJsonArray.put(status.toString());
    }
    deviceStatusObj.put("status", statusJsonArray);
    jsonObject.put("deviceStatus", deviceStatusObj);

    // Print the combined JSON object
    return jsonObject;
  }
}
