/*
 *
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *
 */
package io.mapsmessaging.devices.i2c;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.mapsmessaging.devices.deviceinterfaces.PowerManagement;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.Pca9685Device;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.Ds3231Rtc;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.BNO055Sensor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.Mcp23017Device;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.Lps25Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.pn532.Pn532Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.sen0539.Sen0539Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xSensor;

import java.io.IOException;
import java.util.Map;

/**
 * Explicitly advertised config-topic operations backed by existing device methods.
 * Measurement/state schemas, drivers and existing command payloads are not changed.
 */
final class I2CDeviceActions {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private I2CDeviceActions() {
  }

  static void describe(I2CDevice device, JsonObject commands) {
    if (device == null) {
      return;
    }
    if (device instanceof Sen6xSensor) {
      add(commands, "START_MEASUREMENT", "Start or resume measurements");
      add(commands, "STOP_MEASUREMENT", "Stop measurements");
      add(commands, "RESET", "Soft-reset and restore the previous measurement request");
      add(commands, "START_FAN_CLEANING", "Request a manual fan cleaning cycle");
      add(commands, "GET_FIRMWARE_VERSION", "Read sensor firmware version");
      add(commands, "CLEAR_DEVICE_STATE", "Clear device status flags");
    } else {
      if (device instanceof PowerManagement) {
        add(commands, "POWER_ON", "Enable device power or measurements");
        add(commands, "POWER_OFF", "Disable device power or measurements");
      }
      if (device instanceof Resetable) {
        add(commands, "RESET", "Reset the device using its existing driver operation");
        add(commands, "SOFT_RESET", "Perform the driver's soft-reset operation");
      }
    }
    if (device instanceof Ds3231Rtc) {
      add(commands, "SET_DATE_TIME", "Set the RTC local date and time", string("dateTime"));
      add(commands, "SET_DATE", "Set the RTC calendar date", string("date"));
      add(commands, "SET_TIME", "Set the RTC local time", string("time"));
      add(commands, "SET_ALARM_DATE_TIME", "Set a date-and-time alarm", integer("alarm", 1, 2), string("dateTime"));
      add(commands, "SET_ALARM_TIME", "Set a daily time alarm", integer("alarm", 1, 2), string("time"));
    }
    if (device instanceof BNO055Sensor) {
      add(commands, "SET_CONFIG_MODE", "Switch the sensor to configuration mode");
      add(commands, "SET_OPERATIONAL_MODE", "Switch the sensor to operational mode");
    }
    if (device instanceof Lps25Sensor) {
      add(commands, "SET_POWER_DOWN_MODE", "Set pressure sensor power-down mode", bool("enabled"));
    }
    if (device instanceof BME688Sensor) {
      add(commands, "START_FORCED_MODE", "Start BME688 forced measurement mode");
      add(commands, "START_PARALLEL_MODE", "Start BME688 parallel measurement mode");
    }
    if (device instanceof Sen0539Sensor) {
      add(commands, "SET_MUTED", "Mute or unmute the voice module", bool("muted"));
      add(commands, "SET_VOLUME", "Set voice-module playback volume (0 to 7)", integer("volume", 0, 7));
      add(commands, "SET_WAKE_TIME", "Set recognition wake duration in seconds", integer("seconds", 0, 255));
      add(commands, "PLAY_BY_COMMAND_ID", "Play the response for an existing command ID", integer("id", 0, 255));
    }
    if (device instanceof Mcp23017Device) {
      Param pin = integer("pin", 0, 15);
      for (String action : new String[]{"SET_INPUT", "SET_OUTPUT", "SET_HIGH", "SET_LOW",
          "ENABLE_PULL_UP", "DISABLE_PULL_UP", "ENABLE_INTERRUPT", "DISABLE_INTERRUPT",
          "SET_INTERRUPT_ON_HIGH", "SET_INTERRUPT_ON_LOW", "GET_PIN_STATE"}) {
        add(commands, action, action.replace('_', ' ').toLowerCase(), pin);
      }
    }
    if (device instanceof Pca9685Device) {
      add(commands, "SET_PWM_FREQUENCY", "Set PWM frequency in hertz", number("frequency", 40, 1200));
      add(commands, "SET_PWM", "Set one PWM channel", integer("channel", 0, 15),
          integer("on", 0, 4095), integer("off", 0, 4095));
      add(commands, "SET_ALL_PWM", "Set all PWM channels", integer("on", 0, 4095), integer("off", 0, 4095));
    }
    if (device instanceof Pn532Sensor) {
      add(commands, "POLL_TAG", "Poll for an NFC tag and return its UID");
      add(commands, "GET_CARD_INFORMATION", "Read information about the selected NFC card");
      add(commands, "READ_NTAG", "Read 16 bytes beginning at an NTAG page", integer("page", 0, 231));
      add(commands, "WRITE_NTAG", "Write four bytes to an NTAG user page",
          integer("page", 4, 225), bytes("data", 4));
      add(commands, "READ_ULTRALIGHT", "Read four bytes from an Ultralight page", integer("page", 0, 41));
      add(commands, "WRITE_ULTRALIGHT", "Write four bytes to an Ultralight page",
          integer("page", 4, 225), bytes("data", 4));
      add(commands, "READ_MIFARE_BLOCK", "Read 16 bytes from an authenticated MIFARE block",
          integer("block", 0, 255));
      add(commands, "WRITE_MIFARE_BLOCK", "Write 16 bytes to an authenticated MIFARE data block",
          integer("block", 0, 255), bytes("data", 16));
    }
  }

  static byte[] execute(I2CDevice device, String command, JsonObject request) throws IOException {
    if (device == null) {
      throw new IOException("I2C device is not mounted");
    }
    JsonObject available = new JsonObject();
    describe(device, available);
    if (!available.has(command)) {
      throw new IOException("Unsupported I2C action: " + command);
    }
    validate(request, available.getAsJsonObject(command).getAsJsonObject("parameters"));
    try {
      return dispatch(device, command, request);
    } catch (IllegalArgumentException | UnsupportedOperationException | java.time.DateTimeException exception) {
      throw new IOException("Invalid I2C action " + command + ": " + exception.getMessage(), exception);
    } catch (java.io.UncheckedIOException exception) {
      throw exception.getCause();
    }
  }

  private static byte[] dispatch(I2CDevice device, String command, JsonObject request) throws IOException {
    if (device instanceof Sen6xSensor sen) {
      return switch (command) {
        case "START_MEASUREMENT" -> { sen.powerOn(); yield ok(); }
        case "STOP_MEASUREMENT" -> { sen.powerOff(); yield ok(); }
        case "RESET" -> { sen.reset(); yield ok(); }
        case "START_FAN_CLEANING" -> { sen.startFanCleaning(); yield requested(); }
        case "GET_FIRMWARE_VERSION" -> value(sen.getFirmwareVersion());
        case "CLEAR_DEVICE_STATE" -> { sen.getClearDeviceStateCommand().execute(); yield ok(); }
        default -> throw new IOException("Unsupported SEN6x action: " + command);
      };
    }
    if (device instanceof PowerManagement power) {
      if ("POWER_ON".equals(command)) { power.powerOn(); return ok(); }
      if ("POWER_OFF".equals(command)) { power.powerOff(); return ok(); }
    }
    if (device instanceof Resetable resetable) {
      if ("RESET".equals(command)) { resetable.reset(); return ok(); }
      if ("SOFT_RESET".equals(command)) { resetable.softReset(); return ok(); }
    }
    if (device instanceof Ds3231Rtc clock) {
      switch (command) {
        case "SET_DATE_TIME" -> clock.setDateTime(LocalDateTime.parse(request.get("dateTime").getAsString()));
        case "SET_DATE" -> clock.setDate(LocalDate.parse(request.get("date").getAsString()));
        case "SET_TIME" -> clock.setTime(LocalTime.parse(request.get("time").getAsString()));
        case "SET_ALARM_DATE_TIME" -> clock.setAlarm(intValue(request, "alarm"),
            LocalDateTime.parse(request.get("dateTime").getAsString()));
        case "SET_ALARM_TIME" -> clock.setAlarm(intValue(request, "alarm"),
            LocalTime.parse(request.get("time").getAsString()));
        default -> throw new IOException("Unsupported RTC action: " + command);
      }
      return ok();
    }
    if (device instanceof BNO055Sensor orientation) {
      switch (command) {
        case "SET_CONFIG_MODE" -> orientation.setConfigMode();
        case "SET_OPERATIONAL_MODE" -> orientation.setOperationalMode();
        default -> throw new IOException("Unsupported BNO055 action: " + command);
      }
      return ok();
    }
    if (device instanceof Lps25Sensor lps && "SET_POWER_DOWN_MODE".equals(command)) {
      lps.setPowerDownMode(boolValue(request, "enabled"));
      return ok();
    }
    if (device instanceof BME688Sensor bme) {
      if ("START_FORCED_MODE".equals(command)) { bme.startForceMode(); return ok(); }
      if ("START_PARALLEL_MODE".equals(command)) { bme.startParallelMode(); return ok(); }
    }
    if (device instanceof Sen0539Sensor voice) {
      switch (command) {
        case "SET_MUTED" -> voice.setMuted(boolValue(request, "muted"));
        case "SET_VOLUME" -> voice.setVolume(intValue(request, "volume"));
        case "SET_WAKE_TIME" -> voice.setWakeTimeSeconds(intValue(request, "seconds"));
        case "PLAY_BY_COMMAND_ID" -> voice.playByCommandId(intValue(request, "id"));
        default -> throw new IOException("Unsupported voice module action: " + command);
      }
      return ok();
    }
    if (device instanceof Mcp23017Device gpio) {
      int pin = intValue(request, "pin");
      return switch (command) {
        case "SET_INPUT" -> { gpio.setInput(pin); yield ok(); }
        case "SET_OUTPUT" -> { gpio.setOutput(pin); yield ok(); }
        case "SET_HIGH" -> { gpio.setHigh(pin); yield ok(); }
        case "SET_LOW" -> { gpio.setLow(pin); yield ok(); }
        case "ENABLE_PULL_UP" -> { gpio.enablePullUp(pin); yield ok(); }
        case "DISABLE_PULL_UP" -> { gpio.disablePullUp(pin); yield ok(); }
        case "ENABLE_INTERRUPT" -> { gpio.enableInterrupt(pin); yield ok(); }
        case "DISABLE_INTERRUPT" -> { gpio.disableInterrupt(pin); yield ok(); }
        case "SET_INTERRUPT_ON_HIGH" -> { gpio.setOnHigh(pin); yield ok(); }
        case "SET_INTERRUPT_ON_LOW" -> { gpio.setOnLow(pin); yield ok(); }
        case "GET_PIN_STATE" -> {
          JsonObject state = new JsonObject();
          state.addProperty("output", gpio.isOutput(pin));
          state.addProperty("set", gpio.isSet(pin));
          yield state.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        default -> throw new IOException("Unsupported GPIO action: " + command);
      };
    }
    if (device instanceof Pca9685Device pwm) {
      switch (command) {
        case "SET_PWM_FREQUENCY" -> pwm.setPWMFrequency(numberValue(request, "frequency"));
        case "SET_PWM" -> pwm.setPWM(intValue(request, "channel"), intValue(request, "on"), intValue(request, "off"));
        case "SET_ALL_PWM" -> pwm.setAllPWM(intValue(request, "on"), intValue(request, "off"));
        default -> throw new IOException("Unsupported PWM action: " + command);
      }
      return ok();
    }
    if (device instanceof Pn532Sensor nfc) {
      return switch (command) {
        case "POLL_TAG" -> value(nfc.poll());
        case "GET_CARD_INFORMATION" -> json(nfc.getInformation());
        case "READ_NTAG" -> value(nfc.readNTAG(intValue(request, "page")));
        case "WRITE_NTAG" -> { nfc.writeNTAG(intValue(request, "page"), byteValue(request, "data")); yield ok(); }
        case "READ_ULTRALIGHT" -> value(nfc.readUltralight(intValue(request, "page")));
        case "WRITE_ULTRALIGHT" -> {
          nfc.writeUltralight(intValue(request, "page"), byteValue(request, "data")); yield ok();
        }
        case "READ_MIFARE_BLOCK" -> value(nfc.readMifareBlock(intValue(request, "block")));
        case "WRITE_MIFARE_BLOCK" -> {
          nfc.writeMifareBlock(intValue(request, "block"), byteValue(request, "data")); yield ok();
        }
        default -> throw new IOException("Unsupported NFC action: " + command);
      };
    }
    throw new IOException("Unsupported I2C action: " + command);
  }

  private record Param(String name, String type, Number minimum, Number maximum, Integer length) {
  }

  private static Param integer(String name, int minimum, int maximum) {
    return new Param(name, "integer", minimum, maximum, null);
  }

  private static Param number(String name, double minimum, double maximum) {
    return new Param(name, "number", minimum, maximum, null);
  }

  private static Param string(String name) {
    return new Param(name, "string", null, null, null);
  }

  private static Param bool(String name) {
    return new Param(name, "boolean", null, null, null);
  }

  private static Param bytes(String name, int length) {
    return new Param(name, "array", 0, 255, length);
  }

  private static void add(JsonObject commands, String name, String description, Param... parameters) {
    JsonObject entry = new JsonObject();
    entry.addProperty("description", description);
    JsonObject input = new JsonObject();
    input.addProperty("type", "object");
    JsonObject properties = new JsonObject();
    JsonArray required = new JsonArray();
    for (Param param : parameters) {
      JsonObject schema = new JsonObject();
      schema.addProperty("type", param.type());
      if ("array".equals(param.type())) {
        JsonObject items = new JsonObject();
        items.addProperty("type", "integer");
        items.addProperty("minimum", param.minimum().intValue());
        items.addProperty("maximum", param.maximum().intValue());
        schema.add("items", items);
        schema.addProperty("minItems", param.length());
        schema.addProperty("maxItems", param.length());
      } else {
        if ("string".equals(param.type())) {
          if ("dateTime".equals(param.name())) { schema.addProperty("description", "ISO-8601 local date and time, without an offset"); }
          if ("date".equals(param.name())) { schema.addProperty("format", "date"); }
          if ("time".equals(param.name())) { schema.addProperty("description", "ISO-8601 local time, without an offset"); }
        }
        if (param.minimum() != null) { schema.addProperty("minimum", param.minimum()); }
        if (param.maximum() != null) { schema.addProperty("maximum", param.maximum()); }
      }
      properties.add(param.name(), schema);
      required.add(param.name());
    }
    input.add("properties", properties);
    input.add("required", required);
    input.addProperty("additionalProperties", false);
    entry.add("parameters", input);
    JsonObject response = new JsonObject();
    response.addProperty("type", "object");
    JsonObject responseProperties = new JsonObject();
    JsonObject field = new JsonObject();
    switch (name) {
      case "GET_FIRMWARE_VERSION", "POLL_TAG" -> {
        field.addProperty("type", "string");
        responseProperties.add("value", field);
      }
      case "READ_NTAG", "READ_ULTRALIGHT", "READ_MIFARE_BLOCK" -> {
        field.addProperty("type", "string");
        field.addProperty("contentEncoding", "base64");
        responseProperties.add("data", field);
      }
      case "GET_PIN_STATE" -> {
        field.addProperty("type", "boolean");
        responseProperties.add("output", field.deepCopy());
        responseProperties.add("set", field.deepCopy());
      }
      case "GET_CARD_INFORMATION" -> response.addProperty("description", "NFC card information");
      default -> {
        field.addProperty("type", "string");
        field.addProperty("const", "START_FAN_CLEANING".equals(name) ? "requested" : "success");
        responseProperties.add("status", field);
      }
    }
    response.add("properties", responseProperties);
    entry.add("response", response);
    commands.add(name, entry);
  }

  private static void validate(JsonObject request, JsonObject schema) throws IOException {
    JsonObject fields = schema.getAsJsonObject("properties");
    for (Map.Entry<String, JsonElement> item : request.entrySet()) {
      if ("command".equals(item.getKey())) { continue; }
      if (!fields.has(item.getKey())) { throw new IOException("Unexpected parameter: " + item.getKey()); }
    }
    for (Map.Entry<String, JsonElement> item : fields.entrySet()) {
      String key = item.getKey();
      if (!request.has(key) || request.get(key).isJsonNull()) {
        throw new IOException("Missing required parameter: " + key);
      }
      JsonObject constraints = item.getValue().getAsJsonObject();
      JsonElement value = request.get(key);
      String type = constraints.get("type").getAsString();
      if ("boolean".equals(type)) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
          throw new IOException("Parameter must be boolean: " + key);
        }
      } else if ("array".equals(type)) {
        if (!value.isJsonArray() || value.getAsJsonArray().size() != constraints.get("minItems").getAsInt()) {
          throw new IOException("Incorrect byte-array size for " + key);
        }
        for (JsonElement entry : value.getAsJsonArray()) {
          if (!isIntInRange(entry, 0, 255)) { throw new IOException("Invalid byte value for " + key); }
        }
      } else if ("string".equals(type)) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
          throw new IOException("Parameter must be a string: " + key);
        }
      } else if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
        throw new IOException("Parameter must be numeric: " + key);
      } else {
        if ("integer".equals(type) && !isWhole(value)) {
          throw new IOException("Parameter must be an integer: " + key);
        }
        double numeric = value.getAsDouble();
        if (!Double.isFinite(numeric) || numeric < constraints.get("minimum").getAsDouble()
            || numeric > constraints.get("maximum").getAsDouble()) {
          throw new IOException("Parameter outside range: " + key);
        }
      }
    }
  }

  private static boolean isIntInRange(JsonElement value, int minimum, int maximum) {
    return value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber() && isWhole(value)
        && value.getAsDouble() >= minimum && value.getAsDouble() <= maximum;
  }

  private static boolean isWhole(JsonElement value) {
    double number = value.getAsDouble();
    return Double.isFinite(number) && Math.rint(number) == number;
  }

  private static int intValue(JsonObject request, String name) {
    return request.get(name).getAsInt();
  }

  private static float numberValue(JsonObject request, String name) {
    return request.get(name).getAsFloat();
  }

  private static boolean boolValue(JsonObject request, String name) {
    return request.get(name).getAsBoolean();
  }

  private static byte[] byteValue(JsonObject request, String name) throws IOException {
    return MAPPER.readValue(request.get(name).toString(), byte[].class);
  }

  private static byte[] ok() {
    return "{\"status\":\"success\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
  }

  private static byte[] requested() {
    return "{\"status\":\"requested\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
  }

  private static byte[] value(Object result) throws IOException {
    JsonObject response = new JsonObject();
    if (result instanceof byte[] data) {
      response.addProperty("data", java.util.Base64.getEncoder().encodeToString(data));
    } else if (result instanceof Number number) {
      response.addProperty("value", number);
    } else if (result instanceof Boolean bool) {
      response.addProperty("value", bool);
    } else if (result instanceof String str) {
      response.addProperty("value", str);
    } else {
      response.add("value", com.google.gson.JsonParser.parseString(MAPPER.writeValueAsString(result)));
    }
    return response.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
  }

  private static byte[] json(Object result) throws IOException {
    return MAPPER.writeValueAsBytes(result);
  }
}
