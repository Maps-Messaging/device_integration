Read-only environmental/current sweep. All Java files in seven assigned families read; no changes. Paths below relative to sensors/. Severity P1 means incorrect/unavailable normal operation; P2 narrower mode/error/boundary.

CONFIRMED findings
1. P1 bmp280/BMP280Sensor.java:40-43,133-174 and BMP280Controller.java:34: Entire implementation is an MS5611-style PROM/conversion driver, not BMP280. Sends reset 0x1E, reads PROM0xA0, D1/D2 OSR commands and compensates six coefficients. BMP280 instead uses reset0xE0=0xB6, calibration0x88+, ctrl_meas0xF4 and ADC0xF7/0xFA. Controller declares address0x78; actual BMP280 is0x76/0x77. Real BMP280 cannot return valid readings. Its otherwise correctly-addressed unused PressureRegister/TemperatureRegister also return full24 bits without dropping unused low4 bits (lines35-36).
2. P1 ina219/Ina219Sensor.java:209-215: Enum values BusVoltageRange/GainMask/ShuntADCResolution already contain positioned masks, but configure shifts them again. Default low16 config becomes0x00C6 (range16V,gain40mV,bus9bit,shunt invalid code8,bus-only mode), not intended configuration. ADCResolution itself also contains incorrect encodings:9bit should0x0000,10bit0x0080,11bit0x0100,12bit0x0180. Fix both masks and combination together.
3. P1 ina219/Ina219Sensor.java:64: Default BVOLT_CONTINUOUS measures only bus voltage, although readings expose shunt,current,power. Reset shunt/current/power stay zero; use both conversions.
4. P1 ina219/Ina219Sensor.java:232-235: Bus voltage omits4mV/bit scale.12V raw0x5DC0 gives3000mV. Supply voltage, calculated power, load resistance and delta inherit error.
5. P1 ina219/Ina219Sensor.java:238-248,262-265: Current and shunt signed16 registers decoded unsigned. Negative raw0xFFFF becomes65535 ->6553.5mA or655.35mV instead of-0.1mA/-0.01mV.
6. P1 ina219/Ina219Controller.java:103-134: Update setters only alter Java fields; update calls setCalibration but never configure. Reports success while range/gain/mode/ADC changes never reach device.
7. P1 bme688/register/LargeValueRegister.java:35 and ValueRegister.java:35: First Java byte lacks unsigned mask. ADCs>=0x80000 become negative and RH>=0x8000 becomes negative, corrupting temperature/pressure/humidity compensation.
8. P1 bme688/measurement/CalibrationData.java:65-74 plus TemperatureCalibrationData.java:37-38,HumidityCalibrationData.java:37-41,PressureCalibrationData.java:40-47,GasCalibrationData.java:35-40: all helpers return unsigned values, while T2/P2/P4/P5/P8/P9/GH2 are signed16 and T3/P3/P6/P7/H3/H4/H5/H7/GH1/GH3/res_heat_val/range_sw_err signed8 or signed nibble. Negative factory coefficients become large positives. Additionally P8 andP9 read only one byte at offsets18,20 instead of signed16 pairs. Affects every compensated reading/heater temperature.
9. P1 bme688/measurement/PressureMeasurement.java:64-78: port differs from Bosch compensation: overwrites var1 before P2 linear term (must use original temperature delta); shifts P9 before multiplication instead of shifting product; shifts unsigned8 P10 right17 before multiplying (always zero); combines quadratic and cubic in var3 and discards Bosch's separate final var1 quadratic term. Normal pressure compensation materially incorrect independent of calibration decode.
10. P1 bme688/register/ControlGas1Register.java:41-51 and ControlGas0Register.java:40-41: Calls inherited setControlRegister with target bit mask instead of preservation mask. Inherited helper computes(old & mask)|value. setNbConv clears run_gas, then setRunGas clears nb_conv; clearing gas/heater preserves target bit and erases other controls. initialize profile0 hides part of issue, nonzero profiles and disable are broken. ctrl_gas_0 bit3 is heat_off (1 disables), but public isHeatOn/setHeatOn interpret it as1=on.
11. P1 bme688/BME688Sensor.java:180-182: powerOff changes cached control measurement value but never updateRegister; hardware remains active, and checkState/startForceMode continues scheduling readings regardless.
12. P1 am2315/AM2315Sensor.java:163: adds10 to low temperature byte before bitwise OR;0x00FA(25C) yields26C;0x01FA(50.6C) yields26C because low-byte overflow overlaps high byte. No sign-magnitude handling. AM2320Sensor.java:123 likewise negative temperatures decode as3276.8C+magnitude instead of negative magnitude.
13. P2 am2315/AM2315Sensor.java:189-198: Reads32 bytes despite response count+4, ignores advertised byte count and CRC entirely; derives data length from bus read count. CRC-corrupted packets accepted, short response can return undersized array and getters throw runtime indexing errors outside retry loop.
14. P2 am2320/AM2320Sensor.java:117-120: Bad header/CRC returns success with temperature AND humidity=-273.0 instead of propagating invalid I/O/data. Constructor succeeds on invalid packet; callers cannot distinguish sensor failure through IOException path.
15. P2 am2315/AM2315Sensor.java:90-93 and am2320/AM2320Sensor.java:78-80: isConnected always false even after successful initial read; corresponding controller detect always false. Across all reviewed controllers service-loaded sensor=null means detect never probes passed device (the review treats broader discovery issue).
16. P2 bh1750/Bh1750Sensor.java:137-152 + register/ReadingModeRegister.java:45-56: ONE_TIME supported config powers device down after one conversion, but scanForChange never issues subsequent measurement command. All later polls reuse last physical result forever. Constructor/start mode does not wait initial120-180ms conversion before first reading. read exception advances lastRead before successful I/O, so next getter can silently return old value until timeout.
17. P2 gravity/GasSensor.java:89-99: Temperature-adjusted reading wires this::getConcentration, not existing getTemperatureAdjustedConcentration; published adjusted value always duplicates raw.
18. P2 gravity/module/SensorType.java:116-123 (verify actual line by rg): Unknown valid type returns null, not UNKNOWN; GasSensor.java:69,78-85 immediately dereferences and constructor NPEs. Adjusted helper also calls null module for UNKNOWN.
19. P2 gravity/registers/SensorReadingRegister.java:52-58: CRC mismatch silently returns previous concentration/temperature (zero initially) and advances cache deadline; sensor failure indistinguishable from real clean gas reading. ConcentrationRegister.java:36-39 ignores request boolean entirely and decodes corrupted response.
20. P2 gravity/registers/ConcentrationRegister.java:37 and SensorReadingRegister.java:53: unsigned concentration word decoded with sign-extending high byte. Raw>=32768 becomes negative (not normally reachable with current listed ranges; externally extended sensors/protocol-invalid high values affected).
21. P2 bme688/values/HeaterStep.java:32: STEP_8 writes9, duplicate STEP_9, selects wrong profile.
22. P2 bme688/register/ControlMeasurementRegister.java:49: idx==Oversampling.values().length(6) bypasses > guard and throws; register osrs6/7 are reserved configurations, should handled consistently. Other enum lookups also lack guards for reserved mode/profile codes.
23. P2 bme688/SensorReadings.java:70-75: Reads data whenever idle without requiring hasNewData; no valid/stable gas check beyond gas_valid; separate field reads can mix samples if parallel mode is later implemented. Present forced-only mode lowers mixed-sample exposure. bme688/measurement/TemperatureMeasurement.java:45-47 usesint intermediates while Bosch usesint64; large ADC-calibration differences overflow and corrupt t_fine.

SPEC-DEPENDENT / VERIFY ON HARDWARE
- Gravity checksum loop sumsindices1..6, excludingbyte7. Current official DFRobot driver also sums1..6 (FucCheckSum(...,8),six additions), whereas development-manual prose says1..7. Do NOT report as confirmed bug without firmware trace. Strong corruption test must check which bytes firmware actually covers.
- AM2315 lacks explicit wake pulse before command; retries may wake through first failed command, but actual wake timing/transaction shape needs hardware verification.1s cache on bothAM sensors may violate2s minimum measurement cadence; verify exact revisions.
- BME688 gas run flag only bit5, likely correct high-variant (value2 shifted4), but variant is never validated and low variant would requirebit4 and low-gas compensation/addresses. Avoid claiming bit5 incorrect for BME688 high variant.

PRIMARY SOURCES (root can open these for final citations)
Bosch official BME68x API: https://github.com/boschsensortec/BME68x_SensorAPI/blob/master/bme68x.c (retrieval ; pressure comparisons ,; int64 temp ; high-variant gas ).
Bosch BMP280 datasheet: https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bmp280-ds001.pdf (; addresses ).
TI INA219 datasheet: https://www.ti.com/lit/ds/symlink/ina219.pdf (;4mV scaling ).
Aosong AM2320 product manual mirrored at https://cdn-shop.adafruit.com/product-files/3721/AM2320.pdf (searchturn8search24 confirms sign magnitude).
DFRobot official driver: https://github.com/DFRobot/DFRobot_MultiGasSensor/blob/main/DFRobot_MultiGasSensor.cpp (;checksumturn14view1).

TEST PRIORITIES
- SPI/I2C-independent byte-vector/golden tests comparing BME688 compensation outputs against Bosch C API, coefficients with all signed boundaries and P8/P9 high bytes, unsigned ADC0x7ffff/0x80000/0xfffff andRH0x7fff/0x8000.
- INA219 bus raw0x5dc0->12000mV, signed0xffff and0x8000 current/shunt, captured initialization word and everyconfig update write; ensure both converters mode7 and register masks do not mutate neighboring bits.
- BMP280 manufacturer example calibration/ADC vector and captured register sequence; existing implementation should fail these immediately.
- AM positive0x00fa,0x01fa,negative0x8064; wrongCRC, shortread and count mismatch produce error; discovery successful path.
- BH1750 fake state machine enforcing one-shot rearm and initial conversion deadline, read failure followed by immediate retry.
- Gravity rawvsadjusted with knownCO temperature compensation, unknown type, checksumfail before/after good read, concentration0x8000, exact request and reply capture.

INVENTORY (100 files)
am2315/AM2315Controller.java
am2315/AM2315Sensor.java
am2320/AM2320Controller.java
am2320/AM2320Sensor.java
bh1750/Bh1750Controller.java
bh1750/Bh1750Sensor.java
bh1750/data/ReadingModeData.java
bh1750/register/ReadingModeRegister.java
bh1750/values/ResolutionMode.java
bh1750/values/SensorReadingMode.java
bme688/BME688Controller.java
bme688/BME688Sensor.java
bme688/Config.java
bme688/SensorReadings.java
bme688/data/ChipId.java
bme688/data/ConfigData.java
bme688/data/ControlGas1.java
bme688/data/ControlHumidity.java
bme688/data/ControlMeasurement.java
bme688/data/GasWait.java
bme688/data/HeatResistance.java
bme688/data/HeaterCurrent.java
bme688/data/HeaterOn.java
bme688/data/VariantId.java
bme688/measurement/CalibrationData.java
bme688/measurement/GasCalibrationData.java
bme688/measurement/GasMeasurement.java
bme688/measurement/HumidityCalibrationData.java
bme688/measurement/HumidityMeasurement.java
bme688/measurement/Measurement.java
bme688/measurement/PressureCalibrationData.java
bme688/measurement/PressureMeasurement.java
bme688/measurement/TemperatureCalibrationData.java
bme688/measurement/TemperatureMeasurement.java
bme688/register/Calibration2ByteRegister.java
bme688/register/CalibrationData1Register.java
bme688/register/CalibrationData2Register.java
bme688/register/CalibrationData3Register.java
bme688/register/CalibrationDataRegister.java
bme688/register/ChipIdRegister.java
bme688/register/ConfigRegister.java
bme688/register/ControlGas0Register.java
bme688/register/ControlGas1Register.java
bme688/register/ControlHumidityRegister.java
bme688/register/ControlMeasurementRegister.java
bme688/register/GasReadingRegister.java
bme688/register/GasWaitRegister.java
bme688/register/HeaterCurrentRegister.java
bme688/register/HeaterResistanceRegister.java
bme688/register/LargeValueRegister.java
bme688/register/MeasurementStatusRegister.java
bme688/register/ResetRegister.java
bme688/register/ValueRegister.java
bme688/register/VariantIdRegister.java
bme688/values/FilterSize.java
bme688/values/GasMeasurement.java
bme688/values/HeaterControl.java
bme688/values/HeaterStep.java
bme688/values/Oversampling.java
bme688/values/PowerMode.java
bmp280/BMP280Controller.java
bmp280/BMP280Sensor.java
bmp280/registers/PressureRegister.java
bmp280/registers/TemperatureRegister.java
bmp280/values/OversamplingRate.java
gravity/GasSensor.java
gravity/GasSensorController.java
gravity/config/AcquireMode.java
gravity/config/AlarmType.java
gravity/config/Command.java
gravity/module/COModule.java
gravity/module/Cl2Module.java
gravity/module/H2Module.java
gravity/module/H2SModule.java
gravity/module/HCLModule.java
gravity/module/HFModule.java
gravity/module/NH3Module.java
gravity/module/NO2Module.java
gravity/module/O2Module.java
gravity/module/O3Module.java
gravity/module/PH3Module.java
gravity/module/SO2Module.java
gravity/module/SensorModule.java
gravity/module/SensorType.java
gravity/registers/AcquireModeRegister.java
gravity/registers/ConcentrationRegister.java
gravity/registers/CrcValidatingRegister.java
gravity/registers/I2CRegister.java
gravity/registers/SensorReadingRegister.java
gravity/registers/TemperatureRegister.java
gravity/registers/ThresholdAlarmRegister.java
gravity/registers/VoltageRegister.java
ina219/Ina219Controller.java
ina219/Ina219Sensor.java
ina219/registers/ADCResolution.java
ina219/registers/BusVoltageRange.java
ina219/registers/GainMask.java
ina219/registers/OperatingMode.java
ina219/registers/Registers.java
ina219/registers/ShuntADCResolution.java
