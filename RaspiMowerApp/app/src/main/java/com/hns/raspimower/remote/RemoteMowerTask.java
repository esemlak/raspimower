package com.hns.raspimower.remote;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.regex.Pattern;

public class RemoteMowerTask extends AsyncTask<String, Void, Void> {
    private static int remote_port = 4711;
    private static Socket remote_socket = null;

    public static final String COMMAND = "COMMAND";
    public static final String CONNECT = "CONNECT";

    public static final String Command_Speed = "speed";
    public static final String Command_Steer = "steer";
    public static final String Command_Stop = "stop";
    public static final String Command_Mow = "mow";
    public static final String Command_RemoteHeartbeat = "remote_heartbeat";

    private static final String TAG = RemoteMowerTask.class.getSimpleName();

    private static long last_heartbeat = 0;
    private static Handler handler;
    private static Runnable heartbeat_thread;

    private static boolean looper_prepared = false;

    private static final long heartbeat_delay = 500;

    public RemoteMowerTask() {

    }

    @Override
    protected Void doInBackground(String... commands) {
        String command = commands[0];
        String value = "";
        if (commands.length() > 1) {
            command = command + "|" + commands[1];
        }
        if (command.startsWith(CONNECT)) {
            connect(command);
        } else {
            send_command(command);
        }
        return null;
    }

    private void connect(String command) {
        try {
            String[] parts = command.split(Pattern.quote("|"));
            String remote_ip = parts[1];
            Log.i(TAG, "start remote socket IP " + remote_ip);
            remote_socket = new Socket(remote_ip, remote_port);
            Log.i(TAG, "remote socket OK");
            last_heartbeat = new Date().getTime();

            if (handler != null && heartbeat_thread != null) {
            }

            if (!looper_prepared) {
                Looper.prepare();
                looper_prepared = true;
            }
            handler = new Handler();
            heartbeat_thread = new Runnable() {
                @Override
                public void run() {
                    send_heartbeat();
                    handler.postDelayed(this,heartbeat_delay);
                }
            };

            handler.postDelayed(heartbeat_thread,heartbeat_delay);
        } catch (IOException e) {
            Log.i(TAG, "connect exception " + e.getMessage());
            remote_socket = null;
            e.printStackTrace();
        }
    }

    private void send_command(String command) {
        try {
            Log.i(TAG, "send_command " + command);
            if (remote_socket == null) {
                Log.i(TAG, "no remote socket");
                return;
            }
            PrintWriter out = new PrintWriter(remote_socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(remote_socket.getInputStream()));
            out.println(command);
            Log.i(TAG, "send_command wait for response");
            String response = in.readLine();
            Log.i(TAG, "send_command got response " + response);
            last_heartbeat = new Date().getTime();
            if (response != null && !response.equals("ok")) {
                System.out.println("send_command invalid response " + response);
            }
        } catch (IOException e) {
            Log.i(TAG, "send_command exception " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void send_heartbeat() {
        if (new Date().getTime() - last_heartbeat > heartbeat_delay && remote_socket != null) {
            send_command(Command_RemoteHeartbeat);
        }

    }


}
