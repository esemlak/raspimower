package hns.erse.hardware.real;

import hns.erse.hardware.virtual.Motor;

public class CuttingMotor extends Motor {
    private int pwm_pin;

    public CuttingMotor(int pwm_pin_number, int direction_pin_number) {
        super(pwm_pin_number, direction_pin_number, false, "Cutting Motor", Motor.IO_Type.I2C_PWM);
    }

}
