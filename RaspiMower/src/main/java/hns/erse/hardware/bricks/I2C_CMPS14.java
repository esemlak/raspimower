package hns.erse.hardware.bricks;

import com.pi4j.io.i2c.I2C;
import hns.erse.hardware.OverallContext;
import hns.erse.hardware.virtual.GpioBase;
import hns.erse.threads.InfoDisplay;

// I2C_CMPS14 compass board

public class I2C_CMPS14 implements I2C_Brick {

    I2C i2c_device;

    private static I2C_CMPS14 i2c_cmps14;

    private final static int I2C_CMPS14_ADDRESS = 0x60;      /**< Default I2C_CMPS14 I2C Slave Address */

    private static final int CONTROL_Register = 0;

    private static final int BEARING_Register = 2;
    private static final int PITCH_Register = 4;
    private static final int ROLL_Register = 5;

    private static final int MAGNETX_Register  = 6;
    private static final int MAGNETY_Register  = 8;
    private static final int MAGNETZ_Register = 10;

    private static final int ACCELEROX_Register = 12;
    private static final int ACCELEROY_Register = 14;
    private static final int ACCELEROZ_Register = 16;

    private static final int GYROX_Register = 18;
    private static final int GYROY_Register = 20;
    private static final int GYROZ_Register = 22;

    private static final int ONE_BYTE = 1;
    private static final int TWO_BYTES = 2;
    private static final int FOUR_BYTES = 4;
    private static final int SIX_BYTES  = 6;
    

    public I2C_CMPS14() {
        try {
            i2c_device = GpioBase.createI2CPort(I2C_CMPS14_ADDRESS);
            i2c_device.readRegisterWord(CONTROL_Register);
//            setScale(2);
        } catch (Exception e) {
            InfoDisplay.add_info_line_static("I2C_CMPS14 Exception : " + e.getMessage());
            i2c_device = null;
        }
    }

    public static I2C_CMPS14 getInstance() {
        if (i2c_cmps14 == null) {
            i2c_cmps14 = new I2C_CMPS14();
        }
        return i2c_cmps14;
    }

    public float get_bearing() {
        int bearing = i2c_device.readRegisterWord(BEARING_Register);
        return bearing/10f;
    }

    public boolean isPresent() {
        return (i2c_device != null);
    }
}