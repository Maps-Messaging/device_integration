# Read-only review: motion/environment/NFC families

Base: device_integration/src/main/java/io/mapsmessaging/devices/i2c/devices/sensors/ (all locations below relative to base). No edits, dependency changes, Jira mutations, or hardware tests. Pi4J 2.8.0 preserved. Excludes SEN6x and RegisterMap/lifecycle current PR fixes.

Inventory: read every Java file in as3935 (21), bno055 (17), lps25 (41), lps35 (35), msa311 (65), pmsa003i (2), pn532 (2), scd41 (44), sen0539 (3), sht31 (15), tsl2561 (15): 260 files total, including controller, sensor, data, register/function, and enum classes. Read shared SingleByteRegister/MultiByteRegister and I2CDevice/AddressableDevice to establish semantics.

## Confirmed findings

### High

1. pn532/Pn532Sensor.java:355-385: active readResponseFrameNoIRQ splits a PN532 response across two read transactions, consumes status+5-byte header+TFI+CMD in the first 8 bytes, then expects TFI/CMD again at body[0:2]. NXP says STOP before entire frame loses remaining bytes and each read begins with status. Valid SAMConfiguration/any response therefore fails setup or framing. It also computes checksum over len-1, treats body[len-1] as DCS and body[len] as postamble; correct body indexes are len and len+1, with sum of all len bytes. This is active path (call lines348-353); similarly broken older decoder at435-467 is unused.
2. pn532/Pn532Sensor.java:415,475: writes and reads PN532 frames through writeRegister/readRegister DATA_PORT=0. PN532 is a raw framed I2C target, not a register-map device; writing register 0 injects an extra byte, reading register 0 issues an unwanted write transaction. Combine with finding1 when fixing transport, retaining Pi4J2.8.
3. pn532/Pn532Sensor.java:182,222,260,333: response decoder explicitly strips TFI+command, returning DATA, but NTAG/Classic reads, auth, and getUltraversion still expect command echo 0x41 and status at offset1. Successful DATA begins with status0 and read has17 bytes; all reads/auth/probes are rejected after transport is repaired. Writes at198,245 also ignore returned status, reporting card denial as success.
4. scd41/registers/ForcedRecalRegister.java:28: forceRecallibration instantiates FactoryResetRequest and sends0x3632, not recalibration0x362F. Calling it erases settings/calibration instead of recalibrating. Actual ForcedRecalRequest at26 also lacks required target CO2 argument and response result.
5. scd41/functions/DataReadyRequest.java:31-37 + registers/DataReadyRegister.java:33: isDataReady returns Lombok cached false without executing command. Calling getResponse instead infinitely recurses (getResponse -> readValue -> virtual getResponse) causing StackOverflowError. Request length is0 not3 and mask0x8000 is wrong (ready uses low11 bits), so fixing only recursion is insufficient.
6. scd41/functions/{GetASCERequest:26,GetAltitudeRequest:27,GetAutoCalibrationInitialPeriod:26,GetAutoCalibrationStandardPeriod:26,GetTempOffsetRequest:26,AmbientPressureRequest:27}: all getter commands have responseLength0, yet call readValue which requires3 bytes. Every public getter throws IllegalStateException without reading any response. SelfTestRequest:26 similarly never reads/checks the three-byte test result, treating failed self-test as success.
7. scd41/Scd41Sensor.java:119-120: starts periodic measurement before disabling ASC. ASC settings command is forbidden in measurement mode; sensor rejects it, leaving default automatic baseline calibration active despite intended indoor disabling. All configuration requests must be issued in idle mode; exposed settings registers do not stop periodic mode either.
8. bno055/BNO055Controller.java:75: compares read chip identity with CHIP_ID_ADDR0x00, rather than BNO055_ID0xA0. Correct sensor fails automatic detection; a zero-returning alien device is accepted.
9. bno055/BNO055Sensor.java:267-268: orientation converter assembles unsigned16-bit Euler values. Negative roll/pitch become angles near4096degrees (raw-16 becomes4095deg instead of-1deg). This path supplies controller state and heading sensor; getEuler's readVector sign handling is correct, so exposed values disagree.
10. bno055/BNO055Sensor.java:205 vs127: resets default units then divides gyro data by900 while publishes degrees/s. Default degrees uses16LSB/dps; reported angular velocity is56.25times too small.
11. lps35/Lps35Controller.java:92: lists0x5E only; LPS35HW uses7-bit0x5C/0x5D. Discovery/mount by advertised address misses actual hardware and probes wrong target.
12. tsl2561/TSL2561Sensor.java:190 + values/IntegrationTime.java:26-28: integration normalization multiplies by time ratio0.034/0.252 instead of dividing. Same light at101ms is reported roughly0.0635times402ms reading (13.7ms roughly0.00116times). Lux also omits16x compensation for low gain, which initialise selects at143; default lux is16times low.
13. as3935/registers/TunCapRegister.java:32-33: DISP_TRCO uses bit6 (actual SRCO), DISP_SRCO uses bit7 (actual LCO). Reset/power-on intended TRCO calibration toggle instead toggles SRCO; public SRCO option routes antenna oscillator onto IRQ, disrupting lightning interrupt reporting.
14. as3935/registers/LightningStrikeRegister.java:28-40: address0x04 is LSB and0x05 middle byte but constants/assembly reverse them. Energy0x012345 becomes0x014523. (Top5-bit field is correct.)
15. pmsa003i/Pmsa003iSensor.java:173-177: accepts any32 bytes without header0x42/0x4D, frame length28, checksum(sum0..29), or error byte checks. Corrupt/desynchronized/error frames are published as clean air measurements and cached1second. Need strict frame validation before updating shared data/cache.

### Medium

16. bno055/registers/CalibrationStatusRegister.java:88 and SystemStatusRegister.java:54: both refresh conditions use now<lastRead; lastRead starts now and therefore normally never refreshes. Calibration and status stay at constructor-time values indefinitely.
17. bno055/registers/SystemStatusRegister.java:44-47: SYS_STATUS is enum0..6, treated as bitset. Status5(sensor fusion running) yields IDLE+INITIALIZING_PERIPHERALS rather than SENSOR_FUSION_RUNNING;0(IDLE) yields empty list.
18. bno055/SystemStatusError.java:95-98: getStateString indexes error, not system; a running device with error0 printsIdle. Class currently appears unused, so lower reachability than17.
19. scd41/functions/SetASCERequest.java:36 and GetASCERequest.java:30: enable encodes0x0100 not0x0001, and getter tests0x8000 rather than word==1. Even after idle/length fixes enabling fails and enabled state readsfalse.
20. scd41/functions/SerialNumberRequest.java:34-44 and values/SerialNumber.java:36:48-bit serial accumulated in int, discards high16bits; distinct devices collide. CRC failure of any word is silently omitted and can still produce a nonzero serial accepted by detect.
21. scd41/functions/Request.java:81: high byte is signed, no0xFF mask. Unsigned words>=32768 return negative altitude/temp-offset/etc. CRC failure returns Integer.MIN_VALUE without error (and ASCE bit masking can interpret sentinel incorrectly).
22. scd41/registers/ReadMeasurementRegister.java:35-37: first read is immediately allowed after starting measurement; first data takes5seconds. It neither checks readiness nor delays first sample, so normal immediate getter receivesNACK/invalid frame. Low-power periodic mode still uses5sec cache though updates every30sec.
23. msa311/registers/PowerModeRegister.java:54,64 + values/PowerMode.java:25: suspended enumordinal2 writesreserved mode10; actual suspend is11. PowerOff fails to suspend. Signedbyte>>6 also decodes actual11 as-1 and returnsUNKNOWN.
24. msa311/registers/FreefallDurRegister.java:37-42: manufacturer duration=(raw+1)*2ms but getter returnsraw+1 and setter writesduration-1 despite clamping2..512.20ms requests38ms;512 wrapsbyteFF and getter256. Encoding needsduration/2-1.
25. msa311/registers/TapDurRegister.java:45 + values/TapDuration.java:26-33: indexes a mixed enum (quiet/shock/doubletap entries) directly by3-bit doubletap-window code. Code0 reports20ms instead50ms; code7 reports200ms instead700ms.
26. msa311/values/Latch.java:36: labelscode1010 as2ms; manufacturer1010 is1ms,1011 is2ms, and1111 islatched (missing). Getter returnsNON_LATCHED for valid1011/1111.
27. msa311/registers/FreefallThRegister.java:54: fromData truncates floatmg threshold toint then setter divides7.81. Roundtrip raw1 ->7.81mg ->7 ->raw0; repeating config loses threshold. Same issue other fractional values.
28. msa311/registers/ZBlockRegister.java:44-45,52-53: quantized threshold not clamped/masked to4bits; out-of-range request sets reserved highbits and wraps threshold. Same issue OrientHyRegister:47 for hysteresis>7 sets bit7.
29. msa311/registers/FreefallHyRegister.java:57-60: setter writes localvalue but doesn't update registerValue. Sequential setHysteresis then setFreefallMode loses hysteresis change using old cached bits; ActiveDurRegister:43-44 has same stale-state issue. AS3935 AfeRegister:43-50 similarly fails to cache powerDown; fromData then setGainBoost restores stale PWD bit, so requested power change is undone.
30. lps25/registers/TemperatureRegister.java:41 + lps35/registers/TemperatureRegister.java:37 + lps35/registers/PressureRegister.java:37: two's-complement subtracts0xFFFF/0xFFFFFF instead65536/16777216. Every negative sample is1LSB too high; all-ones yieldszero. Temperature examplesFF9C ->-0.99C instead-1C onLPS35. LPS25 pressure uses correct sign extension.
31. lps35/registers/FiFoStatusRegister.java:44: masks5bits but FIFO count6bits; full32-sample FIFO returns0(empty), leading callers to skip data/drain.
32. lps35/registers/Control3Register.java:103-109: fromData ignores isInterruptActive/isPushPullDrainActive despite toData exposing them. JSON/config changes to polarity/output type silently ignored.
33. lps25/Lps25Sensor.java:193-199: pressure counter increments onnot-ready but never resets after a successful read.21 transient/normal early polls across lifetime cause destructive reinitialisation (zeroing offset/reference/threshold) even if device is healthy.
34. tsl2561/registers/InterruptControlRegister.java:47: INTR occupiesbits5:4 but writesunshiftedordinal. EnablingLEVEL writesbit0(persistence), clearsmodebits and leavesinterruptdisabled.
35. tsl2561/TSL2561Sensor.java:149: advances next-read timestamp before bothADC reads succeed. First/second readfailure leaves cache stale/partially refreshed and suppressesretry forintegration interval.
36. sht31/commands/SoftResetCommand.java:24 + Sht31Sensor.java:92-93: softreset delay0 immediately followedby periodic-start. Datasheet requiresreset completion (up to1.5ms) beforeanothercommand; start canNACK/ignore onnormal initialization.
37. sht31/commands/Command.java:41,47 and scd41/functions/Request.java:62: direct AddressableDevice operations ignorewrite result;SHT31 alsoignores readcount. A failedcommand/partialresponse canbe treated ascompleted or CRC exception insteadofI/O error. SCD41 readlength checkisgood, butwrite failuresaren'tchecked.

## Unconfirmed / hardware or additional spec evidence needed

- SEN0539 Sen0539Sensor:94 assumes a contiguous5byte burstread acrosswrite-onlyPLAY/SET_MUTE/SET_VOLUME. DFRobot officialdriver reads exactlyonebyte perCMDID orWAKE_TIME andexposesonlysettersforMute/Volume. Auto-increment/readbacknotdocumented inreviewedofficialsource. Verify hardware;likelywake/mute/volume wrong. Don'tcallconfirmedsolelyfromdriverusage.
- PMSA003I update usesreadRegister(0) thoughofficialPlantower streaminterfacesmayrequire rawread. NeedI2Cmodule datasheet/bustracebeforeasserting registerpointerfailure.
- TSL2561 ADC/threshold2byte readsat0x8C/0x8E/0x82/0x84 omitWORD/BLOCK commandbits;datasheets distinguishbyte/word/blockprotocol. Verify Pi4J transaction semantics and hardware; coherentADCchannelburst needed.
- AS3935 interruptreason usescachedregister(no reload in getInterruptReason), and resetdirectcommandsleave severalregistercachesstale. RegisterMap snapshot changes may partially mitigate butstandalonegettersarecached; avoiddoublecountingrootrefreshwork.
- LPS25/LPS35 BDU leftdisabled;sampleupdatecouldtearmultibytereads. Needtrace/atomicityrequirements.
- BNO055 isConnected alwaysfalse:obviousplaceholderbut excludedfromlifecycle-rootfixscopehere;rootcanhandlecentrally.

## Tests required (not run)

MockAddressableDevice command/response fixtures: PN532 ACK/SAM/target/Exchange completeframes incl status, checksum andNACK/notready; verifyone rawtransaction/frame and cardstatus handling. SCD41 exact getterlengths, readinessrecursive path/publicgetter, targetrecalibrationcommand vsfactoryreset, idleconfigordering,ASC0x0001+CRC,CRCfailures,48bitserial,immediateread/5sec/30sec cadence. BNO055 chipA0 detection, negativeEuler,raw160gyro=>10deg/s, changingcalibration/status withclockadvance,enumstatuses0..6. AS3935 energybytes45,23,01=>0x012345,TRCO/SRCO masks,AFEconfig bitpreservation. LPS negativeallones/minimumvalues;fullFIFO32;address5C/5D;pressurecounter recovery; configuration roundtrip. MSA suspendcode11/decoding,freefall2/20/512ms,all8tapwindowcodes,all16latchcodes,thresholdroundtrip1LSB, reservedbitpreservation. TSLsameillumination under1x/16x and13.7/101/402ms,interruptmodebits, readretryafterfailure. PMS valid32byteframe plusbadheader/length/checksum/error/shortread. SEN0539 hardwareburstread/readbackverify. SHTreset/starttimeandpartialread/writefailure.

## Primary specifications retrieved

- Bosch BNO055 datasheet https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bno055-ds000.pdf .
- Sensirion SCD4x datasheet https://admin.sensirion.com/media/documents/48C4B7FB/64C134E7/Sensirion_SCD4x_Datasheet.pdf (/;commandtable,idlemode,ASC0x0001).
- NXP PN532 UM0701-02 https://www.nxp.com/docs/en/user-guide/141520.pdf (;pp42-43 STOPlosesrestofframe,readstatusprefix).
- MEMSensing MSA311V1.1 manufacturerdatasheet hostedbyAdafruit https://cdn-shop.adafruit.com/product-files/5309/MSA311-V1.1-ENG.pdf (//;12bitresolution,suspend11,freefall/tap/latch).
- ST LPS35HW https://www.st.com/resource/en/datasheet/lps35hw.pdf (//;addresses5C/5D,FSS5:0,signeddata).
- ams TSL2561 https://look.ams-osram.com/m/6ecb086edd840d9f/original/TSL2561-DS000110.pdf (/;gain16x,timeproportionality,luxformula).
- ScioSense/ams AS3935 https://www.sciosense.com/wp-content/uploads/2024/01/AS3935-Datasheet.pdf (;LSB/MIDregisters,oscillatormasks).
- Sensirion SHT3x https://sensirion.com/media/documents/213E6A3B/63A5A569/Datasheet_SHT3x_DIS.pdf (;resetcompletion).
- DFRobotDF2301Q officialsource https://github.com/DFRobot/DFRobot_DF2301Q/blob/master/DFRobot_DF2301Q.cpp (/;singlebytereads,interaccessdelay).
