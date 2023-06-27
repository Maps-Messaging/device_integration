package io.mapsmessaging.devices.i2c.devices.sensors.ina219;

public enum ShuntADCResolution {
  RES_9BIT_1S_84US(0x0000),  // 1 x 9-bit shunt sample
  RES_10BIT_1S_148US(0x0008),  // 1 x 10-bit shunt sample
  RES_11BIT_1S_276US(0x0010),  // 1 x 11-bit shunt sample
  RES_12BIT_1S_532US(0x0018),  // 1 x 12-bit shunt sample
  RES_12BIT_2S_1060US(0x0048),   // 2 x 12-bit shunt samples averaged together
  RES_12BIT_4S_2130US(0x0050),  // 4 x 12-bit shunt samples averaged together
  RES_12BIT_8S_4260US(0x0058),  // 8 x 12-bit shunt samples averaged together
  RES_12BIT_16S_8510US(0x0060),  // 16 x 12-bit shunt samples averaged together
  RES_12BIT_32S_17MS(0x0068),  // 32 x 12-bit shunt samples averaged together
  RES_12BIT_64S_34MS(0x0070),  // 64 x 12-bit shunt samples averaged together
  RES_12BIT_128S_69MS(0x0078);  // 128 x 12-bit shunt samples averaged together

  private final int value;

  ShuntADCResolution(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}