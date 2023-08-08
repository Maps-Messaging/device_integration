package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602;

import io.mapsmessaging.devices.deviceinterfaces.Output;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;

public class BacklightPwm  extends I2CDevice implements Output {
   private static final byte REG_RED   =      0x04;
  private static final byte REG_GREEN =      0x03;
  private static final byte REG_BLUE  =      0x02;
  private static final byte REG_ONLY  =      0x02 ;

  private static final byte REG_MODE1   = 0x00;
  private static final byte  REG_MODE2    =   0x01;
  private static final byte  REG_OUTPUT  =    0x08;
  protected BacklightPwm(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(BacklightPwm.class));
    initialise();
  }

  public void initialise() {
    sendCommand(REG_MODE1, (byte)0);
    sendCommand(REG_OUTPUT, (byte)0xFF);
    sendCommand(REG_MODE2, (byte)0x20);
    sendCommand(REG_RED, (byte)0xff);
    sendCommand(REG_GREEN, (byte)0xff);
    sendCommand(REG_BLUE, (byte)0x77);
  }

  @Override
  public String getName() {
    return "BacklightPwm";
  }

  @Override
  public String getDescription() {
    return "Backlight control";
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  protected void sendCommand(byte addr, byte val){
    device.writeRegister(addr, new byte[]{val});
  }

}