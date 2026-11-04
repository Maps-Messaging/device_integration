/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.onewire.devices.ds18b20;

import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.onewire.OneWireDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Getter
public class DS18B20Device extends OneWireDevice implements Sensor {

  private final List<SensorReading<?>> readings;
  private boolean initialized;
  private float myCurrent;
  private float myDif;
  private float myMin;
  private float myMax;

  public DS18B20Device(File path) {
    super(path);
    myCurrent = 0.0f;
    myMin = Float.MAX_VALUE;
    myMax = -Float.MAX_VALUE;
    FloatSensorReading current = new FloatSensorReading(
        "temperature",
        "°C",
        "Current temperature from DS18B20",
        25.0f,
        true,
        -55f,
        125f,
        2,
        this::getCurrent
    );

    FloatSensorReading min = new FloatSensorReading(
        "temperature_min",
        "°C",
        "Minimum temperature recorded",
        25.0f,
        true,
        -55f,
        125f,
        2,
        this::getMin
    );

    FloatSensorReading max = new FloatSensorReading(
        "temperature_max",
        "°C",
        "Maximum temperature recorded",
        25.0f,
        true,
        -55f,
        125f,
        2,
        this::getMax
    );

    FloatSensorReading delta = new FloatSensorReading(
        "temperature_delta",
        "°C",
        "Change in temperature since last update",
        0.0f,
        true,
        -180f,
        180f,
        2,
        this::getDif
    );

    this.readings = List.of(current, min, max, delta);

  }

  public synchronized float getCurrent() {
    ensureInitialized();
    return myCurrent;
  }

  public synchronized float getMin() {
    ensureInitialized();
    return myMin;
  }

  public synchronized float getMax() {
    ensureInitialized();
    return myMax;
  }

  public synchronized float getDif() {
    ensureInitialized();
    return myDif;
  }

  private void ensureInitialized() {
    if (!initialized) {
      throw new UncheckedIOException(new IOException("No valid DS18B20 temperature has been read"));
    }
  }

  @Override
  public synchronized void process(List<String> data) {
    try {
      processSample(data);
    } catch (IOException failure) {
      throw new UncheckedIOException("Unable to process DS18B20 sensor data", failure);
    }
  }

  private void processSample(List<String> data) throws IOException {
    if (data.size() < 2 || !data.get(0).trim().endsWith(" YES")) {
      throw new IOException("DS18B20 temperature CRC verification failed or sample is incomplete");
    }
    String sample = data.get(1);
    int position = sample.indexOf("t=");
    if (position < 0) {
      throw new IOException("DS18B20 sample is missing temperature");
    }
    float value;
    try {
      value = Float.parseFloat(sample.substring(position + 2).trim()) / 1000.0f;
    } catch (NumberFormatException invalid) {
      throw new IOException("DS18B20 temperature is malformed", invalid);
    }
    if (!Float.isFinite(value) || value < -55.0f || value > 125.0f) {
      throw new IOException("DS18B20 temperature is outside supported range: " + value);
    }
    myDif = initialized ? value - myCurrent : 0.0f;
    myCurrent = value;
    if (!initialized) {
      myMin = value;
      myMax = value;
    } else {
      myMin = Math.min(myMin, value);
      myMax = Math.max(myMax, value);
    }
    initialized = true;
  }
}
