package hns.erse.hardware.bricks;

import com.pi4j.io.i2c.I2C;
import hns.erse.hardware.virtual.GpioBase;
import hns.erse.hardware.states.GyroState;
import hns.erse.threads.InfoDisplay;

//I2C_ADXL345 adxl345 = I2C_ADXL345.getInstance();
//    for(int i=0;i<1000;i++) {
//System.out.println(String.valueOf(i) + " - " + adxl345.readState());
//}

public class I2C_ADXL345
{
    private final static int ADXL345_ADDRESS          = 0x53;
    private final static int ADXL345_REG_DEVID        = 0x00; // Device ID
    private final static int ADXL345_REG_DATAX0       = 0x32; // X-axis data 0 (6 bytes for X/Y/Z)
    private final static int ADXL345_REG_POWER_CTL    = 0x2D; // Power-saving features control
    private final static int ADXL345_REG_DATA_FORMAT  = 0x31;
    private final static int ADXL345_REG_BW_RATE      = 0x2C;
    private final static int ADXL345_DATARATE_0_10_HZ = 0x00;
    private final static int ADXL345_DATARATE_0_20_HZ = 0x01;
    private final static int ADXL345_DATARATE_0_39_HZ = 0x02;
    private final static int ADXL345_DATARATE_0_78_HZ = 0x03;
    private final static int ADXL345_DATARATE_1_56_HZ = 0x04;
    private final static int ADXL345_DATARATE_3_13_HZ = 0x05;
    private final static int ADXL345_DATARATE_6_25HZ  = 0x06;
    private final static int ADXL345_DATARATE_12_5_HZ = 0x07;
    private final static int ADXL345_DATARATE_25_HZ   = 0x08;
    private final static int ADXL345_DATARATE_50_HZ   = 0x09;
    private final static int ADXL345_DATARATE_100_HZ  = 0x0A; // (default)
    private final static int ADXL345_DATARATE_200_HZ  = 0x0B;
    private final static int ADXL345_DATARATE_400_HZ  = 0x0C;
    private final static int ADXL345_DATARATE_800_HZ  = 0x0D;
    private final static int ADXL345_DATARATE_1600_HZ = 0x0E;
    private final static int ADXL345_DATARATE_3200_HZ = 0x0F;
    private final static int ADXL345_RANGE_2_G        = 0x00; // +/-  2g (default)
    private final static int ADXL345_RANGE_4_G        = 0x01; // +/-  4g
    private final static int ADXL345_RANGE_8_G        = 0x02; // +/-  8g
    private final static int ADXL345_RANGE_16_G       = 0x03; // +/- 16g

    private static final int register_gyro_x = 0x43;
    private static final int register_gyro_y = 0x45;
    private static final int register_gyro_z = 0x47;

    private static final int register_accel_x = 0x3b;
    private static final int register_accel_y = 0x3d;
    private static final int register_accel_z = 0x3f;

    private static I2C_ADXL345 i2c_gy521;

    private I2C gyro_i2c;

    public I2C_ADXL345() {
        try {
            gyro_i2c = GpioBase.createI2CPort(ADXL345_ADDRESS);
            gyro_i2c.readRegisterWord(register_gyro_x);
        } catch (Exception e) {
            InfoDisplay.getInstance().add_info_line("I2C_ADXL345 Exception : " + e.getMessage());
            gyro_i2c = null;
        }
    }

    public static I2C_ADXL345 getInstance() {
        if (i2c_gy521 == null) {
            i2c_gy521 = new I2C_ADXL345();
        }
        return i2c_gy521;
    }

    public GyroState readState() {
        if (gyro_i2c == null) {
            return new GyroState(0,0,0,0,0,0,0,0);
        }

        float gyro_x = gyro_i2c.readRegisterWord(register_gyro_x);
        float gyro_y = gyro_i2c.readRegisterWord(register_gyro_y);
        float gyro_z = gyro_i2c.readRegisterWord(register_gyro_z);

        gyro_x = gyro_x / 131;
        gyro_y = gyro_y / 131;
        gyro_z = gyro_z / 131;

        float accel_x = gyro_i2c.readRegisterWord(register_accel_x);
        float accel_y = gyro_i2c.readRegisterWord(register_accel_y);
        float accel_z = gyro_i2c.readRegisterWord(register_accel_z);

        accel_x = accel_x / 16384;
        accel_y = accel_y / 16384;
        accel_z = accel_z / 16384;

        int rot_x = (int)get_x_rotation(accel_x, accel_y, accel_z);
        int rot_y = (int)get_y_rotation(accel_x, accel_y, accel_z);

        return new GyroState(gyro_x, gyro_y, gyro_z, accel_x, accel_y, accel_z, rot_x, rot_y);
    }

    private float dist(float a, float b) {
        return (float)Math.sqrt((a * a) + (b * b));
    }

    private float get_y_rotation(float x,float y, float z) {
        float radians = (float)Math.atan2(x, dist(y,z));
        return (float)-(radians/Math.PI * 180);
    }

    private float get_x_rotation(float x,float y, float z) {
        float radians = (float)Math.atan2(y, dist(x,z));
        return (float)(radians/Math.PI * 180);
    }
}
