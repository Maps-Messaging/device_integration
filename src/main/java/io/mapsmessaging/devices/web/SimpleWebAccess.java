package io.mapsmessaging.devices.web;

import io.javalin.Javalin;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import org.json.JSONObject;

import java.util.concurrent.*;

public class SimpleWebAccess {

    private I2CBusManager i2cBusManager;

    public SimpleWebAccess(){

    }


    private void startServer(){
        i2cBusManager = new I2CBusManager();
        Javalin app = Javalin.create().start(7000);

        app.get("/device/{id}", ctx -> {
            String id = ctx.pathParam("id");
            I2CDeviceEntry device = i2cBusManager.get(id);
            if (device != null) {
                JSONObject result = new JSONObject();
                result.put("static", new JSONObject(new String(device.getStaticPayload())));
                result.put("update", new JSONObject(new String(device.getUpdatePayload())));
                ctx.json(result.toString(2));
            } else {
                ctx.status(404).result("Device not found");
            }
        });

        app.post("/device/{id}", ctx -> {
            String id = ctx.pathParam("id");
            I2CDeviceEntry device = i2cBusManager.get(id);
            if (device != null) {
                device.setPayload(ctx.body().getBytes());
                ctx.status(200).result("Data written successfully");
            } else {
                ctx.status(404).result("Device not found");
            }
        });

        // Schedule a task to scan for I2C devices every 5 seconds
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(i2cBusManager::scanForDevices, 0, 1, TimeUnit.MINUTES);
    }

    public static void main(String[] args){
        SimpleWebAccess simpleWebAccess = new SimpleWebAccess();
        simpleWebAccess.startServer();
    }


}

