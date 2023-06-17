package io.mapsmessaging.devices;

import io.mapsmessaging.schemas.config.SchemaConfig;

public interface DeviceManager {

  String getName();

  SchemaConfig getSchema();

  byte[] getStaticPayload();

  byte[] getUpdatePayload();

  default void setPayload(byte[] val){}

}
