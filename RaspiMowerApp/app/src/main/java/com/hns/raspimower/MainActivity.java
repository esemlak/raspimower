package com.hns.raspimower;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.raspimower.R;
import com.hns.raspimower.remote.RemoteMowerTask;

public class MainActivity extends AppCompatActivity {

    int max_steer = 200;
    int zero_steer = max_steer/2;
    int zero_speed = 30;
    int max_speed = 100 + zero_speed;

    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button exitButton = (Button) findViewById(R.id.exitButton);

        exitButton.setOnClickListener(new Button.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     new RemoteMowerTask().execute(RemoteMowerTask.Command_Stop);
                     finishAndRemoveTask();
                 }
             }
        );

        Button connectButton = (Button) findViewById(R.id.connectButton);

        connectButton.setOnClickListener(new Button.OnClickListener() {
              @Override
              public void onClick(View view) {
                  EditText ip_text = (EditText) findViewById(R.id.editTextIp);
                  String ip = ip_text.getText().toString();
                  new RemoteMowerTask().execute(RemoteMowerTask.CONNECT, ip);
              }
          }
        );


        SeekBar steerBar = (SeekBar) findViewById(R.id.steerBar);
// you should define max in xml, but if you need to do this by code, you must set max as 0 and then your desired value. this is because a bug in SeekBar (issue 12945) (don't really checked if it was corrected)
        steerBar.setMax(0);
        steerBar.setMax(max_steer);
        steerBar.setProgress(max_steer/2);

        steerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                TextView steerValue = (TextView) findViewById(R.id.steerValue);
                steerValue.setText(String.valueOf(progress-zero_steer));

                new RemoteMowerTask().execute(RemoteMowerTask.Command_Steer, String.valueOf(progress-zero_steer));

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(zero_steer);
            }
        });

        SeekBar speedBar = (SeekBar) findViewById(R.id.speedBar);
        speedBar.setMax(0);
        speedBar.setMax(max_speed);
        speedBar.setProgress(zero_speed);

        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                TextView speedValue = (TextView) findViewById(R.id.speedValue);
                speedValue.setText(String.valueOf(progress - zero_speed));

                new RemoteMowerTask().execute(RemoteMowerTask.Command_Speed, String.valueOf(progress - zero_speed));

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new Button.OnClickListener() {

            @Override
                public void onClick(View view) {
                    TextView speedValue = (TextView) findViewById(R.id.speedValue);
                    speedValue.setText(String.valueOf(0));
                    SeekBar speedBar = (SeekBar) findViewById(R.id.speedBar);
                    speedBar.setProgress(zero_speed);
                    new RemoteMowerTask().execute(RemoteMowerTask.Command_Stop);
                }
            }
        );

        Switch mowingSwitch = (Switch) findViewById(R.id.mowingSwitch);
        mowingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new RemoteMowerTask().execute(RemoteMowerTask.Command_Mow, "1");
                } else {
                    new RemoteMowerTask().execute(RemoteMowerTask.Command_Mow, "0");
                }
            }
        });


    }
}