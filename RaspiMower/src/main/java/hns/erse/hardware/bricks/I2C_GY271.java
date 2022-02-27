package hns.erse.hardware.bricks;

import com.pi4j.io.i2c.I2C;
import hns.erse.hardware.OverallContext;
import hns.erse.hardware.virtual.GpioBase;
import hns.erse.threads.InfoDisplay;

// GY-271 compass board / QMC5883

public class I2C_GY271 implements I2C_Brick {

    I2C i2c_device;

    private static I2C_GY271 i2C_gy271L;

    private final static int  GY271_I2C_ADDRESS = 0x1e;      /**< Default GY271 I2C Slave Address */

    private static final int ConfigurationRegisterA = 0x00;
    private static final int ConfigurationRegisterB = 0x01;
    private static final int ModeRegister = 0x02;

    private static final int AxisXDataRegisterMSB = 0x03;
    private static final int AxisXDataRegisterLSB = 0x04;

    private static final int AxisZDataRegisterMSB = 0x05;
    private static final int AxisZDataRegisterLSB = 0x06;

    private static final int AxisYDataRegisterMSB = 0x07;
    private static final int AxisYDataRegisterLSB = 0x08;


    private static final int StatusRegister = 0x09;
    private static final int IdentificationRegisterA = 0x10;
    private static final int IdentificationRegisterB = 0x11;
    private static final int IdentificationRegisterC = 0x12;


    private static final int MeasurementContinuous = 0x00;
    private static final int MeasurementSingleShot = 0x01;
    private static final int MeasurementIdle = 0x03;

    private int scale_reg;
    private double scale;

    public I2C_GY271() {
        try {
            i2c_device = GpioBase.createI2CPort(GY271_I2C_ADDRESS);
            i2c_device.readRegisterWord(AxisXDataRegisterMSB);
            i2c_device.writeRegister(ModeRegister, MeasurementContinuous);
            setScale(2);
        } catch (Exception e) {
            InfoDisplay.add_info_line_static("I2C_GY271 Exception : " + e.getMessage());
            i2c_device = null;
        }
    }

    public static I2C_GY271 getInstance() {
        if (i2C_gy271L == null) {
            i2C_gy271L = new I2C_GY271();
        }
        return i2C_gy271L;
    }



    public double[] getAxes() {
        if (i2c_device == null) {
            return new double[]{0,0,0};
        }

        byte[] byte_buffer = new byte[6];
        i2c_device.readRegister(AxisXDataRegisterMSB, byte_buffer, 6 );
        int[] int_buffer = OverallContext.get_three_ints_from_byte_buffer(byte_buffer);

        double magno_x = int_buffer[0];
        double magno_y = int_buffer[2];
        double magno_z = int_buffer[1];

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
        int magno2_y = int_buffer[2];
        int magno2_z = int_buffer[1];

        return String.format("%s / %s / %s      %s / %s / %s", magno1_x, magno1_y, magno1_z, magno2_x, magno2_y, magno2_z);

    }

    private void setScale(int gauss) {
        switch (gauss) {
            case 0:
                scale_reg = 0x00;
                scale = 0.73;
                break;
            case 1:
                scale_reg = 0x01;
                scale = 0.92;
                break;
            case 2:
                scale_reg = 0x02;
                scale = 1.22;
                break;
            case 3:
                scale_reg = 0x03;
                scale = 1.52;
                break;
            case 4:
                scale_reg = 0x04;
                scale = 2.27;
                break;
            case 5:
                scale_reg = 0x05;
                scale = 2.56;
                break;
            case 6:
                scale_reg = 0x06;
                scale = 3.03;
                break;
            case 7:
                scale_reg = 0x07;
                scale = 4.35;
                break;
            default:
                scale_reg = 0x01;
                scale = 0.92;
                break;
        }

        scale_reg = scale_reg << 5;
        i2c_device.writeRegister(ConfigurationRegisterB, scale_reg);
    }
    
    public boolean isPresent() {
        return (i2c_device != null);
    }
}