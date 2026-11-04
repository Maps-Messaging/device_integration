package io.mapsmessaging.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.Bh1750Controller;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.ReadingSupplier;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StatefulFloatSensorReading;
import io.mapsmessaging.devices.util.RollingComputations;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class SensorAvailabilityTest {

  @Test
  void nonfiniteNumbersAreUnavailableBeforeRounding() {
    for (float unavailable : new float[] {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
      ComputationResult<Float> result = reading(unavailable).getValue();
      assertTrue(result.hasError(), "Nonfinite input must not become a successful numeric reading");
      assertTrue(result.getError() instanceof IOException);
      assertTrue(result.getError().getMessage().contains("sample"));
    }
  }

  @Test
  void validZeroAndNegativeValuesRemainSuccessfulMeasurements() {
    ComputationResult<Float> zero = reading(0.0f).getValue();
    assertFalse(zero.hasError());
    assertEquals(0.0f, zero.getResult());
    assertEquals(-2.35f, reading(-2.3456f).getValue().getResult());
  }

  @Test
  void roundingHelpersDoNotTurnUnavailableValuesIntoZero() {
    assertTrue(Float.isNaN(FloatSensorReading.roundToDecimalPlaces(Float.NaN, 2)));
    assertEquals(Float.POSITIVE_INFINITY, FloatSensorReading.roundToDecimalPlaces(Float.POSITIVE_INFINITY, 2));
    assertTrue(Float.isNaN(StatefulFloatSensorReading.roundToDecimalPlaces(Float.NaN, 2)));
  }

  @Test
  void legacyUncheckedIoFailuresPreserveTheOriginalCause() {
    IOException unavailable = new IOException("First acquisition failed");
    FloatSensorReading reading = new FloatSensorReading("sample", "", "", 1.0f, true, -100.0f, 100.0f, 2, () -> {
      throw new UncheckedIOException(unavailable);
    });
    ComputationResult<Float> result = reading.getValue();
    assertTrue(result.hasError());
    assertSame(unavailable, result.getError());
  }

  @Test
  void initialRollingWindowWithoutDataIsUnavailable() {
    StatefulFloatSensorReading reading = history(() -> Float.NaN);
    assertTrue(reading.getValue().hasError());
  }

  @Test
  void rollingWindowPreservesAcquisitionFailure() {
    IOException unavailable = new IOException("Sensor has not completed its first conversion");
    StatefulFloatSensorReading reading = history(() -> {
      throw unavailable;
    });
    ComputationResult<Float> result = reading.getValue();
    assertTrue(result.hasError());
    assertSame(unavailable, result.getError());
  }

  @Test
  void initialTrendWithoutEnoughHistoryIsUnavailable() {
    StatefulFloatSensorReading trend = new StatefulFloatSensorReading("trend", "", "", 0.0f, true, -100.0f, 100.0f, 2,
        () -> 10.0f, 10, 60000, RollingComputations.slopeLeastSquaresPerHour());
    assertTrue(trend.getValue().hasError());
  }

  @Test
  void unavailableReadingsSerializeAsErrorsInsteadOfZeroOrBareNaN() throws IOException {
    ExposedController controller = new ExposedController();
    JsonObject state = JsonParser.parseString(controller.read(List.of(reading(Float.NaN), history(() -> Float.NaN)))).getAsJsonObject();
    assertTrue(state.get("sample").getAsJsonPrimitive().isString());
    assertTrue(state.get("history").getAsJsonPrimitive().isString());
    assertTrue(state.get("sample").getAsString().contains("unavailable"));
    assertTrue(state.get("history").getAsString().contains("unavailable"));
  }

  private static FloatSensorReading reading(float value) {
    return new FloatSensorReading("sample", "", "", 1.0f, true, -100.0f, 100.0f, 2, () -> value);
  }

  private static StatefulFloatSensorReading history(ReadingSupplier<Float> source) {
    return new StatefulFloatSensorReading("history", "", "", 1.0f, true, -100.0f, 100.0f, 2, source, 10, 60000, RollingComputations.average());
  }

  private static class ExposedController extends Bh1750Controller {
    String read(List<SensorReading<?>> readings) throws IOException {
      JsonObject state = new JsonObject();
      walkSensorReadings(state, readings);
      return new String(convert(state), StandardCharsets.UTF_8);
    }
  }
}
