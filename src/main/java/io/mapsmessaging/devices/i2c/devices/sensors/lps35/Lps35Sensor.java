package io.mapsmessaging.devices.i2c.devices.sensors.lps35;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.values.DataRate;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

/**
 * Original CPP source <a href="https://github.com/adafruit/Adafruit_LPS35HW/blob/master/Adafruit_LPS35HW.cpp">...</a>
 */

@Getter
public class Lps35Sensor extends I2CDevice implements Sensor, Resetable {

  public static final byte WHO_AM_I = 0x0F;
  private final InterruptConfigRegister interruptConfigRegister;
  @Getter
  private final ReferencePressureRegister referencePressureRegister;
  @Getter
  private final PressureOffsetRegister pressureOffsetRegister;
  @Getter
  private final ThresholdPressureRegister thresholdPressureRegister;
  @Getter
  private final TemperatureRegister temperatureRegister;
  @Getter
  private final PressureRegister pressureRegister;
  @Getter
  private final InterruptSourceRegister interruptSourceRegister;
  @Getter
  private final FiFoStatusRegister fiFoStatusRegister;
  @Getter
  private final StatusRegister statusRegister;
  @Getter
  private final WhoAmIRegister whoAmIRegister;
  @Getter
  private final FiFoControlRegister fiFoControlRegister;
  @Getter
  private final LowPowerModeRegister lowPowerModeRegister;
  @Getter
  private final Control1Register control1Register;
  @Getter
  private final Control2Register control2Register;
  @Getter
  private final Control3Register control3Register;
  @Getter
  private final List<SensorReading<?>> readings;


  public Lps35Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Lps35Sensor.class));
    interruptConfigRegister = new InterruptConfigRegister(this);
    referencePressureRegister = new ReferencePressureRegister(this);
    thresholdPressureRegister = new ThresholdPressureRegister(this);
    pressureOffsetRegister = new PressureOffsetRegister(this);
    temperatureRegister = new TemperatureRegister(this);
    pressureRegister = new PressureRegister(this);
    interruptSourceRegister = new InterruptSourceRegister(this);
    fiFoStatusRegister = new FiFoStatusRegister(this);
    statusRegister = new StatusRegister(this);
    whoAmIRegister = new WhoAmIRegister(this);
    fiFoControlRegister = new FiFoControlRegister(this);
    lowPowerModeRegister = new LowPowerModeRegister(this);
    control1Register = new Control1Register(this);
    control2Register = new Control2Register(this);
    control3Register = new Control3Register(this);

    FloatSensorReading pressureReading = new FloatSensorReading("pressure", "hPa", 260, 1260, 0, this::getPressure);
    FloatSensorReading temperatureReading = new FloatSensorReading("temperature", "C", -30, 70, 1, this::getTemperature);
    readings = List.of(pressureReading, temperatureReading);
    initialise();
  }

  public static int getId(AddressableDevice device) {
    return device.readRegister(WHO_AM_I);
  }

  private void initialise() throws IOException {
    control1Register.setDataRate(DataRate.RATE_1_HZ);
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public String getName() {
    return "LPS35";
  }

  @Override
  public String getDescription() {
    return "Pressure sensor: 260-1260 hPa";
  }

  @Override
  public void reset() throws IOException {
    control2Register.boot();
    initialise();
  }

  @Override
  public void softReset() throws IOException {
    control2Register.softReset();
  }

  protected float getPressure() throws IOException {
    return pressureRegister.getPressure();
  }

  protected float getTemperature() throws IOException {
    return temperatureRegister.getTemperature();
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}
