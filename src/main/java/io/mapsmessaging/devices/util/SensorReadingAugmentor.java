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

import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.ReadingSupplier;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SensorReadingAugmentor {
  @Getter
  @Setter
  private static boolean ENABLE_COMPUTED_READINGS = true;

  private static final String TEMPERATURE = "temperature";
  private static final String HUMIDITY = "humidity";
  private static final String PRESSURE = "pressure";

  private SensorReadingAugmentor() {
    // no-op
  }

  public static List<SensorReading<?>> addComputedReadings(List<SensorReading<?>> baseReadings) {
    if(!ENABLE_COMPUTED_READINGS){
      return baseReadings;
    }

    Map<String, SensorReading<?>> lookup = baseReadings.stream()
        .collect(Collectors.toMap(r -> r.getName().toLowerCase(), r -> r));
    List<SensorReading<?>> computed = new ArrayList<>();
    scanForDewPoint(lookup, computed);
    scanForAQI(lookup, computed);
    scanForCO2Quality(lookup, computed);
    List<SensorReading<?>> result = new ArrayList<>(baseReadings);
    result.addAll(computed);
    return result;
  }

  private static void scanForDewPoint(Map<String, SensorReading<?>> lookup, List<SensorReading<?>> computed) {
    boolean hasTemp = lookup.containsKey(TEMPERATURE);
    boolean hasHumidity = lookup.containsKey(HUMIDITY);
    boolean hasPressure = lookup.containsKey(PRESSURE);


    if (hasTemp && hasHumidity) {
      var tempSupplier = getFloatSupplier(lookup.get(TEMPERATURE));
      var humiditySupplier = getFloatSupplier(lookup.get(HUMIDITY));
      ReadingSupplier<Float> pressureSupplier;
      if (hasPressure) {
        pressureSupplier = getFloatSupplier(lookup.get(PRESSURE));
      } else {
        pressureSupplier = () -> Float.NaN;
      }

      computed.add(new FloatSensorReading(
          "dewPoint",
          "°C",
          "Calculated dew point based on temperature and humidity",
          0f,
          false,
          -50f,
          100f,
          1,
          () -> (float) ComputeDewPoint.computeDewPoint(
              tempSupplier.get(),
              humiditySupplier.get(),
              pressureSupplier.get())
      ));

      computed.add(new FloatSensorReading(
          "condensationRisk",
          "",
          "1 if condensation risk (dew point within 2°C of temp), 0 otherwise",
          0f,
          false,
          0f,
          1f,
          0,
          () -> ComputeDewPoint.dewPointWarning(
              tempSupplier.get(),
              humiditySupplier.get(),
              pressureSupplier.get()) ? 1f : 0f
      ));
    }
  }

  private static void scanForAQI(Map<String, SensorReading<?>> lookup, List<SensorReading<?>> computed) {
    boolean hasPM25 = lookup.containsKey("pm2_5");
    boolean hasPM10 = lookup.containsKey("pm10");
    boolean hasVOC = lookup.containsKey("vocIndex");
    boolean hasNOx = lookup.containsKey("noxIndex");

    if (hasPM25 && hasPM10) {
      var pm25 = getFloatSupplier(lookup.get("pm2_5"));
      var pm10 = getFloatSupplier(lookup.get("pm10"));

      computed.add(new FloatSensorReading(
          "AQI",
          "",
          "Air Quality Index (PM2.5 and PM10)",
          0f,
          false,
          0f,
          500f,
          0,
          () -> AqiCalculator.computeFromPMSA003I(pm25.get(), pm10.get())
      ));
      computed.add(new StringSensorReading(
          "AQICategory",
          "",
          "Air Quality Category from AQI",
          "Good",
          false,
          () -> {
            float aqi = AqiCalculator.computeFromPMSA003I(pm25.get(), pm10.get());
            return AqiCalculator.describeAqi(aqi);
          }
      ));

    } else if (hasVOC && hasNOx) {
      var voc = getFloatSupplier(lookup.get("vocIndex"));
      var nox = getFloatSupplier(lookup.get("noxIndex"));

      computed.add(new FloatSensorReading(
          "AQI",
          "",
          "Air Quality Index (VOC and NOx Index)",
          0f,
          false,
          0f,
          500f,
          0,
          () -> AqiCalculator.computeFromSEN66(voc.get(), voc.get(), nox.get())
      ));
      computed.add(new StringSensorReading(
          "AQICategory",
          "",
          "Air Quality Category from AQI",
          "Good",
          false,
          () -> {
            float aqi = AqiCalculator.computeFromSEN66(Float.NaN, voc.get(), nox.get());
            return AqiCalculator.describeAqi(aqi);
          }
      ));

    }

  }

  private static void scanForCO2Quality(Map<String, SensorReading<?>> lookup, List<SensorReading<?>> computed) {
    boolean hasCO2 = lookup.containsKey("CO₂");
    boolean hasHumidity = lookup.containsKey(HUMIDITY);
    boolean hasTemperature = lookup.containsKey(TEMPERATURE);

    if (hasCO2 && hasHumidity && hasTemperature) {
      var co2 = getFloatSupplier(lookup.get("CO₂"));
      var humidity = getFloatSupplier(lookup.get(HUMIDITY));
      var temperature = getFloatSupplier(lookup.get(TEMPERATURE));

      computed.add(new StringSensorReading(
          "CO₂ Category",
          "",
          "CO₂-based air quality classification",
          "Unknown",
          false,
          () -> AqiCalculator.describeCo2Quality(
              Math.round(co2.get()), humidity.get(), temperature.get()
          )
      ));
    }
  }

  private static ReadingSupplier<Float> getFloatSupplier(SensorReading<?> reading) {
    // Your FloatSensorReading likely implements this; adjust if needed
    if (reading instanceof FloatSensorReading fsr) {
      return fsr.getSupplier();
    }
    return () -> Float.NaN;
  }
}