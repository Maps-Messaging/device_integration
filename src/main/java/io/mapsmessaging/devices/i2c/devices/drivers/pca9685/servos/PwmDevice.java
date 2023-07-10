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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685.servos;

import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.PCA9685Device;
import io.mapsmessaging.devices.util.Delay;

import java.io.IOException;

public abstract class PwmDevice {

    protected final PCA9685Device myPWMController;
    protected final short myServoPort;
    protected final AngleResponse myResponse;


    protected PwmDevice(PCA9685Device pwm, short servoId, AngleResponse response) throws IOException {
        myPWMController = pwm;
        myServoPort = servoId;
        myResponse = response;
        setPosition(myResponse.getMin());
        Delay.pause(500); // allow the servo to get to min
        setPosition(myResponse.getMax());
        Delay.pause(500); // allow the servo to get to max
        setPosition(myResponse.getIdle()); // Set to idle position
    }

    public int getPort() {
        return myServoPort;
    }

    abstract void setPosition(float angle) throws IOException;

    public void close() throws IOException {
        setPosition(myResponse.getIdle());
    }
}