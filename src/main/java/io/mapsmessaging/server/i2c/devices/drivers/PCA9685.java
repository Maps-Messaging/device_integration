package io.mapsmessaging.server.i2c.devices.drivers;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import io.mapsmessaging.server.i2c.Delay;
import io.mapsmessaging.server.i2c.devices.drivers.servos.AngleResponse;
import io.mapsmessaging.server.i2c.devices.drivers.servos.PWM_Device;
import io.mapsmessaging.server.i2c.devices.drivers.servos.Servo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

public class PCA9685 extends I2CController{

  private static final int __LED0_ON_L = 0x06;
  private static final int __LED0_ON_H = 0x07;
  private static final int __LED0_OFF_L = 0x08;
  private static final int __LED0_OFF_H = 0x09;
  private static final int __ALL_LED_ON_L = 0xFA;
  private static final int __ALL_LED_ON_H = 0xFB;
  private static final int __ALL_LED_OFF_L = 0xFC;
  private static final int __ALL_LED_OFF_H = 0xFD;
  private static final int __PRESCALE = 0xFE;
  private static final int __MODE1 = 0x00;
  private static final int __MODE2 = 0x01;
  private static final int __SUBADR1 = 0x02;
  private static final int __SUBADR2 = 0x03;
  private static final int __SUBADR3 = 0x04;
  private static final int __RESTART = 0x80;
  private static final int __SLEEP = 0x10;
  private static final int __ALLCALL = 0x01;
  private static final int __INVRT = 0x10;
  private static final int __OUTDRV = 0x04;
  
  private final BitSet myServos;

  private final ArrayList<PWM_Device> myServoList;

  public PCA9685(int bus, int device) throws IOException, UnsupportedBusNumberException {
    super(bus, device);
    myServos = new BitSet(16);
    myServoList = new ArrayList<>();
    initialise();
  }

  public void close() throws IOException {
    for (PWM_Device device : myServoList) {
      device.close();
    }
    myServoList.clear();
    myServos.clear();
  }

  public Servo allocateServo(int port, AngleResponse response) throws IOException {
    if (myServos.get(port)) {
      throw new IOException("Servo already allocated");
    }
    myServos.set(port);
    Servo servo = new Servo(this, (short) port, response);
    myServoList.add(servo);
    return servo;
  }

  public void deallocateServo(Servo servo) throws IOException {
    if (!myServos.get(servo.getPort())) {
      throw new IOException("Servo not allocated");
    }
    servo.close();
    myServos.clear(servo.getPort());
    myServoList.remove(servo);
  }

  public void setPWMFrequency(double frequency) throws IOException {
    double prescaleval = 25000000.0;//    # 25MHz
    prescaleval /= 4096.0;       // 12-bit
    prescaleval /= frequency;
    prescaleval -= 1.0;

    //log("Setting PWM frequency to " + frequency + "Hz");
    //log("Estimated pre-scale:" + prescaleval);
    double prescale = Math.floor(prescaleval + 0.5);
    //log("Final pre-scale: " + prescale);

    int oldmode = myDevice.read((byte) __MODE1);
    int newmode = (oldmode & 0x7F) | 0x10;             // sleep
    int intPrescale = (int) (Math.floor(prescale));

    myDevice.write(__MODE1, (byte) newmode);
    myDevice.write(__PRESCALE, (byte) intPrescale);
    myDevice.write(__MODE1, (byte) oldmode);
    Delay.pause(5);
    myDevice.write(__MODE1, (byte) (oldmode | 0x80));
  }

  public void setPWM(short channel, short on, short off) throws IOException {
    myDevice.write(__LED0_ON_L + 4 * channel, (byte) (on & 0xFF));
    myDevice.write(__LED0_ON_H + 4 * channel, (byte) (on >> 8));
    myDevice.write(__LED0_OFF_L + 4 * channel, (byte) (off & 0xFF));
    myDevice.write(__LED0_OFF_H + 4 * channel, (byte) (off >> 8));
  }

  private void initialise() throws IOException {
    if (exists()) {
      setAllPWM((byte) 0, (byte) 0); // Reset ALL servos
      myDevice.write(__MODE2, (byte) __OUTDRV);
      myDevice.write(__MODE1, (byte) __ALLCALL);
      Delay.pause(5);
      int mode1 = myDevice.read(__MODE1);
      mode1 = mode1 & ~(__SLEEP);
      myDevice.write(__MODE1, (byte) mode1);
      Delay.pause(5);
    }
  }

  private void setAllPWM(short on, short off) throws IOException {
    myDevice.write(__ALL_LED_ON_L, (byte) (on & 0xff));
    myDevice.write(__ALL_LED_ON_H, (byte) (on >> 8));
    myDevice.write(__ALL_LED_OFF_L, (byte) (off & 0xff));
    myDevice.write(__ALL_LED_OFF_H, (byte) (off >> 8));
  }

}
