package io.mapsmessaging.devices.i2c.devices.sensors.ds3231;

public class StatusRegister {

  private byte statusByte;

  public StatusRegister(byte statusByte) {
    this.statusByte = statusByte;
  }

  public boolean isOscillatorStopped() {
    return (statusByte & 0x80) != 0;
  }

  public boolean is32kHzOutputEnabled() {
    return (statusByte & 0x08) != 0;
  }

  public boolean isAlarm2FlagSet() {
    return (statusByte & 0x02) != 0;
  }

  public boolean isAlarm1FlagSet() {
    return (statusByte & 0x01) != 0;
  }

  public void clearAlarm2Flag() {
    statusByte &= 0xFD;
  }

  public void clearAlarm1Flag() {
    statusByte &= 0xFE;
  }

  public byte toByte() {
    return statusByte;
  }

  @Override
  public String toString() {
    return "Oscillator Stopped : " + isOscillatorStopped() + "\n" +
        "32kHz Output Enabled : " + is32kHzOutputEnabled() + "\n" +
        "Alarm 2 Flag Set : " + isAlarm2FlagSet() + "\n" +
        "Alarm 1 Flag Set : " + isAlarm1FlagSet() + "\n";
  }
}

