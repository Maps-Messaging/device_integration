# Linux setup for the MapsMessaging device library

This guide covers Debian/Ubuntu and Raspberry Pi OS hosts with directly connected devices. Use the correct board pinout and device permissions; there is no universal I²C bus number, GPIO chip offset or SPI chip-select assignment.

## Runtime and packages

The current library targets **JDK 25**, **Pi4J 4.0.2** with Linux FFM providers, and Maven for source builds. The app's JVM must permit native access:

```text
--enable-native-access=ALL-UNNAMED
```

For named modules, grant access to the Pi4J FFM module as documented by Pi4J. This flag is unrelated to Linux permissions. Verify the configured Java runtime with `java -version`.

On Debian-family hosts, an initial package setup is typically:

```bash
sudo apt update
sudo apt install i2c-tools libi2c-dev
# Install JDK 25 and Maven from your approved package repository or toolchain.
```

SPI and GPIO access also depend on kernel configuration and device nodes; install board-specific tools/packages when required. The library's serial devices use jSerialComm; they do not require the FFM SPI/I²C provider.

## Enable buses and check hardware

On Raspberry Pi OS, enable **I2C** and **SPI** via `sudo raspi-config` (Interface Options). For equivalent Debian/Ubuntu installations, configure the board's device-tree overlays/firmware; edit `/boot/firmware/config.txt` only if that is the active firmware configuration path for your distribution.

```bash
ls -l /dev/i2c-* /dev/spidev* /dev/gpiochip* 2>/dev/null
i2cdetect -l
# Replace 1 with the actual bus; do not scan a bus while another process uses it.
sudo i2cdetect -y 1
```

`i2cdetect` can issue transactions that are not harmless to every peripheral. Use a controlled bus and device-specific guidance; do not repeatedly probe EEPROMs, RTCs, gas sensors or other active devices. Prefer the library's validated discovery or explicit mounting where possible.

**Typical Raspberry Pi wiring:** I²C uses SDA/SCL plus ground and suitable pull-ups, with supply voltage per breakout datasheet. SPI uses SCLK/MOSI/MISO/CS plus ground. Check voltage compatibility and common ground before power-up.

## Run without root

Identify the service account and check actual group ownership:

```bash
id maps
ls -l /dev/i2c-* /dev/spidev* /dev/gpiochip* /dev/ttyUSB* 2>/dev/null
```

Replace `maps` with your actual account. If the required groups exist:

```bash
sudo usermod -aG i2c,spi,dialout maps
```

Some systems use `gpio` or different udev group names; create narrow udev rules for specific nodes only when necessary. Reload permissions/restart the service after group changes, and check `SupplementaryGroups=`, `DeviceAllow=`, `PrivateDevices=` and sandbox settings if using systemd.

Never solve device access by running the whole messaging server as root or making `/dev` nodes world-writable.

## 1-Wire: DS18B20

Enable the board's 1-Wire kernel controller/device-tree overlay (commonly `dtoverlay=w1-gpio` on Raspberry Pi with its appropriate data GPIO). Use a suitable data-line pull-up, commonly **4.7 kΩ** for a short conventional 3-wire connection; parasitic-power deployments have additional timing/power requirements and should follow the sensor datasheet.

```bash
sudo modprobe w1-gpio
sudo modprobe w1-therm
ls /sys/bus/w1/devices/
cat /sys/bus/w1/devices/28-*/w1_slave
```

The DS18B20 implementation reads `w1_slave`; it requires a line ending in `YES` for valid CRC and converts `t=` from millidegrees Celsius. The current implementation does **not** bit-bang 1-Wire or bypass the kernel's CRC.

## SPI: MCP3004/MCP3008/MCP3204/MCP3208

Confirm the Linux node `/dev/spidev<bus>.<chipSelect>`. Supply a real voltage reference and ensure analog inputs remain inside allowed ranges. The current Java SPI controller requires `spiBus`, `spiChipSelect`, `spiMode`, `resolution` (10 or 12) and `channels` (4 or 8). Start with mode 0 if compatible with the board/datasheet; check the ADC's wiring and timing.

```bash
ls -l /dev/spidev*
```

The raw values are ADC codes (10-bit 0–1023; 12-bit 0–4095), **not volts**. Voltage requires applying the particular board's VREF and ADC resolution.

## I²C FFM fallback caveat

Pi4J 4.0.2 can fall back from the direct I²C implementation to a file-backed implementation on adapters that lack direct support. The library relies on correct register-address and offset semantics. Do **not** assume that fallback is equivalent: validate combined transactions and transfer lengths on each adapter, or use a supported direct backend.

## Build, test and rollout

```bash
java -version
mvn -Psnapshot clean test
```

For hardware verification, run as the actual service account. Confirm one working sample per driver, expected schema and payload fields, I²C address collisions, SPI full-scale/zero values, 1-Wire CRC errors and unplug/replug handling. For outputs, confirm command operations **and** physical state; a Java call returning successfully does not prove that LEDs, PWM or EEPROM contents changed as expected.

See [device reference](devices.md) and [examples](examples.md). For FFM provider details use https://www.pi4j.com/documentation/providers/ffm/.
