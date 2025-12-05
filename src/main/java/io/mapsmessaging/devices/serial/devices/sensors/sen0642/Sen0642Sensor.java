/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.serial.devices.sensors.sen0642;

import io.mapsmessaging.devices.Device;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import lombok.Getter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class Sen0642Sensor implements Device, Sensor {

  private static final int FUNCTION_READ_HOLDING_REGISTER = 0x03;
  private static final int FUNCTION_WRITE_SINGLE_REGISTER = 0x06;

  private static final int REGISTER_UV_IRRADIANCE = 0x0000; // UV (mW/cm²) * 100
  private static final int REGISTER_UV_INDEX = 0x0001;      // UVI (integer)
  private static final int REGISTER_DEVIATION = 0x0052;
  private static final int REGISTER_DEVICE_ADDRESS = 0x07D0;
  private static final int REGISTER_BAUD_RATE = 0x07D1;

  private static final int MIN_UV_RAW = 0;
  private static final int MAX_UV_RAW = 65535; // allow full 16-bit range, sensor will constrain

  private static final int MIN_UVI = 0;
  private static final int MAX_UVI = 20;

  private static final int MIN_SOLAR_VALUE = 0;
  private static final int MAX_SOLAR_VALUE = 1800;

  private static final int MIN_DEVICE_ADDRESS = 1;
  private static final int MAX_DEVICE_ADDRESS = 254;

  private final SerialDevice serialPort;

  @Getter
  private final List<SensorReading<?>> readings;

  private int deviceAddress = 0x01;
  private Duration responseTimeout = Duration.ofMillis(500);

  public Sen0642Sensor(SerialDevice serialPort) throws IOException {
    this.serialPort = serialPort;
    open();
    this.readings = List.of(
        new FloatSensorReading(
            "uvIrradiance",
            "mW/cm²",
            "Ultraviolet irradiance (SEN0642)",
            1.23f,
            true,
            0.0f,
            100.0f,
            2,
            this::getUvIrradiance
        ),
        new IntegerSensorReading(
            "uvIndex",
            "idx",
            "Ultraviolet Index (SEN0642)",
            3,
            true,
            MIN_UVI,
            MAX_UVI,
            this::getUvIndex
        )
    );
  }

  @Override
  public String getName() {
    return "SEN0642";
  }

  @Override
  public String getDescription() {
    return "DFRobot SEN0642 UV sensor (UART/Modbus)";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  public void open() throws IOException {
    if (serialPort.isOpen()) {
      return;
    }
    if (!serialPort.openPort()) {
      throw new IOException("Failed to open serial port: " + serialPort.getSystemPortName());
    }
  }

  public void close() {
    if (serialPort.isOpen()) {
      serialPort.closePort();
    }
  }

  // -------------------------------------------------------------------------
  // Configuration
  // -------------------------------------------------------------------------

  public void setDeviceAddressInClient(int deviceAddress) {
    if (deviceAddress < MIN_DEVICE_ADDRESS || deviceAddress > MAX_DEVICE_ADDRESS) {
      throw new IllegalArgumentException("Device address must be between 1 and 254");
    }
    this.deviceAddress = deviceAddress;
  }

  public void setResponseTimeout(Duration responseTimeout) {
    this.responseTimeout = Objects.requireNonNull(responseTimeout, "responseTimeout");
  }

  // -------------------------------------------------------------------------
  // High-level sensor operations
  // -------------------------------------------------------------------------

  /**
   * UV irradiance in mW/cm².
   * Arduino: data = (Data[3] * 256 + Data[4]) / 100.00;
   */
  public float getUvIrradiance() throws IOException {
    int rawValue = readSingleRegister(REGISTER_UV_IRRADIANCE);
    if (rawValue < MIN_UV_RAW || rawValue > MAX_UV_RAW) {
      throw new IOException("UV irradiance raw value out of range: " + rawValue);
    }
    return rawValue / 100.0f;
  }

  /**
   * UV Index (unitless).
   * Arduino: data1 = Data1[3] * 256 + Data1[4];
   */
  public int getUvIndex() throws IOException {
    int value = readSingleRegister(REGISTER_UV_INDEX);
    if (value < MIN_UVI || value > MAX_UVI) {
      throw new IOException("UVI value out of range: " + value);
    }
    return value;
  }

  /**
   * Read deviation value (W/m²) from register 0x0052.
   */
  public int getDeviation() throws IOException {
    return readSingleRegister(REGISTER_DEVIATION);
  }

  /**
   * Set deviation value (W/m²) in register 0x0052.
   */
  public void setDeviation(int deviation) throws IOException {
    if (deviation < MIN_SOLAR_VALUE || deviation > MAX_SOLAR_VALUE) {
      throw new IllegalArgumentException("Deviation must be between 0 and 1800 W/m²");
    }
    writeSingleRegister(REGISTER_DEVIATION, deviation);
  }

  /**
   * Read device address from register 0x07D0.
   */
  public int getDeviceAddressFromSensor() throws IOException {
    int value = readSingleRegister(REGISTER_DEVICE_ADDRESS);
    if (value < MIN_DEVICE_ADDRESS || value > MAX_DEVICE_ADDRESS) {
      throw new IOException("Device address out of range: " + value);
    }
    return value;
  }

  /**
   * Write device address to register 0x07D0 and update client-side address.
   */
  public void setDeviceAddressOnSensor(int newAddress) throws IOException {
    if (newAddress < MIN_DEVICE_ADDRESS || newAddress > MAX_DEVICE_ADDRESS) {
      throw new IllegalArgumentException("Device address must be between 1 and 254");
    }
    writeSingleRegister(REGISTER_DEVICE_ADDRESS, newAddress);
    this.deviceAddress = newAddress;
  }

  /**
   * Read current baud rate from register 0x07D1.
   */
  public BaudRate getBaudRate() throws IOException {
    int value = readSingleRegister(REGISTER_BAUD_RATE);
    return BaudRate.fromRegisterValue(value);
  }

  /**
   * Set baud rate in register 0x07D1.
   * Note: you must also reconfigure the underlying SerialPort after this call.
   */
  public void setBaudRate(BaudRate baudRate) throws IOException {
    writeSingleRegister(REGISTER_BAUD_RATE, baudRate.getRegisterValue());
  }

  // -------------------------------------------------------------------------
  // Low-level Modbus helpers
  // -------------------------------------------------------------------------

  private int readSingleRegister(int registerAddress) throws IOException {
    byte[] request = buildReadHoldingRegisterRequest(deviceAddress, registerAddress, 1);
    byte[] response = sendAndReceive(request, 7);

    validateCommonResponse(response, FUNCTION_READ_HOLDING_REGISTER);

    int byteCount = response[2] & 0xFF;
    if (byteCount != 2) {
      throw new IOException("Unexpected byte count in response: " + byteCount);
    }

    int highByte = response[3] & 0xFF;
    int lowByte = response[4] & 0xFF;
    return (highByte << 8) | lowByte;
  }

  private void writeSingleRegister(int registerAddress, int value) throws IOException {
    if ((value & 0xFFFF0000) != 0) {
      throw new IllegalArgumentException("Register value must be 16-bit");
    }

    byte[] request = buildWriteSingleRegisterRequest(deviceAddress, registerAddress, value);
    byte[] response = sendAndReceive(request, 8);

    validateCommonResponse(response, FUNCTION_WRITE_SINGLE_REGISTER);

    if (response[2] != request[2] || response[3] != request[3]
        || response[4] != request[4] || response[5] != request[5]) {
      throw new IOException("Write response does not echo register/value");
    }
  }

  private void validateCommonResponse(byte[] response, int expectedFunction) throws IOException {
    if (response.length < 5) {
      throw new IOException("Response too short");
    }
    if ((response[0] & 0xFF) != deviceAddress) {
      throw new IOException("Unexpected device address in response: " + (response[0] & 0xFF));
    }
    if ((response[1] & 0xFF) != expectedFunction) {
      throw new IOException("Unexpected function code in response: " + (response[1] & 0xFF));
    }
    if (!checkCrc(response)) {
      throw new IOException("CRC check failed for response");
    }
  }

  private byte[] sendAndReceive(byte[] request, int expectedResponseLength) throws IOException {
    if (!serialPort.isOpen()) {
      throw new IOException("Serial port is not open");
    }

    int bytesWritten = serialPort.writeBytes(request, request.length);
    if (bytesWritten != request.length) {
      throw new IOException("Failed to write full request frame, wrote " + bytesWritten);
    }

    byte[] buffer = new byte[expectedResponseLength];
    int bytesRead = 0;
    long deadline = System.nanoTime() + responseTimeout.toNanos();

    while (bytesRead < expectedResponseLength) {
      long now = System.nanoTime();
      if (now >= deadline) {
        throw new IOException("Timeout waiting for response from sensor");
      }

      int remaining = expectedResponseLength - bytesRead;

      int readCount = serialPort.readBytes(buffer, remaining);
      if (readCount < 0) {
        throw new IOException("Error reading from serial port, readBytes=" + readCount);
      }
      if (readCount == 0) {
        continue;
      }

      bytesRead += readCount;
    }

    return buffer;
  }

  private static byte[] buildReadHoldingRegisterRequest(int deviceAddress,
                                                        int startRegister,
                                                        int registerCount) {
    byte[] frame = new byte[8];
    frame[0] = (byte) deviceAddress;
    frame[1] = (byte) FUNCTION_READ_HOLDING_REGISTER;
    frame[2] = (byte) ((startRegister >> 8) & 0xFF);
    frame[3] = (byte) (startRegister & 0xFF);
    frame[4] = (byte) ((registerCount >> 8) & 0xFF);
    frame[5] = (byte) (registerCount & 0xFF);
    appendCrc(frame, 6);
    return frame;
  }

  private static byte[] buildWriteSingleRegisterRequest(int deviceAddress,
                                                        int registerAddress,
                                                        int value) {
    byte[] frame = new byte[8];
    frame[0] = (byte) deviceAddress;
    frame[1] = (byte) FUNCTION_WRITE_SINGLE_REGISTER;
    frame[2] = (byte) ((registerAddress >> 8) & 0xFF);
    frame[3] = (byte) (registerAddress & 0xFF);
    frame[4] = (byte) ((value >> 8) & 0xFF);
    frame[5] = (byte) (value & 0xFF);
    appendCrc(frame, 6);
    return frame;
  }

  // -------------------------------------------------------------------------
  // CRC16 (Modbus) helpers: polynomial 0xA001, low-byte first
  // -------------------------------------------------------------------------

  private static void appendCrc(byte[] frame, int lengthWithoutCrc) {
    int crcValue = computeCrc16(frame, 0, lengthWithoutCrc);
    frame[lengthWithoutCrc] = (byte) (crcValue & 0xFF);
    frame[lengthWithoutCrc + 1] = (byte) ((crcValue >> 8) & 0xFF);
  }

  private static boolean checkCrc(byte[] frame) {
    if (frame.length < 3) {
      return false;
    }
    int lengthWithoutCrc = frame.length - 2;
    int expectedCrc = computeCrc16(frame, 0, lengthWithoutCrc);
    int receivedCrc = (frame[lengthWithoutCrc] & 0xFF)
        | ((frame[lengthWithoutCrc + 1] & 0xFF) << 8);
    return expectedCrc == receivedCrc;
  }

  private static int computeCrc16(byte[] data, int offset, int length) {
    int crcValue = 0xFFFF;
    for (int index = 0; index < length; index++) {
      crcValue ^= (data[offset + index] & 0xFF);
      for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
        if ((crcValue & 0x0001) != 0) {
          crcValue = (crcValue >>> 1) ^ 0xA001;
        } else {
          crcValue = (crcValue >>> 1);
        }
      }
    }
    return crcValue & 0xFFFF;
  }

  // -------------------------------------------------------------------------
  // Baud rate enum mapped to register 0x07D1 values
  // -------------------------------------------------------------------------

  public enum BaudRate {
    BPS_2400(0, 2400),
    BPS_4800(1, 4800),
    BPS_9600(2, 9600);

    private final int registerValue;
    private final int bitsPerSecond;

    BaudRate(int registerValue, int bitsPerSecond) {
      this.registerValue = registerValue;
      this.bitsPerSecond = bitsPerSecond;
    }

    public int getRegisterValue() {
      return registerValue;
    }

    public int getBitsPerSecond() {
      return bitsPerSecond;
    }

    public static BaudRate fromRegisterValue(int value) {
      for (BaudRate baudRate : values()) {
        if (baudRate.registerValue == value) {
          return baudRate;
        }
      }
      throw new IllegalArgumentException("Unsupported baud rate register value: " + value);
    }
  }
}
