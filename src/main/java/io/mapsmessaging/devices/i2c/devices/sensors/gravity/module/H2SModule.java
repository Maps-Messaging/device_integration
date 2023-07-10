package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class H2SModule extends SensorModule {

    public H2SModule() {
        super();
    }

    @Override
    protected float calculateSensorConcentration(float temperature, float rawConcentration) {
        if (temperature > -40 && temperature <= 20) {
            return rawConcentration / (0.005f * temperature + 0.92f);
        }
        if (temperature > 20 && temperature <= 60) {
            return rawConcentration / (0.015f * temperature - 0.3f);
        }
        return 0;
    }
}
