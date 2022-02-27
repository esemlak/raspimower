package hns.erse.hardware;

import hns.erse.hardware.real.*;
import hns.erse.hardware.virtual.Motor;
import hns.erse.remote.RemoteClient;
import hns.erse.remote.RemoteHeartBeat;
import hns.erse.threads.InfoDisplay;
import hns.erse.threads.UserInput;
import org.jline.utils.Display;

import java.util.regex.Pattern;

public class Mower extends Thread {

    private static Mower mower;

    private WheelMotor left_motor;
    private WheelMotor right_motor;
    private CuttingMotor cutting_motor;

    public Gps gps;
    public Gyro gyro;
    public Compass compass;

    public UserInput user_input;
    public InfoDisplay info_display;
    private RemoteHeartBeat remote_heartbeat;


    private enum RunMode {Idle, Driving, Mowing};

    private enum TurnMode {Straight, Left, Right};

    public static final String Command_Speed = "speed";
    public static final String Command_Steer = "steer";
    public static final String Command_Stop = "stop";
    public static final String Command_Mow = "mow";
    public static final String Command_RemoteTimeOut = "remote_timeout";
    public static final String Command_RemoteHeartbeat = "remote_heartbeat";

    private Motor.Direction mower_direction = Motor.Direction.DIRECTION_STOP;

    private int mower_speed = 0;

    private RunMode run_mode = RunMode.Idle;

    private static final int left_motor_pwm_pin = 18;
    private static final int left_motor_direction_pin = 15;

    private static final int right_motor_pwm_pin = 13;
    private static final int right_motor_direction_pin = 14;

    private static final int cutting_motor_i2c_pwm_pin = 0;
    private static final int cutting_motor_direction_pin = 17;

    private static RemoteClient remote_client = null;

    public static Mower getInstance() {
        if (mower == null) {
            mower = new Mower();
            mower.init();
        }
        return mower;
    }

    public void init() {
        OverallContext.getInstance();

        info_display = InfoDisplay.setInstance(this);

        left_motor = new WheelMotor(left_motor_pwm_pin, left_motor_direction_pin, true, "left motor");
        right_motor = new WheelMotor(right_motor_pwm_pin, right_motor_direction_pin, false, "right motor");

//        cutting_motor = new CuttingMotor(cutting_motor_i2c_pwm_pin, cutting_motor_direction_pin);
//        gps = Gps.getInstance();
        gyro = Gyro.getInstance();
        compass = Compass.getInstance();

        user_input = UserInput.getInstance(this);

        info_display.start_display();
        user_input.start_input();

        remote_heartbeat = new RemoteHeartBeat(this);
        remote_heartbeat.start();

        remote_client = new RemoteClient(this, remote_heartbeat);
        remote_client.start();
    }

    public void run() {

    }

    public RunMode getMode() {
        return run_mode;
    }

    private void shutDown() {
        OverallContext.getInstance().getPi4j().shutdown();
    }

    public void handle_keystroke(char keystroke) {
        OverallContext.getInstance().add_debug_line("handle_keystroke " + keystroke, 1);
        switch (keystroke) {
            case 'x':
                shutdown();
                break;

            case 'w':
                if (mower_direction == Motor.Direction.DIRECTION_STOP) {
                    mower_direction = Motor.Direction.DIRECTION_FORWARD;
                    mower_speed = Motor.MinSpeed - 5;
                    left_motor.set_direction(Motor.Direction.DIRECTION_FORWARD);
                    right_motor.set_direction(Motor.Direction.DIRECTION_FORWARD);
                }
                if (mower_direction == Motor.Direction.DIRECTION_FORWARD) {
                    wheel_motors_set_new_speed(mower_speed+5);
                } else {
                    wheel_motors_set_new_speed(mower_speed-5);
                }
                break;

            case 'y':
                if (mower_direction == Motor.Direction.DIRECTION_STOP) {
                    mower_direction = Motor.Direction.DIRECTION_BACKWARD;
                    mower_speed = Motor.MinSpeed - 5;
                    left_motor.set_direction(Motor.Direction.DIRECTION_BACKWARD);
                    right_motor.set_direction(Motor.Direction.DIRECTION_BACKWARD);
                }
                if (mower_direction == Motor.Direction.DIRECTION_FORWARD) {
                    wheel_motors_set_new_speed(mower_speed-5);
                } else {
                    wheel_motors_set_new_speed(mower_speed+5);
                }
                break;

            case 'a':
                wheel_motors_turn(TurnMode.Left, true, 1, true);
                break;

            case 'd':
                wheel_motors_turn(TurnMode.Right, true, 1, true);
                break;

            case 'ö':
                wheel_motors_turn(TurnMode.Left, false, 0.3f, true);
                break;

            case 'ä':
                wheel_motors_turn(TurnMode.Right, false,0.3f, true);
                break;

            case 's':
                wheel_motors_stop(false);
                break;

        }
    }

    private void shutdown() {

        InfoDisplay.getInstance().add_info_line("Shutdown ....");
        if (left_motor != null) {
            left_motor.stop();
        }
        if (right_motor != null) {
            right_motor.stop();
        }
        if (cutting_motor != null) {
            cutting_motor.stop();
        }

        OverallContext.getInstance().getPi4j().shutdown();

        UserInput.getInstance().stop_input();
        InfoDisplay.getInstance().stop_display();
        System.out.println("shutdown");
        this.stop();
        System.exit(0);;
    }

    public Motor get_left_motor() {
        return left_motor;
    }

    public Motor get_right_motor() {
        return right_motor;
    }

    public Motor get_cutting_motor() {
        return cutting_motor;
    }

    private void wheel_motors_set_new_speed(int new_speed) {
        if (new_speed > Motor.MaxSpeed) {
            new_speed = Motor.MaxSpeed;
        }

        if (new_speed < Motor.NegativeMinSpeed) {
            new_speed = Motor.NegativeMinSpeed;
        }

        OverallContext.getInstance().add_debug_line("wheel_motors_set_new_speed  " + new_speed, 1);
        if (new_speed == mower_speed)
            return;

        if (new_speed > mower_speed) {
            for (int speed=mower_speed; speed<=new_speed; speed++) {
                if (speed > 0 && mower_direction != Motor.Direction.DIRECTION_FORWARD) {
                    mower_direction = Motor.Direction.DIRECTION_FORWARD;
                    left_motor.set_direction(Motor.Direction.DIRECTION_FORWARD);
                    right_motor.set_direction(Motor.Direction.DIRECTION_FORWARD);
                }
                if (speed < 0 && mower_direction != Motor.Direction.DIRECTION_BACKWARD) {
                    mower_direction = Motor.Direction.DIRECTION_BACKWARD;
                    left_motor.set_direction(Motor.Direction.DIRECTION_BACKWARD);
                    right_motor.set_direction(Motor.Direction.DIRECTION_BACKWARD);
                }
                int abs_speed = Math.abs(speed);
                left_motor.set_speed(abs_speed);
                right_motor.set_speed(abs_speed);
                if (abs_speed > 15) {
                    OverallContext.sleep(20);
                }
            }
        } else {
            for (int speed=mower_speed; speed>=new_speed; speed--) {
                if (speed > 0 && mower_direction != Motor.Direction.DIRECTION_FORWARD) {
                    mower_direction = Motor.Direction.DIRECTION_FORWARD;
                    left_motor.set_direction(Motor.Direction.DIRECTION_FORWARD);
                    right_motor.set_direction(Motor.Direction.DIRECTION_FORWARD);
                }
                if (speed < 0 && mower_direction != Motor.Direction.DIRECTION_BACKWARD) {
                    mower_direction = Motor.Direction.DIRECTION_BACKWARD;
                    left_motor.set_direction(Motor.Direction.DIRECTION_BACKWARD);
                    right_motor.set_direction(Motor.Direction.DIRECTION_BACKWARD);
                }
                int abs_speed = Math.abs(speed);
                left_motor.set_speed(abs_speed);
                right_motor.set_speed(abs_speed);
                if (abs_speed > 15) {
                    OverallContext.sleep(20);
                }
            }
            if (Math.abs(new_speed) < Motor.MinSpeed) {
                wheel_motors_stop(true);
                mower_direction = Motor.Direction.DIRECTION_STOP;
            }
        }

        mower_speed = new_speed;
    }

    private void wheel_motors_turn(TurnMode turn_mode, boolean stop, float turn_percentage, boolean return_straight) {

        int use_mower_speed = mower_speed;
        if (mower_speed != 0) {

            turn_percentage = (float)Math.min(turn_percentage, 0.8f);

            int turn_speed = (int)((float)use_mower_speed * (1f-turn_percentage));

            OverallContext.getInstance().add_debug_line("wheel_motors_turn run turn_mode " + turn_mode + " turn_percentage " + turn_percentage + " turn_speed " + turn_speed, 1);

            if (Math.abs(turn_speed) < Motor.MinSpeed) {
                if (mower_speed > 30) {
                    turn_speed = ((int) Math.signum(turn_speed)) * Motor.MinSpeed;
                } else {
                    turn_speed = 0;
                }
            }

            switch(turn_mode) {
                case Left:
                    left_motor.set_speed(turn_speed);
                    right_motor.set_speed(use_mower_speed);
                    break;
                case Right:
                    left_motor.set_speed(use_mower_speed);
                    right_motor.set_speed(turn_speed);
                    break;
                default:
                    left_motor.set_speed(use_mower_speed);
                    right_motor.set_speed(use_mower_speed);
            }
            if (return_straight && turn_percentage != 0) {
                OverallContext.sleep(1000);
                left_motor.set_speed(mower_speed);
                right_motor.set_speed(mower_speed);
            }
        } else {
            if (turn_percentage > 0) {
                int turn_speed = (int)(Motor.MinSpeed*2f*turn_percentage);
                OverallContext.getInstance().add_debug_line("wheel_motors_turn stop turn_mode " + turn_mode + " turn_percentage " + turn_percentage + " turn_speed " + turn_speed, 1);
                if (turn_mode == TurnMode.Left) {
                    left_motor.set_direction(Motor.Direction.DIRECTION_BACKWARD);
                    right_motor.set_direction(Motor.Direction.DIRECTION_FORWARD);
                    left_motor.set_speed(turn_speed);
                    right_motor.set_speed(turn_speed);
                } else {
                    left_motor.set_direction(Motor.Direction.DIRECTION_FORWARD);
                    right_motor.set_direction(Motor.Direction.DIRECTION_BACKWARD);
                    left_motor.set_speed(turn_speed);
                    right_motor.set_speed(turn_speed);
                }
            }
            if (stop || turn_percentage == 0 ) {
                if (stop) {
                    OverallContext.sleep(1000);
                }
                left_motor.stop();
                right_motor.stop();
                if (mower_direction == Motor.Direction.DIRECTION_FORWARD) {
                    left_motor.set_direction(Motor.Direction.DIRECTION_FORWARD);
                    right_motor.set_direction(Motor.Direction.DIRECTION_FORWARD);
                } else {
                    left_motor.set_direction(Motor.Direction.DIRECTION_BACKWARD);
                    right_motor.set_direction(Motor.Direction.DIRECTION_BACKWARD);
                }
            }
        }
    }

    private void wheel_motors_stop(boolean immediately) {
        OverallContext.getInstance().add_debug_line("wheel_motors_stop", 1);
        if (immediately || !(left_motor.get_direction().equals(right_motor.get_direction()))) {
            left_motor.stop();
            right_motor.stop();
        } else {
            wheel_motors_set_new_speed(0);
        }
        mower_speed = 0;
        left_motor.set_direction(Motor.Direction.DIRECTION_STOP);
        right_motor.set_direction(Motor.Direction.DIRECTION_STOP);
        mower_direction = Motor.Direction.DIRECTION_STOP;
    }

    public String getCurrentState() {
        return "speed " + mower_speed + " direction " + mower_direction;
    }


    public void execute_remote_command(String command) {
        InfoDisplay.getInstance().add_info_line("remote command: " + command);
        String[] parts = command.split(Pattern.quote("|"));

        String command_part = parts[0];

        String value_part = "0";
        if (parts.length > 1) {
            value_part = parts[1];
        }

        float value_value = 0;

        try {
            value_value = Integer.parseInt(value_part);
        } catch (Exception e) {
            InfoDisplay.getInstance().add_info_line("remote command: value_part not numeric " + value_part);
            value_value = 0;
        }

        switch (command_part) {
            case Command_Speed:
                if (value_value != 0) {
                    wheel_motors_set_new_speed((int) value_value);
                } else {
                    wheel_motors_stop(false);
                }
                break;
            case Command_Steer:
                TurnMode turn_mode = TurnMode.Straight;
                if (value_value < 0) {
                    turn_mode = TurnMode.Left;
                }
                if (value_value > 0) {
                    turn_mode = TurnMode.Right;
                }
                float turn_percentage = Math.abs(value_value)/100f;
                wheel_motors_turn(turn_mode, false, turn_percentage, false);
                break;
            case Command_Stop:
            case Command_RemoteTimeOut:
                wheel_motors_stop(true);
                break;
            case Command_Mow:
                break;
        }
    }

}
