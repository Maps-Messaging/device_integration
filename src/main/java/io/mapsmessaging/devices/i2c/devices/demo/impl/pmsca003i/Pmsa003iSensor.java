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

package io.mapsmessaging.devices.i2c.devices.demo.impl.pmsca003i;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.demo.SimulatedIntValue;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

public class Pmsa003iSensor extends I2CDevice implements Sensor {
  @Getter
  private final List<SensorReading<?>> readings;
  private final SimulatedIntValue pm1_0;
  private final SimulatedIntValue pm2_5;
  private final SimulatedIntValue  pm10;
  private final SimulatedIntValue pm1_0_atm;
  private final SimulatedIntValue pm2_5_atm;
  private final SimulatedIntValue pm10_atm;
  private final SimulatedIntValue p3;
  private final SimulatedIntValue p5;
  private final SimulatedIntValue p10;
  private final SimulatedIntValue p25;
  private final SimulatedIntValue p50;
  private final SimulatedIntValue p100;


  public Pmsa003iSensor(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(Pmsa003iSensor.class));
    pm1_0 = new SimulatedIntValue(5, 35, 10, 1);
    pm2_5 = new SimulatedIntValue(10, 60, 15, 1);
    pm10 = new SimulatedIntValue(15, 80, 20, 1);

    pm1_0_atm = new SimulatedIntValue(5, 35, 10, 1);
    pm2_5_atm = new SimulatedIntValue(10, 60, 15, 1);
    pm10_atm = new SimulatedIntValue(15, 80, 20, 1);

    p3 = new SimulatedIntValue(300, 1000, 500, 10);
    p5 = new SimulatedIntValue(200, 700, 300, 8);
    p10 = new SimulatedIntValue(100, 400, 150, 5);
    p25 = new SimulatedIntValue(50, 200, 80, 4);
    p50 = new SimulatedIntValue(20, 100, 40, 2);
    p100 = new SimulatedIntValue(5, 40, 20, 1);

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

  protected int getPm1_0Standard() { return pm1_0.next(); }
  protected int getPm2_5Standard() { return pm2_5.next(); }
  protected int getPm10Standard() { return pm10.next(); }

  protected int getPm1_0Atmospheric() { return pm1_0_atm.next(); }
  protected int getPm2_5Atmospheric() { return pm2_5_atm.next(); }
  protected int getPm10Atmospheric() { return pm10_atm.next(); }

  protected int getParticlesLargerThan3() { return p3.next(); }
  protected int getParticlesLargerThan5() { return p5.next(); }
  protected int getParticlesLargerThan10() { return p10.next(); }
  protected int getParticlesLargerThan25() { return p25.next(); }
  protected int getParticlesLargerThan50() { return p50.next(); }
  protected int getParticlesLargerThan100() { return p100.next(); }

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
    return "Demo PMSA003I";
  }

  @Override
  public String getDescription() {
    return "Demo Air Quality Breakout";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}