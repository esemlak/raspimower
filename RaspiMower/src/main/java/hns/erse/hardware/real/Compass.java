package hns.erse.hardware.real;

import hns.erse.hardware.OverallContext;
import hns.erse.hardware.bricks.I2C_CMPS14;
import hns.erse.hardware.bricks.I2C_GY271;
import hns.erse.hardware.bricks.I2C_GY271_0d;
import hns.erse.hardware.states.CompassCalibration;

//Compass compass = Compass.getInstance();
//
//for(int i=0;i<1000;i++) {
//System.out.println(String.format("%d - %f / %f / %f / %f",  i, values[0], values[1], values[2], values[3]));
//OverallContext.sleep(500);
//}
//

public class Compass implements RealDevice {

    // compass type GY-271
    // similar to hmc5883l

    private static Compass compass;

    //I2C_GY271_0d i2c_gy271;
    //I2C_GY271 i2c_gy271;
    I2C_CMPS14 i2C_cmps14;

    private double declinationDeg;
    private double declinationMin;
    private double declination;

    private CompassCalibration compassCalibration;

    public static Compass getInstance() {
        if (compass == null) {
            compass = new Compass();
        }
        return compass;
    }

    public Compass() {
//        i2c_gy271 = I2C_GY271.getInstance();
//        i2c_gy271 = I2C_GY271_0d.getInstance();
        i2C_cmps14 = I2C_CMPS14.getInstance();
        setDeclination(4, 26);
    }

    public float getHeading() {
//        double[] scaled_x_y_z = getAxes();
//        return getHeading(scaled_x_y_z);
        return i2C_cmps14.get_bearing();
    }

//    public float getHeading(double[] scaled_x_y_z) {
//        double scaled_x = scaled_x_y_z[0];
//        double scaled_y = scaled_x_y_z[1];
//        double scaled_z = scaled_x_y_z[2];
//
//        double headingRad = Math.atan2(scaled_y, scaled_x);
//        headingRad += declination;
//
//        // Correct for reversed heading
//        if (headingRad < 0) {
//            headingRad += 2 * Math.PI;
//        }
//
//        //Check for wrap and compensate
//        if (headingRad > 2 * Math.PI) {
//            headingRad -= 2 * Math.PI;
//        }
//
//        //Convert to degrees from radians
//        float headingDeg = (float)(headingRad * 180f / Math.PI);
//
//        OverallContext.write_compass_log(String.format("%f;%f;%f;%f", scaled_x, scaled_y, scaled_z, headingDeg));
//
//
//        return headingDeg;
//    }
//
//    public double[] getAxes() {
//        double[] axes = i2c_gy271.getAxes();
//        return compensate(axes);
//    }

//    public void setCalibration(CompassCalibration compassCalibration) {
//        this.compassCalibration = compassCalibration;
//    }

//    private double[] compensate(double[] axes) {
//        if (compassCalibration == null) {
//            return axes;
//        }
//        double[] comp_axes = new double[3];
//        double off_x = axes[0] - compassCalibration.bias_x;
//        double off_y = axes[1] - compassCalibration.bias_y;
//        double off_z = axes[2] - compassCalibration.bias_z;
//        comp_axes[0] = off_x * compassCalibration.compensation_x_x + off_y * compassCalibration.compensation_x_y + off_z * compassCalibration.compensation_x_z;
//        comp_axes[1] = off_x * compassCalibration.compensation_y_x + off_y * compassCalibration.compensation_y_y + off_z * compassCalibration.compensation_y_z;
//        comp_axes[2] = off_x * compassCalibration.compensation_z_x + off_y * compassCalibration.compensation_z_y + off_z * compassCalibration.compensation_z_z;
//        return comp_axes;
//    }


//    public String getAxesState() {
//        double[] axes = getAxes();
//        return String.format("%f / %f / %f",  axes[0], axes[1], axes[2]);
//    }
//
//    public String getAxesDebug() {
//        return i2c_gy271.getAxesDebug();
//    }


    public String getState() {
        //double[] axes = getAxes();
        //double heading = getHeading(axes);
        double heading = getHeading();
        //return String.format("%f / %f / %f --- %f",  axes[0], axes[1], axes[2], heading);
        return String.format("heading %f", heading);
    }

    public boolean isPresent() {
        return i2C_cmps14.isPresent();
    }

    private void setDeclination(float degree, float min) {
        declinationDeg = degree;
        declinationMin = min;
        declination = (degree+min/60f) * (Math.PI/180);
    }



}
