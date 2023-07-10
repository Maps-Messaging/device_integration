package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class O3Module extends SensorModule {

    public O3Module() {
        super();
    }

    @Override
    protected float calculateSensorConcentration(float temperature, float rawConcentration) {
        if (temperature > -20 && temperature <= 0) {
            return rawConcentration / (0.015f * temperature + 1.1f) - 0.05f;
        }
        if (temperature > 0 && temperature <= 20) {
            return rawConcentration - (0.01f * temperature);
        }
        if (temperature > 20 && temperature <= 40) {
            return rawConcentration - (0.005f * temperature + 0.4f);
        }
        if (temperature > 40 && temperature < 50) {
            return rawConcentration - (0.067f * temperature - 1.68f);
        }
        return 0;
    }
}