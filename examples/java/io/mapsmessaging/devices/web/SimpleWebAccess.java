/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static io.mapsmessaging.schemas.config.SchemaConfigFactory.gson;

public class SimpleWebAccess {

  private final DeviceBusManager deviceBusManager;
  private Javalin app;

  public SimpleWebAccess() {
    deviceBusManager = DeviceBusManager.getInstance();
    if(!deviceBusManager.isAvailable()){
      throw new RuntimeException("PI4J not supported");
    }
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        try {
          scan();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }, 2000, 120000);
  }

  public static void main(String[] args) throws InterruptedException {
    SimpleWebAccess simpleWebAccess = new SimpleWebAccess();
    simpleWebAccess.startServer();
  }

  public void scan() throws InterruptedException {
    for (I2CBusManager manager : deviceBusManager.getI2cBusManager()) {
      manager.scanForDevices(500);
    }
  }

  private void startServer() {
    app = Javalin.create().start(7001);
    app.get("/device/exit", ctx -> {
      app.stop();
      System.exit(0);
    });
    app.get("/device/list", ctx -> {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("i2c[0]", packList(deviceBusManager.getI2cBusManager()[0].getActive()));
      jsonObject.add("i2c[1]", packList(deviceBusManager.getI2cBusManager()[1].getActive()));
      jsonObject.add("1Wire", packList(deviceBusManager.getOneWireBusManager().getActive()));
      jsonObject.add("spi", packList(deviceBusManager.getSpiBusManager().getActive()));
      ctx.json(gson.toJson(jsonObject));
    });

    //<editor-fold desc="I2C handler">
    // Add the I2C bus
    app.get("/device/i2c/{bus}/scan", ctx -> {
      int bus = Integer.parseInt(ctx.pathParam("bus"));
      List<Integer> found = deviceBusManager.getI2cBusManager()[bus].findDevicesOnBus(0);
      List<String> map = deviceBusManager.getI2cBusManager()[bus].listDetected(found);

      JsonArray jsonArray = new JsonArray();
      for (String line : map) {
        jsonArray.add(line);
      }

      JsonObject json = new JsonObject();
      json.add("I2C_Detect", jsonArray);
      ctx.json(gson.toJson(json));
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
    String res = deviceController.getName() + " - " + deviceController.getDescription() + "\n";
    if (deviceController instanceof I2CDeviceController) {
      res += ((I2CDeviceController) deviceController).getDevice().registerMap.toString();
    }
    ctx.result(res);
  }

  private void handleFunction(Context ctx, DeviceController deviceController, String function) throws IOException {
    if (deviceController instanceof I2CDeviceController) {
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
    ctx.result("unknown or unhandled function received " + function);
  }

  private void handleGetFunctions(Context ctx, DeviceController deviceController) {
    JsonObject schemaObject = new JsonObject();
    JsonArray functionList = new JsonArray();

    if (deviceController instanceof I2CDeviceController i2cController) {
      I2CDevice device = i2cController.getDevice();
      if (device instanceof PowerManagement) {
        functionList.add("powerOn");
        functionList.add("powerOff");
      }
      if (device instanceof Resetable) {
        functionList.add("reset");
        functionList.add("softReset");
      }
    }

    schemaObject.add("functions", functionList);
    ctx.json(gson.toJson(schemaObject));
  }


  private void handleGetSchema(Context ctx, DeviceController deviceController) throws IOException {
    JsonObject schemaObject = deviceController.getSchema().getSchema();

    JsonObject obj1 = schemaObject.getAsJsonObject("schema");
    if (obj1 != null && obj1.has("jsonSchema")) {
      JsonElement rawSchema = obj1.get("jsonSchema");
      obj1.remove("jsonSchema");
      obj1.add("jsonSchema", rawSchema); // Re-insert to preserve order/trigger re-serialization
    }

    ctx.json(gson.toJson(schemaObject));
  }

  private void handleGetStatic(Context ctx, DeviceController deviceController) throws IOException {
    ctx.json(new String(deviceController.getDeviceConfiguration()));
  }

  private void handleDeviceGet(Context ctx, DeviceController deviceController) throws IOException {
    ctx.json(new String(deviceController.getDeviceState()));
  }

  private JsonArray packList(Map<String, DeviceController> devices) {
    JsonArray list = new JsonArray();
    for (Map.Entry<String, DeviceController> entry : devices.entrySet()) {
      JsonObject obj = new JsonObject();
      obj.addProperty("id", entry.getKey());
      obj.addProperty("name", entry.getValue().getName());
      obj.addProperty("description", entry.getValue().getDescription());
      list.add(obj);
    }
    return list;
  }


}

