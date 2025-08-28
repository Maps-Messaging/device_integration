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

package io.mapsmessaging.devices.i2c.devices.demo.impl.scd41;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.demo.SimulatedFloatValue;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.util.List;
import java.util.Random;

@Getter
public class Scd41Device extends I2CDevice implements Sensor {

  private final List<SensorReading<?>> readings;
  private final Random random;
  private final SimulatedFloatValue temperatureSim;
  private final SimulatedFloatValue humiditySim;
  private final SimulatedFloatValue co2Sim;



  @SuppressWarnings("java:S2245") // this is a demo, it is NOT for prod
  public Scd41Device(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(Scd41Device.class));
    random = new Random();
    if (random.nextBoolean()) {
      temperatureSim = new SimulatedFloatValue(18.0f, 24.0f, 21.0f, 0.1f);
      humiditySim = new SimulatedFloatValue(30.0f, 50.0f, 40.0f, 0.5f);
      co2Sim = new SimulatedFloatValue(400, 1400, 800, 5);
    } else {
      temperatureSim = new SimulatedFloatValue(-5.0f, 35.0f, 15.0f, 0.2f);
      humiditySim = new SimulatedFloatValue(20.0f, 80.0f, 50.0f, 1.0f);
      co2Sim = new SimulatedFloatValue(350, 500, 400, 2);
    }


    IntegerSensorReading co2Sensor = new IntegerSensorReading(
        "CO₂",
        "ppm",
        "Carbon dioxide concentration from SCD41 sensor",
        400,
        true,
        0,
        5000,
        this::getCo2
    );

    FloatSensorReading humidity = new FloatSensorReading(
        "humidity",
        "%RH",
        "Relative humidity from SCD41 sensor",
        50.0f,
        true,
        0f,
        100.0f,
        2,
        this::getHumidity
    );

    FloatSensorReading temperature = new FloatSensorReading(
        "temperature",
        "°C",
        "Ambient temperature from SCD41 sensor",
        25.0f,
        true,
        -10f,
        60.0f,
        2,
        this::getTemperature
    );

    StringSensorReading category = new StringSensorReading(
        "airQuality",
        "",
        "Air quality category based on CO₂ levels",
        "Good",
        true,
        this::getAirQuality
    );

    readings = List.of(co2Sensor, humidity, temperature, category);
  }

  private Float getTemperature() {
    return temperatureSim.next();
  }

  private Float getHumidity() {
    return humiditySim.next();
  }

  private Integer getCo2() {
    return Math.round(co2Sim.next());
  }

  private String getAirQuality() {
    int co2 = getCo2();
    if (co2 < 800) return "Good";
    if (co2 < 1200) return "Moderate";
    if (co2 < 1800) return "Poor";
    return "Very Poor";
  }

  @Override
  public String getName() {
    return "SCD41 Demo Device";
  }

  @Override
  public String getDescription() {
    return "CO₂ demo sensor reading";
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}