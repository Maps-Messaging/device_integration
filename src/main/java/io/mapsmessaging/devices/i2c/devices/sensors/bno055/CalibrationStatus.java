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

package io.mapsmessaging.devices.i2c.devices.sensors.bno055;


public class CalibrationStatus {

    private final int myStatus;

    protected CalibrationStatus(int status) {
        myStatus = status;
    }

    public int getSystem() {
        return (myStatus >> 6) & 0x3;
    }

    public int getGryoscope() {
        return (myStatus >> 4) & 0x3;
    }

    public int getAccelerometer() {
        return (myStatus >> 2) & 0x3;
    }

    public int getMagnetometer() {
        return myStatus & 0x3;
    }

    public boolean isCalibrated() {
        return getGryoscope() == 3 && getAccelerometer() == 3 && getMagnetometer() == 3 && getSystem() == 3;
    }

    public String toString() {
        return "Gryoscope:" + getGryoscope() + " Accelerometer:" + getAccelerometer() + " Magnetometer:" + getMagnetometer() + " System:" + getSystem();
    }
}