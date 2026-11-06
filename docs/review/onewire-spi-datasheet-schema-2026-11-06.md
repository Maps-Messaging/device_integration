# 1-Wire and SPI datasheet/schema review

Branch: `review/i2c-sensor-datasheet-audit-20261105`. No changes to `development`.

## Inventory

The service registrations contain one 1-Wire controller (DS18B20) and one SPI family controller (MCP3004/3008, MCP3204/3208).

## DS18B20: 1-Wire / Linux w1_therm

Datasheet: https://cdn-shop.adafruit.com/datasheets/DS18B20.pdf
Linux kernel interface: https://cdn.kernel.org/doc/html/latest/w1/slaves/w1_therm.html

- The existing driver uses the Linux kernel's `w1_slave` text interface, **not** native 1-Wire ROM commands or scratchpad decoding.
- Kernel `YES` CRC status is required before reading `t=` millidegrees C. Invalid/incomplete samples reject the update, without silently changing last valid readings.
- Current source tracks negative and positive extrema, delta, and temperature range (-55 to +125 °C); no correction to the parser or statistics was necessary.
- Schema previously described `temperature_min`, `temperature_max`, `temperature_delta`, despite `getDeviceState()` emitting **only `temperature`**. Schema now matches emitted state exactly, without changing the wire contract.
- Tests added for schema shape, negative temperatures, CRC failures, and retained last-known-good readings.

## MCP3004/3008 and MCP3204/3208: SPI

Microchip datasheets:
- 10-bit: https://ww1.microchip.com/downloads/aemDocuments/documents/MSLD/ProductDocuments/DataSheets/MCP3004-MCP3008-Data-Sheet-DS20001295.pdf
- 12-bit: https://www.microchip.com/en-us/product/MCP3204

- Existing three-byte 10-bit and 12-bit transfer framing and ADC decode matched datasheet protocols in static review and remain unchanged.
- Constructor now rejects unsupported resolutions/channel counts; channel read rejects negative indices as well as out-of-range positive indices.
- Fixed raw ADC range metadata to `0..1023` for 10-bit and `0..4095` for 12-bit; it previously used `(1 << (bits + 1)) - 1`.
- `getDeviceState()` returns `{"current":[code0,...]}`, not the individual `channel_0` etc. Schema now specifies a required fixed-length `current` array of bounded integers and forbids unexpected state properties.
- `getDeviceConfiguration()` separately reports `resolution`, `channels`, and legacy `dutyCycle`; schema describes it under `x-deviceConfiguration` and enumerates supported ADC variants.
- Tests added for 10-bit and 12-bit frames, invalid channels and variants, schema and state matches across 4/8-channel variants, and unmounted controller state.

## Limits / separate findings

- No physical DS18B20 or SPI ADC tests were run; serial timing, electrical noise, bus recovery and conversion accuracy are hardware-verification items.
- The 1-Wire implementation intentionally delegates sensor protocol and CRC to the Linux kernel. It does not itself implement the DS18B20 command layer.
- Legacy SPI `dutyCycle` appears as a fixed 100000 metadata value, not an actual measured sample rate. Its name and value are retained to preserve compatibility.
- The underlying DS18B20 sensor offers temperature minimum, maximum and delta via the reading API, while the controller state exposes only the latest temperature. Adding those to state would change the JSON contract, so was not done.
- Existing `OneWireDeviceController.setRaiseExceptionOnError()` is a no-op. Changing exception configuration would be a separate API/lifecycle fix, not part of the present datasheet/schema corrections.
- No Maven/JDK 25 tests or runtime JSON-Schema validator were executed in this agent environment. Changes are committed but require CI/hardware verification.
