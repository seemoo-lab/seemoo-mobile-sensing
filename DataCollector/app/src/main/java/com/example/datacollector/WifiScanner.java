package com.example.datacollector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.crashlytics.android.Crashlytics;
import com.instacart.library.truetime.TrueTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class WifiScanner extends BroadcastReceiver {
    private final SimpleDateFormat timeFormat;
    MainActivity mainActivity;
    File wifiDataFile;
    FileOutputStream fileOutputStream;
    WifiManager wifiManager;
    private File path;
    Timer t;
    Date previousDate;


    private boolean isScanning = false;
    private boolean isEnabled = false;

    public WifiScanner(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        wifiManager = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
        timeFormat = new SimpleDateFormat("yyyy-MM-dd/kk:mm:ss.SSS");
        t = new Timer();
    }


    int iteration = 0;

    public void startDiscovery(){
        if (!isEnabled){
            return;
        }
        mainActivity.registerReceiver(this,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        path = new File(mainActivity.parentPath,"wifi");
        wifiDataFile = createFile("wifi");
        path.setReadable(true);
        path.setWritable(true);
        path.mkdirs();
        try {
            fileOutputStream = new FileOutputStream(wifiDataFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //start scan every 5 seconds so we have 2 scans per 10 sconds and we will take the union of both scans
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (iteration++ == 2){
                    print(printList);
                    printList = new ArrayList<>();
                    iteration = 1;
                }
                if (!isScanning){
                    boolean isStarted = wifiManager.startScan();
                    if (!isStarted) {
                        Crashlytics.log("Something with scanning went wrong \n Start scan is : "+isStarted);
                    }
                    isScanning = true;
                }

            }},0,5000);
    }



    public void stop() {
        if (!isEnabled){
            return;
        }
        mainActivity.unregisterReceiver(this);
        t.cancel();
        t.purge();
        t=new Timer();
        if (!printList.isEmpty()){
            print(printList);
            printList = new ArrayList<>();
        }
        mainActivity.addFile(wifiDataFile);
    }

    private List<ScanResult> printList = new ArrayList();
    @Override
    public void onReceive(Context context, Intent intent) {
        isScanning = false;
        for (ScanResult result : wifiManager.getScanResults()){
            if (contains(printList,result)) continue;
            printList.add(result);
        }
    }

    private boolean contains(List<ScanResult> oldList, ScanResult result) {
        for (ScanResult temp : oldList){
            if(result.BSSID.equals(temp.BSSID)){
                return true;
            }
        }
        return false;
    }

    public void print(List<ScanResult> results){
        StringBuilder sb;
        String time = timeFormat.format(TrueTime.now()).replace("/","T");
        for (ScanResult result :results){
            sb=new StringBuilder();
            sb.append(result.BSSID);
            sb.append(" ");
            sb.append(result.level);
            sb.append( "dBm ");
            sb.append(time);
            sb.append("\r\n");
            mainActivity.writeToFile(fileOutputStream,sb.toString());
        }
    }

    public File createFile(String fileName) {

        File file = new File(path,fileName+".txt");
        try {
            if (file.exists()){
                file.createNewFile();
                file.setReadable(true);
                file.setWritable(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
}
