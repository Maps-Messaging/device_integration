# I2C Device Controller

This project provides a unified interface for communicating with I2C devices. It supports a wide range of sensors, LED panels, servo drivers, and other I2C devices. The controller can automatically detect known devices on the bus and provide access to these devices through a standard JSON-based API.

## Features

- **I2C Bus Scanning**: The controller can automatically scan the I2C bus to detect connected devices.

- **Device Loading**: Known devices are automatically loaded and made ready for interaction.

- **Unified JSON-based API**: All devices can be accessed and manipulated using a standard API, which exchanges data in JSON format.

- **Generic I2C Device Entries**: Each detected device is represented by an `I2CDeviceEntry` instance, which holds all necessary information for interacting with the device.

## Usage

Before using the controller, make sure all your I2C devices are connected correctly.

1. **Start the Controller**: Use the provided script to start the controller.

    ```bash
    ./startController.sh
    ```

2. **Scan the I2C Bus**: The controller will automatically scan the I2C bus and load all known devices.

3. **Access Devices via the API**: You can access and manipulate devices using the provided API. Data is exchanged in JSON format.

   For example, to read data from a device:

    ```bash
    curl http://localhost:8080/device/read?address=0x28
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

4. **Add More Devices**: You can add support for more devices by implementing the `I2CDeviceEntry` interface and adding the new device to the controller's known devices.

## Contribute

Contributions are always welcome! Please read the contributing guidelines first.

