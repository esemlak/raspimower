package hns.erse.hardware.bricks;

import com.pi4j.io.i2c.I2C;
import hns.erse.hardware.virtual.GpioBase;
import hns.erse.threads.InfoDisplay;


public class I2C_INA219 {
    private final static int vcc_i2c_ADDRESS = 0x40;

    /**
     * Enum for the Bus Voltage Range setting (BRNG)
     */
    public enum Brng {
        V16(0), // 16 Volts
        V32(1); // 32 Volts

        private int value;

        Brng(int val) {
            value = val;
        }

        int getValue() {
            return value;
        }
    }

    /**
     * Enum for the PGA gain and range setting options.
     */
    public enum Pga {
        GAIN_1(0), // 1
        GAIN_2(1), // /2
        GAIN_4(2), // /4
        GAIN_8(3); // /8

        private int value;

        Pga(int val) {
            value = val;
        }

        int getValue() {
            return value;
        }
    }

    /**
     * Enum for the Bus and Shunt ADC Resolution/Averaging settings.
     */
    public enum Adc {
        BITS_9(0), //9 bit samples
        BITS_10(1), //10 bit samples
        BITS_11(2), //11 bit samples
        BITS_12(3), //12 bit samples
        SAMPLES_2(9), //2 sample average
        SAMPLES_4(10), //4 sample average
        SAMPLES_8(11), //8 sample average
        SAMPLES_16(12), //16 sample average
        SAMPLES_32(13), //32 sample average
        SAMPLES_64(14), //64 sample average
        SAMPLES_128(15); //128 sample average

        private int value;

        Adc(int val) {
            value = val;
        }

        int getValue() {
            return value;
        }
    }

    public enum RegisterAddress {
        CONFIGURATION(0), SHUNT_VOLTAGE(1), BUS_VOLTAGE(2), POWER(3), CURRENT(4), CALIBRATION(5);

        private final int addr;

        RegisterAddress(final int a) {
            addr = a;
        }

        int getValue() {
            return addr;
        }

    }

    private static final double SHUNT_VOLTAGE_LSB = 10e-6;
    private static final double BUS_VOLTAGE_LSB = 4e-3;
    private static final int POWER_LSB_SCALE = 20;

    private static I2C_INA219 i2c_INA219;
    private I2C vcc_i2c;
    double currentLSB = 0;

    public I2C_INA219() {
        try {
            vcc_i2c = GpioBase.createI2CPort(vcc_i2c_ADDRESS);

            double shuntResistance = 0.01;
            double maxExpectedCurrent= 3.2;

            currentLSB = (maxExpectedCurrent / 32768);
            int cal = (int) (((0.04096 * 32768) / (maxExpectedCurrent * shuntResistance)));


            configure(Brng.V32, Pga.GAIN_2, Adc.BITS_12, Adc.SAMPLES_4);
            vcc_i2c.writeRegisterWord(RegisterAddress.CALIBRATION.getValue(), cal);


        } catch (Exception e) {
            InfoDisplay.getInstance().add_info_line("i2c_INA219 Exception : " + e.getMessage());
            vcc_i2c = null;
        }
    }

    private void configure(final I2C_INA219.Brng busVoltageRange, final I2C_INA219.Pga pga, final I2C_INA219.Adc badc,
                           final I2C_INA219.Adc sadc)  {
        int regValue = (busVoltageRange.getValue() << 13) | (pga.getValue() << 11) | (badc.getValue() << 7)
                | (sadc.getValue() << 3) | 0x7;

        vcc_i2c.writeRegisterWord(RegisterAddress.CONFIGURATION.getValue(), regValue);
    }

    /**
     * Reads and returns the shunt voltage.
     *
     * @return The shunt voltage.
     */
    public double getShuntVoltage()  {
        int rval = vcc_i2c.readRegisterWord(RegisterAddress.SHUNT_VOLTAGE.getValue());
        return rval * SHUNT_VOLTAGE_LSB;
    }

    /**
     * Reads and returns the bus voltage.
     *
     * @return The bus voltage.
     */
    public double getBusVoltage() {
        int rval = vcc_i2c.readRegisterWord(RegisterAddress.BUS_VOLTAGE.getValue());
        return (rval >> 3) * BUS_VOLTAGE_LSB;
    }

    /**
     * Reads and returns the power.
     *
     * @return The power value.
     */
    public double getPower() {
        int rval = vcc_i2c.readRegisterWord(RegisterAddress.POWER.getValue());
        return rval * POWER_LSB_SCALE * currentLSB;
    }

    /**
     * Reads and returns the current.
     *
     * @return The current value.
     */
    public double getCurrent() {
        int rval = vcc_i2c.readRegisterWord(RegisterAddress.CURRENT.getValue());
        return rval * currentLSB;
    }


    public static I2C_INA219 getInstance() {
        if (i2c_INA219 == null) {
            i2c_INA219 = new I2C_INA219();
        }
        return i2c_INA219;
    }

    public String getState() {
        return String.format("shuntVoltage %s / shuntCurrent %s / busVoltage %s / busPower %s", getShuntVoltage(), getCurrent(), getBusVoltage(), getPower());
    }

}

