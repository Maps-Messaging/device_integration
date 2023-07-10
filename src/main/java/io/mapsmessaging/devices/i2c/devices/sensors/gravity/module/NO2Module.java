package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class NO2Module extends SensorModule {

    public NO2Module() {
        super();
    }

    @Override
    protected float calculateSensorConcentration(float temperature, float rawConcentration) {
        if (temperature > -20 && temperature <= 0) {
            return rawConcentration / (0.005f * temperature + 0.9f) - (-0.0025f * temperature + 0.005f);
        }
        if (temperature > 0 && temperature <= 20) {
            return rawConcentration / (0.005f * temperature + 0.9f) - (0.005f * temperature + 0.005f);
        }
        if (temperature > 20 && temperature <= 40) {
            return rawConcentration / (0.005f * temperature + 0.9f) - (0.0025f * temperature + 0.3f);
        }
        if (temperature > 40 && temperature < 50) {
            return rawConcentration / (0.005f * temperature + 0.9f) - (-0.048f * temperature + 0.92f);
        }
        return 0;
    }
}