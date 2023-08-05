package io.mapsmessaging.devices.pressure;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.Lps25Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataRate;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.FiFoMode;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.SensorReading;

import java.io.IOException;

import static io.mapsmessaging.devices.util.AltitudeMonitor.calculateHeightDifference;
import static io.mapsmessaging.devices.util.Constants.roundFloatToString;


public class PressureMonitor implements Runnable {

  private final Lps25Sensor device;

  public PressureMonitor(Lps25Sensor device) throws IOException {
    this.device = device;
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      device.reset();
      device.delay(100);
      device.getControl1().setDataRate(DataRate.RATE_1_HZ);
      device.getFiFoControl().setFifoMode(FiFoMode.FIFO);
      device.getControl2().enableFiFo(true);
      device.getControl1().setBlockUpdate(true);
      device.getControl1().setPowerDownMode(true);
    }

    Thread t = new Thread(this);
    t.start();
  }


  public void run() {
    SensorReading<?> pressure = null;
    SensorReading<?> temp = null;

    for (SensorReading<?> val : device.getReadings()) {
      if (val.getUnit().equals("hPa")) {
        pressure = val;
      }
      if (val.getUnit().equals("C")) {
        temp = val;
      }
    }
    long stop = System.currentTimeMillis() + 600_000;

    float altitude = 0.0f;
    float pResOld = 0.0f;
    while (pressure != null && temp != null && stop > System.currentTimeMillis()) {
      synchronized (I2CDeviceScheduler.getI2cBusLock()) {
        ComputationResult<Float> tempResult = (ComputationResult<Float>) temp.getValue();
        ComputationResult<Float> result = (ComputationResult<Float>) pressure.getValue();
        if (!result.hasError()) {
          float pRes = result.getResult();
          float tRes = tempResult.getResult();
          if (pResOld == 0f) {
            pResOld = pRes;
          }
          float dist = (float) calculateHeightDifference(pRes, pResOld, tRes) * 1000;
          pResOld = pRes;
          altitude += dist;
          String pre = roundFloatToString(pRes, 2);
          String tmp = roundFloatToString(tRes, 1);
          String dis = roundFloatToString(dist, 3);
          System.err.println(pre + " hPa\t" + tmp + " C\t" + dis + "mm\t"+altitude+"mm");
        }
      }
    }
  }


  public static void main(String[] args) throws IOException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }
    // Configure and mount a device on address 0x5D as a LPS25 pressure & temperature
    I2CDeviceController deviceController = i2cBusManagers[bus].configureDevice(0x5D, "LPS25");
    if (deviceController != null) {
      I2CDevice sensor = deviceController.getDevice();
      if (sensor instanceof Lps25Sensor) {
        new PressureMonitor((Lps25Sensor) sensor);
      }
    }

  }
}
