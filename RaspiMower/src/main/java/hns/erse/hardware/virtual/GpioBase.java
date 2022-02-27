package hns.erse.hardware.virtual;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmType;
import hns.erse.hardware.OverallContext;

public class GpioBase {

    public static DigitalOutput createDigitalGpioPort(int gpio_pin_number) {
        Context pi4j = OverallContext.getInstance().getPi4j();

        DigitalOutputConfigBuilder direction_config = DigitalOutput.newConfigBuilder(pi4j)
                .id("BCM_" + gpio_pin_number)
                .name("direction")
                .address(gpio_pin_number)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("pigpio-digital-output");
        return pi4j.create(direction_config);
    }

    public static Pwm createPwmGpioPort(int gpio_pin_number) {
        Context pi4j = OverallContext.getInstance().getPi4j();

        PwmConfig pwm_config = Pwm.newConfigBuilder(pi4j)
                .id("BCM_" + gpio_pin_number)
                .name("pwm")
                .address(gpio_pin_number)
                .pwmType(PwmType.HARDWARE)
                .initial(0)
                .shutdown(0)
                .provider("pigpio-pwm")
                .build();

        return pi4j.create(pwm_config);
    }

    public static I2C createI2CPort(int i2c_address) {
        Context pi4j = OverallContext.getInstance().getPi4j();
        I2CProvider i2CProvider = pi4j.provider("pigpio-i2c");
        I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j).id("gyro_" + i2c_address).bus(1).device(i2c_address).build();
        return i2CProvider.create(i2cConfig);
    }


}
