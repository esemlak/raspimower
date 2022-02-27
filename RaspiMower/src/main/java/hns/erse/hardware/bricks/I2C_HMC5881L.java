package hns.erse.hardware.bricks;

import com.pi4j.io.i2c.I2C;
import hns.erse.hardware.virtual.GpioBase;
import hns.erse.threads.InfoDisplay;

// GY-271 compass board

public class I2C_HMC5881L implements I2C_Brick {

    I2C i2c_device;

    private static I2C_HMC5881L i2c_hmc5881l;

    private final static int  HMC5881L_I2C_ADDRESS = 0x1e;      /**< Default GY271 I2C Slave Address */

    private final static int ConfigurationRegisterA = 0x00;
    private final static int ConfigurationRegisterB = 0x01;
    private final static int ModeRegister = 0x02;
    private final static int AxisXDataRegisterMSB = 0x03;
    private final static int AxisXDataRegisterLSB = 0x04;
    private final static int AxisZDataRegisterMSB = 0x05;
    private final static int AxisZDataRegisterLSB = 0x06;
    private final static int AxisYDataRegisterMSB = 0x07;
    private final static int AxisYDataRegisterLSB = 0x08;
    private final static int StatusRegister = 0x09;
    private final static int IdentificationRegisterA = 0x10;
    private final static int IdentificationRegisterB = 0x11;
    private final static int IdentificationRegisterC = 0x12;

    private final static int MeasurementContinuous = 0x00;
    private final static int MeasurementSingleShot = 0x01;
    private final static int MeasurementIdle = 0x03;
    private double declinationDeg;
    private double declinationMin;
    private double declination;
    private int scale_reg;
    private double scale;

    private final static float Gauss_088 = 0.88f;
    private final static float Gauss_13 = 1.3f;
    private final static float Gauss_19 = 1.9f;
    private final static float Gauss_25 = 2.5f;
    private final static float Gauss_40 = 4.0f;
    private final static float Gauss_47 = 4.7f;
    private final static float Gauss_56 = 5.6f;
    private final static float Gauss_81 = 8.1f;

    public I2C_HMC5881L() {
        try {
            i2c_device = GpioBase.createI2CPort(HMC5881L_I2C_ADDRESS);
            i2c_device.writeRegister(ModeRegister, MeasurementContinuous);
    //        i2c_device.writeRegister(ConfigurationRegisterA, 0xF8);
            i2c_device.readRegisterWord(AxisXDataRegisterMSB);
            setScale(Gauss_13);
            setDeclination(4, 26);
        } catch (Exception e) {
            InfoDisplay.getInstance().add_info_line("I2C_HMC5881L Exception : " + e.getMessage());
            i2c_device = null;
        }
    }

    public static I2C_HMC5881L getInstance() {
        if (i2c_hmc5881l == null) {
            i2c_hmc5881l = new I2C_HMC5881L();
        }
        return i2c_hmc5881l;
    }

    public double[] getHeading() {
        double[] scaled_x_y_z = getAxes();
        double scaled_x = scaled_x_y_z[0];
        double scaled_y = scaled_x_y_z[1];
        double scaled_z = scaled_x_y_z[2];

        double headingRad = Math.atan2(scaled_y, scaled_x);
        headingRad += declination;

        // Correct for reversed heading
        if (headingRad < 0) {
            headingRad += 2 * Math.PI;
        }

        //Check for wrap and compensate
        if (headingRad > 2 * Math.PI) {
            headingRad -= 2 * Math.PI;
        }

        //Convert to degrees from radians
        double headingDeg = headingRad * 180 / Math.PI;
        double degrees = Math.floor(headingDeg);
        double minutes = Math.round(((headingDeg - degrees) * 60));
        return new double[]{degrees, minutes};
    }


    public double[] getAxes() {
        if (i2c_device == null) {
            return new double[]{0,0,0};
        }
        double magno_x = i2c_device.readRegisterWord(AxisXDataRegisterMSB);
        double magno_y = i2c_device.readRegisterWord(AxisYDataRegisterMSB);
        double magno_z = i2c_device.readRegisterWord(AxisZDataRegisterMSB);

        //        scale = 1;

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

    public void setDeclination(float degree, float min) {
        declinationDeg = degree;
        declinationMin = min;
        declination = (degree+min/60f) * (Math.PI/180);
    }


    public void setScale(float gauss) {
        if (i2c_device == null) {
            return;
        }

        if (gauss == Gauss_088) {
            scale_reg = 0x00;
            scale = 0.73;
        } else if (gauss == Gauss_13) {
            scale_reg = 0x01;
            scale = 0.92;
        } else if ( gauss == Gauss_19) {
            scale_reg = 0x02;
            scale = 1.22;
        } else if ( gauss == Gauss_25) {
            scale_reg = 0x03;
            scale = 1.52;
        } else if ( gauss == Gauss_40) {
            scale_reg = 0x04;
            scale = 2.27;
        } else if ( gauss == Gauss_47) {
            scale_reg = 0x05;
            scale = 2.56;
        } else if ( gauss == Gauss_56) {
            scale_reg = 0x06;
            scale = 3.03;
        } else if ( gauss == Gauss_81) {
            scale_reg = 0x07;
            scale = 4.35;
        }
        scale_reg = scale_reg << 5;
        i2c_device.writeRegister(ConfigurationRegisterB, scale_reg);
//        System.out.println("setScale scale " + scale);
    }

    public boolean isPresent() {
        return (i2c_device != null);
    }

}
