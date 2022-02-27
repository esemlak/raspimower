package hns.erse.hardware.virtual;

import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.pwm.Pwm;
import hns.erse.hardware.OverallContext;
import hns.erse.hardware.bricks.I2C_PCA9685;

public class Motor {

    private int pwm_pin_number;
    private int direction_pin_number;

    private DigitalOutput direction_gpio;
    private Pwm pwm_gpio;
    private boolean reverse_direction;

    public enum IO_Type {Local_PWM, I2C_PWM};
    private IO_Type io_type = IO_Type.Local_PWM;

    private static int frequency = 20000;

    public static final int MaxSpeed = 99;
    public static final int MinSpeed = 15;
    public static final int NegativeMinSpeed = -30;

    public enum Direction {DIRECTION_FORWARD, DIRECTION_BACKWARD, DIRECTION_STOP};

    private Direction current_direction = Direction.DIRECTION_STOP;
    private int current_speed = 0;

    private String name = "Motor";

    public Motor(int pwm_pin_number, int direction_pin_number, boolean reverse_direction, String name, IO_Type io_type) {
        OverallContext.getInstance().add_debug_line("new Motor " + name + " io_type " + io_type + " pwm_pin_number " + pwm_pin_number + " / direction_pin_number " + direction_pin_number, 20);
        this.name = name;
        this.pwm_pin_number = pwm_pin_number;
        this.direction_pin_number = direction_pin_number;
        this.reverse_direction = reverse_direction;
        this.io_type = io_type;
        this.setup_gpio();
    }


    private void setup_gpio() {
        direction_gpio = GpioBase.createDigitalGpioPort(direction_pin_number);
        if (io_type == IO_Type.Local_PWM) {
            pwm_gpio = GpioBase.createPwmGpioPort(pwm_pin_number);
        } else {
            I2C_PCA9685.getInstance();
        }
    }

    public void set_speed(int speed)  {
        OverallContext.getInstance().add_debug_line("Motor " + name + " set_speed " + speed, 20);
        if (speed == 0) {
            stop();
            return;
        }
        speed = (int) Math.max(Motor.MinSpeed, speed);
        speed = (int) Math.min(Motor.MaxSpeed, speed);
        if (current_speed == speed) {
            return;
        }
        current_speed = speed;
        if (io_type == IO_Type.Local_PWM) {
            pwm_gpio.on(speed, frequency);
        } else {
            I2C_PCA9685.getInstance().set_dutycycle(pwm_pin_number, speed);
        }
    }

    public void stop() {
        OverallContext.getInstance().add_debug_line("Motor " + name + " stop" , 20);
        current_speed = 0;
        if (io_type == IO_Type.Local_PWM) {
            pwm_gpio.off();
        } else {
            I2C_PCA9685.getInstance().off(pwm_pin_number);
        }
    }

    public Direction get_direction() {
        return current_direction;
    }

    public void set_direction(Direction direction)  {
        OverallContext.getInstance().add_debug_line("Motor " + name + " set_direction " + direction, 20);
        if (!direction.equals(current_direction)) {
            if (current_speed > 0) {
                stop();
            }
        }
        current_direction = direction;
        if (direction.equals(Direction.DIRECTION_STOP)) {
            return;
        }

        if (reverse_direction) {
            direction_gpio.setState(direction.equals(Direction.DIRECTION_BACKWARD));
        } else {
            direction_gpio.setState(direction.equals(Direction.DIRECTION_FORWARD));
        }
    }

    public String getState() {
        return "speed " + current_speed + " direction " + current_direction;
    }

    private Direction get_real_direction() {
        Direction real_direction = null;
        if (reverse_direction) {
            switch (current_direction) {
                case DIRECTION_FORWARD:
                    real_direction = Direction.DIRECTION_BACKWARD;
                    break;
                case DIRECTION_BACKWARD:
                    real_direction = Direction.DIRECTION_FORWARD;
                    break;
                case DIRECTION_STOP:
                    real_direction = Direction.DIRECTION_STOP;
                    break;
            }
        } else {
            real_direction = current_direction;
        }
        return real_direction;
    }

    public int get_speed() {
        return current_speed;
    }

}
