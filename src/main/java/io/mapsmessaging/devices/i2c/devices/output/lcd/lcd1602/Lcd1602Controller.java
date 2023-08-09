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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.data.ActionType;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.data.Lcd1602Command;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.data.Lcd1602Response;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.task.Clock;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class Lcd1602Controller extends I2CDeviceController {

  private final Lcd1602Device display;

  @Getter
  private final String name = "LCD1602";
  @Getter
  private final String description = "LCD1602 16*2 lcd display";

  // Used during ServiceLoading
  public Lcd1602Controller() {
    display = null;
  }

  protected Lcd1602Controller(AddressableDevice device) throws IOException {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      display = new Lcd1602Device(device);
      display.clearDisplay();
      display.setRows(2);
      display.setColumns(16);
      Thread t = new Thread( new Clock(this));
      t.setDaemon(true);
      t.start();
    }
  }

  public I2CDevice getDevice() {
    return display;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return display != null && display.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Lcd1602Controller(device);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    JavaType type = objectMapper.getTypeFactory().constructType(Lcd1602Command.class);
    Lcd1602Command command = objectMapper.readValue(new String(val), type);
    Lcd1602Response response = null;
    if (display != null) {
      if (command.getAction() == ActionType.READ) {
        byte[] data = display.readBlock(command.getAddress(), command.getLength());
        response = new Lcd1602Response("Success", data);
      } else if (command.getAction() == ActionType.WRITE) {
        display.writeBlock(command.getAddress(), command.getData());
        response = new Lcd1602Response("Success", new byte[0]);
      } else if (command.getAction() == ActionType.CLEAR) {
        display.clearDisplay();
        response = new Lcd1602Response("Success", new byte[0]);
      }
    } else {
      response = new Lcd1602Response("Error", new byte[0]);
    }
    ObjectMapper objectMapper2 = new ObjectMapper();
    return objectMapper2.writeValueAsString(response).getBytes();
  }


  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (display != null) {
      //
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (display != null) {
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device LCD1602 2x16 LCD display");
    config.setSource("I2C bus address : 0x3e");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("display");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{0x3e};
  }
}
