/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.ina219;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.ina219.registers.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

@Getter
public class Ina219Sensor extends I2CDevice implements Sensor {

  private static final double CURRENT_LSB = 0.0001; // A per bit
  private static final double POWER_LSB = CURRENT_LSB * 20; // W per bit

  @Getter
  private final List<SensorReading<?>> readings;
  @Setter
  private ADCResolution adcResolution;
  @Getter
  @Setter
  private BusVoltageRange busVoltageRange;
  @Getter
  @Setter
  private GainMask gainMask;
  @Getter
  @Setter
  private OperatingMode operatingMode;
  @Getter
  @Setter
  private ShuntADCResolution shuntADCResolution;

  public Ina219Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Ina219Sensor.class));
    adcResolution = ADCResolution.RES_12BIT;
    busVoltageRange = BusVoltageRange.RANGE_32V;
    gainMask = GainMask.GAIN_8_320MV;
    operatingMode = OperatingMode.BVOLT_CONTINUOUS;
    shuntADCResolution = ShuntADCResolution.RES_12BIT_1S_532US;
    FloatSensorReading busVoltage = new FloatSensorReading(
        "bus_voltage",
        "mV",
        "Bus voltage measured by INA219",
        12000.0f,
        true,
        0f,
        32000f,
        0,
        () -> (float) getBusVoltage()
    );

    FloatSensorReading shuntVoltage = new FloatSensorReading(
        "shunt_voltage",
        "mV",
        "Voltage across the shunt resistor",
        2.5f,
        true,
        -320f,
        320f,
        2,
        () -> (float) getShuntVoltage()
    );

    FloatSensorReading current = new FloatSensorReading(
        "current",
        "mA",
        "Measured current",
        100.0f,
        true,
        -32768f,
        32767f,
        1,
        this::getCurrent_mA
    );

    FloatSensorReading power = new FloatSensorReading(
        "power",
        "mW",
        "Power calculated by INA219",
        500f,
        true,
        0f,
        65535f,
        2,
        this::getPower
    );

    FloatSensorReading supplyVoltage = new FloatSensorReading(
        "supply_voltage",
        "mV",
        "Estimated supply voltage (bus + shunt)",
        12000f,
        true,
        0f,
        33000f,
        0,
        () -> (float) (getBusVoltage() + getShuntVoltage())
    );

    FloatSensorReading loadResistance = new FloatSensorReading(
        "load_resistance",
        "Ohm",
        "Estimated resistance of the load",
        100f,
        true,
        0f,
        1000f,
        2,
        () -> {
          float currentA = getCurrent_mA() / 1000f;
          float vBus = getBusVoltage() / 1000f;
          return currentA > 0.0001f ? vBus / currentA : Float.NaN;
        }
    );

    FloatSensorReading powerCalc = new FloatSensorReading(
        "power_calculated",
        "mW",
        "Calculated power from V × I",
        500f,
        true,
        0f,
        65535f,
        2,
        () -> {
          float currentA = getCurrent_mA() / 1000f;
          float vBus = getBusVoltage() / 1000f;
          return vBus * currentA * 1000f;
        }
    );

    FloatSensorReading powerDelta = new FloatSensorReading(
        "power_delta",
        "mW",
        "Difference between calculated and measured power",
        50f,
        true,
        0f,
        500f,
        2,
        () -> {
          float calculated = getBusVoltage() / 1000f * getCurrent_mA();
          float measured = getPower();
          return Math.abs(calculated - measured);
        }
    );

    FloatSensorReading percentUsage = new FloatSensorReading(
        "percent_usage",
        "%",
        "Load as percent of max expected power (24W)",
        100f,
        true,
        0f,
        100f,
        1,
        () -> {
          float p = getPower();
          return Math.min(100f, p / 24000f * 100f);
        }
    );

    this.readings = generateSensorReadings(
        List.of(
            busVoltage,
            shuntVoltage,
            current,
            power,
            supplyVoltage,
            powerCalc,
            powerDelta,
            percentUsage,
            loadResistance
        )
    );
  }

  public void initialize() throws IOException {
    configure();
    setCalibration();
  }

  public void configure() throws IOException {
    int config =
        busVoltageRange.getValue() << 13 |
            gainMask.getValue() << 11 |
            adcResolution.getValue() << 7 |
            shuntADCResolution.getValue() << 3 |
            operatingMode.getValue(); // assumed already correct
    writeDevice(Registers.CONFIGURATION, config);
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public float getCurrent_mA() throws IOException {
    return (float) (getCurrent() * CURRENT_LSB * 1000); // to mA
  }

  public float getPower() throws IOException {
    return (float) (readDevice(Registers.POWER) * POWER_LSB * 1000); // to mW
  }

  public int getBusVoltage() throws IOException {
    int value = readDevice(Registers.BUS_VOLTAGE);
    value = value >> 3;
    return value;
  }

  public int getCurrent() throws IOException {
    return readDevice(Registers.CURRENT);
  }

  public int getShuntVoltageRaw() throws IOException {
    return readDevice(Registers.SHUNT_VOLTAGE);
  }

  public double getShuntVoltage() throws IOException {
    int rawValue = getShuntVoltageRaw();
    return rawValue * 0.01;
  }

  public void setCalibration() throws IOException {
    double currentLsb = 0.0001;      // 100 µA per bit
    double shuntResistor = 0.1;      // 0.1 ohm
    int calibration = calculateCalibration(currentLsb, shuntResistor);
    writeDevice(Registers.CALIBRATION, calibration);
  }

  private int calculateCalibration(double currentLsb, double shuntResistanceOhms) {
    return (int) (0.04096 / (currentLsb * shuntResistanceOhms));
  }

  private int readDevice(Registers register) throws IOException {
    byte[] buf = new byte[2];
    readRegister(register.getAddress(), buf, 0, 2);
    return (buf[0] & 0xff) << 8 | (buf[1] & 0xff);
  }

  private void writeDevice(Registers register, int data) throws IOException {
    byte[] buf = new byte[2];
    buf[0] = (byte) ((data >> 8) & 0xff);
    buf[1] = (byte) (data & 0xff);
    write(register.getAddress(), buf);
  }

  @Override
  public String getName() {
    return "INA219";
  }

  @Override
  public String getDescription() {
    return "Zero-Drift, Bidirectional Current/Power Monitor";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}