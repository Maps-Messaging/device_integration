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
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.Base64;

public abstract class HT16K33Driver extends I2CDevice {

  private static final byte BRIGHTNESS_COMMAND = (byte) 0xE0;

  private static final byte BLINK_COMMAND = (byte) 0x80;
  private static final byte BLINK_DISPLAYON = 0x01;

  @Getter
  private byte brightness;
  @Getter
  private boolean isOn;
  @Getter
  private BlinkRate rate;
  @Getter
  private String current;

  protected HT16K33Driver(I2C device) throws IOException {
    super(device, LoggerFactory.getLogger(HT16K33Driver.class));
    isOn = false;
    rate = BlinkRate.OFF;
    current = "     ";
    turnOn();
    setBrightness((byte) 0);
    setBlinkRate(rate);
  }

  @Override
  public void close(){
    try {
      turnOff();
    }
    catch(IOException ex){
      // we might have lost the device, so this will fail
    }
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public abstract byte[] encode(String val);

  public void turnOn() throws IOException {
    writeCommand((byte) 0x21); // Turn on
    writeCommand((byte) 0x81); // Turn on display
    logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "turnOn()");
    isOn = true;
  }

  public void turnOff() throws IOException {
    writeCommand((byte) 0x20); // Turn off
    logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "turnOff()");
    isOn = false;
  }

  public void setBlinkRate(BlinkRate rate) throws IOException {
    writeCommand((byte) (BLINK_COMMAND | BLINK_DISPLAYON | (rate.getRate() << 1)));
    logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "setBlinkRate("+rate.name()+")");
    this.rate = rate;
  }

  public void setBrightness(byte brightness) throws IOException {
    this.brightness = (byte) (brightness & 0xf);
    logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "setBlinkRate("+brightness+")");

    writeCommand((byte) (BRIGHTNESS_COMMAND | (brightness & 0xf)));
  }


  public void writeRaw(String val) throws IOException {
    byte[] data = Base64.getDecoder().decode(val);
    if(logger.isDebugEnabled()){
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "writeRaw("+val+")");
    }
    write(0, data);
  }

  public void write(String val) throws IOException {
    current = val;
    if(logger.isDebugEnabled()){
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "write("+val+")");
    }
    write(0, encode(val));
  }

  private void writeCommand(byte command) throws IOException {
    write(new byte[]{command});
  }

}
