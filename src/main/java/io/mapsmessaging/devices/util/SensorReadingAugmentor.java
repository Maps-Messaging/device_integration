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

import io.mapsmessaging.devices.sensorreadings.*;
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

  private static final String WINDSPEED = "windspeed";
  private static final String WIND_DIRECTION_ANGLE = "windDirectionAngle";
  private static final String LUX = "lux";

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

    scanForThermalComfort(lookup, computed);
    scanForMoistureMetrics(lookup, computed);
    scanForWindDerived(lookup, computed);
    scanForLightDerived(lookup, computed);


    List<SensorReading<?>> result = new ArrayList<>(baseReadings);
    result.addAll(computed);
    return result;
  }

  private static void scanForDewPoint(Map<String, SensorReading<?>> lookup, List<SensorReading<?>> computed) {
    boolean hasTemp = lookup.containsKey(TEMPERATURE);
    boolean hasHumidity = lookup.containsKey(HUMIDITY);


    if (hasTemp && hasHumidity) {
      var tempSupplier = getFloatSupplier(lookup.get(TEMPERATURE));
      var humiditySupplier = getFloatSupplier(lookup.get(HUMIDITY));
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
              humiditySupplier.get())
      ));

      computed.add(new BooleanSensorReading(
          "condensationRisk",
          "",
          "1 if condensation risk (dew point within 2°C of temp), 0 otherwise",
          true,
          false,
          () -> ComputeDewPoint.dewPointWarning(tempSupplier.get(), humiditySupplier.get())
      ));
    }
  }

  private static void scanForAQI(Map<String, SensorReading<?>> lookup, List<SensorReading<?>> computed) {
    boolean hasPM25 = lookup.containsKey("pm_2_5");
    boolean hasPM10 = lookup.containsKey("pm_10");
    boolean hasVOC = lookup.containsKey("vocIndex");
    boolean hasNOx = lookup.containsKey("noxIndex");

    if (hasPM25 && hasPM10) {
      var pm25 = getFloatSupplier(lookup.get("pm_2_5"));
      var pm10 = getFloatSupplier(lookup.get("pm_10"));

      computed.add(new FloatSensorReading(
          "AQI",
          "",
          "Air Quality Index (PM2.5 and PM10)",
          0f,
          false,
          0f,
          500f,
          0,
          () -> AqiCalculator.computePmAqiFromPmsa003I(pm25.get(), pm10.get())
      ));
      computed.add(new StringSensorReading(
          "AQICategory",
          "",
          "Air Quality Category from AQI",
          "Good",
          false,
          () -> {
            float aqi = AqiCalculator.computePmAqiFromPmsa003I(pm25.get(), pm10.get());
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

  private static void scanForThermalComfort(Map<String, SensorReading<?>> lookup, List<SensorReading<?>> computed) {
    boolean hasTemp = lookup.containsKey(TEMPERATURE);
    boolean hasHumidity = lookup.containsKey(HUMIDITY);
    boolean hasWindSpeed = lookup.containsKey(WINDSPEED);

    if (hasTemp && hasHumidity) {
      ReadingSupplier<Float> temperature = getFloatSupplier(lookup.get(TEMPERATURE));
      ReadingSupplier<Float> humidity = getFloatSupplier(lookup.get(HUMIDITY));

      computed.add(new FloatSensorReading(
          "heatIndex",
          "°C",
          "Calculated heat index from temperature and humidity",
          0f,
          false,
          -40f,
          80f,
          1,
          () -> (float) computeHeatIndexCelsius(temperature.get(), humidity.get())
      ));

      computed.add(new FloatSensorReading(
          "humidex",
          "°C",
          "Calculated humidex from temperature and humidity",
          0f,
          false,
          -40f,
          80f,
          1,
          () -> (float) computeHumidexCelsius(temperature.get(), humidity.get())
      ));
    }

    if (hasTemp && hasWindSpeed) {
      ReadingSupplier<Float> temperature = getFloatSupplier(lookup.get(TEMPERATURE));
      ReadingSupplier<Float> windSpeed = getFloatSupplier(lookup.get(WINDSPEED));

      computed.add(new FloatSensorReading(
          "windChill",
          "°C",
          "Calculated wind chill from temperature and wind speed",
          0f,
          false,
          -60f,
          40f,
          1,
          () -> (float) computeWindChillCelsius(temperature.get(), windSpeed.get())
      ));
    }
  }

  private static void scanForMoistureMetrics(Map<String, SensorReading<?>> lookup, List<SensorReading<?>> computed) {
    boolean hasTemp = lookup.containsKey(TEMPERATURE);
    boolean hasHumidity = lookup.containsKey(HUMIDITY);

    if (!hasTemp || !hasHumidity) {
      return;
    }

    ReadingSupplier<Float> temperature = getFloatSupplier(lookup.get(TEMPERATURE));
    ReadingSupplier<Float> humidity = getFloatSupplier(lookup.get(HUMIDITY));

    computed.add(new FloatSensorReading(
        "saturationVapourPressure",
        "kPa",
        "Saturation vapour pressure at current temperature",
        0f,
        false,
        0f,
        20f,
        3,
        () -> (float) saturationVapourPressureKpa(temperature.get())
    ));

    computed.add(new FloatSensorReading(
        "vapourPressure",
        "kPa",
        "Actual vapour pressure from temperature and relative humidity",
        0f,
        false,
        0f,
        20f,
        3,
        () -> (float) vapourPressureKpa(temperature.get(), humidity.get())
    ));

    computed.add(new FloatSensorReading(
        "absoluteHumidity",
        "g/m3",
        "Absolute humidity from temperature and relative humidity",
        0f,
        false,
        0f,
        50f,
        2,
        () -> (float) absoluteHumidityGPerM3(temperature.get(), humidity.get())
    ));

    computed.add(new FloatSensorReading(
        "dewPointSpread",
        "°C",
        "Temperature minus dew point (dryness indicator)",
        0f,
        false,
        -50f,
        80f,
        1,
        () -> {
          float tempCelsius = temperature.get();
          float rh = humidity.get();
          float dewPoint = (float) ComputeDewPoint.computeDewPoint(tempCelsius, rh);
          if (Float.isNaN(dewPoint) || Float.isNaN(tempCelsius)) {
            return Float.NaN;
          }
          return tempCelsius - dewPoint;
        }
    ));

    computed.add(new StringSensorReading(
        "humidityCategory",
        "",
        "Simple humidity comfort classification",
        "Unknown",
        false,
        () -> describeHumidityCategory(humidity.get())
    ));
  }

  private static void scanForWindDerived(Map<String, SensorReading<?>> lookup, List<SensorReading<?>> computed) {
    boolean hasWindSpeed = lookup.containsKey(WINDSPEED);
    boolean hasWindAngle = lookup.containsKey(WIND_DIRECTION_ANGLE);

    if (hasWindSpeed) {
      ReadingSupplier<Float> windSpeed = getFloatSupplier(lookup.get(WINDSPEED));

      computed.add(new IntegerSensorReading(
          "beaufortScale",
          "",
          "Beaufort scale derived from wind speed",
          0,
          false,
          0,
          12,
          () -> computeBeaufortScale(windSpeed.get())
      ));

      computed.add(new StringSensorReading(
          "beaufortDescription",
          "",
          "Beaufort description derived from wind speed",
          "Calm",
          false,
          () -> describeBeaufort(computeBeaufortScale(windSpeed.get()))
      ));
    }

    if (hasWindAngle) {
      ReadingSupplier<Float> windAngle = getFloatSupplier(lookup.get(WIND_DIRECTION_ANGLE));

      computed.add(new StringSensorReading(
          "windDirectionText",
          "",
          "Compass direction (16-point) derived from wind direction angle",
          "N",
          false,
          () -> toCompass16(windAngle.get())
      ));
    }
  }

  private static void scanForLightDerived(Map<String, SensorReading<?>> lookup, List<SensorReading<?>> computed) {
    boolean hasLux = lookup.containsKey(LUX);
    if (!hasLux) {
      return;
    }

    ReadingSupplier<Float> lux = getFloatSupplier(lookup.get(LUX));

    computed.add(new StringSensorReading(
        "daylightState",
        "",
        "Simple daylight state derived from lux",
        "Unknown",
        false,
        () -> describeDaylightState(lux.get())
    ));
  }

  private static ReadingSupplier<Float> getFloatSupplier(SensorReading<?> reading) {
    if (reading instanceof FloatSensorReading floatSensorReading) {
      return floatSensorReading.getSupplier();
    }
    if (reading instanceof IntegerSensorReading integerSensorReading) {
      return () -> (float) integerSensorReading.getSupplier().get();
    }
    if (reading instanceof LongSensorReading longSensorReading) {
      return () -> (float) longSensorReading.getSupplier().get();
    }
    return () -> Float.NaN;
  }

  private static double computeHeatIndexCelsius(float temperatureCelsius, float relativeHumidityPercent) {
    if (Float.isNaN(temperatureCelsius) || Float.isNaN(relativeHumidityPercent)) {
      return Double.NaN;
    }

    if (temperatureCelsius < 26.7f || relativeHumidityPercent < 40.0f) {
      return temperatureCelsius;
    }

    double temperatureFahrenheit = (temperatureCelsius * 9.0 / 5.0) + 32.0;
    double rh = relativeHumidityPercent;

    double heatIndexFahrenheit =
        -42.379
            + 2.04901523 * temperatureFahrenheit
            + 10.14333127 * rh
            - 0.22475541 * temperatureFahrenheit * rh
            - 0.00683783 * temperatureFahrenheit * temperatureFahrenheit
            - 0.05481717 * rh * rh
            + 0.00122874 * temperatureFahrenheit * temperatureFahrenheit * rh
            + 0.00085282 * temperatureFahrenheit * rh * rh
            - 0.00000199 * temperatureFahrenheit * temperatureFahrenheit * rh * rh;

    return (heatIndexFahrenheit - 32.0) * 5.0 / 9.0;
  }

  private static double computeHumidexCelsius(float temperatureCelsius, float relativeHumidityPercent) {
    if (Float.isNaN(temperatureCelsius) || Float.isNaN(relativeHumidityPercent)) {
      return Double.NaN;
    }

    double dewPoint = ComputeDewPoint.computeDewPoint(temperatureCelsius, relativeHumidityPercent);
    if (Double.isNaN(dewPoint)) {
      return Double.NaN;
    }

    double eHpa = 6.11 * Math.exp(5417.7530 * (1.0 / 273.16 - 1.0 / (dewPoint + 273.15)));
    return temperatureCelsius + (0.5555 * (eHpa - 10.0));
  }

  private static double computeWindChillCelsius(float temperatureCelsius, float windSpeedMetersPerSecond) {
    if (Float.isNaN(temperatureCelsius) || Float.isNaN(windSpeedMetersPerSecond)) {
      return Double.NaN;
    }

    double windSpeedKilometersPerHour = windSpeedMetersPerSecond * 3.6;

    if (temperatureCelsius > 10.0 || windSpeedKilometersPerHour <= 4.8) {
      return temperatureCelsius;
    }

    double vPow = Math.pow(windSpeedKilometersPerHour, 0.16);
    return 13.12 + 0.6215 * temperatureCelsius - 11.37 * vPow + 0.3965 * temperatureCelsius * vPow;
  }

  private static double saturationVapourPressureKpa(float temperatureCelsius) {
    if (Float.isNaN(temperatureCelsius)) {
      return Double.NaN;
    }
    return 0.6108 * Math.exp((17.27 * temperatureCelsius) / (temperatureCelsius + 237.3));
  }

  private static double vapourPressureKpa(float temperatureCelsius, float relativeHumidityPercent) {
    if (Float.isNaN(temperatureCelsius) || Float.isNaN(relativeHumidityPercent)) {
      return Double.NaN;
    }

    double es = saturationVapourPressureKpa(temperatureCelsius);
    return (relativeHumidityPercent / 100.0) * es;
  }

  private static double absoluteHumidityGPerM3(float temperatureCelsius, float relativeHumidityPercent) {
    if (Float.isNaN(temperatureCelsius) || Float.isNaN(relativeHumidityPercent)) {
      return Double.NaN;
    }

    double eKpa = vapourPressureKpa(temperatureCelsius, relativeHumidityPercent);
    if (Double.isNaN(eKpa)) {
      return Double.NaN;
    }

    double eHpa = eKpa * 10.0;
    double tempKelvin = temperatureCelsius + 273.15;
    return 216.7 * (eHpa / tempKelvin);
  }

  private static String describeHumidityCategory(float relativeHumidityPercent) {
    if (Float.isNaN(relativeHumidityPercent)) {
      return "Unknown";
    }
    if (relativeHumidityPercent < 30.0f) {
      return "Dry";
    }
    if (relativeHumidityPercent < 60.0f) {
      return "Comfortable";
    }
    if (relativeHumidityPercent < 75.0f) {
      return "Humid";
    }
    return "Very Humid";
  }

  private static int computeBeaufortScale(float windSpeedMetersPerSecond) {
    if (Float.isNaN(windSpeedMetersPerSecond) || windSpeedMetersPerSecond < 0.0f) {
      return 0;
    }

    if (windSpeedMetersPerSecond < 0.5f) {
      return 0;
    }
    if (windSpeedMetersPerSecond < 1.6f) {
      return 1;
    }
    if (windSpeedMetersPerSecond < 3.4f) {
      return 2;
    }
    if (windSpeedMetersPerSecond < 5.5f) {
      return 3;
    }
    if (windSpeedMetersPerSecond < 8.0f) {
      return 4;
    }
    if (windSpeedMetersPerSecond < 10.8f) {
      return 5;
    }
    if (windSpeedMetersPerSecond < 13.9f) {
      return 6;
    }
    if (windSpeedMetersPerSecond < 17.2f) {
      return 7;
    }
    if (windSpeedMetersPerSecond < 20.8f) {
      return 8;
    }
    if (windSpeedMetersPerSecond < 24.5f) {
      return 9;
    }
    if (windSpeedMetersPerSecond < 28.5f) {
      return 10;
    }
    if (windSpeedMetersPerSecond < 32.7f) {
      return 11;
    }
    return 12;
  }

  private static String describeBeaufort(int beaufortScale) {
    return switch (beaufortScale) {
      case 0 -> "Calm";
      case 1 -> "Light Air";
      case 2 -> "Light Breeze";
      case 3 -> "Gentle Breeze";
      case 4 -> "Moderate Breeze";
      case 5 -> "Fresh Breeze";
      case 6 -> "Strong Breeze";
      case 7 -> "Near Gale";
      case 8 -> "Gale";
      case 9 -> "Severe Gale";
      case 10 -> "Storm";
      case 11 -> "Violent Storm";
      default -> "Hurricane";
    };
  }

  private static String toCompass16(float angleDegrees) {
    if (Float.isNaN(angleDegrees)) {
      return "Unknown";
    }

    double normalized = angleDegrees % 360.0;
    if (normalized < 0.0) {
      normalized += 360.0;
    }

    String[] points = {
        "N", "NNE", "NE", "ENE",
        "E", "ESE", "SE", "SSE",
        "S", "SSW", "SW", "WSW",
        "W", "WNW", "NW", "NNW"
    };

    int index = (int) Math.round(normalized / 22.5) % 16;
    return points[index];
  }

  private static String describeDaylightState(float lux) {
    if (Float.isNaN(lux)) {
      return "Unknown";
    }
    if (lux < 10.0f) {
      return "Night";
    }
    if (lux < 100.0f) {
      return "Twilight";
    }
    if (lux < 10_000.0f) {
      return "Day";
    }
    return "Bright";
  }
}