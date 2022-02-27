package hns.erse.remote;

import hns.erse.hardware.Mower;
import hns.erse.threads.InfoDisplay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteClient extends Thread {

    private static int local_port = 4711;
    private static ServerSocket server_socket = null;
    private static Socket remote_socket = null;
    private static Mower mower;
    private static RemoteHeartBeat remote_heartbeat;
    private static boolean keep_up = true;

    public RemoteClient(Mower mower, RemoteHeartBeat remote_heartbeat) {
        this.mower = mower;
        this.remote_heartbeat = remote_heartbeat;
    }

    public static void connect() {
        try {
            InfoDisplay.getInstance().add_info_line("waiting for remote connection");
            if (server_socket != null) {
                server_socket.close();
            }
            server_socket = new ServerSocket(local_port);
            remote_socket = server_socket.accept();
            InfoDisplay.getInstance().add_info_line("remote connection: " + remote_socket.getInetAddress().toString());
            remote_heartbeat.set_heartbeat();
            remote_heartbeat.set_check_active();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void listen() {
        try {
            while(keep_up) {
                PrintWriter out = null;
                out = new PrintWriter(remote_socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(remote_socket.getInputStream()));
                String command = in.readLine();
                if (command == null) {
                    //disconnect();
                    return;
                }
                remote_heartbeat.set_heartbeat();
//                if (!command.equals(Mower.Command_RemoteHeartbeat)) {
                    mower.execute_remote_command(command);
//                }
                out.println("ok");
            }
        } catch (IOException e) {
            mower.execute_remote_command("stop|0");
            e.printStackTrace();
        }
    }

    private static void disconnect() {
        try {
            mower.execute_remote_command("stop|0");
            remote_heartbeat.set_check_inactive();
            remote_socket.close();
            server_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(keep_up) {
            connect();
            listen();
        }
    }

}
