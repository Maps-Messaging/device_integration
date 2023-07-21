/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices.sensors.pmsa003i;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.BufferedRegister;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Pmsa003iSensor extends I2CDevice implements Sensor {
  private long lastRead;
  private final  byte[] data;

  private final BufferedRegister pm1_0StandardRegister;
  private final BufferedRegister pm2_5StandardRegister;
  private final BufferedRegister pm10StandardRegister;
  private final BufferedRegister pm1_0AtmosphericRegister;
  private final BufferedRegister pm2_5AtmosphericRegister;
  private final BufferedRegister pm10AtmosphericRegister;
  private final BufferedRegister particlesLargerThan3Register;
  private final BufferedRegister particlesLargerThan5Register;
  private final BufferedRegister particlesLargerThan10Register;
  private final BufferedRegister particlesLargerThan25Register;
  private final BufferedRegister particlesLargerThan50Register;
  private final BufferedRegister particlesLargerThan100Register;
  private final BufferedRegister versionRegister;
  private final BufferedRegister errorCodeRegister;

  @Getter
  private final List<SensorReading<?>> readings;

  public Pmsa003iSensor(I2C device) {
    super(device, LoggerFactory.getLogger(Pmsa003iSensor.class));
    data = new byte[0x20];

    this.pm1_0StandardRegister = new BufferedRegister(this, 4, 2, "Pm1_0Standard", data);
    this.pm2_5StandardRegister = new BufferedRegister(this, 6, 2, "Pm2_5Standard", data);
    this.pm10StandardRegister = new BufferedRegister(this, 8, 2, "Pm10Standard", data);

    this.pm1_0AtmosphericRegister = new BufferedRegister(this, 0xa, 2, "Pm1_0Atmospheric", data);
    this.pm2_5AtmosphericRegister = new BufferedRegister(this, 0xc,2, "Pm2_5Atmospheric", data);
    this.pm10AtmosphericRegister = new BufferedRegister(this, 0xe, 2,"Pm10Atmospheric", data);

    this.particlesLargerThan3Register = new BufferedRegister(this, 0x10, 2, "ParticlesLargerThan3", data);
    this.particlesLargerThan5Register = new BufferedRegister(this, 0x12, 2, "ParticlesLargerThan5", data);
    this.particlesLargerThan10Register = new BufferedRegister(this, 0x14, 2, "ParticlesLargerThan10", data);
    this.particlesLargerThan25Register = new BufferedRegister(this, 0x16, 2, "ParticlesLargerThan25", data);
    this.particlesLargerThan50Register = new BufferedRegister(this, 0x18, 2, "ParticlesLargerThan50", data);
    this.particlesLargerThan100Register = new BufferedRegister(this, 0x1a, 2, "ParticlesLargerThan100", data);
    this.versionRegister = new BufferedRegister(this, 0x1c, 1, "Version", data);
    this.errorCodeRegister = new BufferedRegister(this, 0x1d, 1, "ErrorCode", data);
    lastRead = 0;
    readings = new ArrayList<>();
    readings.add(new IntegerSensorReading("PM 1.0", "pm", 0, 0x7ffff, this::getPm1_0Standard));
    readings.add(new IntegerSensorReading("PM 2.5", "pm", 0, 0x7ffff, this::getPm2_5Standard));
    readings.add(new IntegerSensorReading("PM 10", "pm", 0, 0x7ffff, this::getPm10Standard));

    readings.add(new IntegerSensorReading("PM 1.0 Atmospheric", "pm", 0, 0x7ffff, this::getPm1_0Atmospheric));
    readings.add(new IntegerSensorReading("PM 2.5 Atmospheric", "pm", 0, 0x7ffff, this::getPm2_5Atmospheric));
    readings.add(new IntegerSensorReading("PM 10 Atmospheric", "pm", 0, 0x7ffff, this::getPm10Atmospheric));

    readings.add(new IntegerSensorReading("Particles larger than 3", "count", 0, 0x7ffff, this::getParticlesLargerThan3));
    readings.add(new IntegerSensorReading("Particles larger than 5", "count", 0, 0x7ffff, this::getParticlesLargerThan5));
    readings.add(new IntegerSensorReading("Particles larger than 10", "count", 0, 0x7ffff, this::getParticlesLargerThan10));
    readings.add(new IntegerSensorReading("Particles larger than 25", "count", 0, 0x7ffff, this::getParticlesLargerThan25));
    readings.add(new IntegerSensorReading("Particles larger than 50", "count", 0, 0x7ffff, this::getParticlesLargerThan50));
    readings.add(new IntegerSensorReading("Particles larger than 100", "count", 0, 0x7ffff, this::getParticlesLargerThan100));
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  protected int getPm1_0Standard() throws IOException {
    update();
    return pm1_0StandardRegister.getValueReverse();
  }

  protected int getPm2_5Standard() throws IOException {
    update();
    return pm2_5StandardRegister.getValueReverse();
  }

  protected int getPm10Standard() throws IOException {
    update();
    return pm10StandardRegister.getValueReverse();
  }

  protected int getPm1_0Atmospheric() throws IOException {
    update();
    return pm1_0AtmosphericRegister.getValueReverse();
  }

  protected int getPm2_5Atmospheric() throws IOException {
    update();
    return pm2_5AtmosphericRegister.getValueReverse();
  }

  protected int getPm10Atmospheric() throws IOException {
    update();
    return pm10AtmosphericRegister.getValueReverse();
  }

  protected int getParticlesLargerThan3() throws IOException {
    update();
    return particlesLargerThan3Register.getValueReverse();
  }

  protected int getParticlesLargerThan5() throws IOException {
    update();
    return particlesLargerThan5Register.getValueReverse();
  }

  protected int getParticlesLargerThan10() throws IOException {
    update();
    return particlesLargerThan10Register.getValueReverse();
  }

  protected int getParticlesLargerThan25() throws IOException {
    update();
    return particlesLargerThan25Register.getValueReverse();
  }

  protected int getParticlesLargerThan50() throws IOException {
    update();
    return particlesLargerThan50Register.getValueReverse();
  }

  protected int getParticlesLargerThan100() throws IOException {
    update();
    return particlesLargerThan100Register.getValueReverse();
  }

  public int getVersion() throws IOException {
    update();
    return versionRegister.getValueReverse();
  }

  protected int getErrorCode() throws IOException {
    update();
    return errorCodeRegister.getValueReverse();
  }

  protected void update() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      readRegister(0, data, 0, data.length);
      lastRead = System.currentTimeMillis() + 1000;
    }
  }

  @Override
  public String getName() {
    return "PMSA003I";
  }

  @Override
  public String getDescription() {
    return "Air Quality Breakout";
  }


}