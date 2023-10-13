package io.mapsmessaging.devices.spi;

import com.pi4j.context.Context;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.schemas.config.SchemaConfig;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class SpiDeviceScheduler extends SpiDeviceController {

  private static final Semaphore SPI_BUS_SEMAPHORE = new Semaphore(1);

  private final SpiDeviceController deviceController;

  public SpiDeviceScheduler(SpiDeviceController deviceController) {
    this.deviceController = deviceController;
  }

  @Override
  public String getName() {
    return deviceController.getName();
  }

  @Override
  public String getDescription() {
    return deviceController.getDescription();
  }

  @Override
  public SchemaConfig getSchema() {
    return deviceController.getSchema();
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    try {
      SPI_BUS_SEMAPHORE.acquireUninterruptibly();
      return deviceController.getDeviceConfiguration();
    } finally {
      SPI_BUS_SEMAPHORE.release();
    }
  }
  public DeviceType getType(){
    return deviceController.getType();
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    try {
      SPI_BUS_SEMAPHORE.acquireUninterruptibly();
      return deviceController.getDeviceState();
    } finally {
      SPI_BUS_SEMAPHORE.release();
    }
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    try {
      SPI_BUS_SEMAPHORE.acquireUninterruptibly();
      return deviceController.updateDeviceConfiguration(val);
    } finally {
      SPI_BUS_SEMAPHORE.release();
    }
  }

  @Override
  public SpiDeviceController mount(Context pi4j, Map<String, String> config) {
    return null; // Device is already mounted
  }
}