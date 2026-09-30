# Device Library mocked-device coverage expansion

Tracking: MSG-350 / PR202. Pi4J remains **2.8.0**. This expansion adds tests and test utilities only; production drivers, dependencies and coverage exclusions are unchanged.

## Test scope

The tests run real drivers, register classes and controllers against fake `AddressableDevice`, SPI and serial transports. One-wire tests use kernel-shaped temporary files. Fixtures carry full response layouts and independently specified expected values or outbound bytes.

| Area | Covered behavior |
| --- | --- |
| SEN63C / SEN65 / SEN66 / SEN68 | Controller JSON, all model fields, nested flags, unknown measurements, readiness/cache invalidation, binary version, status clear and manual cleaning transitions |
| SHT31 | All fifteen periodic command combinations, heater/status/break/ART commands, endpoint conversions, second-word CRC rejection, shared samples and power transition bytes |
| PMSA003I / BH1750 | Full particulate frame, all mass/count fields, derived AQI, and big-endian lux scaling |
| LPS25 / LPS35 | Pressure/temperature physical scales, refresh, supported signed path, identity and transport failure |
| BNO055 | Signed Euler/vector scales, quaternion Q14, chip rejection and failed vector reads |
| AS3935 / MSA311 / TSL2561 / Gravity | Correct packed configuration paths, distance sentinel, axis/rate independence, timing bit preservation, gas packet framing/conversion/checksum rejection and cache sharing |
| MCP23017 / PCA9685 / DS3231 / HT16K33 | Port decoding, pull-ups, interrupt flags, valid output payloads, PWM channel/full flags/reset, correct RTC BCD fields and positive temperature, display control bytes and RAM clearing |
| MCP300x / SEN0640 / DS18B20 | Ten-bit SPI framing, complete Modbus requests/replies, CRC/echo/range/transport/deadline errors, and file-backed millidegree conversion |
| Derived readings | Physical dew point/moisture/thermal values, Beaufort bands, humidity/light categories, source errors, signed pressure trends, rain reset handling and bounded sample windows |

## Assertion quality

A mutation check temporarily changed SEN66 PM1 scaling from `/10` to `/1`. The new end-to-end controller fixture failed with **expected 10, actual 100**. The production source was restored before the final clean run. Literal Modbus CRCs, Gravity checksums, pressure and motion scales, SHT endpoint values and wind-chill reference were independently reviewed.

The clean full suite passes **174 tests**, including **116 additional tests**. Overall line coverage rises from **18.9% to 30.1%**. These are local JaCoCo results on Java 17, not refreshed Sonar server metrics.

| Scope | Line coverage | Covered / total lines |
| --- | ---: | ---: |
| Overall | 30.1% | 3174 / 10546 |
| SEN6x | 87.7% | 512 / 584 |
| SHT31 | 77.3% | 116 / 150 |

## Remaining work

[Coverage blockers](review/coverage-blockers.md) identify known driver defects that should be corrected before asserting those physical behaviors. Broader coverage remains below the eventual 80% target. Additional fixtures should follow those fixes rather than treating incorrect decoding or silently failed commands as expected behavior. No hardware validation is claimed.
