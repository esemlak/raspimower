package hns.erse.hardware.bricks;

import com.pi4j.io.i2c.I2C;
import hns.erse.hardware.OverallContext;
import hns.erse.hardware.virtual.GpioBase;
import hns.erse.threads.InfoDisplay;


//I2C_ADS1115 i2C_ads1115 = I2C_ADS1115.getInstance();
//for(int i=0;i<1000;i++) {
//    System.out.println("Pin 0 " + i2C_ads1115.read(0));
//    System.out.println("Pin 1 " + i2C_ads1115.read(1));
//    System.out.println("Pin 2 " + i2C_ads1115.read(2));
//    System.out.println("Pin 3 " + i2C_ads1115.read(3));
//    OverallContext.sleep(500);
//}


public class I2C_ADS1115  implements I2C_Brick {

    I2C i2c_device;

    private static I2C_ADS1115 i2C_ads1115;

    private final static int ADS1115_I2C_ADDRESS = 0x48;      /**< Default ADS1115 I2C Slave Address */

    private final static int ADS1X15_POINTER_CONVERSION = 0x00;
    private final static int ADS1X15_POINTER_CONFIG = 0x01;
    private final static int ADS1X15_CONFIG_OS_SINGLE = 0x8000;
    private final static int ADS1X15_CONFIG_MUX_OFFSET = 12;
    private final static int ADS1X15_CONFIG_COMP_QUE_DISABLE = 0x0003;

    public final static int ADS1X15_CONFIG_GAIN_1 = 0x0200;
    public final static int ADS1X15_CONFIG_GAIN_2_3 = 0x0000;
    public final static int ADS1X15_CONFIG_GAIN_2 = 0x0400;
    public final static int ADS1X15_CONFIG_GAIN_4 = 0x0600;
    public final static int ADS1X15_CONFIG_GAIN_8 = 0x0800;
    public final static int ADS1X15_CONFIG_GAIN_16 = 0x0A00;

    public final static int MODE_CONTINUOUS = 0x0000;
    public final static int MODE_SINGLE = 0x0100;

    public final static int ADS1115_CONFIG_DR_8 = 0x0000;
    public final static int ADS1115_CONFIG_DR_16 = 0x0020;
    public final static int ADS1115_CONFIG_DR_32 = 0x0040;
    public final static int ADS1115_CONFIG_DR_64 = 0x0060;
    public final static int ADS1115_CONFIG_DR_128 = 0x0080;
    public final static int ADS1115_CONFIG_DR_250 = 0x00A0;
    public final static int ADS1115_CONFIG_DR_475 = 0x00C0;
    public final static int ADS1115_CONFIG_DR_860 = 0x00E0;

    private int last_pin_read = -1;
    private int mode = MODE_CONTINUOUS;
    private int gain = ADS1X15_CONFIG_GAIN_1;
    private int data_rate = ADS1115_CONFIG_DR_32;

    public I2C_ADS1115() {
        try {
            i2c_device = GpioBase.createI2CPort(ADS1115_I2C_ADDRESS);
        } catch (Exception e) {
            InfoDisplay.getInstance().add_info_line("I2C_ADS1115 Exception : " + e.getMessage());
            i2c_device = null;
        }
    }

    public static I2C_ADS1115 getInstance() {
        if (i2C_ads1115 == null) {
            i2C_ads1115 = new I2C_ADS1115();
        }
        return i2C_ads1115;
    }


    public int read(int pin) {

        int config = 0;
        if (mode == MODE_SINGLE) {
            config = ADS1X15_CONFIG_OS_SINGLE;
        } else {

            config |= (pin & 0x07) << ADS1X15_CONFIG_MUX_OFFSET;
            config |= gain;
            config |= mode;
            config |= data_rate;
            config |= ADS1X15_CONFIG_COMP_QUE_DISABLE;
        }
        i2c_device.writeRegisterWord(ADS1X15_POINTER_CONFIG, config);
        OverallContext.sleep(100);

        int word = i2c_device.readRegisterWord(ADS1X15_POINTER_CONVERSION);
        if (word > 32767)
        {
            word -= 65535;
        }
        return word;
    }


    public boolean isPresent() {
        return (i2c_device != null);
    }

}
