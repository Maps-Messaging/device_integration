/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.util;

import java.text.DecimalFormat;

public class Constants {

  public static final float EARTH_GRAVITY_FLOAT = 9.80665f;   // m/s^2

  public static String roundFloatToString(float number, int decimalPlaces) {
    // Create the pattern for DecimalFormat based on the specified decimalPlaces
    StringBuilder patternBuilder = new StringBuilder("#.");
    for (int i = 0; i < decimalPlaces; i++) {
      patternBuilder.append("#");
    }
    DecimalFormat decimalFormat = new DecimalFormat(patternBuilder.toString());

    // Format the number and return the rounded string representation
    return decimalFormat.format(number);
  }
}
