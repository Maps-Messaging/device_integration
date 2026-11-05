# I2C JSON schema review (2026-11-06)

Branch: `review/i2c-sensor-datasheet-audit-20261105`. Scope: schemas and schema metadata only; no device measurement, command or configuration behaviour changed.

## Existing wire contracts

- Most I2C sensors use `I2CDeviceController.getDeviceState()`, populated from `Sensor.getReadings()`, with an optional ISO-8601 `timestamp`. A failing reading can become an error-message string. An unavailable optional reading can disappear. An exception caught at controller level can result in `{}`.
- BNO055 overrides the generic sensor state and emits flat numeric `heading`, `roll`, `pitch`. Its calibration, system status, error status and version instead come from `getDeviceConfiguration()`.
- INA219 emits electrical measurements as state; ADC/range/gain/mode enums are writable configuration, not state readings.
- AT24C EEPROM emits `name` and `size` (bytes), optionally a Jackson `@class` discriminator. Its READ/WRITE operation returns `status` and a byte-array `data`, encoded as Base64 by Jackson.
- HT16K33 LED controllers emit `display`, `blink`, `enabled`, `brightness`. The same controllers accept writable settings and `display`, `raw`, `task` actions.
- LCD1602, ST7735, PCA9685, MCP23017 and LCD backlight controllers currently emit `{}` as state, even if the underlying hardware offers more information. LCD1602 and ST7735 expose write commands separately.

## Changes

1. I2C-specific reading schemas now represent a value **or** the error string actually emitted; no reading is wrongly required when the controller can return `{}`. Unmounted controllers have valid empty schemas.
2. BNO055 state schema describes its three flat angles and separately declares configuration using `x-deviceConfiguration`.
3. INA219 no longer mixes configuration enum keys into its measurement properties. The configuration contract is documented under `x-deviceConfiguration`.
4. AT24C schema describes name and byte capacity, plus `x-deviceCommand` and `x-deviceResponse` (including Base64 byte arrays). No EEPROM read/write semantics changed.
5. HT16K33, LCD1602 and ST7735 describe supported commands, emitted state and responses separately, without claiming unsupported static configuration.
6. PCA9685, MCP23017 and LCD backlights no longer publish ungrounded data schemas. Sensor-description corrections were applied for AM2320, SCD41, SEN6x and TSL2561. The demo PMSA003I controller now exposes its simulated readings instead of an empty schema.

## Suggested long-term schema convention (NOT implemented)

Use three distinct named schemas per device: `state` for `getDeviceState()`, `configuration` for `getDeviceConfiguration()`, and `command`/`response` for `updateDeviceConfiguration()`. Attach these to device metadata using stable, versioned names. A flat state schema should validate the flat state payload; never confuse it with the command envelope. Treat `x-deviceConfiguration`, `x-deviceCommand`, `deviceState`, etc. as transition metadata, not as a standardised new public protocol.

For LED/LCD state, expose actual visible text, brightness, dimensions and last-operation status only if the controller implements that state. For EEPROM, expose capacity and memory geometry as metadata but **not** arbitrary memory contents; use explicitly bounded READ/WRITE commands for bytes.

## Limitations and separate findings

- Controller state is currently empty for MCP23017 despite its 16 GPIO readings being accessible via the device API. Exposing those readings would change runtime behaviour and was **not** implemented.
- RTC `LocalDateTime` is passed through Gson's generic object-serialization path; its actual date JSON formatting under JDK25 needs a runtime capture. This may be a serializer defect, not merely a schema issue.
- The current repository has no single standardized multi-contract schema format. The newly added names are backward-compatible metadata only. Upgrading consumers to a universal layout is a separate design decision.
- Static review and dedicated unit tests were added; no full Maven/JDK25 suite or hardware validation was performed in this environment.
