package io.mapsmessaging.devices.i2c.devices.sensors.lps25;

import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.*;
import org.everit.json.schema.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class JsonHelper {

  public static byte[] unpackJson(JSONObject jsonObject, Lps25Sensor sensor) throws IOException {
    // Interrupt Config Register
    JSONObject response = new JSONObject();
    if (jsonObject.has("interruptConfig")) {
      JSONObject interrupt = new JSONObject();
      response.put("interruptConfig", interrupt);
      JSONObject interruptConfigObj = jsonObject.getJSONObject("interruptConfig");
      if (interruptConfigObj.has("latchInterruptToSource")) {
        boolean latchInterruptToSource = interruptConfigObj.getBoolean("latchInterruptToSource");
        sensor.enableLatchInterrupt(latchInterruptToSource);
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

    // Pressure Threshold register
    if (jsonObject.has("pressureOffset")) {
      int offset =  jsonObject.getInt("pressureOffset");
      sensor.setPressureOffset(offset);
      response.put("pressureOffset", offset);
    }


    // resolution register
    if (jsonObject.has("resolutionReg")) {
      JSONObject resolution = jsonObject.getJSONObject("resolutionReg");
      JSONObject res = new JSONObject();
      response.put("resolutionReg", res);
      if (resolution.has("averageTemperature")) {
        String ave = resolution.getString("averageTemperature");
        TemperatureAverage aver = TemperatureAverage.valueOf(ave);
        sensor.setAverageTemperature(aver);
        res.put("averageTemperature", aver.name());
      }
      if (resolution.has("averagePressure")) {
        String ave = resolution.getString("averagePressure");
        PressureAverage aver = PressureAverage.valueOf(ave);
        sensor.setAveragePressure(aver);
        res.put("averagePressure", aver.name());
      }
    }


    if (jsonObject.has("controlReg1")) {
      JSONObject controlReg1 = jsonObject.getJSONObject("controlReg1");
      JSONObject register1 = new JSONObject();
      response.put("controlReg1", register1);
      // Control Register 1
      if (controlReg1.has("dataRate")) {
        String rate = controlReg1.getString("dataRate");
        DataRate dataRate = DataRate.valueOf(rate);
        sensor.setDataRate(dataRate);
        register1.put("dataRate", dataRate.name());
      }
      if (controlReg1.has("powerDown")) {
        boolean powerDown = controlReg1.getBoolean("powerDown");
        sensor.setPowerDownMode(powerDown);
        register1.put("powerDown", powerDown);
      }
      if (controlReg1.has("interruptGeneration")) {
        boolean lowPassFilterConfig = controlReg1.getBoolean("interruptGeneration");
        sensor.setInterruptGeneration(lowPassFilterConfig);
        register1.put("interruptGeneration", lowPassFilterConfig);
      }
      if (controlReg1.has("blockUpdate")) {
        boolean blockUpdate = controlReg1.getBoolean("blockUpdate");
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
        sensor.reset();
        register2.put("reset", true);
      }
      if (controlReg2.has("enableOneShot")) {
        boolean oneShotEnabled = controlReg2.getBoolean("enableOneShot");
        sensor.enableOneShot(oneShotEnabled);
        register2.put("enableOneShot", oneShotEnabled);
      }
    }

    if (jsonObject.has("controlReg4")) {
      JSONObject controlReg4 = jsonObject.getJSONObject("controlReg4");
      JSONObject register4 = new JSONObject();
      response.put("controlReg4", register4);

      if (controlReg4.has("fifoDrainInterruptEnabled")) {
        boolean fifoEmptyInterrupt = controlReg4.getBoolean("fifoEmptyInterrupt");
        sensor.enabledFiFoEmptyInterrupt(fifoEmptyInterrupt);
        register4.put("fifoDrainInterruptEnabled", fifoEmptyInterrupt);
      }
      if (controlReg4.has("fiFoWatermarkInterrupt")) {
        boolean fifoWatermarkInterruptEnabled = controlReg4.getBoolean("fiFoWatermarkInterrupt");
        sensor.enableFiFoWatermarkInterrupt(fifoWatermarkInterruptEnabled);
        controlReg4.put("fiFoWatermarkInterrupt", fifoWatermarkInterruptEnabled);
      }
      if (controlReg4.has("fiFoOverrunInterrupt")) {
        boolean fifoOverrunInterruptEnabled = controlReg4.getBoolean("fiFoOverrunInterrupt");
        sensor.enableFiFoOverrunInterrupt(fifoOverrunInterruptEnabled);
        register4.put("fiFoOverrunInterrupt", fifoOverrunInterruptEnabled);
      }
      if (controlReg4.has("signalOnInterrupt")) {
        DataReadyInterrupt dataReadyInterrupt = DataReadyInterrupt.valueOf(controlReg4.getString("signalOnInterrupt"));
        sensor.setSignalOnInterrupt(dataReadyInterrupt);
        register4.put("signalOnInterrupt", dataReadyInterrupt);
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
    return response.toString(2).getBytes();
  }

  public static JSONObject packStaticPayload(Lps25Sensor sensor) throws IOException {
    JSONObject jsonObject = new JSONObject();
    // Interrupt Config Register
    JSONObject interruptConfigObj = new JSONObject();
    interruptConfigObj.put("resetAutoRifp", false); // Placeholder value
    interruptConfigObj.put("resetAutoZero", false); // Placeholder value
    interruptConfigObj.put("enableInterrupt", sensor.isInterruptActive());
    interruptConfigObj.put("latchInterruptToSource", sensor.isLatchInterruptEnabled());
    interruptConfigObj.put("latchInterruptToPressureLow", sensor.isLatchInterruptToPressureLow());
    interruptConfigObj.put("latchInterruptToPressureHigh", sensor.isLatchInterruptToPressureHigh());
    jsonObject.put("interruptConfig", interruptConfigObj);

    // Pressure Threshold register
    jsonObject.put("pressureThreshold", sensor.getThresholdPressure());

    // Who Am I register
    jsonObject.put("whoAmI", sensor.whoAmI());

    // resolution register
    JSONObject resolution = new JSONObject();
    resolution.put("averageTemperature", sensor.getAverageTemperature().name());
    resolution.put("averagePressure", sensor.getAveragePressure().name());
    jsonObject.put("resolutionReg", resolution);


    // Control Register 1
    JSONObject controlReg1Obj = new JSONObject();
    controlReg1Obj.put("dataRate", sensor.getDataRate().toString());
    controlReg1Obj.put("blockUpdate", sensor.isBlockUpdateSet());
    controlReg1Obj.put("powerDownMode", sensor.getPowerDownMode());
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
    controlReg3Obj.put("signalOnInterrupt", sensor.isSignalOnInterrupt().name());
    jsonObject.put("controlReg3", controlReg3Obj);

    JSONObject controlReg4Obj = new JSONObject();
    controlReg4Obj.put("fiFoEmptyInterrupt", sensor.isFiFoEmptyEnabled());
    controlReg4Obj.put("fiFoWatermarkInterrupt", sensor.isFiFoWatermarkInterruptEnabled());
    controlReg4Obj.put("fiFoOverrunInterrupt", sensor.isFiFoOverrunInterruptEnabled());
    controlReg4Obj.put("dataReadyInterrupt", sensor.isDataReadyInterrupt());
    jsonObject.put("controlReg3", controlReg3Obj);

    // FiFo Control Register
    JSONObject fifoCtrlObj = new JSONObject();
    fifoCtrlObj.put("fifoMode", sensor.getFifoMode().toString());
    fifoCtrlObj.put("fifoWaterMark", sensor.getFiFoWaterMark());
    jsonObject.put("fifoCtrl", fifoCtrlObj);

    // Reference Pressure Registers
    jsonObject.put("referencePressure", sensor.getReferencePressure());

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

    jsonObject.put("pressureOffset", sensor.getPressureOffset());
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
        .possibleValue("RATE_7_HZ")
        .possibleValue("RATE_12_5_HZ")
        .possibleValue("RATE_25_HZ")
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
            .possibleValue("FIFO_MEAN")
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
    schemaBuilder.addPropertySchema("pressureOffset", NumberSchema.builder().build());


    return schemaBuilder;
  }
}


/*
{
    "fifoCtrl": {
        "fifoMode": "FIFO",
    },
     "controlReg1": {
         "powerDown": true,
        "dataRate": "RATE_1_HZ",
        "blockUpdate": false
    },
     "controlReg2": {
         "reset": true,
        "enableFiFo": true,
        "enableOneShot": false
    },
}
 */


