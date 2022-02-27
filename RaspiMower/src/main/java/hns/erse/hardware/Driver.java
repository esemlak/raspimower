package hns.erse.hardware;

import hns.erse.hardware.real.WheelMotor;

public class Driver {

    private WheelMotor left_motor;
    private WheelMotor right_motor;
    private int turn_radius = 0;
    private int target_speed = 0;
    private int current_speed = 0;

    public enum DriveMode {Driving, Turning, Accelerating, Stopped};

    private DriveMode current_drive_mode = DriveMode.Stopped;

    public Driver(WheelMotor left_motor, WheelMotor right_motor) {
        this.left_motor = left_motor;
        this.right_motor = right_motor;
    }

    public void accelerate(int target_speed) {
        if (!current_drive_mode.equals(DriveMode.Turning)) {
            this.target_speed = target_speed;
        }
    }

}
