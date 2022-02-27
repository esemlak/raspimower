package hns.erse.hardware.bricks;

import com.pi4j.io.i2c.I2C;
import hns.erse.hardware.virtual.GpioBase;
import hns.erse.hardware.states.GyroState;
import hns.erse.hardware.OverallContext;
import hns.erse.threads.InfoDisplay;

// GY521 Gyro / Accel Board

public class I2C_GY521 implements I2C_Brick
{
    private final static int I2C_GY521_ADDRESS = 0x68;

    private static final int Power_Mgmt_1 = 0x6b;
    private static final int Power_Mgmt_2 = 0x6c;

    private static final int REGISTER_GYRO_X = 0x43;
    private static final int REGISTER_GYRO_Y = 0x45;
    private static final int REGISTER_GYRO_Z = 0x47;

    private static final int REGISTER_ACCEL_X = 0x3b;
    private static final int REGISTER_ACCEL_Y = 0x3d;
    private static final int REGISTER_ACCEL_Z = 0x3f;

    private static final int REGISTER_GYRO_CONFIG = 0x1b;
    private static final int GYRO_CONFIG_250 = 0;
    private static final int GYRO_CONFIG_500 = 1;
    private static final int GYRO_CONFIG_1000 = 2;
    private static final int GYRO_CONFIG_2000 = 3;

    private static final int REGISTER_ACCEL_CONFIG = 0x1c;
    private static final int ACCEL_CONFIG_2 = 0;
    private static final int ACCEL_CONFIG_4 = 1;
    private static final int ACCEL_CONFIG_8 = 2;
    private static final int ACCEL_CONFIG_16 = 3;

    private static I2C_GY521 i2c_gy521 = null;
    private I2C gyro_i2c;

    public I2C_GY521() {
        try {
            gyro_i2c = GpioBase.createI2CPort(I2C_GY521_ADDRESS);
            gyro_i2c.writeRegister(Power_Mgmt_1, 0);
            gyro_i2c.writeRegister(REGISTER_GYRO_CONFIG, 0);
            gyro_i2c.writeRegister(REGISTER_ACCEL_CONFIG, 0);
            gyro_i2c.readRegisterWord(REGISTER_GYRO_X);
        } catch (Exception e) {
            InfoDisplay.getInstance().add_info_line("I2C_GY521 Exception : " + e.getMessage());
            gyro_i2c = null;
        }
    }

    public static I2C_GY521 getInstance() {
        if (i2c_gy521 == null) {
            i2c_gy521 = new I2C_GY521();
        }
        return i2c_gy521;
    }

    public GyroState getState() {

        if (gyro_i2c == null) {
            return new GyroState(0,0,0,0,0,0,0,0);
        }

        byte[] byte_buffer = new byte[6];
        gyro_i2c.readRegister(REGISTER_GYRO_X, byte_buffer, 6 );
        int[] int_buffer = OverallContext.get_three_ints_from_byte_buffer(byte_buffer);

        float gyro_x = int_buffer[0] / 131f;
        float gyro_y = int_buffer[1] / 131f;
        float gyro_z = int_buffer[2] / 131f;

        gyro_i2c.readRegister(REGISTER_ACCEL_X, byte_buffer, 6 );
        int_buffer = OverallContext.get_three_ints_from_byte_buffer(byte_buffer);

        float accel_x = int_buffer[0] / 16384f;
        float accel_y = int_buffer[1] / 16384f;
        float accel_z = int_buffer[2] / 16384f;


        float rot_x = (float) get_x_rotation(accel_x, accel_y, accel_z);
        float rot_y = (float) get_y_rotation(accel_x, accel_y, accel_z);

        return new GyroState(gyro_x, gyro_y, gyro_z, accel_x, accel_y, accel_z, rot_x, rot_y);
    }


    private float dist(float a, float b) {
        return (float)Math.sqrt((a * a) + (b * b));
    }

    private float get_y_rotation(float x,float y, float z) {
        float radians = (float)Math.atan2(x, dist(y,z));
        return (float)-(radians/Math.PI * 180f);
    }

    private float get_x_rotation(float x,float y, float z) {
        float radians = (float)Math.atan2(y, dist(x,z));
        return (float)(radians/Math.PI * 180f);
    }

    public boolean isPresent() {
        return (gyro_i2c != null);
    }
}
