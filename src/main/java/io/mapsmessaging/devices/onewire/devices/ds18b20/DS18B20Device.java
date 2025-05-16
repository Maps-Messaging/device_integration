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

package io.mapsmessaging.devices.onewire.devices.ds18b20;

import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.onewire.OneWireDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import lombok.Getter;

import java.io.File;
import java.util.List;

@Getter
public class DS18B20Device extends OneWireDevice implements Sensor {

  private final List<SensorReading<?>> readings;
  private float myCurrent;
  private float myDif;
  private float myMin;
  private float myMax;

  public DS18B20Device(File path) {
    super(path);
    myCurrent = 0.0f;
    myMin = Float.MAX_VALUE;
    myMax = Float.MIN_VALUE;
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

  public float getCurrent() {
    return myCurrent;
  }

  public float getMin() {
    return myMin;
  }

  public float getMax() {
    return myMax;
  }

  public float getDif() {
    return myDif;
  }

  public void process(List<String> data) {
    for (String aData : data) {
      int pos = aData.indexOf("t=");
      if (pos > 0) {
        String tmp = aData.substring(pos + 2);
        if (tmp.length() > 0) {
          updateData(tmp);
        }
      }
    }
  }

  private void updateData(String val) {
    float fVal = Float.parseFloat(val) / 1000.0f;
    if (fVal != myCurrent) {
      myDif = fVal - myCurrent;
      if (myDif == fVal) myDif = 0.0f;
      myCurrent = fVal;
      if (myMax < myCurrent) myMax = myCurrent;
      if (myMin > myCurrent) myMin = myCurrent;
    }
  }
}
