# Device Controller

This project provides a unified interface for communicating with I2C, SPI and 1-Wire devices. 
It supports a wide range of sensors, LED panels, servo drivers, and other devices. 
The controller can automatically detect known devices on the bus and provide access to these devices through a standard JSON-based API.

## Features
- **JSON-Schema**: All devices define a JSON Schema that can be fetched to enable easy integration of devices for read or write
- **Unified JSON-based API**: All devices can be accessed and manipulated using a standard API, which exchanges data in JSON format.
- **Device Loading**: Known devices are automatically loaded and made ready for interaction.
- **Generic I2C Device Entries**: Each detected device is represented by an `I2CDeviceEntry` instance, which holds all necessary information for interacting with the device.
- **Generic SPI Device Entries**: Can load and access SPI Devices and access by a `SPIDeviceEntry` instance.
- **1-Wire Device Entries**: Scans and autoloads any 1-wire device found within the filesystems namespace
- **I2C Bus Scanning**: The controller can automatically scan the I2C bus to detect connected devices.

## Device Configuration

### SPI Device Configuration

This document describes the configuration parameters for an SPI device using the `deviceConfig` map.

### Example Configuration

```java
        Map<String, Object> deviceConfig = new LinkedHashMap<>();
        // Required for SPI Bus Configuration
        deviceConfig.put("spiBus", "0"); // Can be 0 to 6
        deviceConfig.put("spiChipSelect", "0"); // Either 0, 1 or 2
        deviceConfig.put("spiMode", "0");    // Can be 0 to 3

        // Device Specific configuration
        deviceConfig.put("resolution", "12");
        deviceConfig.put("channels", "8");
        
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Mcp3y0x", deviceConfig);
        deviceBusManager.getSpiBusManager().configureDevices(map);
```
#### Configuration Parameters

The `deviceConfig` map contains the following parameters for the SPI device:

#### spiBus

- Type: `String`
- Description: Specifies the SPI bus number. This parameter determines the specific SPI bus to which the device is connected.
- Example: `"0"`

#### csAddress

- Type: `String`
- Description: Specifies the chip select (CS) address of the device. This parameter determines the specific CS address to which the device is assigned.
- Example: `"5"`

#### resolution

- Type: `String`
- Description: Specifies the resolution of the SPI device. This parameter determines the number of bits used for digital representation in the device.
- Example: `"12"`

#### channels

- Type: `String`
- Description: Specifies the number of channels available on the SPI device. This parameter determines the number of independent input or output channels supported by the device.
- Example: `"8"`

### Usage

To configure an SPI device, you can use the `configureDevices` method provided by the SPI device manager. Pass the configuration map as an argument to the method.

Example usage:

```java
deviceBusManager.getSpiBusManager().configureDevices(map);
```
## Sample Website Usage

Before using the controller, make sure all your I2C devices are connected correctly.

1. **Start the Controller**: Use the provided script to start the controller.

    ```bash
    ./startController.sh
    ```

2. **Scan the I2C Bus**: The controller will automatically scan the I2C bus and load all known devices.
3. **Access Devices via the API**: You can access and manipulate devices using the provided API. Data is exchanged in JSON format.
4. **Add More Devices**: You can add support for more devices by implementing the `I2CDeviceEntry` interface and adding the new device to the controller's known devices.

   For example, to read data from a device:

    ```bash
    curl http://localhost:8080/device/i2c/read?address=0x28
    ```

   This would return a JSON object with the device's data:

    ```json
    {
        "data": {
            "temperature": 25.6,
            "humidity": 45.2
        }
    }
    ```

## Known Devices

### I2C

#### Sensors

| Sensors             | Manufacturer            | Description                             | Functionality                                                                                                                                                                                        | Datasheet                                                                                                                             |
|---------------------|-------------------------|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| AM2315              | Aosong                  | Temperature and humidity sensor         | - Measures ambient temperature and humidity                                                                                                                                                          | [Datasheet](https://asairsensors.com/wp-content/uploads/2021/09/Data-Sheet-AM2315C-Humidity-and-Temperature-Module-ASAIR-V1.0.02.pdf) |
| AM2320              | Aosong                  | Temperature and humidity sensor         | - Measures ambient temperature and humidity                                                                                                                                                          | [Datasheet](https://core-electronics.com.au/attachments/localcontent/AM2320_13434819210.pdf)                                          |
| AS3935              | AMS                     | Lightning sensor                        | - Detects and provides information on nearby lightning activity                                                                                                                                      | [Datasheet](https://www.mouser.com/datasheet/2/588/ams_AS3935_Datasheet_EN_v5-1214568.pdf)                                            |
| BH1750              | ROHM                    | LUX sensor                              | - Measures ambient light in lux                                                                                                                                                                      | [Datasheet](https://www.mouser.com/datasheet/2/348/bh1750fvi-e-186247.pdf)                                                            |
| BME688              | Bosch Sensortec         | Gas, Pressure, temp and humidity sensor | - measures VOC, Pressure, Temperature and humidity                                                                                                                                                   | [Datasheet](https://www.bosch-sensortec.com/products/environmental-sensors/gas-sensors/bme688/#documents)                             |
| BMP280              | Bosch Sensortec         | Pressure and temperature sensor         | - Measures atmospheric pressure and ambient temperature                                                                                                                                              | [Datasheet](https://www.bosch-sensortec.com/products/environmental-sensors/pressure-sensors/bmp280/)                                  |
| BNO055              | Bosch Sensortec         | 9-axis absolute orientation sensor      | - Provides precise orientation, acceleration, and magnetic data                                                                                                                                      | [Datasheet](https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bno055-ds000.pdf)                           |
| DFRobot Gas Sensors | DFRobot                 | Gas detection sensors                   | - Detects various gases such as Carbon Monoxide, Hydrogen Sulfide, Alcohol, Oxygen, Hydrogen, Ozone, Sulfur Dioxide, Nitrogen Dioxide, Hydrogen Chloride, Chlorine, Hydrogen Fluoride, and Phosphine | [Datasheet](https://wiki.dfrobot.com/SKU_SEN0465toSEN0476_Gravity_Gas_Sensor_Calibrated_I2C_UART)                                     |
| DS3231              | Maxim Integrated        | Real-time clock                         | - Provides accurate timekeeping and calendar functions                                                                                                                                               | [Datasheet](https://www.analog.com/media/en/technical-documentation/data-sheets/DS3231.pdf)                                           |
| INA219              | Texas Instruments       | High-side current sensor                | - Measures high-side current and voltage                                                                                                                                                             | [Datasheet](https://www.ti.com/lit/ds/symlink/ina219.pdf)                                                                             |
| LPS25               | STMicroelectronics      | MEMS pressure and temperature sensor    | - Measures barometric pressure                                                                                                                                                                       | [Datasheet](https://www.st.com/resource/en/datasheet/lps25hb.pdf)                                                                     |
| LPS35HW             | STMicroelectronics      | MEMS pressure and temperature sensor    | - Measures barometric pressure                                                                                                                                                                       | [Datasheet](https://www.st.com/resource/en/datasheet/lps35hw.pdf)                                                                     |
| MSA311              | Memsensing Microsystems | Triple Axis Accelerometer               | - Orientation and direction sensor                                                                                                                                                                   | [Datasheet](https://cdn-shop.adafruit.com/product-files/5309/MSA311-V1.1-ENG.pdf)                                                                     |
| PMSA003I            | Plantower               | Particulate matter sensor               | - Detects and measures particulate matter in the air                                                                                                                                                 | [Datasheet](https://cdn-shop.adafruit.com/product-files/4632/4505_PMSA003I_series_data_manual_English_V2.6.pdf)                       |
| SCD4x               | Sensirion               | CO2 Sensor with temperature/humidity    | - Measure CO2 in PPM                                                                                                                                                                                 | [Datasheet](https://sensirion.com/media/documents/48C4B7FB/6426E14D/CD_DS_SCD40_SCD41_Datasheet_D1_052023.pdf)                        |
| SEN0539             | DFRobot                 | Offline voice recognition               | - Converts voice commands to command id values                                                                                                                                                       | [Datasheet](https://wiki.dfrobot.com/SKU_SEN0539-EN_Gravity_Voice_Recognition_Module_I2C_UART#)                                       |
| Sen6x               | Sensirion               | Air Quaility sensors                    | - Measures Air Particulate, Humidity, Temperature, CO2, VOC, NOx, HCHO depending on model                                                                                                            | [Datasheet](https://sensirion.com/media/documents/FAFC548D/6731FFFA/Sensirion_Datasheet_SEN6x.pdf)                                    |
| TLS2561             | Texas Instruments       | Digital light sensor                    | - Measures ambient light intensity                                                                                                                                                                   | [Datasheet](https://cdn-shop.adafruit.com/datasheets/TSL2561.pdf)                                                                     |
| SHT3x-D             | Sensirion               | Temperature and Humidity Sensor         | - Measures  Humidity, Temperature                                                                                                                                                                    | [Datasheet](https://sensirion.com/media/documents/213E6A3B/63A5A569/Datasheet_SHT3x_DIS.pdf)                                          |

#### Output Devices

| Device                | Manufacturer | Description                               | Functionality                                | Datasheet                                                       |
|-----------------------|--------------|-------------------------------------------|----------------------------------------------|-----------------------------------------------------------------|
| PCA9685               | NXP          | 16-channel PWM controller                 | Controls up to 16 channels of PWM output     | [Datasheet](https://www.nxp.com/docs/en/data-sheet/PCA9685.pdf) |
| Adafruit 7-Segment    | Adafruit     | I2C Quad Segment Alphanumeric LED Display | Displays alphanumeric characters and symbols | [Product Page](https://www.adafruit.com/product/1268)           |
| Adafruit Alphanumeric | Adafruit     | I2C Quad Alphanumeric LED Backpack        | Displays alphanumeric characters and symbols | [Product Page](https://www.adafruit.com/product/1911)           |

#### Storage Devices

| Device     | Manufacturer | Description               | Functionality                   | Datasheet                                                                 |
|------------|--------------|---------------------------|---------------------------------|---------------------------------------------------------------------------|
| AT24C32/64 | Atmel        | 4KB or 64KB serial EEPROM | Can read or write 4K or 8K data | [Datasheet](https://ww1.microchip.com/downloads/en/devicedoc/doc0336.pdf) |
|

### SPI

| Device  | Manufacturer | Description                                     | Functionality                                             | Datasheet                                                     |
|---------|--------------|-------------------------------------------------|-----------------------------------------------------------|---------------------------------------------------------------|
| MCP3004 | Microchip    | 10-bit Analog-to-Digital Converter - 4 channels | Converts analog signals to digital with 10-bit resolution | [Datasheet](https://www.microchip.com/wwwproducts/en/MCP3004) |
| MCP3204 | Microchip    | 12-bit Analog-to-Digital Converter - 4 channels | Converts analog signals to digital with 12-bit resolution | [Datasheet](https://www.microchip.com/wwwproducts/en/MCP3204) |
| MCP3008 | Microchip    | 10-bit Analog-to-Digital Converter - 8 channels | Converts analog signals to digital with 10-bit resolution | [Datasheet](https://www.microchip.com/wwwproducts/en/MCP3008) |
| MCP3208 | Microchip    | 12-bit Analog-to-Digital Converter - 8 channels | Converts analog signals to digital with 12-bit resolution | [Datasheet](https://www.microchip.com/wwwproducts/en/MCP3208) |

### Serial

| Device  | Manufacturer | Description                      | Functionality                                                                                                                                              | Datasheet                                                                      |
|---------|--------------|----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| SEN0640 | DFRobot      | Solar Radition Sensor 400-1100nm | Can measure the solar radiation value in the spectral range of 400-1100nm.                                                                                              | [Wiki](https://wiki.dfrobot.com/SKU_SEN0657_RS485_7in1_Weather_Station_Sensor) |
| SEN0642 | DFRobot      | UV Sensor 290-390nm              | Can measure the UV intensity and index values in the wavelength range of 290-390nm                                                                         | [Wiki](https://wiki.dfrobot.com/SKU_SEN0642_RS485_RS485_UV_Sensor)             |
| SEN0657 | DFRobot      | 7 in 1 Weather station           | Can measure seven meteorological parameters including wind speed, wind direction, temperature, humidity, air pressure, light and rainfall at the same time | [Wiki](https://wiki.dfrobot.com/SKU_SEN0657_RS485_7in1_Weather_Station_Sensor) |




## Contribute

Contributions are always welcome! Please read the contributing guidelines first.

