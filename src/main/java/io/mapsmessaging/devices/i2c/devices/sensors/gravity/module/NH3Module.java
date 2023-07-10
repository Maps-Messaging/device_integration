package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class NH3Module extends SensorModule {

    public NH3Module() {
        super();
    }

    @Override
    protected float calculateSensorConcentration(float temperature, float rawConcentration) {
        if (temperature > -40 && temperature <= 0) {
            return rawConcentration / (0.006f * temperature + 0.95f) - (0.006f * temperature + 0.25f);
        }
        if (temperature > 0 && temperature <= 20) {
            return rawConcentration / (0.006f * temperature + 0.95f) - (0.012f * temperature + 0.25f);
        }
        if (temperature > 20 && temperature < 40) {
            return rawConcentration / (0.005f * temperature + 1.08f) - (0.1f * temperature + 2);
        }
        return 0;
    }
}
