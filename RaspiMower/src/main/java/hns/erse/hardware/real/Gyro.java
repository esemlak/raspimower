package hns.erse.hardware.real;

import hns.erse.hardware.bricks.I2C_GY521;
import hns.erse.hardware.states.GyroState;

//        Gyro gyro = Gyro.getInstance();
//        for(int i=0;i<1000;i++) {
//            String info = gyro.getState().toStringRot();
//
//            System.out.println(i + " - " + info);
//            OverallContext.write_log(info, gyro);
//
//            OverallContext.sleep(500);
//        }


public class Gyro implements RealDevice {

    // gyro GY-521

    I2C_GY521 gy521;

    private static Gyro gyro;

    public static Gyro getInstance() {
        if (gyro == null) {
            gyro = new Gyro();
        }
        return gyro;
    }



    public Gyro() {
        gy521 = new I2C_GY521();
    }

    public boolean isPresent() {
        return gy521.isPresent();
    }
    public GyroState getState() {
        return gy521.getState();
    }

}

