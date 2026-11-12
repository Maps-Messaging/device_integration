/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.i2c;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.schemas.config.SchemaConfig;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/** Config-topic discovery and named command routing for I2C controllers. */
public final class I2CConfigurationContract {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private I2CConfigurationContract() {
  }

  public static byte[] describe(I2CDeviceController controller) throws IOException {
    JsonObject contract = new JsonObject();
    contract.addProperty("device", controller.getName());
    contract.addProperty("description", controller.getDescription());
    JsonObject commands = new JsonObject();
    JsonObject schema = schema(controller);
    describeOperations(commands, schema, "x-deviceCommand");
    describeOperations(commands, schema, "x-deviceWrite");
    describeSettings(commands, schema, "x-deviceStatic", controller);
    describeSettings(commands, schema, "x-deviceConfiguration", controller);
    I2CDeviceActions.describe(controller.getDevice(), commands);
    if (registerUpdates(controller) && controller.getDevice() != null) {
      for (Register register : controller.getDevice().getRegisterMap().getRegisters()) {
        if (!writable(register)) {
          continue;
        }
        RegisterData current = register.toData();
        if (current == null) {
          continue;
        }
        String commandName = registerCommand(register);
        if (commands.has(commandName)) {
          throw new IOException("Duplicate config command: " + commandName);
        }
        JsonObject command = new JsonObject();
        command.addProperty("description", "Set " + register.getName());
        command.add("parameters", registerParameters(current));
        commands.add(commandName, command);
      }
    }
    contract.add("commands", commands);
    contract.addProperty("readOnly", commands.isEmpty());
    return contract.toString().getBytes(StandardCharsets.UTF_8);
  }

  public static byte[] dispatch(I2CDeviceController controller, byte[] payload) throws IOException {
    JsonObject request;
    try {
      JsonElement value = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8));
      if (!value.isJsonObject()) {
        return controller.updateDeviceConfiguration(payload);
      }
      request = value.getAsJsonObject();
    } catch (RuntimeException ignored) {
      // Retain plain-text LCD writes and previously supported configuration payloads.
      return controller.updateDeviceConfiguration(payload);
    }
    if (!request.has("command")) {
      return controller.updateDeviceConfiguration(payload);
    }
    JsonElement requested = request.get("command");
    if (!requested.isJsonPrimitive() || !requested.getAsJsonPrimitive().isString()) {
      throw new IOException("Config command must be a string");
    }
    String command = requested.getAsString();
    JsonObject actions = new JsonObject();
    I2CDeviceActions.describe(controller.getDevice(), actions);
    if (actions.has(command)) {
      return I2CDeviceActions.execute(controller.getDevice(), command, request);
    }
    if (registerUpdates(controller) && controller.getDevice() != null) {
      for (Register register : controller.getDevice().getRegisterMap().getRegisters()) {
        if (writable(register) && registerCommand(register).equals(command)) {
          return updateRegister(register, request);
        }
      }
    }

    JsonObject schema = schema(controller);
    if ("SET_CONFIG".equals(command) && !registerUpdates(controller)
        && (object(schema, "x-deviceStatic") != null || object(schema, "x-deviceConfiguration") != null)) {
      return cleanResponse(controller.updateDeviceConfiguration(withoutCommand(request)));
    }
    for (String extension : new String[]{"x-deviceCommand", "x-deviceWrite"}) {
      JsonObject definition = object(schema, extension);
      JsonObject properties = object(definition, "properties");
      if (properties == null) {
        continue;
      }
      String selector = properties.has("action") ? "action" : properties.has("command") ? "command" : null;
      if (selector != null && allowed(object(properties, selector), command)) {
        JsonObject translated = request.deepCopy();
        translated.remove("command");
        translated.addProperty(selector, command);
        return cleanResponse(controller.updateDeviceConfiguration(translated.toString().getBytes(StandardCharsets.UTF_8)));
      }
      if (selector == null && "WRITE".equals(command) && "x-deviceWrite".equals(extension)) {
        return cleanResponse(controller.updateDeviceConfiguration(withoutCommand(request)));
      }
    }
    throw new IOException("Unsupported I2C config command: " + command);
  }

  private static byte[] updateRegister(Register register, JsonObject request) throws IOException {
    RegisterData current = register.toData();
    if (current == null) {
      throw new IOException("Register has no configuration: " + register.getName());
    }
    ObjectNode content = MAPPER.valueToTree(current);
    boolean hasParameters = false;
    for (Map.Entry<String, JsonElement> field : request.entrySet()) {
      if ("command".equals(field.getKey())) {
        continue;
      }
      if ("className".equals(field.getKey()) || !content.has(field.getKey())) {
        throw new IOException("Unknown config parameter: " + field.getKey());
      }
      content.set(field.getKey(), MAPPER.readTree(field.getValue().toString()));
      hasParameters = true;
    }
    if (!hasParameters) {
      throw new IOException("Config command requires parameters: " + register.getName());
    }
    try {
      RegisterData updated = (RegisterData) MAPPER.treeToValue(content, current.getClass());
      if (!register.fromData(updated)) {
        throw new IOException("Register rejected config command: " + register.getName());
      }
    } catch (IllegalArgumentException exception) {
      throw new IOException("Invalid config for " + register.getName(), exception);
    }
    return "{}".getBytes(StandardCharsets.UTF_8);
  }

  private static JsonObject registerParameters(RegisterData data) {
    JsonObject schema = new JsonObject();
    schema.addProperty("type", "object");
    JsonObject properties = new JsonObject();
    JsonObject values = JsonParser.parseString(MAPPER.valueToTree(data).toString()).getAsJsonObject();
    for (Map.Entry<String, JsonElement> value : values.entrySet()) {
      if ("className".equals(value.getKey())) {
        continue;
      }
      JsonObject property = new JsonObject();
      JsonElement item = value.getValue();
      property.addProperty("type", item.isJsonArray() ? "array" : item.isJsonObject() ? "object"
          : item.isJsonPrimitive() && item.getAsJsonPrimitive().isBoolean() ? "boolean"
          : item.isJsonPrimitive() && item.getAsJsonPrimitive().isNumber() ? "number" : "string");
      try {
        Field field = data.getClass().getDeclaredField(value.getKey());
        Class<?> type = field.getType();
        if (type.isEnum()) {
          JsonArray choices = new JsonArray();
          for (Object choice : type.getEnumConstants()) {
            choices.add(((Enum<?>) choice).name());
          }
          property.add("enum", choices);
        }
        if (type == int.class || type == long.class || type == short.class || type == byte.class) {
          property.addProperty("type", "integer");
        }
      } catch (NoSuchFieldException ignored) {
        // Derived JSON property.
      }
      properties.add(value.getKey(), property);
    }
    schema.add("properties", properties);
    schema.addProperty("additionalProperties", false);
    return schema;
  }

  private static void describeOperations(JsonObject commands, JsonObject schema, String extension) {
    JsonObject definition = object(schema, extension);
    JsonObject properties = object(definition, "properties");
    if (properties == null) {
      return;
    }
    String selector = properties.has("action") ? "action" : properties.has("command") ? "command" : null;
    JsonObject field = selector == null ? null : object(properties, selector);
    if (field != null && field.has("enum")) {
      for (JsonElement choice : field.getAsJsonArray("enum")) {
        addCommand(commands, choice.getAsString(), definition, selector, schema);
      }
    } else if ("x-deviceWrite".equals(extension)) {
      addCommand(commands, "WRITE", definition, null, schema);
    }
  }

  private static void describeSettings(JsonObject commands, JsonObject schema, String extension,
                                       I2CDeviceController controller) {
    JsonObject definition = object(schema, extension);
    // Diagnostic/read-only configuration must not be advertised as writable.
    if (definition != null && !registerUpdates(controller)) {
      addCommand(commands, "SET_CONFIG", definition, null, schema);
    }
  }

  private static void addCommand(JsonObject commands, String name, JsonObject definition,
                                 String selector, JsonObject root) {
    JsonObject descriptor = new JsonObject();
    descriptor.addProperty("description", name.replace('_', ' ').toLowerCase(Locale.ROOT));
    JsonObject parameters = definition.deepCopy();
    JsonObject props = object(parameters, "properties");
    if (props != null) {
      props.remove("@class");
      if (selector != null) {
        props.remove(selector);
      }
    }
    if (parameters.has("required")) {
      JsonArray required = new JsonArray();
      for (JsonElement field : parameters.getAsJsonArray("required")) {
        if (!field.getAsString().equals(selector) && !"@class".equals(field.getAsString())) {
          required.add(field.deepCopy());
        }
      }
      parameters.add("required", required);
    }
    descriptor.add("parameters", parameters);
    JsonObject response = object(root, "x-deviceResponse");
    if (response != null) {
      JsonObject cleaned = response.deepCopy();
      JsonObject fields = object(cleaned, "properties");
      if (fields != null) {
        fields.remove("@class");
      }
      descriptor.add("response", cleaned);
    }
    commands.add(name, descriptor);
  }

  private static boolean allowed(JsonObject field, String name) {
    if (field != null && field.has("enum")) {
      for (JsonElement choice : field.getAsJsonArray("enum")) {
        if (name.equals(choice.getAsString())) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean registerUpdates(I2CDeviceController controller) {
    try {
      return controller.getClass().getMethod("updateDeviceConfiguration", byte[].class)
          .getDeclaringClass() == I2CDeviceController.class;
    } catch (NoSuchMethodException exception) {
      return false;
    }
  }

  private static boolean writable(Register register) {
    try {
      return register.getClass().getMethod("fromData", RegisterData.class)
          .getDeclaringClass() != Register.class;
    } catch (NoSuchMethodException exception) {
      return false;
    }
  }

  private static String registerCommand(Register register) {
    return "SET_" + register.getName().replaceAll("[^A-Za-z0-9]+", "_")
        .replaceAll("(?:^_|_$)", "").toUpperCase(Locale.ROOT);
  }

  private static JsonObject schema(I2CDeviceController controller) {
    SchemaConfig config = controller.getSchema();
    return config != null && config.getSchema() != null ? config.getSchema() : new JsonObject();
  }

  private static JsonObject object(JsonObject parent, String key) {
    return parent != null && parent.has(key) && parent.get(key).isJsonObject()
        ? parent.getAsJsonObject(key) : null;
  }

  /** A new named command never exposes Jackson type metadata in its response. */
  private static byte[] cleanResponse(byte[] response) {
    try {
      JsonElement parsed = JsonParser.parseString(new String(response, StandardCharsets.UTF_8));
      if (parsed.isJsonObject()) {
        JsonObject result = parsed.getAsJsonObject();
        result.remove("@class");
        result.remove("className");
        return result.toString().getBytes(StandardCharsets.UTF_8);
      }
    } catch (RuntimeException ignored) {
      // Leave existing non-JSON command responses intact.
    }
    return response;
  }

  private static byte[] withoutCommand(JsonObject request) {
    JsonObject copy = request.deepCopy();
    copy.remove("command");
    return copy.toString().getBytes(StandardCharsets.UTF_8);
  }
}
