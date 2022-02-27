package hns.erse.hardware.real;

import hns.erse.hardware.virtual.Motor;

public class WheelMotor extends Motor {

    public WheelMotor(int pwm_pin_number, int direction_pin_number, boolean reverse_direction, String name ) {
        super(pwm_pin_number, direction_pin_number, reverse_direction, name, Motor.IO_Type.Local_PWM);
    }

}
