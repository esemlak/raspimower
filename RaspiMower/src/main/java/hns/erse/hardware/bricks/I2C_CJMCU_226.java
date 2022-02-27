package hns.erse.hardware.bricks;

import com.pi4j.io.i2c.I2C;
import hns.erse.hardware.virtual.GpioBase;
import hns.erse.threads.InfoDisplay;

//I2C_CJMCU_226 instance = I2C_CJMCU_226.getInstance();
//for(int i=0;i<1000;i++) {
//System.out.println(i + ":" + instance.getState());
//OverallContext.sleep(500);
//}



public class I2C_CJMCU_226 {
    private final static int vcc_i2c_ADDRESS = 0x45;

    private static final int REGISTER_VOLTAGE = 0x43;

    private static final int INA226_REG_CONFIG = 0x00;
    private static final int INA226_REG_SHUNTVOLTAGE = 0x01;
    private static final int INA226_REG_BUSVOLTAGE = 0x02;
    private static final int INA226_REG_POWER = 0x03;
    private static final int INA226_REG_CURRENT = 0x04;
    private static final int INA226_REG_CALIBRATION = 0x05;
    private static final int INA226_REG_MASKENABLE = 0x06;
    private static final int INA226_REG_ALERTLIMIT = 0x07;

    private static final int INA226_BIT_SOL = 0x8000;
    private static final int INA226_BIT_SUL = 0x4000;
    private static final int INA226_BIT_BOL = 0x2000;
    private static final int INA226_BIT_BUL = 0x1000;
    private static final int INA226_BIT_POL = 0x0800;
    private static final int INA226_BIT_CNVR = 0x0400;
    private static final int INA226_BIT_AFF = 0x0010;
    private static final int INA226_BIT_CVRF = 0x0008;
    private static final int INA226_BIT_OVF = 0x0004;
    private static final int INA226_BIT_APOL = 0x0002;
    private static final int INA226_BIT_LEN = 0x0001;

    private static final int INA226_AVERAGES_1 = 0b000;
    private static final int INA226_AVERAGES_4 = 0b001;
    private static final int INA226_AVERAGES_16 = 0b010;
    private static final int INA226_AVERAGES_64 = 0b011;
    private static final int INA226_AVERAGES_128 = 0b100;
    private static final int INA226_AVERAGES_256 = 0b101;
    private static final int INA226_AVERAGES_512 = 0b110;
    private static final int INA226_AVERAGES_1024 = 0b111;

    private static final int INA226_BUS_CONV_TIME_140US = 0b000;
    private static final int INA226_BUS_CONV_TIME_204US = 0b001;
    private static final int INA226_BUS_CONV_TIME_332US = 0b010;
    private static final int INA226_BUS_CONV_TIME_588US = 0b011;
    private static final int INA226_BUS_CONV_TIME_1100US = 0b100;
    private static final int INA226_BUS_CONV_TIME_2116US = 0b101;
    private static final int INA226_BUS_CONV_TIME_4156US = 0b110;
    private static final int INA226_BUS_CONV_TIME_8244US = 0b111;

    private static final int INA226_SHUNT_CONV_TIME_140US = 0b000;
    private static final int INA226_SHUNT_CONV_TIME_204US = 0b001;
    private static final int INA226_SHUNT_CONV_TIME_332US = 0b010;
    private static final int INA226_SHUNT_CONV_TIME_588US = 0b011;
    private static final int INA226_SHUNT_CONV_TIME_1100US = 0b100;
    private static final int INA226_SHUNT_CONV_TIME_2116US = 0b101;
    private static final int INA226_SHUNT_CONV_TIME_4156US = 0b110;
    private static final int INA226_SHUNT_CONV_TIME_8244US = 0b111;

    private static final int INA226_MODE_POWER_DOWN = 0b000;
    private static final int INA226_MODE_SHUNT_TRIG = 0b001;
    private static final int INA226_MODE_BUS_TRIG = 0b010;
    private static final int INA226_MODE_SHUNT_BUS_TRIG = 0b011;
    private static final int INA226_MODE_ADC_OFF = 0b100;
    private static final int INA226_MODE_SHUNT_CONT = 0b101;
    private static final int INA226_MODE_BUS_CONT = 0b110;
    private static final int INA226_MODE_SHUNT_BUS_CONT = 0b111;

    private static final int I2C_DEFAULT_CLK_KHZ = 100;
    private static final int I2C_DEFAULT_BUS_NUMBER = 0;

    private static I2C_CJMCU_226 i2C_cjmcu_226;
    float vBusMax = 36;
    float vShuntMax = 0.08192f;
    float rShunt = 0.1f;
    float currentLSB = 0;
    float powerLSB = 0;
    float iMaxPossible = 400;
    private I2C vcc_i2c;

    public I2C_CJMCU_226() {
        try {
            vcc_i2c = GpioBase.createI2CPort(vcc_i2c_ADDRESS);
            configure(INA226_AVERAGES_1);
            calibrate(0.01f, 0.4f);
            vcc_i2c.readRegisterWord(INA226_REG_CURRENT);
        } catch (Exception e) {
            InfoDisplay.getInstance().add_info_line("I2C_CJMCU_226 Exception : " + e.getMessage());
            vcc_i2c = null;
        }
    }

    public static I2C_CJMCU_226 getInstance() {
        if (i2C_cjmcu_226 == null) {
            i2C_cjmcu_226 = new I2C_CJMCU_226();
        }
        return i2C_cjmcu_226;
    }

    public String getState() {
        return String.format("shuntVoltage %s / shuntCurrent %s / busVoltage %s / busPower %s", readShuntVoltage(), readShuntCurrent(), readBusVoltage(), readBusPower());
    }

    private void configure(int avg) {
        if (vcc_i2c == null) {
            return;
        }

        short config = 0;
        short busConvTime = INA226_BUS_CONV_TIME_1100US;
        short shuntConvTime = INA226_SHUNT_CONV_TIME_1100US;
        short mode = INA226_MODE_SHUNT_BUS_CONT;
        config |= (avg << 9 | busConvTime << 6 | shuntConvTime << 3 | mode);
        vcc_i2c.writeRegisterWord(INA226_REG_CONFIG, config);
    }

    private void calibrate(float rShuntValue, float iMaxExcepted) {
        if (vcc_i2c == null) {
            return;
        }

        rShunt = rShuntValue;

        iMaxPossible = vShuntMax / rShunt;

        float minimumLSB = (float) iMaxExcepted / 32768;

        currentLSB =(float)Math.floor(minimumLSB * 100000000);
        currentLSB /= 100000000.0;
        currentLSB *= 10000;
        currentLSB = (float) Math.ceil(currentLSB);
        currentLSB /= 10000;

        powerLSB = currentLSB * 25;

        int calibrationValue = (int) (((0.00512) / (currentLSB * rShunt)));

        vcc_i2c.writeRegisterWord(INA226_REG_CALIBRATION, calibrationValue);
    }

    public float readShuntCurrent() {
        if (vcc_i2c == null) {
            return 0;
        }
        return vcc_i2c.readRegisterWord(INA226_REG_CURRENT) * currentLSB * 1000f;
    }

    public float readShuntVoltage() {
        if (vcc_i2c == null) {
            return 0;
        }
        return vcc_i2c.readRegisterWord(INA226_REG_SHUNTVOLTAGE) * 0.0000025f;
    }

    public float readBusVoltage() {
        if (vcc_i2c == null) {
            return 0;
        }
        return vcc_i2c.readRegisterWord(INA226_REG_BUSVOLTAGE) * 0.00125f;
    }

    public float readBusPower() {
        if (vcc_i2c == null) {
            return 0;
        }
        return vcc_i2c.readRegisterWord(INA226_REG_POWER) * powerLSB * 1000;
    }

}