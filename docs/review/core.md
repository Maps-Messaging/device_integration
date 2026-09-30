# Core device sweep

Snapshot before the current fixes. Grouped serialization and grouped schema findings (4 and 5) are fixed in this PR; other findings remain open.

Scope: all 53 current Java files in devices top-level (6), impl (4), io (5), sensorreadings (17), util (11), i2c top-level (4), and i2c/devices base registers (6). Read source and traced relevant device callers. Excluded device-specific implementations except caller tracing; known RegisterMap double toData, I2CBusManager close/cache, and SEN6x-specific protocol fixes reserved to root. No repository edits. Findings below are confirmed by source/data flow; no new executable tests ran (jshell unavailable in this runtime).

## Important confirmed findings

1. **High: I2C configuration routing fails on its required metadata.** `DeviceBusManager.java:120-121` reads `i2c.bus` and passes the same map to `I2CBusManager.configureDevices`; `I2CBusManager.java:105` parses every key with Integer.parseInt. Any configuration including the required bus key eventually throws NumberFormatException. If address entries precede bus, premature return can mask failure but still skips remaining devices. Strip metadata before forwarding or explicitly distinguish it in bus manager.

2. **High: Multi-device configuration mounts only the first recognized device.** `I2CBusManager.java:113` returns createAndMountDevice inside the iteration. A map with two valid addresses never configures the second. Configure all entries, preserving existing single-controller return contract deliberately.

3. **High: Clearing one GPIO bit preserves that bit and destroys its neighbors.** `i2c/devices/BitsetRegister.java:123` uses `buffer[wordIndex] &= (1 << bit)` rather than the complemented mask. Example 0xff clear(2) becomes 0x04, rather than 0xfb. MCP23017 uses this base operation for configureOutput, setLow, disable pullup and interrupts. Can change unrelated pins and fail to deassert selected output. Relayed to GPIO reviewer/root to deduplicate.

4. **High: Grouped sensor values are always discarded.** `DeviceController.java:103-106` allocates readingObject but recurses into a different new JsonObject. readingObject stays empty, so group is never added. Only current concrete GroupSensorReading caller is SEN6x (status group); core serializer must recurse into readingObject. **Fixed in MSG-350 with regression coverage.**

5. **Medium: Grouped JSON schema has wrong nesting and required scope.** `SchemaBuilder.java:81-83` passes the group property object directly as a properties map and reuses parent required array. Produces `"group":{"child":{"type":...}}` instead of type/object/properties, and requires child at root. After fixing grouped serialization, valid grouped payloads fail schema validation. Current concrete caller SEN6x.

6. **High: raiseExceptionOnError cannot expose I2C reading failures.** `i2c/I2CDeviceController.java:94-97` catches the IOException intentionally thrown by DeviceController.addProperty when flag true, and returns `{}` with no log. Every ordinary I2C controller inheriting getDeviceState silently masks requested fail-fast behavior; transport consumers cannot distinguish broken device from an empty state. Separate inconsistency: `DeviceController.java:82-83` getRaiseExceptionOnError always returns true, despite field initially false and setter changing it. Scheduler delegates this incorrect getter.

7. **Medium: Unknown float readings become legitimate zero measurements.** `sensorreadings/FloatSensorReading.java:34-42` rounds via Math.round(float), which maps NaN to 0; infinities become finite integer-limit values. INA219 `load_resistance` deliberately returns NaN when current near zero (Ina219Sensor:138), yet exported reading becomes 0 Ohm. Computed dew point/humidex can similarly turn unavailable/invalid results into zero. StatefulFloatSensorReading correctly checks nonfinite inputs before rounding, but normal FloatSensorReading does not. Additionally finite `value*10^precision` above int range saturates; fix double-based rounding with nonfinite preservation or an explicit unavailable policy.

8. **Medium: No-data rolling computations break full JSON state serialization.** `StatefulFloatSensorReading.java:108-121` returns NaN on source error/empty sample set, and `RollingComputations.java:196-197` least-squares returns NaN before two samples. `DeviceController.convert:147` uses Gson without special float support; Gson rejects NaN on serialization. Active SEN0657 creates pressure trend readings, so its first state poll inevitably has one sample and conversion fails; `Sen0657Controller.java:86` catches it and yields `{}`, discarding other valid readings. Core addProperty should implement an unavailable policy for nonfinite numbers; serial reviewer owns device-specific handling.

9. **Medium: Timestamp disable API has no effect.** `DeviceBusManager.java:129-130` updates static timestampReadings, but rg finds no reads of that flag anywhere. `DeviceController.java:143` always inserts timestamp. Calling enableTimestamping(false) still exports timestamps on all normal sensor states.

10. **Medium: Storm warnings use kPa-scale cutoffs on hPa inputs.** `util/StormHeuristics.java:81-84,103` compares currentPressureHpa to 99/100, while its only caller SEN0657 supplies atmosphericPressureHpa (normally about 1000, schema example 1013). A falling trend at 990 hPa never triggers stormWarning and gets no low-pressure risk boost. Thresholds need matching units; trend thresholds also appear scaled by ten (0.30 hPa/3h) but that part is a heuristic calibration question, not asserted here.

## Potential/conditional issues

- `I2CDevice.java:82,103,159-170` accepts short positive bulk transfers; MultiByteRegister.reload consumes zero-filled partial data as a complete measurement. On a provider reporting actual transfer lengths (linuxfs), a 1-byte read into a 3-byte pressure register is accepted and produces a fabricated numeric reading. Similar short writes accepted. Conditional on provider behavior; enforce full lengths only for providers with known length semantics. This was source-traced, not hardware reproduced.
- `DeviceBusManager.java:141-143` shuts down Pi4J only, without closing serial active devices (jSerialComm and STL19P reader do not belong to Pi4J). SerialBusManager has no close-all path. Explicit shutdown can leave serial ports and reader threads open. Serial reviewer should deduplicate/trace mounted lifecycle.
- `impl/SpiDeviceImpl.java:49-50` bulk write is an unconditional no-op returning 0. No current production instantiation found by rg, so latent adapter defect rather than an active-device finding.
- `io/RegisterDataDeserializer.java:64-66` and TypeNameResolver accept arbitrary className via Class.forName without restricting to RegisterData implementations before instantiation. Malformed config can load/instantiate unrelated application classes before cast rejection. Need restricted type registry/allowlist if configuration crosses trust boundary; no concrete exploit established.
- `Register.java:55` waitForDevice assigns `sensor.readRegister(address) > -1`, which is true for every successful read; exceptions leave wait true. LPS25 boot/reset therefore always take 100 iterations and never check self-clearing BOOT/RESET bits. It also returns normally after all reads fail. Device-specific reviewer should decide bit-aware timeout contract.
- `I2CDevice.java:190-192` delay uses wait without an elapsed-time loop; spurious wakeup can shorten conversion/reset waits. delay(0) waits indefinitely, unlike Device default delay(0). No internal notify on bus lock or current zero-delay caller found, so conditional rather than an observed regression.

## Minor

- SchemaBuilder fallback describes ByteArraySensorReading as string, while serializer emits JSON arrays. No current concrete ByteArraySensorReading instantiation found.
- OptionalSensorReading other than OptionalBoolean remains required and schema string regardless of payload; currently no concrete generic OptionalSensorReading instantiation found.
- SerialisationHelper uses platform charset for both directions rather than UTF-8; relevant if JSON crosses systems using different default encodings.
- AqiCalculator.computeAqi(Float.NaN) and gas score NaN paths fall through to 500, yielding a false hazardous/very strong classification; currently computed PM inputs come from PMSA003I integer readings and do not deliberately return NaN, so latent reusable calculator issue.
