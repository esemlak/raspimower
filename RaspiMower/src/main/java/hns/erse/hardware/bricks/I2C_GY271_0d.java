package hns.erse.hardware.bricks;

import com.pi4j.io.i2c.I2C;
import hns.erse.hardware.OverallContext;
import hns.erse.hardware.virtual.GpioBase;
import hns.erse.threads.InfoDisplay;

// GY-271 compass board / QMC5883

public class I2C_GY271_0d implements I2C_Brick {

    I2C i2c_device;

    private static I2C_GY271_0d i2C_gy271L;

    private final static int  GY271_I2C_ADDRESS = 0x0d;      /**< Default GY271 I2C Slave Address */

    private static final int ConfigurationRegister1 = 0x09;
    private static final int ConfigurationRegister2 = 0x0a;
    private static final int AxisXDataRegisterMSB = 0x00;
    private static final int AxisXDataRegisterLSB = 0x01;
    private static final int AxisYDataRegisterMSB = 0x02;
    private static final int AxisYDataRegisterLSB = 0x03;
    private static final int AxisZDataRegisterMSB = 0x04;
    private static final int AxisZDataRegisterLSB = 0x05;


    private static final int Mode_Continuous = 0b00000001;
    private static final int Mode_Standby = 0b00000000;

    private static final int ODR_10Hz = 0b00000000;
    private static final int ODR_50Hz = 0b00000100;
    private static final int ODR_100Hz = 0b00001000;
    private static final int ODR_200Hz = 0b00001100;

    private static final int RNG_2G = 0b00000000;
    private static final int RNG_8G = 0b00010000;

    private static final int OSR_512 = 0b00000000;
    private static final int OSR_256 = 0b01000000;
    private static final int OSR_128 = 0b10000000;
    private static final int OSR_64 = 0b11000000;


    private int scale_reg;
    private double scale = 1;

    public I2C_GY271_0d() {
        try {
            i2c_device = GpioBase.createI2CPort(GY271_I2C_ADDRESS);
            i2c_device.readRegisterWord(AxisXDataRegisterMSB);
            i2c_device.writeRegister(ConfigurationRegister1, Mode_Continuous | ODR_200Hz | RNG_8G | OSR_512);
            //setScale(2);
        } catch (Exception e) {
            InfoDisplay.getInstance().add_info_line("I2C_GY271 Exception : " + e.getMessage());
            i2c_device = null;
        }
    }

    public static I2C_GY271_0d getInstance() {
        if (i2C_gy271L == null) {
            i2C_gy271L = new I2C_GY271_0d();
        }
        return i2C_gy271L;
    }

    public double[] getAxes() {
        if (i2c_device == null) {
            return new double[]{0,0,0};
        }

//        byte[] byte_buffer = new byte[6];
//        i2c_device.readRegister(AxisXDataRegisterMSB, byte_buffer, 6 );
//        int[] int_buffer = OverallContext.get_three_ints_from_byte_buffer(byte_buffer);
//
//        double magno_x = int_buffer[0];
//        double magno_y = int_buffer[1];
//        double magno_z = int_buffer[2];


        double magno_x = i2c_device.readRegisterWord(AxisXDataRegisterMSB);
        double magno_y = i2c_device.readRegisterWord(AxisYDataRegisterMSB);
        double magno_z = i2c_device.readRegisterWord(AxisZDataRegisterMSB);

        if (magno_x == -4096) {
            magno_x = 0;
        } else {
            magno_x = Math.round(magno_x * 100) / 100f * scale;
        }

        if (magno_y == -4096) {
            magno_y = 0;
        } else {
            magno_y = Math.round(magno_y * 100) / 100f * scale;
        }

        if (magno_z == -4096) {
            magno_z = 0;
        } else {
            magno_z = Math.round(magno_z * 100) / 100f * scale;
        }

        return new double[]{magno_x, magno_y, magno_z};
    }

    public String getAxesDebug() {

        int magno1_x = i2c_device.readRegisterWord(AxisXDataRegisterMSB);
        int magno1_y = i2c_device.readRegisterWord(AxisYDataRegisterMSB);
        int magno1_z = i2c_device.readRegisterWord(AxisZDataRegisterMSB);


        byte[] byte_buffer = new byte[6];
        i2c_device.readRegister(AxisXDataRegisterMSB, byte_buffer, 6 );
        int[] int_buffer = OverallContext.get_three_ints_from_byte_buffer(byte_buffer);

        int magno2_x = int_buffer[0];
        int magno2_y = int_buffer[1];
        int magno2_z = int_buffer[2];

        return String.format("%s / %s / %s      %s / %s / %s", magno1_x, magno1_y, magno1_z, magno2_x, magno2_y, magno2_z);

    }


    public boolean isPresent() {
        return (i2c_device != null);
    }
}