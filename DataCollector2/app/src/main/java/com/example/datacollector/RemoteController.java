package com.example.datacollector;

import android.content.Context;
import android.content.pm.ServiceInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;


/**
 *
 * Remote Contoller client which connects to an @{@link NsdManager} Service
 *
 */
public abstract class RemoteController implements NsdManager.DiscoveryListener{

    MainActivity context;
    private NsdManager mNsdManager;
    private ServiceInfo info;
    private Socket s = null;
    private String TAG = "RemoteController";

    public RemoteController(MainActivity context){
        this.context = context;
    }

    public void setConnected(final boolean isConnected){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                TextView connView = context.findViewById(R.id.connection_view);
                connView.setText(isConnected ? "Connected" : "Not Connected");
                ToggleButton button = context.findViewById(R.id.toggleButton);
                button.setChecked(isConnected);
            }
        });
    }

    public void discoverServices(){

        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mNsdManager.discoverServices("_http._tcp", NsdManager.PROTOCOL_DNS_SD, this);
    }

    private byte[] getMessage(byte[] bytes) {
        int length = 0;
        for (byte b : bytes){
            if (b != 0) {
                length++;
            } else {
                continue;
            }
        }
        return Arrays.copyOf(bytes,length);
    }

    public abstract void gotMessage(String s) ;


    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.d(TAG, "onStartDiscoveryFailed: start failed");
        disccomect();
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {

        disccomect();

    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
        Log.d(TAG, "onDiscoveryStarted: Started");
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.d(TAG, "onDiscoveryStopped: Stopped");
        if (s == null){
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToggleButton button = context.findViewById(R.id.toggleButton);
                    button.setChecked(false);
                }
            });
        }
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
        Log.d(TAG, "onServiceFound: Found service");
        mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "onResolveFailed: Failed to resolve"+ errorCode);
            }

            @Override
            public void onServiceResolved(final NsdServiceInfo serviceInfo) {

                /**
                 * connection with local Server
                 */

                Thread listener = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Listen for new messages inc.

                        try {
                            s = new Socket(serviceInfo.getHost(), serviceInfo.getPort());
                            setConnected(true);
                            //mNsdManager.stopServiceDiscovery(getListener());
                            //s.getOutputStream().write("Samsung S6".getBytes());
                            String number = context.getNumber();
                            String name = Settings.Global.getString(context.getContentResolver(), (Settings.Global.DEVICE_NAME)) + " "+number;
                            s.getOutputStream().write(name.getBytes());
                            while (true) {
                                byte[] bytes = new byte[1024];

                                if (s.getInputStream().read(bytes) == -1){
                                    Log.d(TAG, "Socket Cloesd bcause -1");
                                    //connection closed
                                    disccomect();
                                    return;
                                }
                                byte [] message = getMessage(bytes);
                                gotMessage(new String(message));

                            }
                        } catch (IOException e) {
                            if (e instanceof SocketException){
                                //reset everything
                                setConnected(false);
                            }
                            Log.d(TAG, "run: No Socket -_-");
                        }
                    }});
                listener.start();
            }
        });
    }


    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        Log.d(TAG, "onServiceLost: Service Lost");
    //    disccomect();
    }

    public void disccomect() {
        try {
            if (s != null){
                s.close();
                s = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mNsdManager.stopServiceDiscovery(this);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        setConnected(false);
    }

    public NsdManager.DiscoveryListener getListener() {
        return this;
    }
}
