# Device library examples

Use JDK 25, the hardware permissions in [Linux setup](linux-setup.md), and the current `io.mapsmessaging.devices` APIs. These examples are **library examples**, not promises of a separate HTTP endpoint in the device library.

## 1. Discover I²C devices and retrieve their schemas

```java
import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.schemas.config.SchemaConfig;

DeviceBusManager manager = DeviceBusManager.getInstance();
I2CBusManager bus = manager.getI2cBusManager()[1]; // Replace with your actual bus.
bus.scanForDevices(0);

for (DeviceController controller : bus.getActive().values()) {
  SchemaConfig schema = controller.getSchema();
  byte[] state = controller.getDeviceState();
  System.out.println(controller.getName() + ": " + new String(state, java.nio.charset.StandardCharsets.UTF_8));
  // Server integration registers schema and associates it with its topic
  // before publishing the state bytes. Avoid registering schema on every poll.
}
```

Bus scans and mounted controllers are not a substitute for hardware identity when several peripherals share an address. In particular check the MPU6050/DS3231 collision at `0x68`.

## 2. Explicitly select an I²C device

```java
import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CDeviceController;

I2CDeviceController controller =
    DeviceBusManager.getInstance().getI2cBusManager()[1].configureDevice(0x68, "DS3231");
byte[] state = controller.getDeviceState();
```

This uses a registered driver name and a seven-bit device address. The bus manager's `configureDevices(...)` currently mounts the **first matching configured entry**, so configure additional devices individually or scan, rather than assuming one call mounts every entry in a map.

## 3. Configure an MCP3208 SPI ADC

```java
import io.mapsmessaging.devices.DeviceBusManager;
import java.util.LinkedHashMap;
import java.util.Map;

Map<String, String> adc = new LinkedHashMap<>();
adc.put("spiBus", "0");
adc.put("spiChipSelect", "0");
adc.put("spiMode", "0");
adc.put("resolution", "12");
adc.put("channels", "8");

Map<String, Object> spi = new LinkedHashMap<>();
spi.put("Mcp3y0x", adc);

DeviceBusManager.getInstance().getSpiBusManager().configureDevices(spi);
```

Its state has a `current` array of eight **12-bit ADC codes** (0–4095), not calibrated voltages. For MCP3004/3008 use `resolution=10`; choose 4 or 8 channels according to the actual chip.

## 4. Read a Linux DS18B20 via its controller

```java
import io.mapsmessaging.devices.onewire.devices.ds18b20.DS18B20Controller;
import java.io.File;
import java.nio.charset.StandardCharsets;

File sample = new File("/sys/bus/w1/devices/28-XXXXXXXXXXXX/w1_slave");
DS18B20Controller sensor = new DS18B20Controller(sample);
System.out.println(new String(sensor.getDeviceState(), StandardCharsets.UTF_8));
```

Replace the placeholder with an actual `28-*` directory. Expect a JSON `temperature` in °C. The kernel's first `w1_slave` line must report CRC `YES`; a failed read is not a new measurement.

## 5. Output device command JSON

Send the UTF-8 JSON bytes to the **mounted** controller's `updateDeviceConfiguration(...)`.

HT16K33 text and brightness:

```json
{"deviceStatic":{"brightness":8,"blink":"OFF","enabled":true},"deviceWrite":{"display":"12:34"}}
```

LCD1602 displays `Hello` at character offset zero:

```json
{"action":"WRITE","address":0,"data":[72,101,108,108,111]}
```

Or clear the supported display:

```json
{"action":"CLEAR"}
```

ST7735 RGB565 adapter:

```json
{"command":"SET_PIXEL","x":3,"y":4,"color":"#ff0000"}
```

`DRAW_IMAGE` requires `x`, `y`, `width`, `height`, and Base64-encoded RGB565 bytes, exactly two bytes per pixel, within the logical 160 × 80 bounds. The supported adapter is an **I²C** implementation; a plain SPI ST7735 cannot be connected through this controller.

## 6. EEPROM command JSON

For AT24C devices the controller exposes separate state and command contracts. A READ request selects a byte offset and length; write payloads are binary bytes encoded using the controller's Jackson mapping. Use its `getSchema()` command metadata to inspect the accepted fields. For example, the conceptual request fields are:

```json
{"action":"READ","address":0,"length":16}
```

**Important:** The `AT24CnnCommand` class currently uses Jackson `@class` polymorphic metadata. Depending on the mapper's type-id resolution, callers may need the type discriminator; test your exact JSON on the running controller before treating this as a complete on-wire example. The default driver capacity is conservatively 4096 bytes and cannot identify a larger chip by probing. Use an explicitly configured capacity for larger EEPROM models.

## 7. Server-side schema association

At discovery time the MapsMessaging server should consume both the **schema** and payload from a mounted controller. The server is responsible for:

1. Determining/creating the correct topic and permissions.
2. Registering or locating the schema by stable identity and version.
3. Associating the schema with the topic **before** first publish.
4. Publishing the bytes returned by `getDeviceState()` without device-specific sensor logic.
5. Updating the association when the device model or encoding/schema changes.

These are integration responsibilities, **not** new library APIs implemented by the examples. Currently schema content describes JSON; a future Protobuf/Avro integration will need a matching serialization and schema-format contract. Do not attach JSON Schema metadata to binary Protobuf bytes.

## 8. Verification example checklist

After mounting a device, inspect `getDeviceState()` and `getSchema()` from the **same** instance, check that units and field names match, then verify at least one real measurement against an independent instrument. For outputs, inspect the command response and visible state. Disconnect once to check error reporting and reconnect behaviour. Do not use live safety-critical gas values, writable RFID tags or stored EEPROM data as casual test targets.
