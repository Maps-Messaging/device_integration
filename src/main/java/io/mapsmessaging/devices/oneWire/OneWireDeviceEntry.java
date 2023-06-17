package io.mapsmessaging.devices.oneWire;

import io.mapsmessaging.devices.DeviceManager;

import java.io.File;

public interface OneWireDeviceEntry extends DeviceManager {

  String getId();

  OneWireDeviceEntry mount (File path);


}
