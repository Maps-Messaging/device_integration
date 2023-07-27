/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.web;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.onewire.OneWireDeviceController;
import io.mapsmessaging.devices.spi.SpiDeviceController;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleWebAccess {

  private Javalin app;
  private final DeviceBusManager deviceBusManager;

  public SimpleWebAccess() {
    deviceBusManager = DeviceBusManager.getInstance();
    scan();
    Map<String, Object> deviceConfig = new LinkedHashMap<>();
    deviceConfig.put("spiBus", "0");
    deviceConfig.put("spiMode", "0");
    deviceConfig.put("spiChipSelect", "0");
    deviceConfig.put("resolution", "12");
    deviceConfig.put("channels", "8");

    Map<String, Object> map = new LinkedHashMap<>();
    map.put("Mcp3y0x", deviceConfig);
    deviceBusManager.getSpiBusManager().configureDevices(map);
  }

  protected void scan() {
    for (I2CBusManager manager : deviceBusManager.getI2cBusManager()) {
      manager.scanForDevices();
    }
  }

  public static void main(String[] args) {
    SimpleWebAccess simpleWebAccess = new SimpleWebAccess();
    simpleWebAccess.startServer();
  }

  private void startServer() {
    app = Javalin.create().start(7001);
    app.get("/device/exit", ctx -> {
      app.stop();
      System.exit(0);
        });
    app.get("/device/list", ctx -> {
      JSONObject jsonObject = new JSONObject();

      jsonObject.put("i2c[0]", packList(deviceBusManager.getI2cBusManager()[0].getActive()));
      jsonObject.put("i2c[1]", packList(deviceBusManager.getI2cBusManager()[1].getActive()));
      jsonObject.put("1Wire", packList(deviceBusManager.getOneWireBusManager().getActive()));
      jsonObject.put("spi", packList(deviceBusManager.getSpiBusManager().getActive()));
      ctx.json(jsonObject.toString(2));
    });

    //<editor-fold desc="I2C handler">
    // Add the I2C bus
    app.get("/device/i2c/{bus}/scan", ctx -> {
      int bus = Integer.parseInt(ctx.pathParam("bus"));
      List<Integer> found = deviceBusManager.getI2cBusManager()[bus].findDevicesOnBus();
      List<String> map = deviceBusManager.getI2cBusManager()[bus].listDetected(found);
      JSONArray jsonArray = new JSONArray();
      for(String line:map){
        jsonArray.put(line);
      }
      JSONObject json = new JSONObject();
      json.put("I2C_Detect", jsonArray);
      ctx.json(json.toString(2));
    });

    app.get("/device/i2c/{bus}/{id}", ctx -> {
      int bus = Integer.parseInt(ctx.pathParam("bus"));
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager()[bus].get(id);
      if (device != null) {
        try {
          handleDeviceGet(ctx, device);
        } catch (IOException e) {
          deviceBusManager.getI2cBusManager()[bus].close(device);
        }
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    // I2C get Schema
    app.get("/device/i2c/{bus}/{id}/schema", ctx -> {
      int bus = Integer.parseInt(ctx.pathParam("bus"));
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager()[bus].get(id);
      if (device != null) {
        handleGetSchema(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    // I2C get config
    app.get("/device/i2c/{bus}/{id}/config", ctx -> {
      int bus = Integer.parseInt(ctx.pathParam("bus"));
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager()[bus].get(id);
      if (device != null) {
        try {
          handleGetStatic(ctx, device);
        } catch (IOException ex) {
          deviceBusManager.getI2cBusManager()[bus].close(device);
        }
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    // I2C post request handler
    app.post("/device/i2c/{bus}/{id}/config", ctx -> {
      int bus = Integer.parseInt(ctx.pathParam("bus"));
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager()[bus].get(id);
      if (device != null) {
        try {
          ctx.status(200);
          ctx.json(new String(device.updateDeviceConfiguration(ctx.body().getBytes())));
        } catch (IOException e) {
          deviceBusManager.getI2cBusManager()[bus].close(device);
          ctx.status(400).result("Internal Error:" + e.getMessage());
        }
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    // I2C get config
    app.get("/device/i2c/{bus}/{id}/registers", ctx -> {
      int bus = Integer.parseInt(ctx.pathParam("bus"));
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager()[bus].get(id);
      if (device != null) {
        try {
          handleGetRegisters(ctx, device);
        } catch (IOException ex) {
          deviceBusManager.getI2cBusManager()[bus].close(device);
        }
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    // I2C get functions
    app.get("/device/i2c/{bus}/{id}/function", ctx -> {
      int bus = Integer.parseInt(ctx.pathParam("bus"));
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager()[bus].get(id);
      if (device != null) {
        handleGetFunctions(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    // I2C get functions
    app.get("/device/i2c/{bus}/{id}/function/{function}", ctx -> {
      int bus = Integer.parseInt(ctx.pathParam("bus"));
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager()[bus].get(id);
      if (device != null) {
        // execute requested function on device
        handleFunction(ctx, device, ctx.pathParam("function"));
      } else {
        ctx.status(404).result("Device not found");
      }
    });


    //</editor-fold>

    //<editor-fold desc="SPI handler">
    // Add the SPI Bus
    app.get("/device/spi/{id}", ctx -> {
      String id = ctx.pathParam("id");
      SpiDeviceController device = deviceBusManager.getSpiBusManager().get(id);
      if (device != null) {
        handleDeviceGet(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    app.get("/device/spi/{id}/schema", ctx -> {
      String id = ctx.pathParam("id");
      SpiDeviceController device = deviceBusManager.getSpiBusManager().get(id);
      if (device != null) {
        handleGetSchema(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    app.get("/device/spi/{id}/static", ctx -> {
      String id = ctx.pathParam("id");
      SpiDeviceController device = deviceBusManager.getSpiBusManager().get(id);
      if (device != null) {
        handleGetStatic(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    app.post("/device/spi/{id}", ctx -> {
      String id = ctx.pathParam("id");
      SpiDeviceController device = deviceBusManager.getSpiBusManager().get(id);
      if (device != null) {
        ctx.status(200);
        ctx.json(new String(device.updateDeviceConfiguration(ctx.body().getBytes())));
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    //</editor-fold>

    // 1-Wire handler
    app.get("/device/1wire/{id}", ctx -> {
      String id = ctx.pathParam("id");
      OneWireDeviceController device = deviceBusManager.getOneWireBusManager().get(id);
      if (device != null) {
        handleDeviceGet(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    app.get("/device/1wire/{id}/schema", ctx -> {
      String id = ctx.pathParam("id");
      OneWireDeviceController device = deviceBusManager.getOneWireBusManager().get(id);
      if (device != null) {
        handleGetSchema(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });
  }


  private void handleGetRegisters(Context ctx, DeviceController deviceController) throws IOException {
    String res = deviceController.getName()+" - "+deviceController.getDescription()+"\n";
    if(deviceController instanceof I2CDeviceController){
      res += ((I2CDeviceController)deviceController).getDevice().registerMap.toString();
    }
    ctx.result(res);
  }

  private void handleFunction(Context ctx, DeviceController deviceController, String function) throws IOException {
    if(deviceController instanceof I2CDeviceController) {
      I2CDevice device = ((I2CDeviceController) deviceController).getDevice();
      synchronized (I2CDeviceScheduler.getI2cBusLock()) {
        if (device instanceof PowerManagement) {
          if (function.equalsIgnoreCase("powerOn")) {
            ((PowerManagement) device).powerOn();
            ctx.result("Power on executed");
            return;
          }
          if (function.equalsIgnoreCase("powerOff")) {
            ((PowerManagement) device).powerOff();
            ctx.result("Power off executed");
            return;
          }
        }
        if (device instanceof Resetable) {
          if (function.equalsIgnoreCase("reset")) {
            ((Resetable) device).reset();
            ctx.result("Reset executed");
            return;
          }
          if (function.equalsIgnoreCase("softReset")) {
            ((Resetable) device).softReset();
            ctx.result("soft Reset executed");
            return;
          }
        }
      }
    }
    ctx.result("unknown or unhandled function received "+function);
  }

  private void handleGetFunctions(Context ctx, DeviceController deviceController)  {
    JSONObject schemaObject = new JSONObject();
    JSONArray functionList = new JSONArray();
    if(deviceController instanceof I2CDeviceController){
      I2CDevice device = ((I2CDeviceController) deviceController).getDevice();
      if(device instanceof PowerManagement){
        functionList.put("powerOn");
        functionList.put("powerOff");
      }
      if(device instanceof Resetable){
        functionList.put("reset");
        functionList.put("softReset");
      }
    }
    schemaObject.put("functions", functionList);
    ctx.json(schemaObject.toString(2));
  }


  private void handleGetSchema(Context ctx, DeviceController deviceController) throws IOException {
    String schema = deviceController.getSchema().pack();
    JSONObject schemaObject = new JSONObject(schema);
    JSONObject obj1 = schemaObject.getJSONObject("schema");
    if (obj1.has("jsonSchema")) {
      JSONObject rawSchema = new JSONObject(obj1.getString("jsonSchema"));
      obj1.remove("jsonSchema");
      obj1.put("jsonSchema", rawSchema);
    }
    ctx.json(schemaObject.toString(2));
  }

  private void handleGetStatic(Context ctx, DeviceController deviceController) throws IOException {
    ctx.json(new String(deviceController.getDeviceConfiguration()));
  }

  private void handleDeviceGet(Context ctx, DeviceController deviceController) throws IOException {
    ctx.json(new String(deviceController.getDeviceState()));
  }

  private JSONArray packList(Map<String, DeviceController> devices) {
    JSONArray list = new JSONArray();
    for (Map.Entry<String, DeviceController> deviceEntryEntry : devices.entrySet()) {
      JSONObject entry = new JSONObject();
      entry.put("id", deviceEntryEntry.getKey());
      entry.put("name", deviceEntryEntry.getValue().getName());
      entry.put("description", deviceEntryEntry.getValue().getDescription());
      list.put(entry);
    }
    return list;
  }

}

