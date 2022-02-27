package hns.erse.hardware.real;

import hns.erse.hardware.bricks.I2C_CJMCU_226;

public class Voltage {

    I2C_CJMCU_226 cjmcu_226;

    private static Voltage voltage;

    public static Voltage getInstance() {
        if (voltage == null) {
            voltage = new Voltage();
        }
        return voltage;
    }


    public Voltage() {
        cjmcu_226 = new I2C_CJMCU_226();
    }

    public String getState() {
        return cjmcu_226.getState();
    }

}

