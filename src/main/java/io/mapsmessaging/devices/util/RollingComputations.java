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

package io.mapsmessaging.devices.util;


import io.mapsmessaging.devices.sensorreadings.TimedFloatSample;

public class RollingComputations {

  private RollingComputations() {
  }

  public static RollingComputation deltaFirstToLastNonNegative() {
    return samples -> {
      TimedFloatSample first = null;
      TimedFloatSample last = null;

      for (TimedFloatSample sample : samples) {
        if (first == null) {
          first = sample;
        }
        last = sample;
      }

      if (first == null || last == null) {
        return Float.NaN;
      }

      float delta = last.value() - first.value();
      if (delta < 0.0f) {
        return Float.NaN;
      }
      return delta;
    };
  }

  public static RollingComputation ratePerHourFirstToLastNonNegative() {
    return samples -> {
      TimedFloatSample first = null;
      TimedFloatSample last = null;

      for (TimedFloatSample sample : samples) {
        if (first == null) {
          first = sample;
        }
        last = sample;
      }

      if (first == null || last == null) {
        return Float.NaN;
      }

      long dtMillis = last.epochMillis() - first.epochMillis();
      if (dtMillis <= 0L) {
        return Float.NaN;
      }

      float delta = last.value() - first.value();
      if (delta < 0.0f) {
        return Float.NaN;
      }

      double dtHours = (double) dtMillis / 3_600_000.0;
      return (float) (delta / dtHours);
    };
  }


  public static RollingComputation average() {
    return samples -> {
      double sum = 0.0;
      long count = 0L;
      for (TimedFloatSample sample : samples) {
        sum += sample.value();
        count++;
      }
      if (count == 0L) {
        return Float.NaN;
      }
      return (float) (sum / (double) count);
    };
  }

  public static RollingComputation min() {
    return samples -> {
      float min = Float.POSITIVE_INFINITY;
      boolean has = false;
      for (TimedFloatSample sample : samples) {
        has = true;
        if (sample.value() < min) {
          min = sample.value();
        }
      }
      return has ? min : Float.NaN;
    };
  }

  public static RollingComputation max() {
    return samples -> {
      float max = Float.NEGATIVE_INFINITY;
      boolean has = false;
      for (TimedFloatSample sample : samples) {
        has = true;
        if (sample.value() > max) {
          max = sample.value();
        }
      }
      return has ? max : Float.NaN;
    };
  }

  public static RollingComputation deltaFirstToLast() {
    return samples -> {
      TimedFloatSample first = null;
      TimedFloatSample last = null;
      for (TimedFloatSample sample : samples) {
        if (first == null) {
          first = sample;
        }
        last = sample;
      }
      if (first == null || last == null) {
        return Float.NaN;
      }
      return last.value() - first.value();
    };
  }

  public static RollingComputation ratePerHourFirstToLast() {
    return samples -> {
      TimedFloatSample first = null;
      TimedFloatSample last = null;
      for (TimedFloatSample sample : samples) {
        if (first == null) {
          first = sample;
        }
        last = sample;
      }
      if (first == null || last == null) {
        return Float.NaN;
      }
      long dtMillis = last.epochMillis() - first.epochMillis();
      if (dtMillis <= 0L) {
        return Float.NaN;
      }
      double dtHours = (double) dtMillis / 3_600_000.0;
      return (float) ((last.value() - first.value()) / dtHours);
    };
  }

  /**
   * Least-squares slope of value vs time, returned as "units per hour".
   * More stable than first-to-last when pressure is noisy.
   */
  public static RollingComputation slopeLeastSquaresPerHour() {
    return samples -> {
      TimedFloatSample first = null;
      long count = 0L;

      double sumX = 0.0;
      double sumY = 0.0;
      double sumXX = 0.0;
      double sumXY = 0.0;

      for (TimedFloatSample sample : samples) {
        if (first == null) {
          first = sample;
        }

        double xHours = (double) (sample.epochMillis() - first.epochMillis()) / 3_600_000.0;
        double y = sample.value();

        sumX += xHours;
        sumY += y;
        sumXX += xHours * xHours;
        sumXY += xHours * y;
        count++;
      }

      if (count < 2L) {
        return Float.NaN;
      }

      double denom = (count * sumXX) - (sumX * sumX);
      if (denom == 0.0) {
        return Float.NaN;
      }

      double slopePerHour = ((count * sumXY) - (sumX * sumY)) / denom;
      return (float) slopePerHour;
    };
  }
}