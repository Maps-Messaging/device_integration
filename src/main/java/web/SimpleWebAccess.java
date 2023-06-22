package web;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.devices.oneWire.OneWireDeviceEntry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimpleWebAccess {

  private final DeviceBusManager deviceBusManager;

  public SimpleWebAccess() {
    deviceBusManager = DeviceBusManager.getInstance();
    deviceBusManager.getI2cBusManager().scanForDevices();
  }

  public static void main(String[] args) {
    SimpleWebAccess simpleWebAccess = new SimpleWebAccess();
    simpleWebAccess.startServer();
  }

  private void startServer() {
    Javalin app = Javalin.create().start(7000);
      app.get("/device/list", ctx -> {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("i2c", packList(deviceBusManager.getI2cBusManager().getActive()));
      jsonObject.put("1Wire",  packList(deviceBusManager.getOneWireBusManager().getActive()));
      ctx.json(jsonObject.toString(2));
    });
    app.get("/device/i2c/{id}", ctx -> {
      String id = ctx.pathParam("id");
      I2CDeviceEntry device = deviceBusManager.getI2cBusManager().get(id);
      if (device != null) {
        handleDeviceGet(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    app.get("/device/i2c/{id}/schema", ctx -> {
      String id = ctx.pathParam("id");
      I2CDeviceEntry device = deviceBusManager.getI2cBusManager().get(id);
      if (device != null) {
        handleGetSchema(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });

    app.get("/device/1wire/{id}", ctx -> {
      String id = ctx.pathParam("id");
      OneWireDeviceEntry device = deviceBusManager.getOneWireBusManager().get(id);
      if (device != null) {
        handleDeviceGet(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    app.get("/device/1wire/{id}/schema", ctx -> {
      String id = ctx.pathParam("id");
      OneWireDeviceEntry device = deviceBusManager.getOneWireBusManager().get(id);
      if (device != null) {
        handleGetSchema(ctx, device);
      } else {
        ctx.status(404).result("Device not found");
      }
    });

    app.post("/device/i2c/{id}", ctx -> {
      String id = ctx.pathParam("id");
      I2CDeviceEntry device = deviceBusManager.getI2cBusManager().get(id);
      if (device != null) {
        device.setPayload(ctx.body().getBytes());
        ctx.status(200).result("Data written successfully");
      } else {
        ctx.status(404).result("Device not found");
      }
    });

    // Schedule a task to scan for I2C devices every 5 seconds
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(deviceBusManager.getI2cBusManager()::scanForDevices, 0, 1, TimeUnit.MINUTES);
  }


  private void handleGetSchema(Context ctx, DeviceController deviceController) throws IOException {
    JSONObject result = new JSONObject();
    String schema = deviceController.getSchema().pack();
    result.put("schema", new JSONObject(schema));
    ctx.json(result.toString(2));
  }


  private void handleDeviceGet(Context ctx, DeviceController deviceController){
    JSONObject result = new JSONObject();
    result.put("static", new JSONObject(new String(deviceController.getStaticPayload())));
    result.put("update", new JSONObject(new String(deviceController.getUpdatePayload())));
    ctx.json(result.toString(2));
  }

  private JSONArray packList(Map<String, DeviceController> devices){
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

