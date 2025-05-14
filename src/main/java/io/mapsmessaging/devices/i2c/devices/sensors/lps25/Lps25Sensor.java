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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Resetable;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers.*;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataRate;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.List;

@Getter
public class Lps25Sensor extends I2CDevice implements Sensor, Resetable {
  private static final int WHO_AM_I = 0xf;
  private final ResolutionRegister resolutionRegister;
  @Getter
  private final Control1 control1;
  @Getter
  private final Control2 control2;
  @Getter
  private final Control3 control3;
  @Getter
  private final Control4 control4;
  @Getter
  private final InterruptControl interruptControl;
  @Getter
  private final InterruptSourceRegister interruptSource;
  @Getter
  private final FiFoControl fiFoControl;
  @Getter
  private final StatusRegister statusRegister;
  @Getter
  private final TemperatureRegister temperatureRegister;
  @Getter
  private final PressureRegister pressureRegister;
  @Getter
  private final ReferencePressureRegister referencePressureRegister;
  @Getter
  private final FiFoStatusRegister fiFoStatusRegister;
  @Getter
  private final ThresholdPressureRegister thresholdPressureRegister;
  @Getter
  private final WhoAmIRegister whoAmIRegister;
  @Getter
  private final PressureOffset pressureOffset;
  @Getter
  private final List<SensorReading<?>> readings;

  private float pressure;
  private float temperature;

  private int counter;
  private DataRate dataRate;

  public Lps25Sensor(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Lps25Sensor.class));

    control1 = new Control1(this);
    control2 = new Control2(this);
    control3 = new Control3(this);
    control4 = new Control4(this);
    interruptSource = new InterruptSourceRegister(this);
    interruptControl = new InterruptControl(this);
    fiFoControl = new FiFoControl(this);
    statusRegister = new StatusRegister(this);
    temperatureRegister = new TemperatureRegister(this);
    pressureRegister = new PressureRegister(this);
    referencePressureRegister = new ReferencePressureRegister(this);
    fiFoStatusRegister = new FiFoStatusRegister(this);
    thresholdPressureRegister = new ThresholdPressureRegister(this);
    whoAmIRegister = new WhoAmIRegister(this);
    pressureOffset = new PressureOffset(this);
    resolutionRegister = new ResolutionRegister(this);

    FloatSensorReading pressureReading = new FloatSensorReading(
        "pressure",
        "hPa",
        "Absolute pressure from LPS25 sensor",
        1013.25f,
        true,
        260f,
        1260f,
        1,
        this::getPressure
    );

    FloatSensorReading temperatureReading = new FloatSensorReading(
        "temperature",
        "°C",
        "Temperature reading from LPS25 sensor",
        25.0f,
        true,
        -30f,
        70f,
        1,
        this::getTemperature
    );

    readings = List.of(pressureReading, temperatureReading);

    if (whoAmIRegister.getWhoAmI() == 0b10111101) {
      initialise();
    }
  }

  public static int getId(AddressableDevice device) {
    return device.readRegister(WHO_AM_I);
  }

  private void initialise() throws IOException {
    counter = 0;
    control1.setPowerDownMode(false);
    delay(10);
    reset();
    delay(100);
    softReset();
    delay(100);
    control1.setPowerDownMode(true);
    delay(100);
    pressureOffset.setPressureOffset(0);
    referencePressureRegister.setReference(0);
    thresholdPressureRegister.setThreshold(0.0f);
    control1.resetAutoZero(true);
    control1.setDataRate(DataRate.RATE_7_HZ);
    dataRate = DataRate.RATE_7_HZ;
  }

  @Override
  public String toString() {
    return getName() + " - " + getDescription() + "\n" + registerMap.toString();
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public String getName() {
    return "LPS25";
  }

  @Override
  public String getDescription() {
    return "Pressure sensor: 260-1260 hPa";
  }


  public boolean getPowerDownMode() {
    return control1.getPowerDownMode();
  }

  public void setPowerDownMode(boolean flag) throws IOException {
    control1.setPowerDownMode(flag);
  }

  public void reset() throws IOException {
    control2.reset();
  }

  @Override
  public void softReset() throws IOException {
    control2.boot();
  }

  //region Pressure Out Registers
  protected float getPressure() throws IOException {
    if (counter > 20) {
      initialise();
    }
    if (!control1.getDataRate().equals(DataRate.RATE_ONE_SHOT) &&
        !statusRegister.isPressureDataAvailable()) {
      counter++;
      return pressure;
    }
    pressure = pressureRegister.getPressure();
    return pressure;
  }
  //endregion

  //region Temperature Out Registers
  protected float getTemperature() throws IOException {
    if (!control1.getDataRate().equals(DataRate.RATE_ONE_SHOT) &&
        !statusRegister.isTemperatureDataAvailable()) {
      return temperature;
    }
    temperature = temperatureRegister.getTemperature();
    return temperature;
  }

  //endregion
  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }
/*
export CLASSPATH=$MAPS_LIB//commons-logging-1.2.jar:$CLASSPATH

 */
}
