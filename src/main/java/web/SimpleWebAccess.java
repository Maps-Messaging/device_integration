package web;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.onewire.OneWireDeviceController;
import io.mapsmessaging.devices.spi.SpiDeviceController;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimpleWebAccess {


  private final DeviceBusManager deviceBusManager;

  public SimpleWebAccess() {
    deviceBusManager = DeviceBusManager.getInstance();
    deviceBusManager.getI2cBusManager().scanForDevices();
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(deviceBusManager.getI2cBusManager()::scanForDevices, 0, 1, TimeUnit.MINUTES);


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

  public static void main(String[] args) {
    SimpleWebAccess simpleWebAccess = new SimpleWebAccess();
    simpleWebAccess.startServer();
  }

  private void startServer() {

    Javalin app = Javalin.create().start(7001);
    app.get("/device/list", ctx -> {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("i2c", packList(deviceBusManager.getI2cBusManager().getActive()));
      jsonObject.put("1Wire", packList(deviceBusManager.getOneWireBusManager().getActive()));
      jsonObject.put("spi", packList(deviceBusManager.getSpiBusManager().getActive()));
      ctx.json(jsonObject.toString(2));
    });

    //<editor-fold desc="I2C handler">
    // Add the I2C bus
    app.get("/device/i2c/{id}", ctx -> {
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager().get(id);
      if (device != null) {
        try {
          handleDeviceGet(ctx, device);
        } catch (IOException e) {
          deviceBusManager.getI2cBusManager().close(device);
        }
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    // I2C get Schema
    app.get("/device/i2c/{id}/schema", ctx -> {
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager().get(id);
      if (device != null) {
        handleGetSchema(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    // I2C get static
    app.get("/device/i2c/{id}/static", ctx -> {
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager().get(id);
      if (device != null) {
        try {
          handleGetStatic(ctx, device);
        }
        catch (IOException ex) {
          deviceBusManager.getI2cBusManager().close(device);
        }
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    // I2C post request handler
    app.post("/device/i2c/{id}", ctx -> {
      String id = ctx.pathParam("id");
      I2CDeviceController device = deviceBusManager.getI2cBusManager().get(id);
      if (device != null) {
        try {
          ctx.status(200);
          ctx.json(new String( device.setPayload(ctx.body().getBytes())));
        } catch (IOException e) {
          deviceBusManager.getI2cBusManager().close(device);
          ctx.status(400).result("Internal Error:"+e.getMessage());
        }
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
        ctx.json(new String( device.setPayload(ctx.body().getBytes())));
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
    ctx.json(new String(deviceController.getStaticPayload()));
  }

  private void handleDeviceGet(Context ctx, DeviceController deviceController) throws IOException {
    ctx.json(new String(deviceController.getUpdatePayload()));
  }

  private JSONArray packList(Map<String, DeviceController> devices) {
    JSONArray list = new JSONArray();
    for (Map.Entry<String, DeviceController> deviceEntryEntry : devices.entrySet()) {
      JSONObject entry = new JSONObject();
      entry.put("id", deviceEntryEntry.getKey());
      entry.put("name", deviceEntryEntry.getValue().getName());
      list.put(entry);
    }
    return list;
  }

}

