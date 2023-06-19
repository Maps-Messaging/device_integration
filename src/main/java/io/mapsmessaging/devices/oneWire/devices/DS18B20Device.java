/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.oneWire.devices;

import io.mapsmessaging.devices.oneWire.OneWireDevice;
import lombok.Getter;

import java.io.File;
import java.util.List;

public class DS18B20Device extends OneWireDevice {

  @Getter
  private float myCurrent;
  @Getter
  private float myDif;
  @Getter
  private float myMin;
  @Getter
  private float myMax;

  public DS18B20Device(File path) {
    super(path);
    myCurrent = 0.0f;
    myMin = Float.MAX_VALUE;
    myMax = Float.MIN_VALUE;
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
