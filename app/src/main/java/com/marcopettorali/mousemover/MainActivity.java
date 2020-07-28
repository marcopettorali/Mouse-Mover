package com.marcopettorali.mousemover;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.hardware.*;

import java.io.IOException;
import java.net.*;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private double y_value;
    private double z_value;

    boolean bl_sent = false;
    boolean tr_sent = false;

    boolean ip_set = false;

    Socket socket;
    String s_address;
    int s_port;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //create GUI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //initialize sensors
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);


        //setup ok button
        Button ok_btn = findViewById(R.id.ok_btn);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               EditText server_address = findViewById(R.id.server_address);
               EditText server_port = findViewById(R.id.server_port);

               s_address = server_address.getText().toString();
               s_port = Integer.parseInt(server_port.getText().toString());
               ip_set = true;

                Context context = getApplicationContext();
                CharSequence text = "Address and port set";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

        });

        //setup left-click button
        Button left_click = findViewById(R.id.left_click);
        left_click.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                String msg = "";
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    msg = "LD";
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    msg = "LU";
                }
                send(msg);
                return true;
            }

        });

        //setup right-click button
        Button right_click = findViewById(R.id.right_click);
        right_click.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                String msg = "";
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    msg = "RD";
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    msg = "RU";
                }
                send(msg);
                return true;
            }
        });

        //setup bottom-left button
        Button bot_left = findViewById(R.id.bottom_left_btn);
        bot_left.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!ip_set){
                    Context context = getApplicationContext();
                    CharSequence text = "Set server's ip address and port first";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }
                String msg = "BY" + y_value + "C" + z_value;
                send(msg);
                bl_sent = true;
            }
        });

        //setup top-right button
        Button top_right = findViewById(R.id.top_right_button);
        top_right.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!ip_set){
                    Context context = getApplicationContext();
                    CharSequence text = "Set server's ip address and port first";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }
                String msg = "TY" + y_value + "C" + z_value;
                send(msg);
                tr_sent = true;
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            String msg = "WD";
            send(msg);
        }

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            String msg = "WU";
            send(msg);
        }

        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            y_value = event.values[1];
        }
        if (event.sensor.getType()==Sensor.TYPE_ROTATION_VECTOR){
            z_value = event.values[2];
        }

        TextView debug = findViewById(R.id.debug_txt);
        debug.setText(Math.round(100.0 * y_value) / 100.0 + ";" + Math.round(100.0 * z_value) / 100.0);


        if(bl_sent && tr_sent) {
            String msg = "Y" + y_value + "C" + z_value;
            send(msg);
        }
    }

    public synchronized void send(final String msg){

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    byte[] buffer = msg.getBytes();
                    InetAddress address = null;

                    address = InetAddress.getByName(s_address);

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, s_port);
                    DatagramSocket datagramSocket = null;

                    datagramSocket = new DatagramSocket();
                    datagramSocket.send(packet);

                }catch(Exception e) {
                    TextView debug = findViewById(R.id.debug_txt);
                    debug.setText(e.getMessage());

                    if(!ip_set){
                        Context context = getApplicationContext();
                        CharSequence text = "Check server's address and port";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        return;
                    }
                }

            }

        });

        thread.start();


    }
}
