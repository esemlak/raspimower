package hns.erse.hardware;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import hns.erse.threads.InfoDisplay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class OverallContext {

    private static OverallContext context = null;

    private Context pi4j = null;

    private int max_debug_level = 100;

    public OverallContext() {
        pi4j = Pi4J.newAutoContext();
    }

    public static OverallContext getInstance() {
        if (context == null) {
            context = new OverallContext();
        }
        return context;
    }

    public Context getPi4j() {
        return pi4j;
    }

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {
        }
    }

    public static int readWord2C(I2C i2c_device, int address) {
        int value  = i2c_device.readRegisterWord(address);
        if (value > 32768) {
            value = -(65536 - value);
        }
        return value;
    }


    public void add_debug_line(String debug, int debug_level) {
        if (debug_level <= max_debug_level) {
            InfoDisplay.getInstance().add_info_line(debug);
        }
    }

    public static void write_log(String logline, Object object) {
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat format_log = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat format_filename = new SimpleDateFormat("yyyy_MM_dd");

        try {
            FileWriter fw = new FileWriter("/tmp/Mower_log_" + format_filename.format(time)+ ".log", true);
            fw.append(String.format("%s - %s : %s\r\n", format_log.format(time), object.getClass().getSimpleName(), logline));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write_compass_log(String logline) {
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat format_log = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat format_filename = new SimpleDateFormat("yyyy_MM_dd");

        try {
            FileWriter fw = new FileWriter("/tmp/compass_log_" + format_filename.format(time)+ ".log", true);
            fw.append(logline + "\r\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[] get_three_ints_from_byte_buffer(byte[] byte_buffer) {
        int int1;
        int int2;
        int int3;

        ByteBuffer int_buffer = ByteBuffer.allocate(4);
        int_buffer.order(ByteOrder.BIG_ENDIAN);
        int_buffer.put((byte)0x00);
        int_buffer.put((byte)0x00);
        int_buffer.put(byte_buffer[0]);
        int_buffer.put(byte_buffer[1]);
        int_buffer.flip();
        int1 = int_buffer.getInt();
        if (int1 > 32768) {
            int1 = -(65536  - int1);
        }

        int_buffer.clear();
        int_buffer.put((byte)0x00);
        int_buffer.put((byte)0x00);
        int_buffer.put(byte_buffer[2]);
        int_buffer.put(byte_buffer[3]);
        int_buffer.flip();
        int2 = int_buffer.getInt();
        if (int2 > 32768) {
            int2 = -(65536  - int2);
        }

        int_buffer.clear();
        int_buffer.put((byte)0x00);
        int_buffer.put((byte)0x00);
        int_buffer.put(byte_buffer[4]);
        int_buffer.put(byte_buffer[5]);
        int_buffer.flip();
        int3 = int_buffer.getInt();
        if (int3 > 32768) {
            int3 = -(65536  - int3);
        }

        return new int[]{int1,int2,int3};
    }

}
