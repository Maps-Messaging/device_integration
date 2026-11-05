package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.gravity.module.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Independently calculated Arduino examples, retaining the corrected Python H2 coefficients. */
class GravityCompensationTest {
  private static final float TOLERANCE = 0.0001f;

  @Test
  void coIncludesFortyDegrees() {
    assertEquals(84.909091f, new COModule().computeGasConcentration(40, 100), TOLERANCE);
  }

  @Test
  void h2sRejectsTemperaturesAtOrBelowMinusTwenty() {
    H2SModule module = new H2SModule();
    assertAll(
        () -> assertEquals(0, module.computeGasConcentration(-30, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(-20, 10), TOLERANCE));
  }

  @Test
  void nh3UsesNegativeTemperatureCoefficientsInCorrectionTerms() {
    NH3Module module = new NH3Module();
    assertAll(
        () -> assertEquals(10.925955f, module.computeGasConcentration(-10, 10), TOLERANCE),
        () -> assertEquals(9.770990f, module.computeGasConcentration(10, 10), TOLERANCE),
        () -> assertEquals(9.130081f, module.computeGasConcentration(30, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(-20, 10), TOLERANCE));
  }

  @Test
  void no2UsesReferenceCorrectionAndStopsAboveForty() {
    NO2Module module = new NO2Module();
    assertAll(
        () -> assertEquals(9.348810f, module.computeGasConcentration(30, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(45, 10), TOLERANCE));
  }

  @Test
  void ozoneDividesBeforeSubtractingTemperatureCorrection() {
    O3Module module = new O3Module();
    assertAll(
        () -> assertEquals(8.990909f, module.computeGasConcentration(10, 10), TOLERANCE),
        () -> assertEquals(8.940909f, module.computeGasConcentration(30, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(45, 10), TOLERANCE));
  }

  @Test
  void chlorineSubtractsCorrectionAfterDivision() {
    Cl2Module module = new Cl2Module();
    assertAll(
        () -> assertEquals(9.040909f, module.computeGasConcentration(10, 10), TOLERANCE),
        () -> assertEquals(8.940909f, module.computeGasConcentration(30, 10), TOLERANCE));
  }

  @Test
  void hfUsesReferenceColdAndHotCoefficients() {
    HFModule module = new HFModule();
    assertAll(
        () -> assertEquals(9.975f, module.computeGasConcentration(-10, 10), TOLERANCE),
        () -> assertEquals(9.725f, module.computeGasConcentration(30, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(-20, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(45, 10), TOLERANCE));
  }

  @Test
  void phosphineCompensatesBelowTwentyAndRejectsOutsideRange() {
    PH3Module module = new PH3Module();
    assertAll(
        () -> assertEquals(11.764706f, module.computeGasConcentration(-10, 10), TOLERANCE),
        () -> assertEquals(10.526316f, module.computeGasConcentration(10, 10), TOLERANCE),
        () -> assertEquals(10, module.computeGasConcentration(20, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(-20, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(45, 10), TOLERANCE));
  }

  @Test
  void hclRejectsTemperaturesOutsideReferenceRange() {
    HCLModule module = new HCLModule();
    assertAll(
        () -> assertEquals(0, module.computeGasConcentration(-20, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(50, 10), TOLERANCE));
  }

  @Test
  void so2RejectsTemperaturesOutsideReferenceRange() {
    SO2Module module = new SO2Module();
    assertAll(
        () -> assertEquals(0, module.computeGasConcentration(-40, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(61, 10), TOLERANCE));
  }

  @Test
  void coUsesArduinoLowerTemperatureLimit() {
    COModule module = new COModule();
    assertAll(
        () -> assertEquals(0, module.computeGasConcentration(-30, 10), TOLERANCE),
        () -> assertEquals(0, module.computeGasConcentration(-20, 10), TOLERANCE),
        () -> assertEquals(11.764706f, module.computeGasConcentration(-10, 10), TOLERANCE));
  }

  @Test
  void chlorineUsesArduinoColdTemperatureCorrection() {
    Cl2Module module = new Cl2Module();
    assertAll(
        () -> assertEquals(11.548947f, module.computeGasConcentration(-10, 11), TOLERANCE),
        () -> assertEquals(9.995f, module.computeGasConcentration(0, 11), TOLERANCE));
  }

  @Test
  void arduinoCompensationIncludesFortyDegrees() {
    assertAll(
        () -> assertEquals(8.990909f, new Cl2Module().computeGasConcentration(40, 10), TOLERANCE),
        () -> assertEquals(9.8125f, new NH3Module().computeGasConcentration(40, 10), TOLERANCE),
        () -> assertEquals(9.35f, new HFModule().computeGasConcentration(40, 10), TOLERANCE),
        () -> assertEquals(9.090909f, new PH3Module().computeGasConcentration(40, 10), TOLERANCE));
  }

  @Test
  void hydrogenRetainsCorrectedCoefficientsWithoutPythonControlFlowBug() {
    assertAll(
        () -> assertEquals(9.285714f, new H2Module().computeGasConcentration(0, 10), TOLERANCE),
        () -> assertEquals(10.974441f, new H2Module().computeGasConcentration(-10, 10), TOLERANCE),
        () -> assertEquals(20.9f, new O2Module().computeGasConcentration(25, 20.9f), TOLERANCE));
  }
}
