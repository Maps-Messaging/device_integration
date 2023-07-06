package io.mapsmessaging.devices.logging;

import io.mapsmessaging.logging.Category;
import io.mapsmessaging.logging.LEVEL;
import io.mapsmessaging.logging.LogMessage;
import lombok.Getter;

public enum DeviceLogMessage implements LogMessage {

  //<editor-fold desc="Bus Manager messages">
  BUS_MANAGER_STARTUP(LEVEL.WARN, BUS.MANAGER, "Bus Manager starting up"),

  BUS_MANAGER_SHUTDOWN(LEVEL.WARN, BUS.MANAGER, "Bus Manager shutting down"),
  //</editor-fold>

  //<editor-fold desc="I2C Bus Manager messages">
  I2C_BUS_MANAGER_STARTUP(LEVEL.WARN, BUS.I2C, "I2C Bus Manager starting up"),

  I2C_BUS_MANAGER_SHUTDOWN(LEVEL.WARN, BUS.I2C, "I2C Bus Manager shutting down"),
  //</editor-fold>

  //<editor-fold desc="SPI Bus Manager messages">
  SPI_BUS_MANAGER_STARTUP(LEVEL.WARN, BUS.MANAGER, "SPI Bus Manager starting up"),

  SPI_BUS_MANAGER_SHUTDOWN(LEVEL.WARN, BUS.MANAGER, "SPI Bus Manager shutting down"),
  //</editor-fold>

  //<editor-fold desc="1-Wire Bus Manager messages">
  ONE_WIRE_BUS_MANAGER_STARTUP(LEVEL.WARN, BUS.MANAGER, "1-Wire Bus Manager starting up, scanning {}"),

  ONE_WIRE_BUS_MANAGER_SHUTDOWN(LEVEL.WARN, BUS.MANAGER, "1-Wire Bus Manager shutting down");
  //</editor-fold>

  @Getter
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


  public enum BUS implements Category {
    MANAGER("Manager"),
    I2C("I2C"),
    SPI("SPI"),
    ONE_WIRE("1Wire");

    private final @Getter String description;

    BUS(String description) {
      this.description = description;
    }

    public String getDivision() {
      return "Bus";
    }
  }

}
