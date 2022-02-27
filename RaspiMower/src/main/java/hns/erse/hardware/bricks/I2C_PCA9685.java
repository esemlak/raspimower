package hns.erse.hardware.bricks;

import com.pi4j.io.i2c.I2C;
import hns.erse.hardware.virtual.GpioBase;
import hns.erse.hardware.OverallContext;
import hns.erse.threads.InfoDisplay;

// 16 port PWM motor driver board

public class I2C_PCA9685 {

    // REGISTER ADDRESSES
    private final static int  PCA9685_MODE1 = 0x00;      /**< Mode Register 1 */
    private final static int  PCA9685_MODE2 = 0x01;      /**< Mode Register 2 */
    private final static int  PCA9685_SUBADR1 = 0x02;    /**< I2C-bus subaddress 1 */
    private final static int  PCA9685_SUBADR2 = 0x03;    /**< I2C-bus subaddress 2 */
    private final static int  PCA9685_SUBADR3 = 0x04;    /**< I2C-bus subaddress 3 */
    private final static int  PCA9685_ALLCALLADR = 0x05; /**< LED All Call I2C-bus address */
    private final static int  PCA9685_LED0_ON_L = 0x06;  /**< LED0 on tick, low byte*/
    private final static int  PCA9685_LED0_ON_H = 0x07;  /**< LED0 on tick, high byte*/
    private final static int  PCA9685_LED0_OFF_L = 0x08; /**< LED0 off tick, low byte */
    private final static int  PCA9685_LED0_OFF_H = 0x09; /**< LED0 off tick, high byte */

    // etc all 16:  LED15_OFF_H = 0x45
    private final static int  PCA9685_ALLLED_ON_L = 0xFA;  /**< load all the LEDn_ON registers, low */
    private final static int  PCA9685_ALLLED_ON_H = 0xFB;  /**< load all the LEDn_ON registers, high */
    private final static int  PCA9685_ALLLED_OFF_L = 0xFC; /**< load all the LEDn_OFF registers, low */
    private final static int  PCA9685_ALLLED_OFF_H = 0xFD; /**< load all the LEDn_OFF registers,high */
    private final static int  PCA9685_PRESCALE = 0xFE;    /**< Prescaler for PWM output frequency */
    private final static int  PCA9685_TESTMODE = 0xFF;    /**< private final static int s the test mode to be entered */
    
    // MODE1 bits
    private final static int  MODE1_ALLCAL = 0x01;  /**< respond to LED All Call I2C-bus address */
    private final static int  MODE1_SUB3 = 0x02;    /**< respond to I2C-bus subaddress 3 */
    private final static int  MODE1_SUB2 = 0x04;    /**< respond to I2C-bus subaddress 2 */
    private final static int  MODE1_SUB1 = 0x08;    /**< respond to I2C-bus subaddress 1 */
    private final static int  MODE1_SLEEP = 0x10;   /**< Low power mode. Oscillator off */
    private final static int  MODE1_AI = 0x20;      /**< Auto-Increment enabled */
    private final static int  MODE1_EXTCLK = 0x40;  /**< Use EXTCLK pin clock */
    private final static int  MODE1_RESTART = 0x80; /**< Restart enabled */
    // MODE2 bits
    private final static int  MODE2_OUTNE_0 = 0x01; /**< Active LOW output enable input */
    private final static int  MODE2_OUTNE_1 = 0x02; /**< Active LOW output enable input - high impedience */
    private final static int  MODE2_OUTDRV = 0x04; /**< totem pole structure vs open-drain */
    private final static int  MODE2_OCH = 0x08;    /**< Outputs change on ACK vs STOP */
    private final static int  MODE2_INVRT = 0x10;  /**< Output logic state inverted */
    
    private final static int  PCA9685_I2C_ADDRESS = 0x40;      /**< Default PCA9685 I2C Slave Address */
    private final static int  FREQUENCY_OSCILLATOR = 25000000; /**< Int. osc. frequency in datasheet */
    
    private final static int  PCA9685_PRESCALE_MIN = 3;   /**< minimum prescale value */
    private final static int  PCA9685_PRESCALE_MAX = 255;  /**< maximum prescale value */

    private final int _oscillator_freq = 27000000;

    I2C i2c_device;

    private static I2C_PCA9685 i2C_pca9685;

    public I2C_PCA9685() {
        try {
            i2c_device = GpioBase.createI2CPort(PCA9685_I2C_ADDRESS);
            set_frequency(3000);
        } catch (Exception e) {
            InfoDisplay.getInstance().add_info_line("I2C_PCA9685 Exception : " + e.getMessage());
            i2c_device = null;
        }
    }

    public static I2C_PCA9685 getInstance() {
        if (i2C_pca9685 == null) {
            i2C_pca9685 = new I2C_PCA9685();
        }
        return i2C_pca9685;
    }

    public void set_frequency(int frequency) {
//        System.out.println("I2C_PCA9685 set_frequency " + frequency);

        // Range output modulation frequency is dependant on oscillator
        if (frequency < 1)
            frequency = 1;
        if (frequency > 3500)
            frequency = 3500; // Datasheet limit is 3052=50MHz/(4*4096)

        float prescaleval = (float)((_oscillator_freq / (frequency * 4096.0)) + 0.5) - 1;
        if (prescaleval < PCA9685_PRESCALE_MIN)
            prescaleval = PCA9685_PRESCALE_MIN;
        if (prescaleval > PCA9685_PRESCALE_MAX)
            prescaleval = PCA9685_PRESCALE_MAX;


        byte prescale = (byte)prescaleval;
        byte oldmode = i2c_device.readRegisterByte(PCA9685_MODE1);
        byte newmode = (byte)((oldmode & MODE1_RESTART) | MODE1_SLEEP); // sleep
        i2c_device.writeRegister(PCA9685_MODE1, newmode);                             // go to sleep
        i2c_device.writeRegister(PCA9685_PRESCALE, prescale); // set the prescaler
        i2c_device.writeRegister(PCA9685_MODE1, oldmode);
        OverallContext.sleep(5);
        // This sets the MODE1 register to turn on auto increment.
        i2c_device.writeRegister(PCA9685_MODE1, oldmode | MODE1_RESTART | MODE1_AI);

    }

    public void set_dutycycle(int pin, int speed) {
//        System.out.println("I2C_PCA9685 set_dutycycle " + speed);
        int off_cycle = 4096 - (speed * 4096 / 100);
        i2c_device.writeRegisterWord(PCA9685_LED0_ON_L + 4 * pin, 0);
        i2c_device.writeRegisterWord(PCA9685_LED0_ON_L + 4 * pin +2, off_cycle);
    }

    public void off(int pin) {
        i2c_device.writeRegisterWord(PCA9685_LED0_ON_L + 4 * pin, 0);
        i2c_device.writeRegisterWord(PCA9685_LED0_ON_L + 4 * pin +2, 4096);
    }



}
