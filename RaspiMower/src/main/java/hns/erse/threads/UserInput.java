package hns.erse.threads;

import hns.erse.hardware.Mower;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;

public class UserInput extends Thread {

    private Mower mower;
    private boolean do_run = true;
    private Terminal terminal;

    private static UserInput userInput;

    public static UserInput getInstance(Mower mower) {
        if (userInput == null) {
            userInput = new hns.erse.threads.UserInput(mower);
        }
        return userInput;
    }

    public static UserInput getInstance() {
        return userInput;
    }

    public UserInput(Mower mower) {
        this.mower = mower;
    }

    public void run() {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .jna(true)
                    .build();

            Attributes attr = terminal.enterRawMode();
            terminal.puts(InfoCmp.Capability.enter_ca_mode);
            terminal.puts(InfoCmp.Capability.keypad_xmit);

//            System.out.println("terminal reader start");

            while(do_run) {
                char ch = (char)terminal.reader().read();
                mower.handle_keystroke(ch);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("terminal reader finish");
    }

    public void stop_input() {
        do_run = false;
        try {
            terminal.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start_input() {
        do_run = true;
        this.start();
    }

}
