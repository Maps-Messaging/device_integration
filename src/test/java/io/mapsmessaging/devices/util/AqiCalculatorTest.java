package io.mapsmessaging.devices.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AqiCalculatorTest {

  @Test
  void unavailableParticulateMeasurementsRemainUnavailable() {
    assertTrue(Float.isNaN(AqiCalculator.computePm25Aqi(Float.NaN)));
    assertTrue(Float.isNaN(AqiCalculator.computePm10Aqi(Float.POSITIVE_INFINITY)));
    assertTrue(Float.isNaN(AqiCalculator.computePmComponent(10.0f, Float.NaN)));
  }

  @Test
  void unavailableGasMeasurementsDoNotBecomeMaximumSeverity() {
    assertTrue(Float.isNaN(AqiCalculator.computeGasComponent(Float.NaN, 1.0f)));
    assertTrue(Float.isNaN(AqiCalculator.computeGasComponent(100.0f, Float.NaN)));
    assertTrue(Float.isNaN(AqiCalculator.computeGasComponent(Float.NEGATIVE_INFINITY, 1.0f)));
    assertTrue(Float.isNaN(AqiCalculator.computeOverallIndoorScore(10.0f, 20.0f, 100.0f, Float.NaN)));
    assertEquals("Unknown", AqiCalculator.describeGasScore(AqiCalculator.computeGasComponent(Float.NaN, Float.NaN)));
  }

  @Test
  void validMeasurementsKeepTheirExistingScores() {
    assertEquals(50.0f, AqiCalculator.computePm25Aqi(12.0f));
    assertEquals(50.0f, AqiCalculator.computePm10Aqi(54.0f));
    assertEquals(100.0f, AqiCalculator.computeGasComponent(100.0f, 10.0f));
    assertEquals(500.0f, AqiCalculator.computeOverallIndoorScore(600.0f, 700.0f, 500.0f, 500.0f));
  }
}
