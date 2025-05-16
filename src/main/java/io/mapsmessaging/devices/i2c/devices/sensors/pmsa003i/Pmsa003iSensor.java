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

package io.mapsmessaging.devices.i2c.devices.sensors.pmsa003i;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.BufferedRegister;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

public class Pmsa003iSensor extends I2CDevice implements Sensor {
  private final byte[] data;
  private final BufferedRegister pm1_0StandardRegister;
  private final BufferedRegister pm2_5StandardRegister;
  private final BufferedRegister pm10StandardRegister;
  private final BufferedRegister pm1_0AtmosphericRegister;
  private final BufferedRegister pm2_5AtmosphericRegister;
  private final BufferedRegister pm10AtmosphericRegister;
  private final BufferedRegister particlesLargerThan3Register;
  private final BufferedRegister particlesLargerThan5Register;
  private final BufferedRegister particlesLargerThan10Register;
  private final BufferedRegister particlesLargerThan25Register;
  private final BufferedRegister particlesLargerThan50Register;
  private final BufferedRegister particlesLargerThan100Register;
  private final BufferedRegister versionRegister;
  private final BufferedRegister errorCodeRegister;
  @Getter
  private final List<SensorReading<?>> readings;
  private long lastRead;

  public Pmsa003iSensor(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(Pmsa003iSensor.class));
    data = new byte[0x20];

    this.pm1_0StandardRegister = new BufferedRegister(this, 4, 2, "Pm1_0Standard", data);
    this.pm2_5StandardRegister = new BufferedRegister(this, 6, 2, "Pm2_5Standard", data);
    this.pm10StandardRegister = new BufferedRegister(this, 8, 2, "Pm10Standard", data);

    this.pm1_0AtmosphericRegister = new BufferedRegister(this, 0xa, 2, "Pm1_0Atmospheric", data);
    this.pm2_5AtmosphericRegister = new BufferedRegister(this, 0xc, 2, "Pm2_5Atmospheric", data);
    this.pm10AtmosphericRegister = new BufferedRegister(this, 0xe, 2, "Pm10Atmospheric", data);

    this.particlesLargerThan3Register = new BufferedRegister(this, 0x10, 2, "ParticlesLargerThan3", data);
    this.particlesLargerThan5Register = new BufferedRegister(this, 0x12, 2, "ParticlesLargerThan5", data);
    this.particlesLargerThan10Register = new BufferedRegister(this, 0x14, 2, "ParticlesLargerThan10", data);
    this.particlesLargerThan25Register = new BufferedRegister(this, 0x16, 2, "ParticlesLargerThan25", data);
    this.particlesLargerThan50Register = new BufferedRegister(this, 0x18, 2, "ParticlesLargerThan50", data);
    this.particlesLargerThan100Register = new BufferedRegister(this, 0x1a, 2, "ParticlesLargerThan100", data);
    this.versionRegister = new BufferedRegister(this, 0x1c, 1, "Version", data);
    this.errorCodeRegister = new BufferedRegister(this, 0x1d, 1, "ErrorCode", data);
    lastRead = 0;
    readings = List.of(
        new IntegerSensorReading("pm_1_0", "µg/m³", "Standard PM1.0 concentration", 10, true, 0, 0x7ffff, this::getPm1_0Standard),
        new IntegerSensorReading("pm_2_5", "µg/m³", "Standard PM2.5 concentration", 15, true, 0, 0x7ffff, this::getPm2_5Standard),
        new IntegerSensorReading("pm_10", "µg/m³", "Standard PM10 concentration", 20, true, 0, 0x7ffff, this::getPm10Standard),

        new IntegerSensorReading("pm_1_0_atm", "µg/m³", "Atmospheric PM1.0 concentration", 10, true, 0, 0x7ffff, this::getPm1_0Atmospheric),
        new IntegerSensorReading("pm_2_5_atm", "µg/m³", "Atmospheric PM2.5 concentration", 15, true, 0, 0x7ffff, this::getPm2_5Atmospheric),
        new IntegerSensorReading("pm_10_atm", "µg/m³", "Atmospheric PM10 concentration", 20, true, 0, 0x7ffff, this::getPm10Atmospheric),

        new IntegerSensorReading("particles_gt_3", "count/0.1L", "Particles > 0.3μm per 0.1L air", 500, true, 0, 0x7ffff, this::getParticlesLargerThan3),
        new IntegerSensorReading("particles_gt_5", "count/0.1L", "Particles > 0.5μm per 0.1L air", 300, true, 0, 0x7ffff, this::getParticlesLargerThan5),
        new IntegerSensorReading("particles_gt_10", "count/0.1L", "Particles > 1.0μm per 0.1L air", 150, true, 0, 0x7ffff, this::getParticlesLargerThan10),
        new IntegerSensorReading("particles_gt_25", "count/0.1L", "Particles > 2.5μm per 0.1L air", 80, true, 0, 0x7ffff, this::getParticlesLargerThan25),
        new IntegerSensorReading("particles_gt_50", "count/0.1L", "Particles > 5.0μm per 0.1L air", 40, true, 0, 0x7ffff, this::getParticlesLargerThan50),
        new IntegerSensorReading("particles_gt_100", "count/0.1L", "Particles > 10μm per 0.1L air", 20, true, 0, 0x7ffff, this::getParticlesLargerThan100),

        new StringSensorReading("air_quality", "", "Evaluated air quality classification", "Good", true, this::evaluateAirQuality)
    );
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  protected int getPm1_0Standard() throws IOException {
    update();
    return pm1_0StandardRegister.getValueReverse();
  }

  protected int getPm2_5Standard() throws IOException {
    update();
    return pm2_5StandardRegister.getValueReverse();
  }

  protected int getPm10Standard() throws IOException {
    update();
    return pm10StandardRegister.getValueReverse();
  }

  protected int getPm1_0Atmospheric() throws IOException {
    update();
    return pm1_0AtmosphericRegister.getValueReverse();
  }

  protected int getPm2_5Atmospheric() throws IOException {
    update();
    return pm2_5AtmosphericRegister.getValueReverse();
  }

  protected int getPm10Atmospheric() throws IOException {
    update();
    return pm10AtmosphericRegister.getValueReverse();
  }

  protected int getParticlesLargerThan3() throws IOException {
    update();
    return particlesLargerThan3Register.getValueReverse();
  }

  protected int getParticlesLargerThan5() throws IOException {
    update();
    return particlesLargerThan5Register.getValueReverse();
  }

  protected int getParticlesLargerThan10() throws IOException {
    update();
    return particlesLargerThan10Register.getValueReverse();
  }

  protected int getParticlesLargerThan25() throws IOException {
    update();
    return particlesLargerThan25Register.getValueReverse();
  }

  protected int getParticlesLargerThan50() throws IOException {
    update();
    return particlesLargerThan50Register.getValueReverse();
  }

  protected int getParticlesLargerThan100() throws IOException {
    update();
    return particlesLargerThan100Register.getValueReverse();
  }

  public int getVersion() throws IOException {
    update();
    return versionRegister.getValueReverse();
  }

  protected int getErrorCode() throws IOException {
    update();
    return errorCodeRegister.getValueReverse();
  }

  protected void update() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      readRegister(0, data, 0, data.length);
      lastRead = System.currentTimeMillis() + 1000;
    }
  }


  public String evaluateAirQuality() throws IOException {
    // Air quality thresholds for PM1.0, PM2.5, and PM10
    int[] pristineThreshold = {10, 10, 20};     // PM1.0, PM2.5, PM10
    int[] healthyThreshold = {20, 30, 50};
    int[] moderateThreshold = {30, 60, 100};
    int[] poorThreshold = {40, 100, 150};

    int pm1 = getPm1_0Standard();
    int pm25 = getPm2_5Standard();
    int pm10 = getPm10Standard();

    // Evaluate air quality
    if (pm1 <= pristineThreshold[0] && pm25 <= pristineThreshold[1] && pm10 <= pristineThreshold[2]) {
      return "Pristine";
    } else if (pm1 <= healthyThreshold[0] && pm25 <= healthyThreshold[1] && pm10 <= healthyThreshold[2]) {
      return "Healthy";
    } else if (pm1 <= moderateThreshold[0] && pm25 <= moderateThreshold[1] && pm10 <= moderateThreshold[2]) {
      return "Moderate";
    } else if (pm1 <= poorThreshold[0] && pm25 <= poorThreshold[1] && pm10 <= poorThreshold[2]) {
      return "Poor";
    } else {
      return "Hazardous";
    }
  }


  @Override
  public String getName() {
    return "PMSA003I";
  }

  @Override
  public String getDescription() {
    return "Air Quality Breakout";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}