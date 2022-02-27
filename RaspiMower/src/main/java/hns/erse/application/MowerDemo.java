package hns.erse.application;

import hns.erse.hardware.Mower;
import hns.erse.hardware.OverallContext;
import hns.erse.hardware.real.Compass;
import hns.erse.hardware.real.Gyro;
import hns.erse.hardware.states.CompassCalibration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MowerDemo {


    public static void showDemo() {
        OverallContext.getInstance().getPi4j();
//        OverallContext.getInstance().getPi4j().shutdown();


//        CompassCalibration compassCalibration = new CompassCalibration();
//        compassCalibration.bias_x = -17598.982783337226;
//        compassCalibration.bias_y = -43841.53132147319;
//        compassCalibration.bias_z = 69153.61205973863;
//        compassCalibration.compensation_x_x = 180970.45709650347;
//        compassCalibration.compensation_x_y = 6001.767574199331;
//        compassCalibration.compensation_x_z = -947.3552156992447;
//        compassCalibration.compensation_y_x = 6001.767574199323;
//        compassCalibration.compensation_y_y = 196606.74875404264;
//        compassCalibration.compensation_y_z = -653.1149176205276;
//        compassCalibration.compensation_z_x = -947.3552156992446;
//        compassCalibration.compensation_z_y = -653.1149176205276;
//        compassCalibration.compensation_z_z = 133058.54486047852;


//        Compass.getInstance().setCalibration(compassCalibration);

        Mower mower = Mower.getInstance();
        mower.start();

    }
}
