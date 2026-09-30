# Correctness gaps encountered while expanding mocked coverage

These paths need driver corrections before tests can assert the expected physical result. Passing tests cover working neighboring paths; no failing, skipped or disabled tests were added to hide these findings. Production code is unchanged in the coverage expansion.

## Sensor paths

Excluded defect branches rather than blessing incorrect output:
- BNO055 controller compares ID to address; orientation signed Euler and gyro default scaling wrong; calibration/status refresh cache wrong (review findings 8-10,16-17).
- LPS25 negative temperature and LPS35 negative pressure/temperature have off-by-one sign correction; LPS35 FIFO32 loses count; LPS35 controller address wrong.
- AS3935 energy byte order and TRCO/SRCO oscillator selection wrong; AFE cached power-down gets lost across sequential config.
- MSA311 suspend encoding, freefall duration, fractional threshold config, tap/latch and stale freefall/active setters blocked. AxisRegister explicitly shifts two bits for MSA301; avoided asserting MSA311 physical acceleration on this disputed path.
- TSL2561 integration/gain lux conversion and interrupt mode shift wrong. High/low thresholds inherit MultiByteRegister.write big-endian while TSL thresholds are little endian: correct threshold write tests would fail.
- Gravity concentration CRC ignored, bad combined CRC silently caches stale sample, signed high concentration words, adjusted reading wiring wrong. Checksum byte7 exclusion is unresolved per review; fixtures follow official-driver documented convention.


## Other bus paths

Bus device test coverage blockers (production unchanged):
- MCP23017 setOutput/setLow/disableInterrupt/disablePullUp cannot be covered with correct expectations: BitsetRegister.clear uses AND bit rather than AND complement, preserving the selected bit and clearing other pins. setOnHigh/Low does not apply INTCON level semantics. GPIO reads overwrite cached output latch image. Covered set/read/flags without blessing these defects.
- DS3231 hour mode and bit5 decode, alarm day/date polarity and masks, oscillator EOSC polarity, negative temperatures, years>=2080, and burst snapshot correctness remain excluded due to documented defects. Covered correct BCD date/minute/seconds and positive quarter-degree temperature.
- MCP320x 12-bit framing is wrong; only correct MCP300x 10-bit framing and invalid upper-channel behavior are tested.
- HT16K33 numeric third/fourth position mapping and colon/Panel bounds incorrect. Tests cover first two digits, custom alpha font, raw output and real control command bytes, without asserting defective slots.
- SEN0640/42/57 fragmented responses overwrite previously received bytes. SEN0640 tests cover complete-frame protocol success plus CRC/range/echo/transport/deadline failures, not fragmented-response success.
- DS18B20 CRC NO and missing-file handling silently keep readings; negative/stable/zero statistics defective. File-backed positive changing samples are tested.
- PCA9685 prescale rounding and wake/restart timing not asserted due to documented defects/unclear calibration; valid pulse payload, full flags, reset tested.

## Newly reproduced derived-reading defect

`SensorReadingAugmentor.addComputedReadings` lowercases every lookup key, but `WIND_DIRECTION_ANGLE` remains `windDirectionAngle`. `scanForWindDerived` therefore never finds direction input and never adds `windDirectionText`. Eight fixtures (including 0, 90, 360, -90 and 450 degrees) reproduced the missing derived reading. The failing fixture is not part of the passing test suite; the correction belongs in the subsequent logic-fix pass. No behavior was changed here.

## Fixture limits

In-memory register images model address selection, byte order, reads/writes and selected failures. They do not emulate oscillator startup, interrupt electrical behavior, all banked-register side effects, or EEPROM page rollover. The PCA9685 tests assert pulse payloads rather than treating the fake's retained RESTART bit as physical device state. Existing EEPROM mutation/page-size defects and PN532/SCD41 protocol defects remain in the complete review report.
