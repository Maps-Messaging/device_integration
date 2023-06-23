package io.mapsmessaging.devices.i2c.devices.sensors.ds3231;

import com.pi4j.io.i2c.I2C;
import lombok.Getter;

public class Ds3231Registers {

  private byte[] registerValues;

  @Getter
  private ControlRegister controlRegister;

  @Getter
  private StatusRegister statusRegister;

  @Getter
  private Alarm alarm1;

  @Getter
  private Alarm alarm2;

  public Ds3231Registers() {

  }

  void setRegisterValues(byte[] values, I2C device){
    registerValues = values;
    controlRegister = new ControlRegister(registerValues[0xE]);
    statusRegister = new StatusRegister(registerValues[0xf]);
    byte[] alarm1Register = new byte[4];
    System.arraycopy(values, 7, alarm1Register, 0, 4);
    alarm1 = new Alarm(alarm1Register, true, device, 7);

    byte[] alarm2Register = new byte[3];
    System.arraycopy(values, 11, alarm2Register, 0, 3);
    alarm2 = new Alarm(alarm2Register, false, device, 11);
  }

  byte[] getRegisterValues(){
    registerValues[0xE] = controlRegister.toByte();
    registerValues[0xF] = statusRegister.toByte();
    System.arraycopy(alarm1.getRegisters(), 0, registerValues, 7, 4);
    System.arraycopy(alarm2.getRegisters(), 0, registerValues, 11, 3);
    return registerValues;
  }

  public int getSeconds() {
    return bcdToDecimal(registerValues[0] & 0x7F);
  }

  public int getMinutes() {
    return bcdToDecimal(registerValues[1] & 0x7F);
  }

  public int getHours() {
    byte value = registerValues[2];
    boolean is12HourFormat = (value & 0x40) != 0;
    int hours = bcdToDecimal(value & 0x3F);

    if (is12HourFormat) {
      boolean isPM = (value & 0x20) != 0;
      hours = convert12HourTo24Hour(hours, isPM);
    }

    return hours;
  }

  public int getDayOfWeek() {
    return registerValues[3] & 0b111;
  }

  public int getDate() {
    return bcdToDecimal(registerValues[4] & 0x3F);
  }

  public int getMonth() {
    return bcdToDecimal(registerValues[5] & 0x1F);
  }

  public int getYear() {
    return bcdToDecimal(registerValues[6]) + 2000;
  }

  public int getControl() {
    return registerValues[14] & 0xFF;
  }

  public int getStatus() {
    return registerValues[15] & 0xFF;
  }

  public int getAgingOffset() {
    return registerValues[16];
  }

  public float getTemperature() {
    int tempValue = ((registerValues[17] & 0x7F) << 2) + ((registerValues[18] >> 6) & 0x03);
    return tempValue / 4.0f;
  }

  protected static int bcdToDecimal(int bcdValue) {
    return ((bcdValue & 0xF0) >> 4) * 10 + (bcdValue & 0x0F);
  }

  protected static int convert12HourTo24Hour(int hours, boolean isPM) {
    if (isPM) {
      hours += 12;
    }
    return hours;
  }

  public void setSeconds(int seconds) {
    registerValues[0] = decimalToBcd(seconds % 60);
  }

  public void setMinutes(int minutes) {
    registerValues[1] = decimalToBcd(minutes % 60);
  }

  public void setHours(int hours, boolean is12HourFormat) {
    if (is12HourFormat) {
      boolean isPM = hours >= 12;
      if (hours > 12) {
        hours -= 12;
      }
      registerValues[2] = (byte) (0x40 | (decimalToBcd(hours) & 0x1F));
      if (isPM) {
        registerValues[2] |= 0x20;
      }
    } else {
      registerValues[2] = decimalToBcd(hours % 24);
    }
  }

  public void setDayOfWeek(int dayOfWeek) {
    registerValues[3] = (byte) (dayOfWeek & 0x07);
  }

  public void setDate(int date) {
    registerValues[4] = decimalToBcd(date % 32);
  }

  public void setMonth(int month) {
    registerValues[5] = decimalToBcd(month % 13);
  }

  public void setYear(int year) {
    registerValues[6] = decimalToBcd((year - 2000) % 100);
  }

  public void setControl(int control) {
    registerValues[14] = (byte) (control & 0xFF);
  }

  public void setStatus(int status) {
    registerValues[15] = (byte) (status & 0xFF);
  }

  public void setAgingOffset(int agingOffset) {
    registerValues[16] = (byte) agingOffset;
  }

  // Other setter methods for the remaining registers...

  protected static byte decimalToBcd(int decimalValue) {
    return (byte) (((decimalValue / 10) << 4) | (decimalValue % 10));
  }

  @Override
  public String toString(){
    StringBuilder stringBuffer = new StringBuilder();
    stringBuffer.append("Date   : ").append(getDate()).append("-").append(getMonth()).append("-").append(getYear()).append(" DOW:").append(getDayOfWeek()).append("\n");
    stringBuffer.append("Time   : ").append(getHours()).append(":").append(getMinutes()).append(":").append(getSeconds()).append("\n");
    stringBuffer.append("Alarm1 : ").append(getAlarm1()).append("\n");
    stringBuffer.append("Alarm2 : ").append(getAlarm2()).append("\n");
    stringBuffer.append("Aging  : ").append(getAgingOffset()).append("\n");
    stringBuffer.append("Temp   : ").append(getTemperature()).append("\n");
    stringBuffer.append("Control Register\n").append(controlRegister).append("\n");
    stringBuffer.append("Status Register\n").append(statusRegister).append("\n");
    return stringBuffer.toString();
  }
}
