/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.Sen6xMeasurementManager;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.data.MeasurementBlock;
import io.mapsmessaging.devices.sensorreadings.ReadingSupplier;
import java.io.IOException;
import java.io.InterruptedIOException;

public class ResetMonitor {

  private final Sen6xSensor sen6xSensor;
  private final Sen6xMeasurementManager sen6xMeasurementManager;

  public ResetMonitor(Sen6xMeasurementManager sen6xMeasurementManager, Sen6xSensor sen6xSensor) {
    this.sen6xSensor = sen6xSensor;
    this.sen6xMeasurementManager = sen6xMeasurementManager;
  }

  /** The sensor owns lifecycle changes; acquire its lock before the measurement-manager and device locks. */
  public MeasurementBlock read(ReadingSupplier<MeasurementBlock> reader) throws IOException {
    synchronized (sen6xSensor) {
      if (!sen6xSensor.isMeasurementRequested()) {
        throw new IOException("SEN6x measurement is stopped");
      }
      if (!sen6xSensor.isMeasuring()) {
        check();
        if (!sen6xSensor.isMeasuring()) {
          throw new IOException("SEN6x measurement is awaiting recovery");
        }
      }
      try {
        return reader.get();
      } catch (IOException failure) {
        if (!(failure instanceof InterruptedIOException) && !Thread.currentThread().isInterrupted()) {
          try {
            check();
          } catch (IOException recoveryFailure) {
            failure.addSuppressed(recoveryFailure);
          }
        }
        throw failure;
      }
    }
  }

  public void check() throws IOException {
    synchronized (sen6xSensor) {
      if (sen6xSensor.isMeasurementRequested() && sen6xMeasurementManager.hasLocked()) {
        sen6xSensor.reset();
      }
    }
  }
}
