# Device Library review — 30 September 2026

Tracking: MSG-350. Branch: `refactor/msg-350-device-code-cleanup`. Pi4J remains **2.8.0**; no Java 25 migration is included.

## Scope and evidence

The sweep covers all **643 current production Java files** under `io/mapsmessaging/devices`, including every controller, sensor, register, driver, storage, output, serial, SPI, GPIO, one-wire, direct GPIO and mock implementation. Font tables were structurally checked and sampled. [Full inventory](review/inventory.txt).

“Confirmed” in the detailed review means established by source/data flow or manufacturer documentation, not demonstrated on physical hardware. Conditional findings identify provider, concurrency or hardware assumptions. New defects outside the agreed SEN6x/register/I2C cleanup scope are documented for separate Jira-backed changes, not silently repaired here. Line references describe the reviewed snapshot and can move with subsequent fixes. Overlapping reports are evidence groups, not a unique issue count.

## Highest-priority remaining work

| Area | Finding | Consequence | First regression fixture |
| --- | --- | --- | --- |
| AT24 EEPROM | Mount probes capacity by writing; smaller parts wrap addresses; page sizes wrong for AT24C128/256 | Existing stored bytes can change during mounting or page writes | Address-wrap and busy-NACK EEPROM model; assert all bytes preserved |
| SCD41 | Recalibration sends factory-reset command; readiness recurses; getters request no response | Calibration/settings can be erased and common operations fail | Exact command bytes, idle ordering, response length and target CO2 |
| BMP280 | Implementation uses MS5611-style commands and wrong address | Valid BMP280 cannot provide correct data | Bosch chip ID/calibration/ADC fixture |
| I2C configuration | Required bus metadata parsed as address; only first device mounted | Normal multi-device setup fails or omits devices | Two addresses plus bus metadata |
| Shared bit registers / MCP23017 | Clear mask preserves selected bit and clears neighbors; interrupt setup ignores INTCON | Unrelated GPIO outputs can change; wrong interrupts | Full port before/after clear and interrupt mode fixture |
| BME688 | Signed calibration and ADC decoding, pressure compensation and control masks incorrect | Measurements and configuration are wrong | Bosch reference vectors and register-preservation assertions |
| INA219 | Configuration masks shifted twice; missing voltage scale and signed current | Voltage/current/power can be materially wrong | 12V and negative-current vectors; exact configuration word |
| PN532 | Response split at STOP and TFI/command decoded twice | Initialization/card operations fail on valid responses | Complete raw ACK/response transaction fixtures |
| LCD clock | Infinite task holds global bus lock | Other I2C devices stop making progress | Concurrent scheduled sensor while clock runs |
| Serial / SPI / RTC | Fragmented reads overwrite data; MCP320x frame timing wrong; DS3231 mode/EOSC inverted | Broken measurements, alarms or battery timekeeping | Fragmentation, conversion frames and full day/hour roundtrips |

Detailed evidence and proposed fixtures:

- [Core, serialization and shared register behavior](review/core.md)
- [AM2315/AM2320, BH1750, BME688, BMP280, Gravity and INA219](review/environmental-sensors.md)
- [AS3935, BNO055, LPS25/LPS35, MSA311, PMSA003I, PN532, SCD41, SEN0539, SHT31 and TSL2561](review/motion-air-quality-nfc.md)
- [Serial, SPI, one-wire, GPIO, displays, DS3231, PCA9685, EEPROM, direct GPIO and mock bus](review/other-buses-output-storage.md)

## SEN6x specification check and implemented corrections

Primary specification: [Sensirion SEN6x datasheet, version 0.92, December 2025](https://sensirion.com/media/documents/FAFC548D/693FBB15/PS_DS_SEN6x.pdf). Firmware encoding was also checked against Sensirion's [official embedded driver](https://github.com/Sensirion/embedded-i2c-sen66).

| Operation | Command | Wire response bytes | Required processing wait / behavior |
| --- | --- | --- | --- |
| Start measurement | `0x0021` | 0 | 50 ms; readiness checked before reading |
| Stop measurement | `0x0104` | 0 | 1400 ms |
| Device reset | `0xD304` | 0 | Idle only; 1200 ms |
| Data ready | `0x0202` | 3 | 20 ms; decoded second byte is readiness |
| Read status | `0xD206` | 6 | 20 ms; two words form unsigned 32-bit flags |
| Read and clear status | `0xD210` | 6 | 20 ms; consume and validate response |
| Firmware version | `0xD100` | 3 | 20 ms; binary major/minor, not ASCII |
| Product / serial | `0xD014` / `0xD033` | 48 | 20 ms; NUL-terminated ASCII after CRC removal |
| Fan cleaning | `0x5607` | 0 | Idle only; wait at least 10 seconds before restarting measurement |

Transport now validates complete writes/reads and every CRC byte, including received `0xFF`. CRC is polynomial `0x31`, initial `0xFF`, no final XOR (known `0xBEEF` vector gives `0x92`). Command IDs are sent as two bytes without an added data CRC; payload words each receive CRC. Transactions are serialized on the underlying addressable device. Interrupts terminate the command without reading prematurely.

| Model | Measurement command | Wire / decoded bytes | Measurement fields |
| --- | --- | --- | --- |
| SEN63C | `0x0471` | 21 / 14 | Four PM values, RH, temperature, CO2 |
| SEN65 | `0x0446` | 24 / 16 | Four PM values, RH, temperature, VOC, NOx; no CO2 |
| SEN66 | `0x0300` | 27 / 18 | SEN65 fields plus CO2 |
| SEN68 | `0x0467` | 27 / 18 | SEN65 fields plus formaldehyde |

PM uses unsigned words /10; RH signed /100; temperature signed /200; VOC/NOx signed /10. CO2 is unscaled ppm (signed on SEN63C, unsigned on SEN66); SEN68 formaldehyde is unsigned /10 ppb. Unknown values (`0x7FFF` signed, `0xFFFF` unsigned) remain unavailable instead of becoming zero or very large numeric readings. A reading supplier reports unavailability through the existing error-result path; JSON consumers should expect the existing error representation during sensor warm-up.

Capabilities now match each model. Reserved status bit 15 is no longer exported as “Compensation Active”. The grouped status serializer and schema now preserve nested flags correctly.

The old automatic fan-cleaning interval methods were not SEN6x commands: `0xD210` actually reads and clears device status, and `0xD208` is undefined. Existing public methods remain deprecated and explicitly throw `UnsupportedOperationException` without bus I/O. Manual fan cleaning stops measurement, waits ten seconds and resumes when appropriate. Reset/stop/start failures are propagated; measurement caches are invalidated on state transitions. SEN63C restart respects the 24-second minimum after a short measurement run.

## Other fixes in this change

- RegisterMap emits the first register snapshot instead of reading a second time.
- I2C controller close delegates to its device, scheduler close delegates under its operation locks, and manager close removes cached handles so a remount creates a fresh device. Repeated/stale closes do not remove a newer mount.
- Prior cleanup preserves display trailing-byte clearing and decimal consumption, and SEN6x delays cannot be shortened by monitor notification.

## Validation and limits

Mocked `AddressableDevice` and Pi4J I2C/provider fixtures exercise protocol bytes, CRC failures, truncated transfers, interrupts, model scaling/sentinels, capability lists, failed lifecycle commands, grouped values/schema, register read counts and close/remount behavior. The latest full test run passes **57 tests** on Java 17 with Pi4J 2.8.0.

The local JaCoCo snapshot is **18.9% overall line coverage** and **69.5% for SEN6x packages**. These are local measurements, not a refreshed Sonar server result, and do not meet the eventual 80% target. Most of the remaining coverage can be built with scripted bus fixtures; physical hardware is still needed to validate electrical behavior, provider timing and device-specific concurrency assumptions. No physical SEN6x or other sensor was exercised in this review.
