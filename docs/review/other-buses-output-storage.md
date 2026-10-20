# Other buses/output/RTC sweep (read-only)
All paths below relative to device_integration/src/main/java/io/mapsmessaging/devices. Confirmed means established by code inspection/spec, not hardware tested. No source edits, no Jira changes. Pi4J remains 2.8.0. Current PR RegisterMap/cache fixes and already-fixed Quad encoding clear/decimal-consumption defects excluded.

## Confirmed findings
1. **P1 serial fragmented responses overwrite earlier bytes.** serial/devices/sensors/sen0640/Sen0640Sensor.java:264, sen0642/Sen0642Sensor.java:299, sen0657/Sen0657Sensor.java:565. Each loop advances bytesRead but calls readBytes(buffer,remaining), without destination offset or temporary append. Actual example adapter delegates straight to jSerialComm (examples/.../weather/Serial.java:59). A valid 7-byte response arriving 3+4 bytes becomes final four bytes + stale tail; CRC/header fails. Tests: CRC-valid response chunk sizes 1, 2+5, 3+4 plus zeros between chunks.
2. **P1 MCP3204/3208 conversion framing misaligned.** spi/devices/mcp3y0x/Mcp3y0xDevice.java:91-108. Always emits MCP300x [0x01,SGL|channel<<4,0] while 12-bit decode assumes [0x04|SGL<<1|D2,D1D0<<6,0] framing. Start at eighth clock rather than sixth leaves conversion data two clocks late; the transfer ends before the last two result bits, while the current decode assumes the full 12-bit alignment. Channel select bits remain correctly ordered but reported conversion is truncated/misaligned. Manufacturer 21298E section6.1 Fig6.1 specifically five leading zeros for top-four/low-eight decode. Tests: outbound frames channel0/3/4/7 both resolutions, raw 0/1/2048/4095.
3. **P1 DS3231 hours mode/BCD encoding incorrect.** i2c/devices/rtc/ds3231/register/HourRegister.java:52-57,70-93 (and Ds3231Rtc.java:188/285). Mode bit6 high selects 12h, contrary to get/setClock24Mode. Existing 24h hardware 0x23 (23:00) decodes 3, since bit5 ignored unless bit6 set. setHours(13) leaves PM set on later setHours(1), so reads25. 12AM/PM also not normalized. Tests entire0..23,13→1, noon/midnight.
4. **P1 DS3231 alarm date/day polarity reversed, time-only invalid.** register/AlarmDayRegister.java:52-75 and Ds3231Rtc.java:202-210,221-229. setDate(true) sets DY/DT=1 (weekday), yet writes BCD day-of-month. May never fire or fire wrong day. Time-only setAlarm sets unmasked day/date0 instead of selecting hours/minutes/seconds mask; 0 invalid. AlarmDayRegister getDay expression line64 lacks parentheses around units: raw0x25 gives9 rather than25. Alarm1Settings.java:43 and Alarm2Settings.java:42 force day=false for mask0, making weekday mode unreportable. Tests all alarm masks plus date25 and daily time-only alarm.
5. **P1 DS3231 oscillatorEnabled inversion stops battery timekeeping.** register/ControlRegister.java:45-50. true sets EOSC, explicitly stopping oscillator on VBAT; false clears EOSC/enables. Manufacturer DS3231 Control register description verifies inversion. Test true→bit7clear, false→set.
6. **P2 DS3231 negative temperatures lose sign.** register/TemperatureRegister.java:37. buffer[0]&0x7F strips signed integer bit, so raw FF C0 (-0.25C) becomes127.75C. Test -40,-0.25,0,25.25.
7. **P2 DS3231 year decode drops 80s/90s; century unused.** register/BcdRegister.java:30,48; YearRegister.java:38-42. TENS=0x70 drops year bit7 even includeTop=true;2099 reads2019. Year2000 offset always ignores century and setter accepts out-of-range values. Test2079,2080,2099 and2100 handling.
8. **P2 DS3231 date/time reads are torn across rollovers.** Ds3231Rtc.java:173-174,233-237,261-265. Separate register transactions each restart DS3231 shadow snapshot. At23:59:59→00:00:00 getDateTime may combine previous date with new time; at xx:59→next hour individual hour/minute/sec inconsistent. Tests fake rollover between register reads; prefer contiguous7-byte burst snapshot.
9. **P1 LCD commands advertised by schema are treated as literal display text.** i2c/devices/output/lcd/lcd1602/data/Lcd1602Command.java:28-32; Lcd1602Controller.java:106-129. DTO requires Jackson @class type id, schema supplies only action/address/data. Ordinary {"action":"CLEAR"} throws missing-type-id, catch falls through clear+writeBlock raw JSON then reports success. Test public-schema-shaped WRITE/READ/CLEAR with no@class.
10. **P2 LCD CLEAR leaves stale software buffer/cursor.** Lcd1602Device.java:141-143,221-227,257-267. clearDisplay sends only command: readBlock still returns old text; subsequent setDisplay uses pre-clear cursorPos though hardware cursor reset0, eventually array overflow. Test write,clear,read and write32,clear,setDisplay1.
11. **P1 LCD clock monopolizes every I2C bus.** output/lcd/lcd1602/task/Clock.java:50-68. Holds static global I2C lock through infinite display loop + two500ms waits/second. Blocks all scheduled reads/config writes until stopped; stop checked only outside60-iteration loop, delaying release up to60s. Date captured once never rolls at midnight. Test another scheduled device while task active and stop latency.
12. **P2 HT16K33 Panel/test task crashes; colon never enabled.** output/led/ht16k33/Panel.java:53-66 and tasks/TestTask.java:93-106. enableColon(true) immediately overwritesFF with0. circle iterates x0..39 and calls setDisplay(3-x,...);x4 negative index→AIOOBE uncaught by IOException catch. setAllDisplay with hasColon=false iterates bytecount, actual==length slips >guard and crashes. Tests colonflag, negative/boundary positions, task completingcycle.
13. **P2 Quad7Segment encodes third digit into colon slot.** output/led/ht16k33/Quad7Segment.java:52-61. For "1234", RAM bytes0,2,4,6 get digits; board slots0,2,6,8 are four numeric digits and4colon. Third character disappears and last digit empty. Five-char HH:mm works by intentional middle placeholder, but controller advertises up-to4 characters. Tests exact RAM mapping1234 and12:34 (preserve clear/decimal fix).
14. **P2 Display font indexing accepts unsupported characters then crashes.** QuadAlphaNumeric.java:42;Quad7Segment.java:45-49;St7735Device.java:89. Alpha font128entries but arbitrary Java char; numeric Character.isDigit accepts Arabic/fullwidth digits then c-'0' huge index; ST fonts ASCII32..126 and newline/Unicode negative/too-large. Controller display JSON can trigger unchecked exception. Tests nonASCII, newline and documented fallback/error policy.
15. **P2 GPIO interrupt deallocation leaves subscription/interrupt enabled.** gpio/InterruptPin.java:53-55. close clears local listeners but never pin.removeListener(this). For expander pin hardware GPINTEN remains on;Pi4J still retains wrapper. Reuse addListener attaches same wrapper again. GpioExtensionPinManagement interruptInput ctor:47 captures listener without retaining/removing onclose. Tests close/remove/reallocate underlying registration counts.
16. **P1 MCP23017 configured interrupt polarity is never applied.** i2c/devices/gpio/mcp23017/Mcp23017Device.java:91,151-157. reset clears INTCON;setOnHigh/Low only modifies DEFVAL but INTCONbits remain0, so hardware compares previous pin state and interrupts on both changes irrespective selected level. If implementing level semantics INTCONmust be1 and DEFVAL opposite desired activelevel; current setOnHigh writes1 (would interrupt low). Tests fake/hardware edgehigh/low using config on=up/down. Existing BitsetRegister.clear defect independently known to core reviewer.
17. **P2 MCP23017 output read buffer reused as output latch.** Mcp23017Device.java:141-147,161-162. setHigh/Low mutate gpio cached bytes; isSet reloads same buffer from physicalGPIO. Any input/output pin read overwrites cached latch image; subsequent output write writes all16bits including physical input/loaded output readings into OLAT. Example configured output latchhigh forcedlow atpin, isSet refresh then another pin setHigh silently clears first latch. Use OLAT for output state. Tests physicalGPIO differing from OLAT then writeotherpin.
18. **P2 DS18B20 ignores invalid sample status and silently keeps stale readings.** onewire/OneWireDevice.java:44-57;onewire/devices/ds18b20/DS18B20Device.java:113-120. Reads t= line without firstline crcYES check;kernel explicitly prints YES/NO. Filegone/readfailure yields emptyprocess then oldtemperature success. Test crcNO/truncated/missing file; rejector expose failure.
19. **P2 DS18B20 statistics wrong for negative and stable samples.** DS18B20Device.java:48,127-132. Float.MIN_VALUE is smallestpositive, so allnegative readings never update max. Firstreading0 never updates min/max; unchangedsample preserves lastdelta forever, and any legitimate transition from0 has delta forced0 via myDif==fVal. Tests -10→-5,-5→-5,first0,0→5.
20. **P2 serial lifecycle cleanup omitted.** SerialBusManager.java:62-64 only removesactive entry. SEN0640/42/57 controllers have no close override, inherit no-op, leaving serialport opened even explicit controller.close. SPI scheduler also no close delegation and Mcp controller no close; include under root's lifecycle umbrella if already addressing.

## Additional lower-priority confirmed issues
- Sen0657Sensor.java:278-389 defines duplicate rainLast10Minutes/1Hour/24Hours/rainRate names with two distinct algorithms; JSON later overrides earlier readings while duplicate stateful calculations still run. Pressure trend sample180 represents only~180s if reads1Hz, despite advertised3-hour window. Weather readSensors:474 swallowsIO and reports previous/initialzero values as fresh timestamp; no sampleatomicity.
- HT16K33Controller.java:233/243 schema blinkRateinteger/rawhex vs processBlink expects blink enum string/rawBase64. Schema-shaped nesteddeviceStatic/deviceWrite also not consumed by this controller; same LCDschema nested envelope mismatch.
- HT16K33 background clock/test bypass scheduler's global lock by directcontroller.write/rawWrite. Atomicity/potential interference needs concurrency fake; stop only flag does not join tasks, so previous taskmay overwrite new manual text aftercancel.
- St7735Controller.java:91-93 advertises DRAW_IMAGE/CLEAR/etc yet configurationhandler is no-op; Storageimplementation writeBlock/readBlock no-op. Public helper lcdWriteString uses>= bounds, losing glyph exactlyfittingright/bottomedge. Percentagehelper0% drawsonebar(val+=10). Rectanglearguments unchecked, negativew createsNegativeArraySize.
- Mcp3y0xDevice.java:57 range max is(1<<(bits+1))-1, should(1<<bits)-1; mount allows unsupported resolutions/channelcounts and negativechannel accepted. Not hardwareframe regression but schema/configquality issue.
- Demo SCD41/PMS/TSL derived readings call next() again, so category/lux may disagree with displayed physicalvalues in same state response; currently lowimpact simulation.

## Potential / needs specific adapter or hardware verification
- Serial deadline cannot cap blocking readBytes call; adapters with blockingtimeout>responseTimeout stallbeyond advertiseddeadline. Fixed5byte Modbusexception replies incorrectly waitnormal7/8/etc length and time out. No transaction synchronization enables concurrentrequestresponse mixing; test concurrentpoll/config.
- MCP23017 constructor reads registers before forcing BANK0/SEQOP0; mountingchip alreadyBANK1 interprets/writeswrongregisters and resetwritesIOCON0x0A(whichisOLATAinBANK1), neverresetsBANK. Testprefillbankedchip.
- STL19P CRCfailure resetsframeIndex0 without scanning47byte rejectedbuffer for subsequentheader; loseextra frames after droppedbyte;scanpointlist unbounded if neverobservedwrap(<20,>340) withotherwisevalidCRCangles. Needmanufacturer protocol/errorstreamfixture tochooseappropriate handling.

## Validation / references
No projecttests run by this agent (read-only review). Attempted standalone Panel javac probe, but javac absent fromPATH and /tmp,/opt,/usr/lib/jvm; didnotexecute. Code/spec-derived arithmetic and framingfindings are staticproofs, not runtimeclaims.
Primary DS3231 datasheet successfully retrieved: https://www.analog.com/media/en/technical-documentation/data-sheets/ds3231.pdf?pseSrc=pgTutoDs3231 , details (DYDT),(hours),(temp),(EOSC). 
Primary MCP3204/08 https://ww1.microchip.com/downloads/en/DeviceDoc/21298E.pdf ,(section6.1clockalignment).
PrimaryMCP23017datasheetsearch refs ; primaryproduct https://www.microchip.com/en-us/product/mcp23017  verifiesDEFVALmismatch. PDFopenrestricted.
Kernel w1_therm https://kernel.org/doc/html/v5.15/w1/slaves/w1_therm.html  describes CRCYES/NO.

## Inventory inspected (146 Java files; font literal arrays sampled/structurally checked)

serial (12)
- serial/SerialBusManager.java
- serial/SerialDeviceController.java
- serial/devices/sensors/SerialDevice.java
- serial/devices/sensors/sen0547/Stl19pController.java
- serial/devices/sensors/sen0547/Stl19pScanReading.java
- serial/devices/sensors/sen0547/Stl19pSensor.java
- serial/devices/sensors/sen0640/Sen0640Controller.java
- serial/devices/sensors/sen0640/Sen0640Sensor.java
- serial/devices/sensors/sen0642/Sen0642Controller.java
- serial/devices/sensors/sen0642/Sen0642Sensor.java
- serial/devices/sensors/sen0657/Sen0657Controller.java
- serial/devices/sensors/sen0657/Sen0657Sensor.java

spi (6)
- spi/SpiBusManager.java
- spi/SpiDevice.java
- spi/SpiDeviceController.java
- spi/SpiDeviceScheduler.java
- spi/devices/mcp3y0x/Mcp3y0xController.java
- spi/devices/mcp3y0x/Mcp3y0xDevice.java

onewire (5)
- onewire/OneWireBusManager.java
- onewire/OneWireDevice.java
- onewire/OneWireDeviceController.java
- onewire/devices/ds18b20/DS18B20Controller.java
- onewire/devices/ds18b20/DS18B20Device.java

gpio (18)
- gpio/GpioExtensionPinManagement.java
- gpio/GpioInterruptFactory.java
- gpio/InterruptExecutor.java
- gpio/InterruptFactory.java
- gpio/InterruptHandler.java
- gpio/InterruptListener.java
- gpio/InterruptPin.java
- gpio/Pi4JPinManagement.java
- gpio/PiInterruptFactory.java
- gpio/PinManagement.java
- gpio/ThreadInterruptExecutor.java
- gpio/pin/BaseDigital.java
- gpio/pin/BaseDigitalInput.java
- gpio/pin/BaseDigitalOutput.java
- gpio/pin/GpioDigitalInput.java
- gpio/pin/GpioDigitalOutput.java
- gpio/pin/Pi4JDigitalInput.java
- gpio/pin/Pi4JDigitalOutput.java

i2c/devices/output (47)
- i2c/devices/output/Task.java
- i2c/devices/output/TimeHelper.java
- i2c/devices/output/lcd/lcd1602/Lcd1602Controller.java
- i2c/devices/output/lcd/lcd1602/Lcd1602Device.java
- i2c/devices/output/lcd/lcd1602/backlight/BacklightPwm.java
- i2c/devices/output/lcd/lcd1602/backlight/BacklightPwmController.java
- i2c/devices/output/lcd/lcd1602/backlight/BacklightRGBV1Pwm.java
- i2c/devices/output/lcd/lcd1602/backlight/BacklightRGBV1PwmController.java
- i2c/devices/output/lcd/lcd1602/backlight/BacklightRGBV2Pwm.java
- i2c/devices/output/lcd/lcd1602/backlight/BacklightRGBV2PwmController.java
- i2c/devices/output/lcd/lcd1602/backlight/BacklightV1Pwm.java
- i2c/devices/output/lcd/lcd1602/backlight/BacklightV1PwmController.java
- i2c/devices/output/lcd/lcd1602/backlight/BacklightV1_1Pwm.java
- i2c/devices/output/lcd/lcd1602/backlight/BacklightV1_1PwmController.java
- i2c/devices/output/lcd/lcd1602/commands/ClearDisplay.java
- i2c/devices/output/lcd/lcd1602/commands/Command.java
- i2c/devices/output/lcd/lcd1602/commands/Constants.java
- i2c/devices/output/lcd/lcd1602/commands/CursorControl.java
- i2c/devices/output/lcd/lcd1602/commands/CursorHome.java
- i2c/devices/output/lcd/lcd1602/commands/DisplayControl.java
- i2c/devices/output/lcd/lcd1602/commands/EntryModeSet.java
- i2c/devices/output/lcd/lcd1602/commands/FunctionSet.java
- i2c/devices/output/lcd/lcd1602/commands/SetCgramAddress.java
- i2c/devices/output/lcd/lcd1602/commands/SetDdramAddress.java
- i2c/devices/output/lcd/lcd1602/commands/WriteData.java
- i2c/devices/output/lcd/lcd1602/data/ActionType.java
- i2c/devices/output/lcd/lcd1602/data/Details.java
- i2c/devices/output/lcd/lcd1602/data/Lcd1602Command.java
- i2c/devices/output/lcd/lcd1602/data/Lcd1602Response.java
- i2c/devices/output/lcd/lcd1602/task/Clock.java
- i2c/devices/output/lcd/st7735/St7735Controller.java
- i2c/devices/output/lcd/st7735/St7735Device.java
- i2c/devices/output/lcd/st7735/font/FontDef.java
- i2c/devices/output/lcd/st7735/font/Fonts.java
- i2c/devices/output/led/ht16k33/AlphaNumericLed.java
- i2c/devices/output/led/ht16k33/BlinkRate.java
- i2c/devices/output/led/ht16k33/Constants.java
- i2c/devices/output/led/ht16k33/HT16K33Controller.java
- i2c/devices/output/led/ht16k33/HT16K33Driver.java
- i2c/devices/output/led/ht16k33/Panel.java
- i2c/devices/output/led/ht16k33/Quad7Segment.java
- i2c/devices/output/led/ht16k33/Quad7SegmentController.java
- i2c/devices/output/led/ht16k33/QuadAlphaNumeric.java
- i2c/devices/output/led/ht16k33/QuadAlphaNumericController.java
- i2c/devices/output/led/ht16k33/SevenSegmentLed.java
- i2c/devices/output/led/ht16k33/tasks/Clock.java
- i2c/devices/output/led/ht16k33/tasks/TestTask.java

i2c/devices/rtc (35)
- i2c/devices/rtc/ds3231/Ds3231Controller.java
- i2c/devices/rtc/ds3231/Ds3231Rtc.java
- i2c/devices/rtc/ds3231/LocalDateTimeSensorReading.java
- i2c/devices/rtc/ds3231/data/AgingData.java
- i2c/devices/rtc/ds3231/data/Alarm1SettingsData.java
- i2c/devices/rtc/ds3231/data/Alarm2SettingsData.java
- i2c/devices/rtc/ds3231/data/AlarmDaySettingsData.java
- i2c/devices/rtc/ds3231/data/ControlData.java
- i2c/devices/rtc/ds3231/data/HourData.java
- i2c/devices/rtc/ds3231/data/MinuteData.java
- i2c/devices/rtc/ds3231/data/MonthData.java
- i2c/devices/rtc/ds3231/data/MonthDayData.java
- i2c/devices/rtc/ds3231/data/SecondData.java
- i2c/devices/rtc/ds3231/data/StatusData.java
- i2c/devices/rtc/ds3231/data/TemperatureData.java
- i2c/devices/rtc/ds3231/data/WeekDayData.java
- i2c/devices/rtc/ds3231/data/YearData.java
- i2c/devices/rtc/ds3231/register/AgingRegister.java
- i2c/devices/rtc/ds3231/register/Alarm1ModeRegister.java
- i2c/devices/rtc/ds3231/register/Alarm2ModeRegister.java
- i2c/devices/rtc/ds3231/register/AlarmDayRegister.java
- i2c/devices/rtc/ds3231/register/BcdRegister.java
- i2c/devices/rtc/ds3231/register/ControlRegister.java
- i2c/devices/rtc/ds3231/register/HourRegister.java
- i2c/devices/rtc/ds3231/register/MinutesRegister.java
- i2c/devices/rtc/ds3231/register/MonthDayRegister.java
- i2c/devices/rtc/ds3231/register/MonthRegister.java
- i2c/devices/rtc/ds3231/register/SecondsRegister.java
- i2c/devices/rtc/ds3231/register/StatusRegister.java
- i2c/devices/rtc/ds3231/register/TemperatureRegister.java
- i2c/devices/rtc/ds3231/register/WeekDayRegister.java
- i2c/devices/rtc/ds3231/register/YearRegister.java
- i2c/devices/rtc/ds3231/values/Alarm1Settings.java
- i2c/devices/rtc/ds3231/values/Alarm2Settings.java
- i2c/devices/rtc/ds3231/values/ClockFrequency.java

i2c/devices/demo (9)
- i2c/devices/demo/I2cDemoController.java
- i2c/devices/demo/SimulatedFloatValue.java
- i2c/devices/demo/SimulatedIntValue.java
- i2c/devices/demo/impl/pmsca003i/Pmsa003iController.java
- i2c/devices/demo/impl/pmsca003i/Pmsa003iSensor.java
- i2c/devices/demo/impl/scd41/Scd41Controller.java
- i2c/devices/demo/impl/scd41/Scd41Device.java
- i2c/devices/demo/impl/tls2561/TSL2561Controller.java
- i2c/devices/demo/impl/tls2561/TSL2561Sensor.java

i2c/devices/gpio (14)
- i2c/devices/gpio/mcp23017/Mcp23017Controller.java
- i2c/devices/gpio/mcp23017/Mcp23017Device.java
- i2c/devices/gpio/mcp23017/register/DefaultValueRegister.java
- i2c/devices/gpio/mcp23017/register/ExpanderConfigurationRegister.java
- i2c/devices/gpio/mcp23017/register/GenericPinConfigRegister.java
- i2c/devices/gpio/mcp23017/register/GpioPortRegister.java
- i2c/devices/gpio/mcp23017/register/InputPolarityRegister.java
- i2c/devices/gpio/mcp23017/register/InterruptCaptureRegister.java
- i2c/devices/gpio/mcp23017/register/InterruptControlRegister.java
- i2c/devices/gpio/mcp23017/register/InterruptFlagRegister.java
- i2c/devices/gpio/mcp23017/register/InterruptOnChangeRegister.java
- i2c/devices/gpio/mcp23017/register/IoDirectionRegister.java
- i2c/devices/gpio/mcp23017/register/OutputLatchRegister.java
- i2c/devices/gpio/mcp23017/register/PullupResisterRegister.java

# Gap sweep: drivers, storage, direct GPIO and mock bus
Read all 17 driver Java files, all 6 storage Java files, 3 direct Java files, and I2CMockBusManager.java. No source edits. Additional findings follow.

## Confirmed additional findings
21. **P1 PCA9685 prescale updates OR with the previous value.** i2c/devices/drivers/pca9685/registers/PreScaleRegister.java:57-58 uses setControlRegister(0xff,val); SingleByteRegister applies (old & mask) | value. Example old prescale0x1E, requested0x79 produces0x7F, then requested0x04 stays0x7F. All successive frequency changes can be wrong. getPrescale line54 also returns signed byte (>=128 becomes negative). Tests replace0x1E→0x79→0x04, values128/255.
22. **P2 PCA9685 subaddress updates retain old address bits and read as negative.** registers/SubAddressRegister.java:39-44. Getter uses arithmetic shift of byte; hardware0xE0 (7-bit address0x70) returns-16. Setter masks with0xFE instead of clearing address field, preserving every previous address bit:0xE0→requested0x30 stays0xE0 instead of0x60. Tests addresses0x70→0x30→0x01.
23. **P1 AT24 EEPROM mount performs an unsafe capacity probe.** i2c/devices/storage/at24c/AT24CnnDevice.java:45-60. Smaller EEPROMs do not necessarily reject addresses beyond capacity; unused upper address bits are ignored (manufacturer AT24C128 has only14 word-address bits, AT24C25615). Test65535 wraps to the device's last cell and can pass on smaller parts, misclassifying them as65536. Additionally writeByte starts self-timed write cycle then immediately readByte with no ACK-poll/wait. If the busy read throws, catch skips restoring originalValue; mounting permanently changes a user's byte. Even successful restore starts another write cycle and constructor returns without waiting. Tests simulated address mask for each supported capacity, busy NACK after write, verify every original byte survives mount. Prefer explicit capacity configuration or a non-mutating detection strategy.
24. **P1 AT24C128/256 use incorrect page-write size.** AT24CnnDevice.java:63 chooses128 for every size>8192; manufacturer AT24C128/256 pages are64. Once correctly detected or size detection revised, writeBlock at address0 with128 bytes wraps the second64 into the first page, corrupting stored data while reporting success. Current capacity misclassification also makes this affect smaller chips. Tests writes crossing31/32,63/64,127/128 and EEPROM model implementing true page rollover.
25. **P2 EEPROM accepts partial read as a full chunk.** AT24CnnDevice.java:79-81. Any positive read count advances offset by requested len rather than actual read, returning zero-filled holes and skipping unread bytes. Example readChunk requested32 returns5: skips27 bytes and accepts success. readByte:157-159 accepts0 as zero instead of rejecting missing data. Tests partial5/32 and0/1.
26. **P2 EEPROM write readiness timeout silently reports success.** AT24CnnDevice.java:138-150. waitForReady loops ten times, catches all errors, then returns regardless of whether device ever acknowledged. Final page can still fail/busy forever but writeBlock and controller returnSuccess. Test all readiness calls fail and last page has no subsequent operation to expose failure.
27. **P2 EEPROM operations lack address/length bounds.** AT24CnnDevice.java:74,170-190. Negative address is encoded modulo65536; address+data.length beyond capacity wraps physically; negative read length throws unchecked NegativeArraySizeException. JSON command exposes these parameters directly. Tests negative address/length and address=capacity-1,length2; reject before mutating cells.
28. **P2 ShiftRegister cannot construct with expander pin management.** direct/shift/ShiftRegisterDevice.java:73-79 creates config id/name/pin only, but GpioExtensionPinManagement.allocateOutPin.java:60 unconditionally calls config.get("pull").equalsIgnoreCase("up"). Constructor with a legitimate GPIO-expander PinManagement throws NPE at first allocation. Test expander-backed construction, default no-pull behavior. clearPin0 is also treated as absent by conditionline61, even thoughGPIO0 valid.
29. **P2 Mock bus rescan remounts/replaces every active device.** i2cmock/I2CMockBusManager.java:73-77. No existing-device guard or close; every scan resets demo random trajectories and any controller configuration, overwrites active entry. Real manager's current PR remount/lifecycle fix should include this subclass. Tests scan twice retaining same identity/state and closing once.
30. **P2 RotaryEncoder has no listener disposal.** direct/pec11/RotaryEncoder.java:47-51. Registers anonymous inputA listener without retaining it and exposes no close/remove. Replacing encoder on same pin leaves old instance processing and delivering callbacks forever. Tests listener count and no callbacks after disposal.

## Conditional additional findings / lower-priority observations
- PCA9685 PrescaleRegister.java:87 formula uses round(divisor-0.5) rather than datasheet round(divisor)-1. At50Hz produces122 instead of121 (with25MHz oscillator), so small frequency bias; a calibration correction may be intentional, but no calibration parameter/comment explains it.
- Pca9685Device.java:80-83 clearsSLEEP without500us delay or explicit restart, initialise.java:106-108 also immediately restarts after wake. Manufacturer requires SLEEP0 for at least500us beforeRESTART1. Rapid nextsetPWM can violate timing; verify captured transaction timestamps/oscillator behavior. Direct setPrescale/fromData writes while awake are ignored by hardware; only setPWMFrequency brackets sleep.
- Pca9685Device.close.java:64-72 does not delegate to super.close; include in future lifecycle work. setAllPWM(0,0) does not set explicitFULL_OFF flag; verify exact endpoint hardware semantics before calling it unsafe. LedControlRegister.setRate clearsfull-on/off flags and wraps values to12bits with no validation.
- LinearResponse.getIdle.java:49-50 always0 (literal0/2), which may be intended neutral for symmetric-angle servos; for configured range10..90 the idle call clamps to10, not50. Constructor PwmDevice deliberately sweeps min/max before idle (lines38-42), so connecting a throttle/servo actuates its full range during construction; report only if undesired by API contract.
- Rotary table was derived against actual A-only event delivery rather than assuming four-edge sampling. Physical cycles00→01→11→10→00 generate A events11,00,11,00 and deltas-1 each after initial ignored event; reverse00→10→11→01→00 generates10,01,10,01 and deltas+1. Thus no unsupported “wrong quadrature lookup” claim. Initial firstA event is dropped rather than sampling startup state; output is2 counts/fullcycle after startup, ambiguous without documented counts-per-detent. encoderValue is also unsynchronized/nonvolatile across callback/getValue threads.
- AT24CnnCommand requires@class in JSON; plainaction command errors rather than executing. Unlike LCD this controller does not advertise a detailed command schema, so classify as API/documentation requirement until examples confirm intended shape.

## Primary specifications for gap sweep
AT24C128/256 page64 and wrap: https://onlinedocs.microchip.com/oxy/GUID-338FB7E8-CF68-4894-B7AB-B95E716C71D4-en-US-3/GUID-EE1346FF-A5FE-4EE8-A6E8-B5A41D33EFF3.html ; word-address14/15bit: https://onlinedocs.microchip.com/oxy/GUID-338FB7E8-CF68-4894-B7AB-B95E716C71D4-en-US-3/GUID-E43A146F-6523-457F-8801-8CE045605811.html (). Both search excerpts directly verified manufacturer text; open returned503.
PCA9685 datasheet https://www.nxp.com/docs/en/data-sheet/PCA9685.pdf () specifies500us wake interval. Primary kernel driver https://github.com/torvalds/linux/blob/master/drivers/pwm/pwm-pca9685.c () also delays500us.

## Gap inventory (27 Java files)

i2c/devices/drivers (17)
- i2c/devices/drivers/pca9685/Pca9685Controller.java
- i2c/devices/drivers/pca9685/Pca9685Device.java
- i2c/devices/drivers/pca9685/data/LedControlData.java
- i2c/devices/drivers/pca9685/data/Mode1Data.java
- i2c/devices/drivers/pca9685/data/Mode2Data.java
- i2c/devices/drivers/pca9685/data/PreScaleData.java
- i2c/devices/drivers/pca9685/data/SubAddressData.java
- i2c/devices/drivers/pca9685/registers/LedControlRegister.java
- i2c/devices/drivers/pca9685/registers/Mode1Register.java
- i2c/devices/drivers/pca9685/registers/Mode2Register.java
- i2c/devices/drivers/pca9685/registers/PreScaleRegister.java
- i2c/devices/drivers/pca9685/registers/SubAddressRegister.java
- i2c/devices/drivers/pca9685/servos/AngleResponse.java
- i2c/devices/drivers/pca9685/servos/LinearResponse.java
- i2c/devices/drivers/pca9685/servos/PwmDevice.java
- i2c/devices/drivers/pca9685/servos/Servo.java
- i2c/devices/drivers/pca9685/servos/ThrottleResponse.java

i2c/devices/storage (6)
- i2c/devices/storage/at24c/AT24CnnController.java
- i2c/devices/storage/at24c/AT24CnnDevice.java
- i2c/devices/storage/at24c/data/AT24CnnCommand.java
- i2c/devices/storage/at24c/data/AT24CnnResponse.java
- i2c/devices/storage/at24c/data/Details.java
- i2c/devices/storage/at24c/values/ActionType.java

direct (3)
- direct/pec11/RotaryEncoder.java
- direct/pec11/RotaryEncoderListener.java
- direct/shift/ShiftRegisterDevice.java

i2cmock (1)
- i2cmock/I2CMockBusManager.java
