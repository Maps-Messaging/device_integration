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

package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import lombok.Getter;

import java.util.Base64;

public abstract class HT16K33Driver extends I2CDevice {

  private static final byte BRIGHTNESS_COMMAND = (byte) 0xE0;

  private static final byte BLINK_COMMAND = (byte)0x80;
  private static final byte BLINK_DISPLAYON = 0x01;

  @Getter
  private byte brightness;
  @Getter
  private boolean isOn;
  @Getter
  private BlinkRate rate;
  @Getter
  private String current;

  protected HT16K33Driver(I2C device) {
    super(device);
    isOn = false;
    rate = BlinkRate.OFF;
    current = "     ";
    turnOn();
    setBrightness((byte)0);
    setBlinkRate(rate);
  }

  @Override
  public void close() {
    turnOff();
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public abstract byte[] encode(String val);

  public void turnOn() {
    writeCommand( (byte)0x21); // Turn on
    writeCommand( (byte)0x81); // Turn on display
    isOn = true;
  }

  public void turnOff() {
    writeCommand((byte)0x20); // Turn off
    isOn = false;
  }

  public void setBlinkRate(BlinkRate rate){
    writeCommand((byte) (BLINK_COMMAND | BLINK_DISPLAYON | (rate.getRate() << 1)));
    this.rate = rate;
  }

  public void setBrightness(byte brightness) {
    this.brightness = (byte) (brightness & 0xf);
    writeCommand((byte) (BRIGHTNESS_COMMAND | (brightness & 0xf)));
  }


  public void writeRaw(String val){
    write(0, Base64.getDecoder().decode(val));
  }

  public void write(String val) {
    current = val;
    write(0, encode(val));
  }

  private void writeCommand(byte command){
    write(new byte[]{command});
  }

}
