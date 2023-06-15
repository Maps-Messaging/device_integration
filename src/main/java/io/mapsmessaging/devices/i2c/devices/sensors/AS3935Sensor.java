package io.mapsmessaging.devices.i2c.devices.sensors;

import com.pi4j.Pi4J;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;
import java.util.Properties;

public class AS3935Sensor extends I2CDevice {

  public static final byte _IDEL = 0x0;
  public static final byte _INTERRUPT_TOO_HIGH = 0x1;
  public static final byte _INTERRUPT_DISTURBER = 0x4;
  public static final byte _INTERRUPT_LIGHTNING = 0x8;

  private final byte[] registers;
  private final int tuning;

  public AS3935Sensor(I2C device, int tuning, int pinNumber) throws IOException {
    super(device);
    registers = new byte[128];
    this.tuning = tuning;
    initialise();

    if (pinNumber > -1) {
      Properties properties = new Properties();
      properties.put("id", "AS3935InterruptPin");
      properties.put("address", pinNumber);
      properties.put("pull", "DOWN");
      properties.put("name", "AS3935InterruptPin");
      var pi4j = Pi4J.newAutoContext();
      var config = DigitalInput.newConfigBuilder(pi4j)
          .load(properties)
          .build();

      var input = pi4j.din().create(config);
      input.addListener(e -> {
        if (e.state() == DigitalState.HIGH) {
          delay(2);
          try {
            read_data();
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
        }
      });
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
    read(registers, 0, 64);
  }

  public boolean initialise() throws IOException {
    delay(80);
    read_data();
    if (tuning != 0) {
      if (tuning < 0x10 && tuning > -1) {
        write(0x08, (byte) ((registers[0x08] & 0xF0) | tuning));
        registers[8] = (byte) ((registers[0x08] & 0xF0) | tuning);
      }
      delay(200);
      read_data();
    } else {
      throw new IOException("Value of TUN_CAP must be between 0 and 15");
    }
    write(0x08, (byte) (registers[0x08] | 0x20));
    delay(200);
    read_data();
    write(0x08, (byte) (registers[0x08] & 0xDF));
    delay(200);
    read_data();
    return true;
  }


}