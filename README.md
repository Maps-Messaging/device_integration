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


## Contribute

Contributions are always welcome! Please read the contributing guidelines first.

