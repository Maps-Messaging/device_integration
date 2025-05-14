/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.logging;

import io.mapsmessaging.logging.Category;
import io.mapsmessaging.logging.LEVEL;
import io.mapsmessaging.logging.LogMessage;
import lombok.Getter;

@Getter
public enum DeviceLogMessage implements LogMessage {

  //<editor-fold desc="Bus Manager messages">
  BUS_MANAGER_STARTUP(LEVEL.WARN, BUS.MANAGER, "Bus Manager starting up"),
  BUS_MANAGER_CONFIGURE_DEVICES(LEVEL.DEBUG, BUS.MANAGER, "Configure devices called"),
  BUS_MANAGER_PROVIDER(LEVEL.WARN, BUS.MANAGER, "Using GPIO Provider {} from PiGPIO"),
  BUS_MANAGER_SHUTDOWN(LEVEL.WARN, BUS.MANAGER, "Bus Manager shutting down"),
  //</editor-fold>

  //<editor-fold desc="I2C Bus Manager messages">
  I2C_BUS_MANAGER_STARTUP(LEVEL.WARN, BUS.I2C, "I2C Bus Manager starting up"),
  I2C_BUS_LOADED_DEVICE(LEVEL.WARN, BUS.I2C, "I2C Bus Manager ServiceLoader discovered device {} "),
  I2C_BUS_ALLOCATING_ADDRESS(LEVEL.WARN, BUS.I2C, "I2C Bus Manager allocating I2C address {} for device {}"),
  I2C_BUS_CONFIGURING_DEVICE(LEVEL.WARN, BUS.I2C, "I2C Bus Manager configuring device {} at address {}"),
  I2C_BUS_DEVICE_NOT_FOUND(LEVEL.WARN, BUS.I2C, "I2C Bus Manager failed to locate {}, unknown device"),
  I2C_BUS_MANAGER_SHUTDOWN(LEVEL.WARN, BUS.I2C, "I2C Bus Manager shutting down"),
  I2C_BUS_SCAN(LEVEL.WARN, BUS.I2C, "I2C Detect : {}"),
  I2C_BUS_SCAN_MULTIPLE_DEVICES(LEVEL.WARN, BUS.I2C, "Unable to detect which device has been found, needs to be configured {}"),
  //</editor-fold>

  //<editor-fold desc="Bus Device messages">
  I2C_BUS_DEVICE_ALLOCATED(LEVEL.WARN, BUS.I2C_DEVICE, "Allocating device on Bus: {} Address: {}"),
  I2C_BUS_DEVICE_CLOSE(LEVEL.WARN, BUS.I2C_DEVICE, "Closing device on Bus: {} Address: {}"),
  I2C_BUS_DEVICE_READ(LEVEL.DEBUG, BUS.I2C_DEVICE, "Reading from device on Bus: {} Address: {}, Register: {}, Data : {}"),
  I2C_BUS_DEVICE_WRITE(LEVEL.DEBUG, BUS.I2C_DEVICE, "Writing to device on Bus: {} Address: {}, Register: {}, Data : {}"),
  I2C_BUS_DEVICE_DELAY(LEVEL.DEBUG, BUS.I2C_DEVICE, "Delaying device on Bus: {} Address: {} for {}ms"),

  I2C_BUS_DEVICE_READ_REQUEST(LEVEL.DEBUG, BUS.I2C_DEVICE, "{}, requesting {} and received result {}"),
  I2C_BUS_DEVICE_WRITE_REQUEST(LEVEL.DEBUG, BUS.I2C_DEVICE, "{} Called {}"),

  I2C_BUS_INVALID_DATE(LEVEL.ERROR, BUS.I2C_DEVICE, "Invalid date received Year:{}, Month:{}, Date:{}"),
  I2C_BUS_INVALID_TIME(LEVEL.ERROR, BUS.I2C_DEVICE, "Invalid time received Hour:{}, Minute:{}, Second:{}"),
  I2C_BUS_DEVICE_REQUEST_FAILED(LEVEL.DEBUG, BUS.I2C_DEVICE, "{} Failed on request {}, reason {}"),
  //</editor-fold>

  //<editor-fold desc="SPI Bus Manager messages">
  SPI_BUS_MANAGER_STARTUP(LEVEL.WARN, BUS.MANAGER, "SPI Bus Manager starting up"),

  SPI_BUS_MANAGER_SHUTDOWN(LEVEL.WARN, BUS.MANAGER, "SPI Bus Manager shutting down"),
  //</editor-fold>

  //<editor-fold desc="1-Wire Bus Manager messages">
  ONE_WIRE_BUS_MANAGER_STARTUP(LEVEL.WARN, BUS.MANAGER, "1-Wire Bus Manager starting up, scanning {}"),

  ONE_WIRE_BUS_MANAGER_SHUTDOWN(LEVEL.WARN, BUS.MANAGER, "1-Wire Bus Manager shutting down");
  //</editor-fold>

  private final String message;
  @Getter
  private final LEVEL level;
  @Getter
  private final BUS category;
  @Getter
  private final int parameterCount;


  DeviceLogMessage(LEVEL level, BUS category, String message) {
    this.message = message;
    this.level = level;
    this.category = category;
    int location = message.indexOf("{}");
    int count = 0;
    while (location != -1) {
      count++;
      location = message.indexOf("{}", location + 2);
    }
    this.parameterCount = count;
  }


  @Getter
  public enum BUS implements Category {
    MANAGER("Manager"),
    I2C("I2C"),
    I2C_DEVICE("I2C-DEVICE"),
    SPI("SPI"),
    ONE_WIRE("1Wire");

    private final String description;

    BUS(String description) {
      this.description = description;
    }

    public String getDivision() {
      return "Bus";
    }
  }

}
