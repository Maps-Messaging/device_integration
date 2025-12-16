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

package io.mapsmessaging.devices.serial.devices.sensors.sen0657;

import io.mapsmessaging.devices.Device;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.sensorreadings.*;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import io.mapsmessaging.devices.util.AccumulatingCounterDelta;
import io.mapsmessaging.devices.util.RollingBucketAccumulator;
import io.mapsmessaging.devices.util.RollingComputations;
import io.mapsmessaging.devices.util.SensorReadingAugmentor;
import lombok.Getter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static io.mapsmessaging.devices.util.StormHeuristics.*;

public class Sen0657Sensor implements Device, Sensor {

  private static final int FUNCTION_READ_HOLDING_REGISTER = 0x03;

  private static final int REGISTER_WIND_START = 0x01F4;
  private static final int REGISTER_WIND_COUNT = 4;
  private static final int REGISTER_TEMP_HUM_START = 0x01F8;
  private static final int REGISTER_TEMP_HUM_COUNT = 2;
  private static final int REGISTER_PRESSURE_LIGHT_START = 0x01FD;
  private static final int REGISTER_PRESSURE_LIGHT_COUNT = 3;
  private static final int REGISTER_RAINFALL_START = 0x0201;
  private static final int REGISTER_RAINFALL_COUNT = 1;

  private final SerialDevice serialPort;
  private int deviceAddress = 0x01;
  private Duration responseTimeout = Duration.ofMillis(500);

  private long nextReadCycle;
  private int windDirectionCode;
  private int windDirectionAngleDegrees;
  private float atmosphericPressureHpa;
  private float rainfallMillimeters;
  private float temperatureCelsius;
  private float humidityRelativePercent;
  private float windSpeedMetersPerSecond;
  private long lightIntensityLux;

  @Getter
  private final List<SensorReading<?>> readings;

  public Sen0657Sensor(SerialDevice serialPort) throws IOException {
    this.serialPort = serialPort;
    open();
    nextReadCycle = 0;

    List<SensorReading<?>>  physicalList = List.of(
        new FloatSensorReading(
            "pressure",
            "hPa",
            "Atmospheric pressure at sensor height",
            1013f,
            true,
            0.0f,
            1200.0f,
            1,
            this::getAtmosphericPressureHpa
        ),
        new FloatSensorReading(
            "rainfall",
            "mm",
            "Accumulated rainfall since last reset",
            0.0f,
            true,
            0.0f,
            500.0f,
            1,
            this::getRainfallMillimeters
        ),
        new FloatSensorReading(
            "temperature",
            "°C",
            "Ambient air temperature",
            22.5f,
            true,
            -40.0f,
            80.0f,
            1,
            this::getTemperatureCelsius
        ),
        new FloatSensorReading(
            "humidity",
            "%RH",
            "Relative humidity",
            55.0f,
            true,
            0.0f,
            100.0f,
            1,
            this::getHumidityRelativePercent
        ),
        new FloatSensorReading(
            "windspeed",
            "m/s",
            "Horizontal wind speed",
            3.4f,
            true,
            0.0f,
            40.0f,
            1,
            this::getWindSpeedMetersPerSecond
        ),
        new LongSensorReading(
            "lux",
            "lx",
            "Ambient light level measured by the weather station",
            5000L,
            true,
            0L,
            200_000L,
            this::getLightIntensityLux
        ),
        new IntegerSensorReading(
            "windDirection",
            "",
            "Raw wind direction code from the weather station",
            0,
            true,
            0,
            15,
            this::getWindDirectionCode
        ),

        new IntegerSensorReading(
            "windDirectionAngle",
            "°",
            "Wind direction in degrees clockwise from North",
            0,
            true,
            0,
            359,
            this::getWindDirectionAngleDegrees
        ),
        new StatefulFloatSensorReading(
            "windGust",
            "m/s",
            "Wind gust (max windspeed over last 10 seconds)",
            0.0f,
            false,
            0.0f,
            60.0f,
            1,
            this::getWindSpeedMetersPerSecond,
            50,
            10_000L,
            RollingComputations.max()
        )
    );

    readings =  addAugmented(generateSensorReadings(physicalList));
  }

  private List<SensorReading<?>> addAugmented( List<SensorReading<?>> physical){
    if(SensorReadingAugmentor.isENABLE_COMPUTED_READINGS()) {
      AccumulatingCounterDelta rainfallDelta = new AccumulatingCounterDelta();

      RollingBucketAccumulator rain10Min = new RollingBucketAccumulator(10L * 60_000L, 5_000L);        // 5s buckets
      RollingBucketAccumulator rain1Hour = new RollingBucketAccumulator(60L * 60_000L, 60_000L);       // 1m buckets
      RollingBucketAccumulator rain24Hour = new RollingBucketAccumulator(24L * 60L * 60_000L, 60_000L);// 1m buckets

      ReadingSupplier<Float> rainUpdateAndReturn10Min = () -> {
        long now = System.currentTimeMillis();
        float total = getRainfallMillimeters();
        float delta = rainfallDelta.computeDelta(total);

        if (!Float.isNaN(delta)) {
          rain10Min.add(now, delta);
          rain1Hour.add(now, delta);
          rain24Hour.add(now, delta);
        }

        return rain10Min.getSum();
      };


      StatefulFloatSensorReading pressureTrendHpaPerHour = new StatefulFloatSensorReading(
          "pressureTrendHpaPerHour",
          "hPa/h",
          "Pressure trend (least-squares) over the last 3 hours",
          0.0f,
          false,
          -5.0f,
          5.0f,
          3,
          this::getAtmosphericPressureHpa,
          180,              // up to 180 samples
          3L * 3_600_000L,  // 3 hours
          RollingComputations.slopeLeastSquaresPerHour()
      );

      StatefulFloatSensorReading pressureTrendHpaPer3Hours = new StatefulFloatSensorReading(
          "pressureTrendHpaPer3Hours",
          "hPa/3h",
          "Pressure trend converted to hPa per 3 hours",
          0.0f,
          false,
          -10.0f,
          10.0f,
          3,
          () -> pressureTrendHpaPerHour.getSupplier().get(),
          1,
          1_000L,
          samples -> {
            TimedFloatSample last = null;
            for (TimedFloatSample s : samples) {
              last = s;
            }
            if (last == null) {
              return Float.NaN;
            }
            return toHpaPer3Hours(last.value());
          }
      );
      List<SensorReading<?>> tmp = List.of(
          pressureTrendHpaPerHour,
          pressureTrendHpaPer3Hours,
          new StringSensorReading(
              "pressureTendency",
              "",
              "Pressure tendency category derived from pressure trend",
              "Unknown",
              false,
              () -> describePressureTendency(
                  pressureTrendHpaPer3Hours.getSupplier().get()
              )
          ),
          new BooleanSensorReading(
              "stormWarning",
              "",
              "Storm warning flag derived from low pressure and rapid pressure fall",
              true,
              true,
              () -> stormWarning(
                  this.getAtmosphericPressureHpa(),
                  pressureTrendHpaPer3Hours.getSupplier().get()
              )
          ),
          new FloatSensorReading(
              "stormRisk",
              "",
              "Storm risk score (0..1) derived from pressure and trend",
              0.0f,
              true,
              0.0f,
              1.0f,
              2,
              () -> stormRisk(getAtmosphericPressureHpa(), pressureTrendHpaPer3Hours.getSupplier().get())
          ),

          new StatefulFloatSensorReading(
              "rainLast10Minutes",
              "mm",
              "Accumulated rainfall over the last 10 minutes",
              0.0f,
              false,
              0.0f,
              100.0f,
              1,
              this::getRainfallMillimeters,
              600,              // if you sample ~1 Hz
              10L * 60_000L,
              RollingComputations.deltaFirstToLastNonNegative()
          ),

          new StatefulFloatSensorReading(
              "rainLast1Hour",
              "mm",
              "Accumulated rainfall over the last 1 hour",
              0.0f,
              false,
              0.0f,
              500.0f,
              1,
              this::getRainfallMillimeters,
              3_600,            // ~1 Hz
              60L * 60_000L,
              RollingComputations.deltaFirstToLastNonNegative()
          ),

          new StatefulFloatSensorReading(
              "rainLast24Hours",
              "mm",
              "Accumulated rainfall over the last 24 hours",
              0.0f,
              false,
              0.0f,
              2_000.0f,
              1,
              this::getRainfallMillimeters,
              86_400,           // ~1 Hz, yes it’s a lot, tune this if needed
              24L * 60L * 60_000L,
              RollingComputations.deltaFirstToLastNonNegative()
          ),
          new StatefulFloatSensorReading(
              "rainRate",
              "mm/h",
              "Rain rate derived from rainfall accumulator (last 10 minutes)",
              0.0f,
              false,
              0.0f,
              500.0f,
              1,
              this::getRainfallMillimeters,
              600,
              10L * 60_000L,
              RollingComputations.ratePerHourFirstToLastNonNegative()
          ),
          new FloatSensorReading(
              "rainLast10Minutes",
              "mm",
              "Accumulated rainfall over the last 10 minutes",
              0.0f,
              false,
              0.0f,
              200.0f,
              1,
              rainUpdateAndReturn10Min
          ),
          new FloatSensorReading(
              "rainLast1Hour",
              "mm",
              "Accumulated rainfall over the last 1 hour",
              0.0f,
              false,
              0.0f,
              500.0f,
              1,
              () -> {
                rainUpdateAndReturn10Min.get();
                return rain1Hour.getSum();
              }
          ),
          new FloatSensorReading(
              "rainLast24Hours",
              "mm",
              "Accumulated rainfall over the last 24 hours",
              0.0f,
              false,
              0.0f,
              2_000.0f,
              1,
              () -> {
                rainUpdateAndReturn10Min.get();
                return rain24Hour.getSum();
              }
          ),
          new FloatSensorReading(
              "rainRate",
              "mm/h",
              "Rain rate derived from last 10 minutes total",
              0.0f,
              false,
              0.0f,
              500.0f,
              1,
              () -> {
                float mm10 = rainUpdateAndReturn10Min.get();
                if (Float.isNaN(mm10)) {
                  return Float.NaN;
                }
                return (mm10 / 10.0f) * 60.0f;
              }
          )
      );
      physical.addAll(tmp);
    }
    return physical;
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

  public void setDeviceAddressInClient(int deviceAddress) {
    if (deviceAddress < 1 || deviceAddress > 254) {
      throw new IllegalArgumentException("Device address must be between 1 and 254");
    }
    this.deviceAddress = deviceAddress;
  }

  public void setResponseTimeout(Duration responseTimeout) {
    this.responseTimeout = Objects.requireNonNull(responseTimeout, "responseTimeout");
  }

  public long getLightIntensityLux() {
    readSensors();
    return lightIntensityLux;
  }

  public float getWindSpeedMetersPerSecond() {
    readSensors();
    return windSpeedMetersPerSecond;
  }

  public float getHumidityRelativePercent() {
    readSensors();
    return humidityRelativePercent;
  }

  public float getTemperatureCelsius() {
    readSensors();
    return temperatureCelsius;
  }

  public int getWindDirectionCode() {
    readSensors();
    return windDirectionCode;
  }

  public int getWindDirectionAngleDegrees() {
    readSensors();
    return windDirectionAngleDegrees;
  }

  public float getAtmosphericPressureHpa() {
    readSensors();
    return atmosphericPressureHpa;
  }

  public float getRainfallMillimeters() {
    readSensors();
    return rainfallMillimeters;
  }

  private void readSensors() {
    if (nextReadCycle > System.currentTimeMillis()) {
      return;
    }
    nextReadCycle = System.currentTimeMillis() + 1000;
    try {
      readTemperatureAndHumidity();
      readAtmosphericPressureAndLight();
      readWindSpeedAndDirection();
      readRainfall();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  // Public read operations (mirror Arduino functions)
  // --------------------------------------------------------------------------

  public void readTemperatureAndHumidity() throws IOException {
    // response: addr, func, 0x04, 4 data bytes, 2 crc => 9 bytes
    byte[] response = sendAndReceive(REGISTER_TEMP_HUM_START, REGISTER_TEMP_HUM_COUNT, 9);

    validateHeader(response, REGISTER_TEMP_HUM_COUNT);

    int humidityRaw = getU16(response, 3);
    int temperatureRaw = getU16(response, 5);

    humidityRelativePercent = humidityRaw / 10.0f;
    temperatureCelsius = temperatureRaw / 10.0f;
  }

  public void readAtmosphericPressureAndLight() throws IOException {
    // response: addr, func, 0x06, 6 data bytes, 2 crc => 11 bytes
    byte[] response = sendAndReceive(REGISTER_PRESSURE_LIGHT_START, REGISTER_PRESSURE_LIGHT_COUNT, 11);

    validateHeader(response, REGISTER_PRESSURE_LIGHT_COUNT);

    int pressureRaw = getU16(response, 3);
    atmosphericPressureHpa = pressureRaw / 1.0f;

    long b0 = response[5] & 0xFFL;
    long b1 = response[6] & 0xFFL;
    long b2 = response[7] & 0xFFL;
    long b3 = response[8] & 0xFFL;
    lightIntensityLux = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
  }

  public void readRainfall() throws IOException {
    // response: addr, func, 0x02, 2 data bytes, 2 crc => 7 bytes
    byte[] response = sendAndReceive(REGISTER_RAINFALL_START, REGISTER_RAINFALL_COUNT, 7);

    validateHeader(response, REGISTER_RAINFALL_COUNT);

    int rainfallRaw = getU16(response, 3);
    rainfallMillimeters = rainfallRaw / 10.0f;
  }

  public void readWindSpeedAndDirection() throws IOException {
    // response: addr, func, 0x08, 8 data bytes, 2 crc => 13 bytes
    byte[] response = sendAndReceive(REGISTER_WIND_START, REGISTER_WIND_COUNT, 13);

    validateHeader(response, REGISTER_WIND_COUNT);

    int windSpeedRaw = getU16(response, 3);
    windSpeedMetersPerSecond = windSpeedRaw / 100.0f;

    windDirectionCode = getU16(response, 7);
    windDirectionAngleDegrees = getU16(response, 9);
  }


  // --------------------------------------------------------------------------
  // Modbus helpers
  // --------------------------------------------------------------------------

  private byte[] sendAndReceive(int startRegister,
                                int registerCount,
                                int expectedResponseLength) throws IOException {
    if (!serialPort.isOpen()) {
      throw new IOException("Serial port is not open");
    }

    byte[] request = buildReadHoldingRegisterRequest(deviceAddress, startRegister, registerCount);

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

    if (!checkCrc(buffer)) {
      throw new IOException("CRC check failed for response");
    }

    if ((buffer[0] & 0xFF) != deviceAddress) {
      throw new IOException("Unexpected device address in response: " + (buffer[0] & 0xFF));
    }
    if ((buffer[1] & 0xFF) != FUNCTION_READ_HOLDING_REGISTER) {
      throw new IOException("Unexpected function code in response: " + (buffer[1] & 0xFF));
    }

    return buffer;
  }

  private void validateHeader(byte[] response, int registerCount) throws IOException {
    int byteCount = response[2] & 0xFF;
    int expectedByteCount = registerCount * 2;
    if (byteCount != expectedByteCount) {
      throw new IOException("Unexpected byte count in response: " + byteCount
          + ", expected " + expectedByteCount);
    }
  }

  private static int getU16(byte[] data, int offset) {
    int highByte = data[offset] & 0xFF;
    int lowByte = data[offset + 1] & 0xFF;
    return (highByte << 8) | lowByte;
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
    appendCrc(frame);
    return frame;
  }

  private static void appendCrc(byte[] frame) {
    int crcValue = computeCrc16(frame, 6);
    frame[6] = (byte) (crcValue & 0xFF);
    frame[6 + 1] = (byte) ((crcValue >> 8) & 0xFF);
  }

  private static boolean checkCrc(byte[] frame) {
    if (frame.length < 3) {
      return false;
    }
    int lengthWithoutCrc = frame.length - 2;
    int expectedCrc = computeCrc16(frame, lengthWithoutCrc);
    int receivedCrc = (frame[lengthWithoutCrc] & 0xFF)
        | ((frame[lengthWithoutCrc + 1] & 0xFF) << 8);
    return expectedCrc == receivedCrc;
  }

  private static int computeCrc16(byte[] data, int length) {
    int crcValue = 0xFFFF;
    for (int index = 0; index < length; index++) {
      crcValue ^= (data[index] & 0xFF);
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

  @Override
  public String getName() {
    return "Sen0657";
  }

  @Override
  public String getDescription() {
    return "Ultrasonic 7-in-1 RS485 Weather Sensor";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }
}
