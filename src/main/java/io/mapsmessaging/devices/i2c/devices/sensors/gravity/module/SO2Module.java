package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class SO2Module extends SensorModule {

    public SO2Module() {
        super();
    }

    @Override
    protected float calculateSensorConcentration(float temperature, float rawConcentration) {
        if (temperature > -40 && temperature <= 40) {
            return rawConcentration / (0.006f * temperature + 0.95f);
        }
        if (temperature > 40 && temperature <= 60) {
            return rawConcentration / (0.006f * temperature + 0.95f) - (0.05f * temperature - 2);
        }
        return rawConcentration;
    }

}