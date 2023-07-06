package io.mapsmessaging.devices.i2c.devices.sensors.lps35;

public class AltitudeMonitor {

  private float temperature;
  private float pressure;
  private final boolean update;

  public AltitudeMonitor(){
    temperature = Float.NaN;
    pressure = Float.NaN;
    update = true;
  }

  public AltitudeMonitor(float temp, float pressure){
    temperature = temp;
    this.pressure = pressure;
    update = false;
  }

  public double compute(float pressureReading, float temperatureReading){
    double value = 0;
    if(temperature != Float.NaN && pressure != Float.NaN){
      value = calculateHeightDifference(pressure, pressureReading, temperatureReading);
    }
    if(update) {
      temperature = temperatureReading;
      pressure = pressureReading;
    }
    return value;
  }

  public static double computeHeightDifference(double pressure1, double pressure2, double temperature1, double temperature2) {
    double R = 287.053;  // Specific gas constant for dry air (J/(kg·K))
    double g = 9.8;      // Acceleration due to gravity (m/s²)
    double M = 0.02896;  // Molar mass of air (kg/mol)

    double temp1K = temperature1 + 273.15;  // Convert temperature to Kelvin
    double temp2K = temperature2 + 273.15;

    double P0 = pressure1 * 100;  // Convert pressure from hPa to Pa
    double P1 = pressure2 * 100;

    return ((R * (temp1K + temp2K) / (2 * g * M)) * Math.log(P0 / P1)/1000.0);
  }


  /**
   * Calculates the height difference based on pressure change and temperature.
   *
   * @param pressure1 The initial pressure measurement in hPa.
   * @param pressure2 The final pressure measurement in hPa.
   * @param temperature The temperature measurement in degrees Celsius.
   * @return The height difference in meters.
   */
  public static double calculateHeightDifference(double pressure1, double pressure2, double temperature) {
    double deltaP = pressure2 - pressure1;
    double deltaT = temperature + 273.15; // Convert temperature to Kelvin

    // Constants for the International Standard Atmosphere (ISA) model
    final double g = 9.80665; // Acceleration due to gravity (m/s^2)
    final double M = 0.0289644; // Molar mass of Earth's air (kg/mol)
    final double R = 8.31432; // Universal gas constant (J/(mol·K))
    final double P0 = 1013.25; // Standard pressure at sea level (hPa)

    // Calculate the height difference using the barometric formula
    double heightDifference = (R * deltaT) / (M * g) * Math.log((P0 - deltaP) / P0);

    return heightDifference;
  }

}
