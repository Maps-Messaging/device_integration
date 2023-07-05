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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.module.SensorType;
import io.mapsmessaging.devices.util.Delay;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import static java.lang.Math.log;

public class GasSensor extends I2CDevice {

  @Getter
  private final SensorType sensorType;

  private float temperature;
  private int concentration;
  private int decimalPoint;

  private final Logger logger = LoggerFactory.getLogger(GasSensor.class);

  public GasSensor(I2C device) {
    super(device);
    sensorType = detectType();
  }

  public float getTemperature(){
    return temperature;
  }

  public float getConcentration(){
    byte[] data = new byte[9];
    request(Command.GET_GAS_CONCENTRATION, data);
    concentration = (data[2] << 8 | (data[3] & 0xff));
    decimalPoint = data[5];
    return concentration;
  }

  public float getTemperatureAdjustedConcentration(){
    if(sensorType != null){
      return sensorType.getSensorModule().computeGasConcentration(temperature, concentration, decimalPoint);
    }
    return 0;
  }

  public boolean setI2CAddress(byte group){
    byte[] data = new byte[9];
    byte[] request = new byte[6];
    request[1] = group;
    if (request(Command.CHANGE_I2C_ADDR, request, data)){
      return data[2] != 0;
    }
    return false;
  }


  public boolean changeAcquireMode(AcquireMode acquireMode){
    return false;
  }

  public boolean setThresholdAlarm(int threshold, AlarmType alarmType){
   return false;
  }

  public boolean clearThresholdAlarm(int threshold, AlarmType alarmType){
    return false;
  }

  public float readTempC(){
    byte[] data = new byte[9];
    if(request(Command.GET_TEMP, data)){
      int raw = data[2] << 8 | (data[3] & 0xff);
      temperature = computeTemperature(raw);
      return temperature;
    }
    return Float.NaN;
  }

  public float readVoltageData(){
    byte[] recvbuf = new byte[9];
    if(request(Command.SENSOR_VOLTAGE, recvbuf)){
      return ((recvbuf[2] <<8 | recvbuf[3]&0xff) * 3.0f)/1024.0f * 2f;
    }
    return Float.NaN;
  }

  public boolean isDataAvailable(){
    return false;
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public String getName() {
    if(sensorType != null){
      return sensorType.getSku();
    }
    return "GasSensor";
  }

  @Override
  public String getDescription() {
    if(sensorType != null){
      return sensorType.name()+" gas sensor detects from "+sensorType.getMinimumRange()+
          " to "+sensorType.getMaximumRange()+" "+sensorType.getUnits();
    }
    return "Generic Gas Sensor";
  }


  private float computeTemperature(int rawTemperature){
    float vpd3 = 3 * (float)rawTemperature / 1024.0f;
    float rth = vpd3 * 10000f / (3f - vpd3);
    return (float)(1 / (1 / (273.15f + 25) + 1 / 3380.13f * log(rth / 10000f)) - 273.15f);
  }


  // Send the command, wait 100ms for the result and then read the result
  private boolean request(Command command, byte[] result) {
    byte[] buf = new byte[6];
    return request(command, buf, result);
  }

  private boolean request(Command command, byte[] buf, byte[] result) {
    buf[0] = command.getCommandValue();
    write(pack(buf));
    Delay.pause(100);
    readRegister(0, result,0, result.length);
    byte checksum = calculateChecksum(result);
    return (result[8] == checksum);
  }

  private byte[] pack(byte[] data){
    byte[] payload = new byte[9];
    payload[0] =(byte) 0xff;
    payload[1] = 0x1;
    System.arraycopy(data, 0, payload, 2, data.length);
    payload[8] = calculateChecksum(payload);
    return payload;
  }


  private byte calculateChecksum(byte[] data) {
    byte checksum = 0;
    for (int i = 1; i < data.length - 1; i++) {
      checksum += data[i];
    }
    checksum = (byte) (~checksum + 1);
    return checksum;
  }

  private SensorType detectType(){
    byte[] data = new byte[9];
    if(request(Command.GET_GAS_CONCENTRATION, data)){
      return SensorType.getByType(data[4]);
    }
    return null;
  }
}