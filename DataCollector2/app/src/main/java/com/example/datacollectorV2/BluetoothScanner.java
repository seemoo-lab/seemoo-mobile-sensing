package com.example.datacollectorV2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Pair;

import com.instacart.library.truetime.TrueTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class BluetoothScanner {
    private static final int REQUEST_ENABLE_BT = 10001;
    private MainActivity mainActivity;
    private BluetoothAdapter bluetoothAdapter;
    private File bluetoothDataFile;
    private FileOutputStream fileOutputStream;
    Timer t;
    File path;
    String TAG = "BLE";


    private ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            Pair pair = new Pair<>(device.getAddress(), rssi);
            addToList(printList,pair);
        }
    };

    private void addToList(ArrayList<Pair<String, Integer>> printList, Pair<String, Integer> toList) {
        for (Pair<String,Integer> element: printList){
            if (element.first.equals(toList.first)){
                printList.remove(element);
                printList.add(toList);
                return;
            }
        }
        printList.add(toList);
    }

    public BluetoothScanner(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        bluetoothAdapter = ((BluetoothManager) mainActivity.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mainActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        timeFormat = new SimpleDateFormat("yyyy-MM-dd/kk:mm:ss.SSS");
    }

    public void startDiscovery(){
        path = new File(mainActivity.parentPath,"ble");
        bluetoothDataFile = createFile("ble");
        path.setReadable(true);
        path.setWritable(true);
        path.mkdirs();
        try {
            fileOutputStream = new FileOutputStream(bluetoothDataFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mainActivity.registerReceiver(mReceiver  , filter);
        //bluetoothAdapter.startDiscovery();
        startTimer();
    }

    //Start Timer ever 10 seconds 1000ms = 1 sec
    private void startTimer() {
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Should work --- according to documentation discovery should be up for 12 sec but we want new discovery every 10 seconds
                bluetoothAdapter.getBluetoothLeScanner().stopScan(callback);
                /*if (bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                }*/
                bluetoothAdapter.getBluetoothLeScanner().startScan(callback);
                if (!printList.isEmpty()){
                    print(printList);
                    printList = new ArrayList<>();
                }

            }
        },0,1000*10);
    }

    public void stop(){
        t.cancel();
        t.purge();

        //bluetoothAdapter.cancelDiscovery();
        //mainActivity.unregisterReceiver(mReceiver);
        bluetoothAdapter.getBluetoothLeScanner().stopScan(callback);
        if (!printList.isEmpty()){
            print(printList);
        }
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mainActivity.addFile(bluetoothDataFile);

    }

    short minVal = 0;

    ArrayList<Pair<String,Integer>> printList = new ArrayList<>();

    private SimpleDateFormat timeFormat;
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,minVal);
                printList.add(new Pair<String, Integer>(device.getAddress(),rssi));
            }
        }
    };

    private void print(List<Pair<String,Integer>> results){
        StringBuilder sb = new StringBuilder();
        String time = timeFormat.format(TrueTime.now()).replace("/", "T");
        for (Pair<String,Integer>device : results ) {
            sb.append(device.first);
            sb.append(" ");
            sb.append(device.second);
            sb.append("dBm ");
            sb.append(time);
            sb.append("\r\n");
        }
        mainActivity.writeToFile(fileOutputStream,sb.toString());

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
}
