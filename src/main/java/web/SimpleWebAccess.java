package web;

import io.javalin.Javalin;
import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.devices.oneWire.OneWireDeviceEntry;
import org.json.JSONArray;
import org.json.JSONObject;

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
      Map<String, I2CDeviceEntry> map = deviceBusManager.getI2cBusManager().getActive();
      JSONArray i2cList = new JSONArray();
      for (Map.Entry<String, I2CDeviceEntry> deviceEntryEntry : map.entrySet()) {
        JSONObject entry = new JSONObject();
        entry.put("id", deviceEntryEntry.getKey());
        entry.put("name", deviceEntryEntry.getValue().getName());
        i2cList.put(entry);
      }
      jsonObject.put("i2c", i2cList);

      Map<String, OneWireDeviceEntry> oneMap = deviceBusManager.getOneWireBusManager().getActive();
      JSONArray oneList = new JSONArray();
      for (Map.Entry<String, OneWireDeviceEntry> device : oneMap.entrySet()) {
        JSONObject entry = new JSONObject();
        entry.put("id", device.getKey());
        entry.put("name", device.getValue().getName());
        oneList.put(entry);
      }
      jsonObject.put("1Wire", oneList);
      ctx.json(jsonObject.toString(2));
    });
    app.get("/device/i2c/{id}", ctx -> {
      String id = ctx.pathParam("id");
      I2CDeviceEntry device = deviceBusManager.getI2cBusManager().get(id);
      if (device != null) {
        JSONObject result = new JSONObject();
        result.put("static", new JSONObject(new String(device.getStaticPayload())));
        result.put("update", new JSONObject(new String(device.getUpdatePayload())));
        ctx.json(result.toString(2));
      } else {
        ctx.status(404).result("Device not found");
      }
    });
    app.get("/device/1wire/{id}", ctx -> {
      String id = ctx.pathParam("id");
      OneWireDeviceEntry device = deviceBusManager.getOneWireBusManager().get(id);
      if (device != null) {
        JSONObject result = new JSONObject();
        result.put("static", new JSONObject(new String(device.getStaticPayload())));
        result.put("update", new JSONObject(new String(device.getUpdatePayload())));
        ctx.json(result.toString(2));
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


}

