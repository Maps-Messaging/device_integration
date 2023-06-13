package io.mapsmessaging.server.i2c.devices.sensors;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;
import java.io.IOException;
import java.util.concurrent.Callable;

public class AS3935Sensor extends I2CSensor {

  public static final byte _IDEL = 0x0;
  public static final byte _INTERRUPT_TOO_HIGH = 0x1;
  public static final byte _INTERRUPT_DISTURBER = 0x4;
  public static final byte _INTERRUPT_LIGHTNING = 0x8;

  private final byte[] registers;
  private final int tuning;

  public AS3935Sensor(int bus, int device, int tuning, Pin interruptPin) throws IOException {
    super(bus, device);
    registers = new byte[128];
    this.tuning = tuning;
    initialise();

    if (interruptPin != null) {
      GpioPinDigitalInput pin = GpioFactory.getInstance().provisionDigitalInputPin(interruptPin, PinPullResistance.PULL_DOWN);
      pin.addTrigger(new GpioCallbackTrigger(new Callable<Void>() {
        public Void call() throws Exception {
          delay(2);
          read_data();
          return null;
        }
      }));
    }
  }

  public int getMinimumStrikes() {
    return (registers[0x02] >> 4) & 0x03;
  }

  public int getDistance() {
    if ((registers[0x07] & 0x3F) == 0x3F) {
      return -1;
    }
    return registers[0x07] & 0x3F;
  }

  public int getReason() {
    return (registers[3] & 0xf);
  }

  public int getStrength() {
    return ((registers[6] & 0xf) << 16) + (registers[5] << 8) + registers[4];
  }

  public String getRegisters() {
    StringBuilder sb = new StringBuilder("\n");
    for (int x = 0; x < 9; x++) {
      int val = registers[x];
      val = val & 0xff;
      String sval = Integer.toBinaryString(val);
      for (int y = sval.length(); y < 9; y++) {
        sval = "0" + sval;
      }
      sb.append("0x").append(x).append(" ").append(sval).append("\n");
    }
    sb.append("\nPWD      : ").append(registers[0] & 1).append("\n")
        .append("AFE_GB   : ").append(registers[0] & 62).append("\n")
        .append("WDTH     : ").append(registers[1] & 0xf).append("\n")
        .append("NV_LEV   : ").append(registers[1] & 0x70).append("\n")
        .append("SREJ     : ").append(registers[2] & 0xf).append("\n")
        .append("MIN_HIGH : ").append(registers[2] & 0x30).append("\n")
        .append("CL_STAT  : ").append(registers[2] & 0x40).append("\n");
    byte interrupt = (byte) (registers[3] & 0xf);
    String reason = "";
    switch (interrupt) {
      case _INTERRUPT_DISTURBER:
        reason = "DISTURBER";
        break;
      case _INTERRUPT_LIGHTNING:
        reason = "LIGHTNING";
        break;
      case _INTERRUPT_TOO_HIGH:
        reason = "NOISE";
        break;
      default:
        reason = "IDLE:" + interrupt;
    }
    sb.append("INT      : ").append(reason).append("\n");
    sb.append("Mask_Dist: ").append(registers[3] & 0x50).append("\n");
    sb.append("LC_LOW   : ").append(registers[3] & 0xc0).append("\n");
    sb.append("S_LIG_L  : ").append(registers[4]).append("\n");
    sb.append("S_LIG_M  : ").append(registers[5]).append("\n");
    sb.append("S_LIG_MM : ").append(registers[6] & 0x0f).append("\n");
    sb.append("DISTANCE : ").append(getDistance(registers[7] & 0x3f)).append("\n");
    sb.append("TUN_CAP  : ").append(registers[8] & 0x0f).append("\n");
    sb.append("DISP_TRCO: ").append(registers[8] & 0x20).append("\n");
    sb.append("DISP_SRCO: ").append(registers[8] & 0x40).append("\n");
    sb.append("DISP_LCO : ").append(registers[8] & 0x80).append("\n");
    return sb.toString();
  }

  private int getDistance(int distance) {
    if (distance == 0x3f) {
      return -1;
    }
    return distance;
  }

  private void read_data() throws IOException {
    _device.read(registers, 0, 64);
  }

  public boolean initialise() throws IOException {
    delay(80);
    read_data();
    if (tuning != 0) {
      if (tuning < 0x10 && tuning > -1) {
        _device.write(0x08, (byte) ((registers[0x08] & 0xF0) | tuning));
        registers[8] = (byte) ((registers[0x08] & 0xF0) | tuning);
      }
      delay(200);
      read_data();
    } else {
      throw new IOException("Value of TUN_CAP must be between 0 and 15");
    }
    _device.write(0x08, (byte) (registers[0x08] | 0x20));
    delay(200);
    read_data();
    _device.write(0x08, (byte) (registers[0x08] & 0xDF));
    delay(200);
    read_data();
    return true;
  }

}