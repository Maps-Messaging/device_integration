package io.mapsmessaging.devices.i2c.devices.sensors.lps35;

import io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers.*;
import org.everit.json.schema.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class JsonHelper {

  public static byte[] unpackJson(JSONObject jsonObject, Lps35Sensor sensor) throws IOException {
    // Interrupt Config Register
    JSONObject response = new JSONObject();
    if (jsonObject.has("interruptConfig")) {
      JSONObject interrupt = new JSONObject();
      response.put("interruptConfig", interrupt);
      JSONObject interruptConfigObj = jsonObject.getJSONObject("interruptConfig");
      if (interruptConfigObj.has("autoRifpEnabled")) {
        boolean autoRifpEnabled = interruptConfigObj.getBoolean("autoRifpEnabled");
        sensor.enableAutoRifp(autoRifpEnabled);
        interrupt.put("autoRifpEnabled", autoRifpEnabled);
      }
      if (interruptConfigObj.has("autoZeroEnabled")) {
        boolean autoZeroEnabled = interruptConfigObj.getBoolean("autoZeroEnabled");
        sensor.enableAutoZero(autoZeroEnabled);
        interrupt.put("autoZeroEnabled", autoZeroEnabled);
      }
      if (interruptConfigObj.has("interruptEnabled")) {
        boolean interruptEnabled = interruptConfigObj.getBoolean("interruptEnabled");
        sensor.enableInterrupt(interruptEnabled);
        interrupt.put("interruptEnabled", interruptEnabled);
      }
      if (interruptConfigObj.has("latchInterruptToSource")) {
        boolean latchInterruptToSource = interruptConfigObj.getBoolean("latchInterruptToSource");
        sensor.latchInterruptToSource(latchInterruptToSource);
        interrupt.put("latchInterruptToSource", latchInterruptToSource);
      }
      if (interruptConfigObj.has("latchInterruptToPressureLow")) {
        boolean latchInterruptToPressureLow = interruptConfigObj.getBoolean("latchInterruptToPressureLow");
        sensor.latchInterruptToPressureLow(latchInterruptToPressureLow);
        interrupt.put("latchInterruptToPressureLow", latchInterruptToPressureLow);
      }
      if (interruptConfigObj.has("latchInterruptToPressureHigh")) {
        boolean latchInterruptToPressureHigh = interruptConfigObj.getBoolean("latchInterruptToPressureHigh");
        sensor.latchInterruptToPressureHigh(latchInterruptToPressureHigh);
        interrupt.put("latchInterruptToPressureHigh", latchInterruptToPressureHigh);
      }
    }

    // Pressure Threshold register
    if (jsonObject.has("pressureThreshold")) {
      float thresholdPressure = (float) jsonObject.getDouble("pressureThreshold");
      sensor.setThresholdPressure(thresholdPressure);
      response.put("pressureThreshold", thresholdPressure);
    }

    if (jsonObject.has("controlReg1")) {
      JSONObject controlReg1 = jsonObject.getJSONObject("controlReg1");
      JSONObject register1 = new JSONObject();
      response.put("controlReg1", register1);
      // Control Register 1
      if (controlReg1.has("dataRate")) {
        String rate = jsonObject.getString("dataRate");
        DataRate dataRate = DataRate.valueOf(rate);
        sensor.setDataRate(dataRate);
        register1.put("dataRate", dataRate.name());
      }
      if (controlReg1.has("lowPassFilter")) {
        boolean lowPassFilter = jsonObject.getBoolean("lowPassFilter");
        sensor.setLowPassFilter(lowPassFilter);
        register1.put("lowPassFilter", lowPassFilter);
      }
      if (controlReg1.has("lowPassFilterConfig")) {
        boolean lowPassFilterConfig = jsonObject.getBoolean("lowPassFilterConfig");
        sensor.setLowPassFilterConfig(lowPassFilterConfig);
        register1.put("lowPassFilterConfig", lowPassFilterConfig);
      }
      if (controlReg1.has("blockUpdate")) {
        boolean blockUpdate = jsonObject.getBoolean("blockUpdate");
        sensor.setBlockUpdate(blockUpdate);
        register1.put("blockUpdate", blockUpdate);
      }
    }

    // Control Register 2
    if (jsonObject.has("controlReg2")) {
      JSONObject controlReg2 = jsonObject.getJSONObject("controlReg2");
      JSONObject register2 = new JSONObject();
      response.put("controlReg2", register2);

      if (controlReg2.has("boot")) {
        sensor.boot();
        register2.put("boot", true);
      }
      if (controlReg2.has("enableFiFo")) {
        boolean fifoEnabled = controlReg2.getBoolean("enableFiFo");
        sensor.enableFiFo(fifoEnabled);
        register2.put("fifoEnabled", fifoEnabled);
      }
      if (controlReg2.has("stopFiFoOnThresholdEnabled")) {
        boolean stopFiFoOnThresholdEnabled = controlReg2.getBoolean("stopFiFoOnThresholdEnabled");
        sensor.enableStopFiFoOnThreshold(stopFiFoOnThresholdEnabled);
        register2.put("stopFiFoOnThresholdEnabled", stopFiFoOnThresholdEnabled);

      }
      if (controlReg2.has("reset")) {
        sensor.softReset();
        register2.put("reset", true);
      }
      if (controlReg2.has("enableOneShot")) {
        boolean oneShotEnabled = controlReg2.getBoolean("enableOneShot");
        sensor.enableOneShot(oneShotEnabled);
        register2.put("enableOneShot", oneShotEnabled);
      }
    }
    if (jsonObject.has("controlReg3")) {
      JSONObject controlReg3 = jsonObject.getJSONObject("controlReg3");
      JSONObject register3 = new JSONObject();
      response.put("controlReg3", register3);

      if (controlReg3.has("fifoDrainInterruptEnabled")) {
        boolean fifoDrainInterruptEnabled = controlReg3.getBoolean("fifoDrainInterruptEnabled");
        sensor.enableFiFoDrainInterrupt(fifoDrainInterruptEnabled);
        register3.put("fifoDrainInterruptEnabled", fifoDrainInterruptEnabled);
      }
      if (controlReg3.has("fiFoWatermarkInterrupt")) {
        boolean fifoWatermarkInterruptEnabled = controlReg3.getBoolean("fiFoWatermarkInterrupt");
        sensor.enableFiFoWatermarkInterrupt(fifoWatermarkInterruptEnabled);
        register3.put("fiFoWatermarkInterrupt", fifoWatermarkInterruptEnabled);
      }
      if (controlReg3.has("fiFoOverrunInterrupt")) {
        boolean fifoOverrunInterruptEnabled = controlReg3.getBoolean("fiFoOverrunInterrupt");
        sensor.enableFiFoOverrunInterrupt(fifoOverrunInterruptEnabled);
        register3.put("fiFoOverrunInterrupt", fifoOverrunInterruptEnabled);
      }
      if (controlReg3.has("signalOnInterrupt")) {
        DataReadyInterrupt dataReadyInterrupt = DataReadyInterrupt.valueOf(controlReg3.getString("signalOnInterrupt"));
        sensor.setSignalOnInterrupt(dataReadyInterrupt);
        register3.put("signalOnInterrupt", dataReadyInterrupt);
      }
    }
    // FiFo Control Register
    if (jsonObject.has("fifoCtrl")) {
      JSONObject fifoControl = jsonObject.getJSONObject("fifoCtrl");
      JSONObject fifo = new JSONObject();
      response.put("fifoCtrl", fifo);

      if (fifoControl.has("fifoMode")) {
        FiFoMode mode = FiFoMode.valueOf(fifoControl.getString("fifoMode"));
        sensor.setFifoMode(mode);
        fifo.put("fifoMode", mode.name());
      }
      if (fifoControl.has("fifoWaterMark")) {
        int fifoWatermark = fifoControl.getInt("fifoWaterMark");
        sensor.setFiFoWaterMark(fifoWatermark);
        fifo.put("fifoWaterMark", fifoWatermark);
      }
    }
    // Reference Pressure Registers
    if (jsonObject.has("referencePressure")) {
      int referencePressure = jsonObject.getInt("referencePressure");
      sensor.setReferencePressure(referencePressure);
      response.put("referencePressure", referencePressure);
    }

    // Low Power Mode Registers
    if (jsonObject.has("lowPowerModeEnabled")) {
      boolean lowPowerModeEnabled = jsonObject.getBoolean("lowPowerModeEnabled");
      sensor.setLowPowerMode(lowPowerModeEnabled);
      response.put("lowPowerModeEnabled", lowPowerModeEnabled);
    }
    return response.toString(2).getBytes();
  }

  public static JSONObject pack(Lps35Sensor sensor) throws IOException {
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
    jsonObject.put("pressureThreshold", sensor.getThresholdPressure());

    // Who Am I register
    jsonObject.put("whoAmI", sensor.whoAmI());

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
    jsonObject.put("referencePressure", sensor.getReferencePressure());
    jsonObject.put("lowPowerMode", sensor.isLowPowerModeEnabled());

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
    JSONArray statusJsonArray = new JSONArray();
    Status[] statusArray = sensor.getStatus();
    for (Status status : statusArray) {
      statusJsonArray.put(status.toString());
    }
    jsonObject.put("status", statusJsonArray);

    // Print the combined JSON object
    return jsonObject;
  }

  public static ObjectSchema.Builder generateSchema() {
    // Define the JSON Schema manually based on the structure of the JSON object
    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    ObjectSchema.Builder interruptConfigBuilder = ObjectSchema.builder();

    // Define properties for interruptConfig
    interruptConfigBuilder.addPropertySchema("enableAutoRifp", BooleanSchema.builder().build());
    interruptConfigBuilder.addPropertySchema("isAutoZeroEnabled", BooleanSchema.builder().build());
    interruptConfigBuilder.addPropertySchema("resetAutoRifp", BooleanSchema.builder().build());
    interruptConfigBuilder.addPropertySchema("resetAutoZero", BooleanSchema.builder().build());
    interruptConfigBuilder.addPropertySchema("enableInterrupt", BooleanSchema.builder().build());
    interruptConfigBuilder.addPropertySchema("latchInterruptToSource", BooleanSchema.builder().build());
    interruptConfigBuilder.addPropertySchema("latchInterruptToPressureLow", BooleanSchema.builder().build());
    interruptConfigBuilder.addPropertySchema("latchInterruptToPressureHigh", BooleanSchema.builder().build());
    schemaBuilder.addPropertySchema("interruptConfig", interruptConfigBuilder.build());

    // Define properties for pressureThreshold
    schemaBuilder.addPropertySchema("pressureThreshold", NumberSchema.builder().build());

    // Define properties for whoAmI
    ObjectSchema.Builder whoAmIBuilder = ObjectSchema.builder();
    whoAmIBuilder.addPropertySchema("whoAmI", NumberSchema.builder().build());
    schemaBuilder.addPropertySchema("whoAmI", whoAmIBuilder.build());

    // Define properties for controlReg1
    ObjectSchema.Builder controlReg1Builder = ObjectSchema.builder();
    controlReg1Builder.addPropertySchema("dataRate", EnumSchema.builder()
        .possibleValue("RATE_ONE_SHOT")
        .possibleValue("RATE_1_HZ")
        .possibleValue("RATE_10_HZ")
        .possibleValue("RATE_25_HZ")
        .possibleValue("RATE_50_HZ")
        .possibleValue("RATE_75_HZ")
        .build());
    controlReg1Builder.addPropertySchema("lowPassFilter", BooleanSchema.builder().build());
    controlReg1Builder.addPropertySchema("lowPassFilterConfig", BooleanSchema.builder().build());
    controlReg1Builder.addPropertySchema("blockUpdate", BooleanSchema.builder().build());
    schemaBuilder.addPropertySchema("controlReg1", controlReg1Builder.build());

    // Define properties for controlReg2
    ObjectSchema.Builder controlReg2Builder = ObjectSchema.builder();
    controlReg2Builder.addPropertySchema("boot", BooleanSchema.builder().build());
    controlReg2Builder.addPropertySchema("enableFiFo", BooleanSchema.builder().build());
    controlReg2Builder.addPropertySchema("stopFiFoOnThreshold", BooleanSchema.builder().build());
    controlReg2Builder.addPropertySchema("reset", BooleanSchema.builder().build());
    controlReg2Builder.addPropertySchema("enableOneShot", BooleanSchema.builder().build());
    schemaBuilder.addPropertySchema("controlReg2", controlReg2Builder.build());

    // Define properties for controlReg3
    ObjectSchema.Builder controlReg3Builder = ObjectSchema.builder();
    controlReg3Builder.addPropertySchema("interruptActive", BooleanSchema.builder().build());
    controlReg3Builder.addPropertySchema("pushPullDrainActive", BooleanSchema.builder().build());
    controlReg3Builder.addPropertySchema("fiFoDrainInterrupt", BooleanSchema.builder().build());
    controlReg3Builder.addPropertySchema("fiFoWatermarkInterrupt", BooleanSchema.builder().build());
    controlReg3Builder.addPropertySchema("fiFoOverrunInterrupt", BooleanSchema.builder().build());
    controlReg3Builder.addPropertySchema("signalOnInterrupt", EnumSchema.builder()
        .possibleValue("ORDER_OF_PRIORITY")
        .possibleValue("HIGH")
        .possibleValue("LOW")
        .possibleValue("LOW_OR_HIGH")
        .build());
    schemaBuilder.addPropertySchema("controlReg3", controlReg3Builder.build());
    // Define properties for fifoCtrl
    ObjectSchema.Builder fifoCtrlBuilder = ObjectSchema.builder();
    fifoCtrlBuilder.addPropertySchema("fifoMode", EnumSchema.builder()
            .possibleValue("BYPASS")
            .possibleValue("FIFO")
            .possibleValue("STREAM")
            .possibleValue("STREAM_TO_FIFO")
            .possibleValue("BYPASS_TO_STREAM")
            .possibleValue("DYNAMIC_STREAM")
            .possibleValue("BYPASS_TO_FIFO")

            .build())
        .addPropertySchema("status", StringSchema.builder().build())
        .build();
    fifoCtrlBuilder.addPropertySchema("fifoWaterMark", NumberSchema.builder().build());
    schemaBuilder.addPropertySchema("fifoCtrl", fifoCtrlBuilder.build());

    // Define properties for referencePressure
    schemaBuilder.addPropertySchema("referencePressure", NumberSchema.builder().build());

    // Define properties for lowPowerMode
    schemaBuilder.addPropertySchema("lowPowerMode", BooleanSchema.builder().build());

    // Define properties for interruptSource
    ObjectSchema.Builder interruptSourceBuilder = ObjectSchema.builder();
    interruptSourceBuilder.addPropertySchema("source", EnumSchema.builder()
            .possibleValue("BOOT")
            .possibleValue("INTERRUPT_ACTIVE")
            .possibleValue("PRESSURE_LOW")
            .possibleValue("PRESSURE_HIGH")
            .build())
        .addPropertySchema("status", StringSchema.builder().build())
        .build();

    interruptSourceBuilder.addPropertySchema("interruptSource", interruptSourceBuilder.build());
    schemaBuilder.addPropertySchema("interruptSource", interruptSourceBuilder.build());

    // Define properties for fifoStatus
    ObjectSchema.Builder fifoStatusBuilder = ObjectSchema.builder();
    fifoStatusBuilder.addPropertySchema("hitThreshold", BooleanSchema.builder().build());
    fifoStatusBuilder.addPropertySchema("isOverwritten", BooleanSchema.builder().build());
    fifoStatusBuilder.addPropertySchema("size", NumberSchema.builder().build());
    schemaBuilder.addPropertySchema("fifoStatus", fifoStatusBuilder.build());

    // Define properties for deviceStatus
    ObjectSchema.Builder deviceStatusBuilder = ObjectSchema.builder();

    deviceStatusBuilder.addPropertySchema("deviceStatus",
        ObjectSchema.builder()
            .addPropertySchema("status", EnumSchema.builder()
                .possibleValue("TEMPERATURE_OVERRUN")
                .possibleValue("PRESSURE_OVERRUN")
                .possibleValue("TEMPERATURE_DATA_AVAILABLE")
                .possibleValue("PRESSURE_DATA_AVAILABLE")
                .build())
            .addPropertySchema("status", StringSchema.builder().build())
            .build());
    // Define properties for deviceStatus
    schemaBuilder.addPropertySchema("deviceStatus", deviceStatusBuilder.build());


    return schemaBuilder;
  }
}



