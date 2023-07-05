package io.mapsmessaging.devices.i2c.devices.sensors.gravity;

import java.nio.ByteBuffer;

public class Registers {
  private static final int ALL_DATA_SIZE = 9; // Total size of the AllData struct in bytes

  private final ByteBuffer buffer;

  public Registers(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  public byte getHead() {
    return buffer.get(0);
  }

  public void setHead(byte head) {
    buffer.put(0, head);
  }

  public byte getCmd() {
    return buffer.get(1);
  }

  public void setCmd(byte cmd) {
    buffer.put(1, cmd);
  }

  public byte getGasConcentrationH() {
    return buffer.get(2);
  }

  public void setGasConcentrationH(byte gasConcentrationH) {
    buffer.put(2, gasConcentrationH);
  }

  public byte getGasConcentrationL() {
    return buffer.get(3);
  }

  public void setGasConcentrationL(byte gasConcentrationL) {
    buffer.put(3, gasConcentrationL);
  }

  public byte getGasType() {
    return buffer.get(4);
  }

  public void setGasType(byte gasType) {
    buffer.put(4, gasType);
  }

  public byte getGasConcentrationDecimals() {
    return buffer.get(5);
  }

  public void setGasConcentrationDecimals(byte gasConcentrationDecimals) {
    buffer.put(5, gasConcentrationDecimals);
  }

  public byte getTempH() {
    return buffer.get(6);
  }

  public void setTempH(byte tempH) {
    buffer.put(6, tempH);
  }

  public byte getTempL() {
    return buffer.get(7);
  }

  public void setTempL(byte tempL) {
    buffer.put(7, tempL);
  }

  public byte getCheck() {
    return buffer.get(8);
  }

  public void setCheck(byte check) {
    buffer.put(8, check);
  }

  public byte[] toByteArray() {
    return buffer.array();
  }

  public void fromByteArray(byte[] byteArray) {
    buffer.clear();
    buffer.put(byteArray, 0, ALL_DATA_SIZE);
    buffer.rewind();
  }
}
