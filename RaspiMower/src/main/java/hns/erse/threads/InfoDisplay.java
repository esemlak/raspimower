package hns.erse.threads;

import hns.erse.hardware.Mower;
import hns.erse.hardware.OverallContext;
import hns.erse.hardware.real.Compass;
import hns.erse.hardware.real.Gps;
import hns.erse.hardware.real.Gyro;

import java.io.Console;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class InfoDisplay extends Thread {

    private Mower mower;
    private boolean do_display = true;

    private final static int terminal_width = 80;

    private List<String> info_list = new LinkedList<String>();
    private int max_list_len = 10;
    private int info_list_counter;

    private static InfoDisplay infoDisplay;

    public static InfoDisplay setInstance(Mower mower) {
        infoDisplay = new InfoDisplay(mower);
        return infoDisplay;
    }

    public static InfoDisplay getInstance() {
        return infoDisplay;
    }

    public InfoDisplay(Mower mower) {
        this.mower = mower;
    }

    public static void fallback_info(String s) {
        System.out.println(s);
    }

    public void run() {

        while (do_display) {
            Compass compass = Compass.getInstance();
            Gyro gyro = Gyro.getInstance();
            Gps gps = Gps.getInstance();
            Calendar calendar = Calendar.getInstance();

            char escCode = 0x1B;

            Console console = System.console();
            console.writer().print(String.format("%c[%d;%df", 0x1b, 1, 1));
            print_break_line();
            print_decorate_line("Time " + calendar.getTime().toString());

            print_empty_line();

            print_decorate_line("Mower state " + mower.getCurrentState());
            print_empty_line();

            if (mower.get_left_motor() != null) {
                print_decorate_line("Motor left " + mower.get_left_motor().getState());
            }
            if (mower.get_right_motor() != null) {
                print_decorate_line("Motor right " + mower.get_right_motor().getState());
            }
            if (mower.get_cutting_motor() != null) {
                print_decorate_line("Cutting motor " + mower.get_cutting_motor().getState());
            }

//            print_empty_line();
//            print_decorate_line("Position " + gps.getGpsPosition());

            if (gyro.isPresent()) {
                print_decorate_line("Gyro " + gyro.getState().getGyro_z());
//                OverallContext.write_log(gyro.getState().toString(), gyro);

            }
            if (compass.isPresent()) {
                print_decorate_line("Compass " + compass.getHeading());
                //OverallContext.write_log( String.valueOf(compass.getState()), compass);
                OverallContext.write_log( "Compass " + compass.getState(), compass);
            }

            if (info_list_counter > 0 ) {
                print_break_line();
                print_info_lines();
            }
            print_break_line();

            for(int i=0;i<5;i++) {
                print_clear_line();
            }


            try {
                Thread.sleep(100);
            } catch (Exception e) {
                // nothing
            }
        }
    }

    private void print_empty_line() {
        print_decorate_line("");
    }

    private void print_clear_line() {
        System.console().writer().println(String.format("%-" + terminal_width + "s", ""));
    }

    private void print_info_lines() {
        synchronized(info_list) {
            for (String info_line :
                    info_list) {
                print_decorate_line(info_line);
            }
        }
    }

    public void stop_display() {
        do_display = false;
    }

    public void start_display() {
        do_display = true;
        clear_screen();
        this.start();
    }

    public void clear_screen() {
        Console console = System.console();
        console.writer().print("\033[H\033[2J");
    }

    private void print_decorate_line(String text) {
        if (text.length() > terminal_width - 4) {
            text = text.substring(0, terminal_width - 4);
        }
        System.console().writer().println(String.format("* %-" + (terminal_width-4) + "s *", text));
    }

    private void print_break_line() {
        System.console().writer().println(String.format("%" + terminal_width + "s", "").replace(' ', '*'));
    }

    public static void add_info_line_static(String info) {
        InfoDisplay info_display = InfoDisplay.getInstance();
        if (info_display != null) {
            info_display.add_info_line(info);
        } else {
            InfoDisplay.fallback_info(info);
        }
    }

    public void add_info_line(String info) {
        synchronized(info_list) {
            OverallContext.write_log(info, this);
            if (info_list.size() >= max_list_len) {
                info_list.remove(0);
            }
            info_list_counter++;
            info_list.add(info_list_counter + " : " + info);
        }
    }

}
