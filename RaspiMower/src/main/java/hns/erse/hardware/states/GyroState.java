package hns.erse.hardware.states;

public class GyroState {
    private float gyro_x;
    private float gyro_y;
    private float gyro_z;

    private float accel_x;
    private float accel_y;
    private float accel_z;

    private float rot_x;
    private float rot_y;

    public GyroState(float gyro_x, float gyro_y, float gyro_z, float accel_x, float accel_y, float accel_z, float rot_x, float rot_y) {
        this.gyro_x = gyro_x;
        this.gyro_y = gyro_y;
        this.gyro_z = gyro_z;

        this.accel_x = accel_x;
        this.accel_y = accel_y;
        this.accel_z = accel_z;

        this.rot_x = rot_x;
        this.rot_y = rot_y;
    }


    public String toString() {
        return String.format("%s / %s / %s   %s / %s / %s   %s / %s", gyro_x, gyro_y, gyro_z, accel_x, accel_y, accel_z, rot_x, rot_y);
    }

    public String toStringGyro() {
        return String.format("%s / %s / %s ", gyro_x, gyro_y, gyro_z);
    }

    public String toStringAccel() {
        return String.format("%s / %s / %s", accel_x, accel_y, accel_z);
    }

    public String toStringRot() {
        return String.format("%s / %s", rot_x, rot_y);
    }

    public float getGyro_z() {
        return gyro_z;
    }
}
