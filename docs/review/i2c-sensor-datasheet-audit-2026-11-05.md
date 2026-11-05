# I²C sensor datasheet audit — 2026-11-05 (initial verified pass)

Branch: `review/i2c-sensor-datasheet-audit-20261105` from `development`.

## Scope and limits
This is a **read-only source review**, not a hardware validation or an exhaustive sign-off. Only sensor production code and selected dependent registers/controllers were compared against manufacturer specifications. Storage, displays, GPIO expanders, RTC and other buses are excluded. No production code or tests were changed. The earlier 2026-09-30 report in `docs/review/` contains many historical findings; the statuses below reflect files inspected on current `development`, not an assumption those older issues remain open.

## Confirmed outstanding defects

### P1 — SCD41 forced recalibration performs factory reset
- `scd41/registers/ForcedRecalRegister.java`: constructor creates `FactoryResetRequest`, and `forceRecallibration()` sends it.
- `scd41/functions/FactoryResetRequest.java`: that request emits command `0x3632`, *factory reset*, which erases stored settings and calibration history.
- Sensirion specifies forced recalibration command `0x362F` with a reference CO2 concentration word plus CRC and a three-byte response, after stopping measurement. The existing API takes no target value.
- Impact: calling the public forced-recalibration function can reset the device instead of calibrating it; behavior and API need an explicit decision, not a silent implementation change.
- Reference: https://sensirion.com/media/documents/48C4B7FB/67FE0194/CD_DS_SCD4x_Datasheet_D1.pdf

### P1 — BME688 compensation and calibration bytes still decoded incorrectly
- `bme688/register/LargeValueRegister.java`: `buffer[0] << 12` sign-extends an unsigned I²C byte. 20-bit raw ADC values with first byte >= 0x80 can decode negative.
- `bme688/register/ValueRegister.java`: `buffer[0] << 8` similarly sign-extends unsigned values.
- `bme688/measurement/CalibrationData.java`: `getShort` never sign-extends manufacturer-signed 16-bit coefficients; `getByte` only returns unsigned, though some BME68x coefficients are signed 8-bit.
- `bme688/measurement/PressureCalibrationData.java`: P8/P9 read only one byte each (at offsets 18 and 20), although both are two-byte signed coefficients.
- `bme688/measurement/PressureMeasurement.java`: pressure formula differs materially from Bosch's reference: original temperature-delta term is overwritten before P2 is applied, `parP9 >> 12` and `parP10 >> 17` shift tiny coefficients to zero *before multiplication*, and high-order compensation terms are therefore lost.
- `bme688/register/ControlGas1Register.java` also passes target bit masks into `setControlRegister`; verify inherited register mask contract with a byte-level test before changing it.
- Impact: invalid environmental readings, potentially including negative ADCs and inaccurate pressure/gas calibration. Fix in tested slices using Bosch reference vectors.
- Reference: https://github.com/boschsensortec/BME68x_SensorAPI/blob/master/bme68x.c and https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bme680-ds001.pdf

### P2 — AM2320 negative temperature not decoded
- `am2320/AM2320Sensor.java` combines both temperature bytes as an unsigned 16-bit magnitude and divides by 10.
- Datasheet specifies bit 15 = negative sign and remaining 15 bits = magnitude. Example raw `0x8064` represents **-10.0 °C**, not **3286.8 °C**.
- CRC/response-length handling is already present; this finding concerns signed temperature only.
- Reference: https://www.makerhero.com/img/files/download/AM2320-Datasheet.pdf

### P2 — Gravity gas reading names promise temperature adjustment but return the same source
- `gravity/GasSensor.java`: both `concentration` and `concentrationTempAdj` are wired to `this::getConcentration`; `getTemperatureAdjustedConcentration` is unused.
- DFRobot SEN0465 supports sensor-side temperature compensation configured by `setTempCompensation`. Distinguish the firmware-compensated register from host-side recalculation before fixing: the *duplicate reported fields* are certain; the appropriate new semantics are not.
- Reference: https://wiki.dfrobot.com/sen0465/docs/21904 and https://wiki.dfrobot.com/sen0465/docs/21906

## Confirmed historical issues already corrected (do not re-report)
- `bmp280/BMP280Sensor.java`: uses Bosch 0xD0 chip ID 0x58, 0xE0 reset, 0x88 calibration and 0xF7 ADC registers; earlier MS5611-protocol complaint is obsolete. A full golden compensation vector was **not** executed here.
- `ina219/Ina219Sensor.java`: configuration is assembled without the earlier double shifts; bus voltage uses 4 mV per bit and current/shunt use signed short values. Dedicated fixture needed to certify every configurable mode.
- `am2315/AM2315Sensor.java`: signed temperature decoding and packet CRC/length checks are present.
- `sht31/commands/Command.java`: validates CRC per three-byte word; `ReadDataCommand.java` applies Sensirion's temperature and humidity conversion formulas.

## Follow-up verification needed, **not** yet confirmed as defects
- Model/address discrimination for all sensor controllers, especially shared-address devices; an I²C ACK alone is not proof of identity.
- Independent golden vectors for BME688 temperature/pressure/humidity/gas, BMP280 and INA219, including signed boundary bytes and conversion timing.
- SEN6x, SCD41 non-recalibration commands, AM2315/AM2320 cadence, AS3935, BNO055, BH1750, LPS25/35, MSA311, PMSA003I, PN532, TSL2561, SEN0539 and H3LIS200DL: review old findings against current source and current datasheets before determining their status.
- DFRobot SEN0465/SEN0466 firmware compensation mode and response interpretation against actual sensor output.
- Hardware validation across I²C providers and sensor variants.

## Verification performed
Reviewed `development` source via GitHub connector and compared listed behavior with Bosch, Sensirion, Aosong/AM2320 and DFRobot documentation. No compilation, automated tests, hardware access, physical bus capture or runtime measurements were performed. No sensor implementations were changed.
