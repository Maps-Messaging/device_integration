package io.mapsmessaging.devices.sensorreadings;

public class FloatSensorReading extends NumericSensorReading<Float> {

  private final int precision;

  public FloatSensorReading(String name, String unit, float min, float max, ReadingSupplier<Float> valueSupplier) {
    this(name, unit, min, max, -1, valueSupplier);
  }

  public FloatSensorReading(String name, String unit, float min, float max, int precision, ReadingSupplier<Float> valueSupplier) {
    super(name, unit, min, max, valueSupplier);
    this.precision = precision;
  }

  protected Float format(Float val){
    if(precision >= 0){
      return roundToDecimalPlaces(val, precision);
    }
    return val;
  }

  public static float roundToDecimalPlaces(float value, int places) {
    float scale = (float) Math.pow(10, places);
    return Math.round(value * scale) / scale;
  }
}
