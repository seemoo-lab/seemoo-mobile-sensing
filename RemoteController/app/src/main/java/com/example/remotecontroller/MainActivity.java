package com.example.remotecontroller;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ServiceInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {


    private String TAG = "RemoteController";
    private NsdManager mNsdManager;
    private ServiceInfo info;
    private ServerSocket mServerSocket;
    private ArrayList<Socket> clients = new ArrayList<>();
    public HashMap<Socket, String> map = new HashMap();
    private ListView listView;
    public ArrayAdapter<String> listAdapter;
    private Timer t = new Timer();
    private ArrayList<AliveChecker> listOfChecker= new ArrayList<>();

    private String time = "";
    private String duration = "8";
    private PowerManager.WakeLock wl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list_view);
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        listView.setAdapter(listAdapter);
        //t.scheduleAtFixedRate(aliveChecker,0,1000);

        Thread backgroud = new Thread(new Runnable() {

            @Override
            public void run() {
                registerService(1337);
                //discoverServices();

            }
        });
        backgroud.run();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wl != null){
            wl.release();
        }
    }

    public void registerService(int port) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"CollectingSensorData");
        wl.acquire();
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("NsdChat");
        serviceInfo.setServiceType("_http._tcp.");

        try {
            mServerSocket = new ServerSocket(0);
            serviceInfo.setPort(mServerSocket.getLocalPort());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            foundSocket(mServerSocket.accept());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }




        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, new NsdManager.RegistrationListener() {
                    @Override
                    public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.d(TAG, "onRegistrationFailed: Register Failed");
                    }

                    @Override
                    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.d(TAG, "onUnregistrationFailed: Unregister Failed");
                    }

                    @Override
                    public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "onServiceRegistered: Name of the Service:" + serviceInfo.getServiceName());
                    }

                    @Override
                    public void onServiceUnregistered(NsdServiceInfo serviceInfo) {

                    }
                });
    }

    private void foundSocket(Socket accept) {
        if (!contains(clients,accept)){
            byte[] b = new byte[1024]; //should be big enough
            try {
                accept.getInputStream().read(b);
                final String name = new String(b);
                clients.add(accept);
                map.put(accept,name);
                AliveChecker checker = new AliveChecker(accept, this);
                listOfChecker.add(checker);
                checker.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.add(name);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean contains(ArrayList<Socket> clients, Socket accept) {
        for (Socket s : clients){
            if (s.getInetAddress().equals(accept.getInetAddress())){
                return true;
            }
        }
        return false;
    }





    public void setTime(View view){
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                TextView dateView = findViewById(R.id.date_view);
                time = hourOfDay+":"+ minute;
                dateView.setText(time);
            }
        },hour,minute,true );
        timePicker.show();

    }

    public void setDuration(View view){

        final NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(1);
        picker.setMaxValue(12);
        picker.setValue(8);



        AlertDialog diag = new AlertDialog.Builder(this).setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //DO nothing at all
            }
        }).setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextView durView = findViewById(R.id.dur_view);
                duration = String.valueOf(picker.getValue());
                durView.setText(String.valueOf(duration));
            }
        }).setView(picker).create();
        diag.show();
    }

    public void sendStart(View view) {
        send("SetDate:"+time+":"+duration);

    }

    private void send(final String start) {
        Log.d(TAG, "send: "+start);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Socket s : clients){
                    try {
                        s.getOutputStream().write(start.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public class AliveChecker extends Thread {
        final Socket socket;
        final MainActivity activity;

        public AliveChecker(final Socket socket, final MainActivity activity){
            super(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Log.d(TAG, "run: is Closed ? " + socket.getInputStream().read());
                        if (socket.getInputStream().read() == -1){
                            if (activity != null){
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.listAdapter.remove(activity.map.get(socket));
                                        activity.listAdapter.notifyDataSetChanged();
                                        activity.map.remove(socket);
                                        activity.clients.remove(socket);
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        //faild throw out the socket
                        if (activity != null){
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.listAdapter.remove(activity.map.get(socket));
                                    activity.listAdapter.notifyDataSetChanged();
                                    activity.map.remove(socket);
                                    activity.clients.remove(socket);
                                }
                            });
                        }
                    }
                }
            });
            this.activity = activity;
            this.socket = socket;
        }

    }
}

