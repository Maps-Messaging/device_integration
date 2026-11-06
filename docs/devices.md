# Device-by-device reference

This is a practical index of drivers registered in `META-INF/services` on `development`, not a catalogue of every function each physical chip can theoretically perform. Verify the board's datasheet and the mounted controller's generated JSON Schema before integrating. See [general guide](device-guide.md), [Linux setup](linux-setup.md) and [examples](examples.md).

**Notation:** I²C addresses are **7-bit**; some vendor documentation quotes their eight-bit shifted forms. A driver being present in the service list does not mean it can reliably auto-identify every device. I²C buses cannot host two peripherals at the same address without an address change, multiplexer or separate bus.

## I²C environmental and light sensors

| Device | How to use | Special instructions and limitations |
| --- | --- | --- |
| **AM2315** | Read temperature and relative humidity via sensor state | Slow/wake-up sensor: allow wake and conversion time; check CRC and module supply; address commonly `0x5C`, which may conflict with AM2320 |
| **AM2320** | Read temperature and humidity | Same common `0x5C` address; ensure bus wake-up and adequate pause, and avoid simultaneous AM2315 at that address; negative temperature is signed-magnitude, not two's complement |
| **BH1750** | Read illuminance in lux | Usually `0x23` (alternate strap may be `0x5C` on hardware); select supported measurement mode and allow the conversion interval; keep optical window unobstructed |
| **BME688** | Read temperature, humidity, pressure and gas resistance/related state | Heater and gas profiles influence readings and current draw; gas resistance is **not** a calibrated ppm VOC reading. Allow stabilisation; verify configuration and calibration coefficients |
| **BMP280** | Read compensated pressure and temperature | Addresses commonly `0x76/0x77`; this is **not** a humidity sensor; verify chip ID and pressure reference/altitude assumptions |
| **LPS25 / LPS25HB** | Read pressure/temperature | Driver commonly uses `0x5D`; address may depend on SA0 board wiring; use correct board and conversion data-ready behaviour |
| **LPS35HW** | Read pressure/temperature | Driver commonly uses `0x5E`; protect the vent/pressure port and avoid treating the sensor as immersion-rated without the actual assembly rating |
| **SHT31** | Read temperature and relative humidity | Common address `0x44`; checksum each word and allow conversion time; avoid heating the sensor from nearby electronics |
| **TSL2561** | Read broadband/infrared channels and computed lux | Common `0x39`; gain/integration-time affect saturation; sensor may be unsuitable for direct sun without range checks |

## I²C motion, position and event sensors

| Device | How to use | Special instructions and limitations |
| --- | --- | --- |
| **AS3935 / DFRobot SEN0290** | Poll lightning reason, distance estimate (km), raw energy | Addresses `0x01/0x02/0x03` (often `0x03`). Antenna tuning/indoor-outdoor mode matter. Polling without an IRQ may miss or repeat events; `-1` for distance is unavailable/out of range, not physical distance |
| **H3LIS200DL** | Read three axes of high-g acceleration in m/s² | Common `0x19`, configurable `0x18`; `WHO_AM_I` alone may not uniquely identify related ST devices. Driver uses fixed ±100 g / 100 Hz profile; intended for shocks rather than precision static tilt |
| **BNO055** | Read orientation state `heading`, `roll`, `pitch` | Common `0x28/0x29`; mount initialises fusion and resets the chip. State is **flat** three-angle JSON; calibration/system/version are separate configuration JSON. Calibrate in the final installation orientation |
| **MSA311** | Read three-axis acceleration | Verify board orientation, chosen range and output data rate; axes are device-relative |
| **MPU6050** | Read `x/y/z_acceleration` (m/s²), `x/y/z_angular_velocity` (rad/s), and die `temperature` (°C) | `0x68` default, `0x69` if AD0 strapped. Avoid collision with DS3231 at `0x68`. Driver uses ±2 g, ±250 °/s and 100 Hz; no FIFO/DMP/IRQ or bias-calibration API. One state poll shares a 14-byte sample |
| **DS3231 RTC** | Use `date` (ISO-8601 UTC), `temperature`, and the Java clock/alarm APIs | Usually `0x68`; shares the address with MPU6050. Ensure time is initialised when oscillator-stop flag is set; alarms use date/day masks. Battery-backup polarity is important: EOSC set **disables** backup oscillator. A physical RTC has no unique ID |

## I²C air quality, gases, NFC and voice

| Device | How to use | Special instructions and limitations |
| --- | --- | --- |
| **PMSA003I** | Read particulate mass/particle counts and air-quality output | Address `0x12`; airflow and fan warm-up matter; particulate data depend on surrounding conditions. This is the **physical** driver, not the demo implementation |
| **SCD41** | Read CO₂ (ppm), temperature and humidity | Address `0x62`; allow measurement startup/warm-up. Forced recalibration requires a measured reference CO₂ concentration and stopped periodic measurement; **never** use factory reset as a substitute |
| **SEN6x family (SEN63C, SEN65, SEN66, SEN68)** | Read only capabilities supported by the detected module | Address `0x6B`; model-specific CO₂/VOC/NOx/HCHO availability. Validate start/readiness/stop delays and CRC; after reset or fan-cleaning allow the stated recovery interval. Unavailable values are not zero |
| **DFRobot Gravity calibrated gas sensors** | Read gas-specific concentration, sensor temperature and temperature-adjusted value | The model is identified by the module type; SEN0465 O₂ is **% volume**, SEN0466 CO is **ppm**, others have different scales. Allow electrochemical warm-up, airflow and calibration; don't use this software as life-safety gas monitoring. Temperature compensation varies by gas module |
| **SEN0539** | Read recognised offline voice-command IDs/related device state | Address `0x64`; train/configure the module through its supported vocabulary/interface, avoid interpreting recognised command IDs as transcribed speech |
| **PN532 / DFRobot NFC V2** | Poll `tag_present` and `last_uid`; Java API supports supported card reads/writes | Use module **I²C mode** at 7-bit `0x24` (sometimes quoted as `0x48` in 8-bit notation). Requires polling/status transactions; writes to NFC tags can be permanent, and card type/writable areas must be checked |

### Gravity gas-model coverage

The controller supports the DFRobot calibrated gas family: **SEN0465 O₂, SEN0466 CO, SEN0467 H₂S, SEN0468 Cl₂, SEN0469 NH₃, SEN0470 SO₂, SEN0471 NO₂, SEN0472 O₃, SEN0473 H₂, SEN0474 HCl, SEN0475 HF and SEN0476 PH₃**. Identify the *specific module* before interpreting units, detection range or calibration. A reported O₂ value near 20.9% is ambient oxygen; a very low value is cause to verify sensor warm-up/calibration and external instruments, not proof of actual atmospheric depletion.

## I²C outputs, memory and GPIO

| Device | Usage | Special instructions |
| --- | --- | --- |
| **MCP23017** | 16 GPIO input/output lines; read pin states through Java GPIO API | Typically `0x20–0x27` according to address straps; check actual registered address list. Input reads use GPIO, output writes use **OLAT**. Interrupt-on-high/low uses `INTCON` and `DEFVAL`; GPIO direction and pull-ups must be configured |
| **PCA9685** | 16 PWM outputs for LEDs/servos | Common `0x40`; use a suitable external supply for servos, with shared ground. PWM frequency is common to all 16 channels; updating the prescale requires sleep/wake sequence. No generic telemetry state beyond `{}` |
| **HT16K33 4-digit 7-segment** | `display`, `brightness` (0–15), `blink` (OFF/TWO_HZ/ONE_HZ/HALF_HZ), `enabled`; optional raw Base64 and CLOCK/TEST tasks | Controller uses `0x72`; the colon occupies its own display RAM slot. The schema root describes its **flat** state; command definitions are separate. Verify mapping on your exact board |
| **HT16K33 4-character alphanumeric** | Same controller concepts, alphanumeric glyph mapping | Registered controller has **no declared I²C address range**, so normal bus scanning will not auto-mount it. Use an explicit supported mount path; not every Unicode glyph exists in the font |
| **LCD1602** | Command JSON `WRITE`, `READ` or `CLEAR`; address and byte data | Registered at `0x3E` for the supported controller variant, **not** every PCF8574 backpack. State currently `{}`; READ result bytes are Jackson Base64. Display writes address the software's 32-byte buffer. Bus clock tasks must yield shared lock |
| **ST7735 display adapter** | JSON `CLEAR`, `SET_PIXEL`, `DRAW_IMAGE` with RGB565/Base64 pixel data | Registered at `0x18` for the supported **I²C adapter**; a bare ST7735 panel normally uses SPI and is not interchangeable. Logical drawing size 160 × 80. The public state currently `{}`; unsupported storage methods throw |
| **LCD1602 backlight variants V1, V1.1, RGB V1, RGB V2** | Output/backlight PWM hardware, each with its own board register map | Address depends on controller variant (e.g. V1 `0x60`, RGB V2 `0x2D`); inspect the controller registered for the installed board. These controllers currently report `{}`, and generic JSON configuration does not expose arbitrary RGB controls |
| **AT24C32/64/128/256/512 EEPROM** | READ/WRITE bounded byte blocks, state reports model and configured capacity | Address `0x50–0x57`; **mount never probes capacity by writing**. Default is conservatively **4096 bytes** (AT24C32). For larger chips use `AT24CnnController(device, capacityBytes)` or `mount(device, capacityBytes)` in explicit Java integration. Page sizes 32/64/128 bytes by model; do not assume ordinary bus auto-mount discovers the capacity |

### Examples of output commands

HT16K33:

```json
{"deviceStatic":{"brightness":7,"blink":"TWO_HZ"},"deviceWrite":{"display":"12:34"}}
```

LCD1602 (byte-array ASCII):

```json
{"action":"WRITE","address":0,"data":[72,101,108,108,111]}
```

ST7735:

```json
{"command":"SET_PIXEL","x":10,"y":20,"color":"#ff0000"}
```

EEPROM READ/WRITE commands are available through `updateDeviceConfiguration()` and use Jackson's byte-array/Base64 handling. Avoid writing valuable data until capacity and page alignment have been confirmed.

## SPI

| Device | Usage | Special instructions |
| --- | --- | --- |
| **MCP3004 / MCP3008** | 10-bit ADC, `current` array of 4 or 8 unsigned integer codes | Explicitly configure `resolution=10`, `channels=4` or `8`; correct SPI mode, VREF and chip select. Valid ADC codes 0–1023 |
| **MCP3204 / MCP3208** | 12-bit ADC, same `current` array shape | `resolution=12`, `channels=4` or `8`; valid codes 0–4095. An ADC code is **not a voltage** without VREF conversion |

The controller does not discover which ADC is physically attached: the SPI configuration selects its resolution and channel count. The returned configuration includes the legacy fixed `dutyCycle` metadata; it is not a measured sample rate.

## 1-Wire

| Device | Usage | Special instructions |
| --- | --- | --- |
| **DS18B20** | Linux `w1_slave`, controller state `{"temperature":...}` in °C | Attach correct pull-up, enable kernel 1-Wire driver; only `28-*` is recognised. The underlying sensor tracks min/max/delta but public controller state publishes **only current temperature**. Invalid CRC `NO` is rejected and no stale new reading is claimed |

## Serial/UART (jSerialComm)

| Device | Usage | Special instructions |
| --- | --- | --- |
| **DFRobot STL19P / SEN0547** | Serial sensor scan data | Configure real serial port and its documented frame/baud settings. Verify checksum and frame boundaries |
| **SEN0640** | Solar radiation via UART/Modbus | Controller describes **4800 baud, 8N1**. Confirm sensor address and Modbus wiring/termination |
| **SEN0642** | UV radiation via UART/Modbus | **4800 baud, 8N1** per controller. Avoid sharing an RS485 link without unique slave IDs |
| **SEN0657** | Weather/environmental data via serial | Confirm installed module, serial protocol and timing. The controller description currently incorrectly refers to SEN0640; do not use that description to identify hardware |

Serial controllers are mounted with an existing `SerialDevice`; there is no general serial auto-discovery equivalent to I²C scanning. In all buses, use the **mounted instance's** `getSchema()` for its current JSON output.

## Demo devices (not physical sensors)

`demo.impl.scd41`, `demo.impl.pmsca003i` and `demo.impl.tls2561` generate synthetic data. The physical I²C manager excludes `I2cDemoController` types from its normal device scan; demos should be enabled separately and should **never** be presented as a real physical measurement.

## Validation and safety

For each device, check the electrical interface, datasheet chip identity, initialization/write side effects, one physically plausible reading or output, sensor warm-up, schema compatibility and disconnect/reconnect behaviour. Store model-specific installation notes with the hardware inventory (serial number, bus/address, calibration and board revision). See [examples](examples.md) for Java integration and [Linux setup](linux-setup.md) for host permissions.
