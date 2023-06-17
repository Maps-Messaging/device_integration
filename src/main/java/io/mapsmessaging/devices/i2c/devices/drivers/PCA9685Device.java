/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices.drivers;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.drivers.servos.AngleResponse;
import io.mapsmessaging.devices.i2c.devices.drivers.servos.PwmDevice;
import io.mapsmessaging.devices.i2c.devices.drivers.servos.Servo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

public class PCA9685Device extends I2CDevice {

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

  private final ArrayList<PwmDevice> myServoList;

  public PCA9685Device(I2C device) throws IOException {
    super(device);
    myServos = new BitSet(16);
    myServoList = new ArrayList<>();
    initialise();
  }

  public void close()  {
    for (PwmDevice device : myServoList) {
      try {
        device.close();
      } catch (IOException e) {

      }
    }
    myServoList.clear();
    myServos.clear();
  }

  @Override
  public boolean isConnected() {
    byte[] registers = new byte[256];
    int read = read(registers);
    System.err.println(read);
    for(int x=0;x<read;x++){
      System.err.print(Integer.toHexString(registers[x])+", ");
    }
    System.err.println();
    return (read == 256);
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

  public void setPWMFrequency(double frequency) {
    double prescaleval = 25000000.0;//    # 25MHz
    prescaleval /= 4096.0;       // 12-bit
    prescaleval /= frequency;
    prescaleval -= 1.0;

    //log("Setting PWM frequency to " + frequency + "Hz");
    //log("Estimated pre-scale:" + prescaleval);
    double prescale = Math.floor(prescaleval + 0.5);
    //log("Final pre-scale: " + prescale);

    int oldmode = readRegister((byte) __MODE1);
    int newmode = (oldmode & 0x7F) | 0x10;             // sleep
    int intPrescale = (int) (Math.floor(prescale));

    write(__MODE1, (byte) newmode);
    write(__PRESCALE, (byte) intPrescale);
    write(__MODE1, (byte) oldmode);
    delay(5);
    write(__MODE1, (byte) (oldmode | 0x80));
  }

  public void setPWM(short channel, short on, short off) {
    write(__LED0_ON_L + 4 * channel, (byte) (on & 0xFF));
    write(__LED0_ON_H + 4 * channel, (byte) (on >> 8));
    write(__LED0_OFF_L + 4 * channel, (byte) (off & 0xFF));
    write(__LED0_OFF_H + 4 * channel, (byte) (off >> 8));
  }

  private void initialise() throws IOException {
    setAllPWM((byte) 0, (byte) 0); // Reset ALL servos
    write(__MODE2, (byte) __OUTDRV);
    write(__MODE1, (byte) __ALLCALL);
    delay(5);
    int mode1 = readRegister(__MODE1);
    mode1 = mode1 & ~(__SLEEP);
    write(__MODE1, (byte) mode1);
    delay(5);
  }

  private void setAllPWM(short on, short off) {
    write(__ALL_LED_ON_L, (byte) (on & 0xff));
    write(__ALL_LED_ON_H, (byte) (on >> 8));
    write(__ALL_LED_OFF_L, (byte) (off & 0xff));
    write(__ALL_LED_OFF_H, (byte) (off >> 8));
  }

}
