package com.example.datacollector;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.instacart.library.truetime.TrueTime;

import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 *  Main method for DataCollection app to record audio @{@link MyAudioRecorder}, scan for Bluetooth @{@link BluetoothScanner} and Wifi @{@link WifiScanner}
 *  and also to record SensorData on the device. All data will be stored on the device under @{@link Environment#getExternalStorageDirectory()} path
 *  in the directory "SensorData", each recording is Tagged with its timestamp the recording happened
 *  The App uses @{@Link TrueTime} to sync the time with a time server and therefore needs internet connection for an initial synchronisation.
 *
 * @author Timm Lippert
 *
 */


public class MainActivity extends AppCompatActivity {


    /**
     * Files for Sensordata
     */

    File path;
    File unaccDataFile;
    File ungyrDataFile;
    File unmagDataFile;
    File barDataFile;
    File laccDataFile;
    File gyrWDataFile;
    File gyrDataFile;
    File magDataFile;
    File gravDataFile;
    File luxDataFile;




    private Sensor unaccDataSensor;
    private Sensor ungyrDataSensor;
    private Sensor unmagDataSensor;
    private Sensor barDataSensor;
    private Sensor laccDataSensor;
    private Sensor gyrDataSensor;
    private Sensor magDataSensor;
    private Sensor gravDataSensor;
    private Sensor luxDataSensor;


    /**
     * Outputstreams for given files to wirte to
     */
    private FileOutputStream luxDataStream;
    private FileOutputStream unaccDataStream;
    private FileOutputStream ungyrDataStream;
    private FileOutputStream unmagDataStream;
    private FileOutputStream barDataStream;
    private FileOutputStream laccDataStream;
    private FileOutputStream gyrWDataStream;
    private FileOutputStream gyrDataStream;
    private FileOutputStream magDataStream;
    private FileOutputStream gravDataStream;
    private FileOutputStream accDataStream;
    public SimpleDateFormat timeFormat;


    /**
     * SensorListener to record sensors at given rate
     */

    public SensorEventListener luxDataListener = new SensorEventListener() {

        boolean firstrun = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                String[] s;
                if (event.values.length > 1){
                    s =new String[] {"V1","V2","V3"};
                } else{
                    s =new String[] {"V1"};
                }
            //    writeHeader(barDataStream,s);
                firstrun = false;
            }

            writeToFile(luxDataStream,printEventFirst(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private String printEventFirst(SensorEvent event) {
        StringBuilder sb = new StringBuilder();

        sb.append(event.values[0]);
        sb.append(" ");
        sb.append(timeFormat.format(TrueTime.now()).replace("/","T"));
        sb.append("\r\n");
        return sb.toString();
    }

    public SensorEventListener barDataListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                String[] s;
                if (event.values.length > 1){
                    s =new String[] {"V1","V2","V3"};
                } else{
                    s =new String[] {"V1"};
                }
                writeHeader(barDataStream,s);
                firstrun = false;
            }

            writeToFile(barDataStream,printEventFirst(event));
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        boolean firstrun = true;
    };
    private Date oldDateMagnet= new Date(0);
    public SensorEventListener unmagDataListener = new SensorEventListener() {
        public boolean firstrun = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(unmagDataStream,new String[]{"X","Y","Z","E_X","E_Y","E_Z"});
                firstrun = false;
            }
            setMagnetValue(event.values);
            writeToFile(unmagDataStream,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener testListener  = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            //Log.d(TAG, "onSensorChanged: time: " +(event.timestamp - old));
            //oldDate = temp;
            //old = event.timestamp;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private long Hz110ns = 9000000;
    private SensorEventListener accDataListener = new SensorEventListener() {
        long oldstamp = 0;
        public boolean firstrun = true;
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(accDataStream,new String[]{"X","Y","Z"});
                firstrun = false;
            }
            writeToFile(accDataStream,printEvent(event));

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Timer t;
    private TextView textView;
    private PowerManager.WakeLock wl;
    private Sensor accDataSensor;
    private File accDataFile;
    private int Hz100 = 10000; // 100HZ in µs
    private int Hz10 = 100000;
    private int Hz50 = 20000;
    private int Hz40 = 25000;
    private int Hz44 = 22500;
    private int Hz110 = 9000;
    private int Hz5 = 200000;


    private String printValues(float[] values, int size) {
        StringBuilder sb = new StringBuilder();
        if (values == null){
            return "";
        }
        if (size == 0){
            size = values.length;
        }
        for (int i = 0; i < size; i++){
            sb.append(values[i]);
            sb.append(" ");
        }
        sb.append(timeFormat.format(TrueTime.now()).replace("/","T"));
        sb.append("\r\n");
        return sb.toString();
    }
    private String printEvent(SensorEvent event) {
        StringBuilder sb = new StringBuilder();
        for (float val : event.values){
            sb.append(val);
            sb.append(" ");
        }
        sb.append(timeFormat.format(TrueTime.now()).replace("/","T"));
        sb.append("\r\n");
        return sb.toString();
    }

    private SensorEventListener magDataListener = new SensorEventListener() {
        public boolean firstrun = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(magDataStream,new String[]{"X","Y","Z"});
                firstrun = false;
            }
            writeToFile(magDataStream,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener gyrDataListener = new SensorEventListener() {
        public boolean firstrun = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                //writeHeader(gyrWDataStream,new String[]{"X","Y","Z"});
                writeHeader(gyrDataStream,new String[]{"X","Y","Z"});
                firstrun = false;
            }
            /*
            float[] rotationMatrix = new float[16] ,inclinateMatrix = new float[16], invertedM = new float[16], earthCord = new float[16],extendedVector= new float[4];
            if (gravityValue == null || magnetValue == null){
                return;
            }
            if (SensorManager.getRotationMatrix(rotationMatrix,inclinateMatrix,gravityValue, magnetValue)){

                if (android.opengl.Matrix.invertM(invertedM,0,rotationMatrix,0)){
                    extendedVector[0] = event.values[0];
                    extendedVector[1] = event.values[1];
                    extendedVector[2] = event.values[2];
                    extendedVector[3] = 0;
                    android.opengl.Matrix.multiplyMV(earthCord,0,invertedM,0,extendedVector,0);
                    //Log.d(TAG, "onSensorChanged: New Matrix: " +printValues(earthCord,3));
                    writeToFile(gyrWDataStream,printValues(earthCord,3));
                }
            }*/
            writeToFile(gyrDataStream,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Date oldDateAccel = new Date(0);
    private float[] gravityValue=null;
    private float[] magnetValue =null;
    private SensorEventListener laccDataListener = new SensorEventListener() {
        public boolean firstrun = true;

        //TODO: check if implemented correct
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(laccDataStream,new String[]{"X","Y","Z"});
                firstrun = false;
            }

            writeToFile(laccDataStream,printEvent(event));
            //
            //Log.d(TAG, "onSensorChanged: Original : " + printEvent(event));
            //Log.d(TAG, "onSensorChanged: Realworld: " + printValues(earthCord,3));

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener gravDataListener = new SensorEventListener() {
        public boolean firstrun = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(gravDataStream,new String[]{"X","Y","Z"});
                firstrun = false;
            }
            setGravityValue(event.values);
            writeToFile(gravDataStream,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Date oldDateGyro= new Date(0);

    public SensorEventListener ungyrDataListener = new SensorEventListener() {
        private boolean firstrun = true;
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(ungyrDataStream,new String[]{"X","Y","Z","E_X","E_Y","E_Z"});
                firstrun = false;
            }
            writeToFile(ungyrDataStream,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Date oldDateAccelUn= new Date(0);
    public SensorEventListener unaccDataListener = new SensorEventListener() {
        private boolean firstrun = true;
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(unaccDataStream,new String[]{"X","Y","Z","E_X","E_Y","E_Z"});
                firstrun = false;
            }
            writeToFile(unaccDataStream,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    private Context ctx;


    private String TAG = "DataCollector";
    private ToggleButton button;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> arrayAdapter;
    private CharSequence type = "0";
    private RemoteController remote;
    private SensorManager sensorManager;
    private int requestCode = 1234;

    public String parentPath;


    /**
     * BLE WIFI and Audio Recorder
     */
    public BluetoothScanner bluetoothScanner;
    public WifiScanner wifiScanner;
    public MyAudioRecorder myAudioRecorder;


    /**
     * Time and duration set for Recording over Remote app
     */
    private int hour;
    private int mins;
    private int dur;

    private Timer startStopTimer;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == this.requestCode){

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings,menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        ctx = this;

        //First attempt to sync up time zo initialize clock if it fails it can be
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                    if (TrueTime.isInitialized()){
                        button.setEnabled(true);
                        return;
                    }
                    syncTime(0);
                    //TrueTime.build().initialize();
                    Log.d(TAG, "run: initialized");

            }
        });


        // Claim wakelock to prevent this app from slowing down
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"CollectingSensorData");
        wl.acquire();
        timeFormat = new SimpleDateFormat("yyyy-MM-dd/kk:mm:ss.SSS");
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.toggleButton);
        button.setEnabled(false);

        // set up ble wifi and audio
        bluetoothScanner = new BluetoothScanner(this);
        wifiScanner = new WifiScanner(this);
        myAudioRecorder = new MyAudioRecorder(this);


        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    // start recording

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //startRecording();
                            remote.discoverServices();
                        }
                    }).start();

                } else {
                    //stop recording
                    //stopRecording();
                    remote.disccomect();
                }
            }
        });
        //Spinner to select device number
        spinner = findViewById(R.id.selectionItem);
        arrayAdapter = ArrayAdapter.createFromResource(this,R.array.spinner_array,android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = arrayAdapter.getItem(position);
                Log.d(TAG, "onItemSelected: is now "+type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(arrayAdapter.getPosition("0"));

        textView = findViewById(R.id.textView);

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WAKE_LOCK,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.RECORD_AUDIO},requestCode);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sens = sensorManager.getSensorList(Sensor.TYPE_ALL);

        unaccDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
        luxDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        ungyrDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        if (ungyrDataSensor == null){
            ungyrDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        unmagDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        if (unmagDataSensor == null ){
            unmagDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        }

        barDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        laccDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyrDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravDataSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        //sensorManager.registerListener(testListener,unaccDataSensor,2500);
        //createFile("sensorTest");
        remote = new RemoteController(this) {
            @Override
            public void gotMessage(String s) {
                Log.d(TAG, "gotMessage: " +s );
                Calendar date = Calendar.getInstance();
                if (s.contains("SetDate")){
                    if (startStopTimer != null){
                        startStopTimer.cancel();
                        startStopTimer.purge();
                    }
                    String times[] = s.split(":");
                    hour = Integer.parseInt(times[1]);
                    mins = Integer.parseInt(times[2]);
                    dur = Integer.parseInt(times[3]);
                    startStopTimer = new Timer();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textDur = findViewById(R.id.date_dur);
                            textDur.setText(String.valueOf(dur));
                            TextView textStart = findViewById(R.id.date_start);
                            textStart.setText(hour+":"+mins);
                        }
                    });
                    date.set(Calendar.HOUR_OF_DAY,hour);
                    date.set(Calendar.MINUTE,mins);
                    date.set(Calendar.SECOND,0);
                    startStopTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    startRecording();

                                    setStopTimer();

                                    //remote.discoverServices();
                                }
                            }).start();
                        }
                    },date.getTime());
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            button.setChecked(true);
                        }
                    });
                    */

                }if (s.equals("Stop")){
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            button.setChecked(false);
                        }
                    });
                */
                }
            }
        };

    }

    private void setStopTimer() {
        startStopTimer = new Timer();
        Calendar date = Calendar.getInstance();
        date.add(Calendar.HOUR_OF_DAY,dur);
        startStopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopRecording();
            }
        },date.getTime());
    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx,msg,Toast.LENGTH_LONG);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void stopRecording() {
        bluetoothScanner.stop();
        wifiScanner.stop();
        myAudioRecorder.stop();
        unregisterAllSensors();
        closeAllStreams();
        addFile(accDataFile);
        //addFile(unaccDataFile);
        //addFile(ungyrDataFile);
        //addFile(unmagDataFile);
        addFile(barDataFile);
        //addFile(laccDataFile);
        //addFile(gyrWDataFile);
        //addFile(gravDataFile);
        addFile(magDataFile);
        addFile(gyrDataFile);
        addFile(luxDataFile);
        t.cancel();
        t.purge();
        wl.release();


    }

    private void closeAllStreams() {
        try {
            unaccDataStream.close();
            ungyrDataStream.close();
            unmagDataStream.close();
            barDataStream.close();
            laccDataStream.close();
            gyrWDataStream.close();
            magDataStream.close();
            gravDataStream.close();
            gyrDataStream.close();
            luxDataStream.close();
        } catch (IOException e) {
            e.printStackTrace();

        } catch (NullPointerException e){
            Log.d(TAG, "closeAllStreams: No Files written because streams are null");
        }
    }

    private void startRecording() {



        syncTime(0);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd/kk_mm_ss");

        parentPath = Environment.getExternalStorageDirectory().getPath()+"/SensorData/"+format.format(TrueTime.now()).replace("/","_");
        //preparation for sensors
        path = new File(parentPath,"sensors");
        path.setReadable(true);
        path.setWritable(true);
        path.mkdirs();
        if (accDataSensor != null){
            accDataFile = createFile("accData");
        }
        luxDataFile = createFile("luxData");
        //unaccDataFile = createFile("unaccData");
        //ungyrDataFile = createFile("ungyrData");
        //unmagDataFile = createFile("unmagData");
        barDataFile = createFile("barData");
        //laccDataFile = createFile("laccData");
        //gyrWDataFile = createFile("gyrWData");
        gyrDataFile = createFile("gyrData");
        magDataFile = createFile("magData");
        //gravDataFile = createFile("gravData");

        try {
            if (accDataSensor != null){
                accDataStream = new FileOutputStream(accDataFile);
            }
//            unaccDataStream = new FileOutputStream(unaccDataFile);
            //writeHeader(unaccDataStream,unaccDataSensor);
//            ungyrDataStream = new FileOutputStream(ungyrDataFile);
            //writeHeader(ungyrDataStream,ungyrDataSensor);
  //          unmagDataStream = new FileOutputStream(unmagDataFile);
            //writeHeader(unmagDataStream,unmagDataSensor);
            barDataStream = new FileOutputStream(barDataFile);
    //        laccDataStream = new FileOutputStream(laccDataFile);
      //      gyrWDataStream = new FileOutputStream(gyrWDataFile);
            gyrDataStream =new FileOutputStream(gyrDataFile);
            magDataStream = new FileOutputStream(magDataFile);
          //  gravDataStream = new FileOutputStream(gravDataFile);
            luxDataStream = new FileOutputStream(luxDataFile);
            //writeHeader(barDataStream,barDataSensor);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("Couldnt create FileStreams to write data");
                }
            });
        }

        //Start Wifi and Bluetooth discovery
        startWifiDiscovery();
        startBluetoothDiscovery();
        startAudio();


        startCountDown();
    }

    private void startAudio() {
        myAudioRecorder.start();
    }

    private void  startBluetoothDiscovery() {
        bluetoothScanner.startDiscovery();
    }
    private void startWifiDiscovery() {
        wifiScanner.startDiscovery();
    }

    Calendar calendar;
    SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
    private void startCountDown() {
        t = new Timer();
        Date date = new Date(0L);
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR,-1);

        //TODO: make countdown for recording data; Not really nessesary because we have almost no deleay in getting the actual time
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                calendar.add(Calendar.SECOND,1);
                calendar.getTime();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(timerFormat.format(calendar.getTime()));
                    }
                });

            }
        },0,1000);
        registerAllSensors();
    }

    public File createFile(String fileName) {

        File file = new File(path,/*type+"_"+*/fileName+".txt");
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
    static int attempt = 0;
    private void syncTime(int i) {
        try {
            switch (i) {
                case 0 : TrueTime.build().withNtpHost("0.de.pool.ntp.org").initialize(); break;
                case 1 : TrueTime.build().withNtpHost("1.de.pool.ntp.org").initialize(); break;
                case 2 : TrueTime.build().withNtpHost("2.de.pool.ntp.org").initialize(); break;
                case 3 : TrueTime.build().withNtpHost("3.de.pool.ntp.org").initialize(); break;
                default:  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!TrueTime.isInitialized()){
                            textView.setText("Time could not sync");
                            findViewById(R.id.button_resync).setVisibility(View.VISIBLE);
                        }
                    }
                });
                return;

            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("00:00:00");
                    button.setEnabled(true);
                    findViewById(R.id.button_resync).setVisibility(View.GONE);
                }
            });

            //TrueTime.build().initialize();
        } catch (IOException e) {
            e.printStackTrace();
            syncTime(attempt++);
        }

    }

    public void reSync(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncTime(1);
            }
        }).start();

    }

    public void writeToFile(final OutputStream stream, final String text){
        try {
            stream.write(text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHeader(OutputStream stream, String[] headers){
        /*StringBuilder sb = new StringBuilder();
        for (String s: headers){
            sb.append(s);
            sb.append(" ");
        }
        sb.append("TS \r\n");
        writeToFile(stream,sb.toString());*/
    }

    private void unregisterAllSensors(){
        if (accDataSensor != null){
            sensorManager.unregisterListener(accDataListener);
            accDataListener.onSensorChanged(null);

        }
        sensorManager.unregisterListener(unaccDataListener);
        unaccDataListener.onSensorChanged(null);
        sensorManager.unregisterListener(ungyrDataListener);
        ungyrDataListener.onSensorChanged(null);
        sensorManager.unregisterListener(unmagDataListener);
        unmagDataListener.onSensorChanged(null);
        sensorManager.unregisterListener(barDataListener);
        barDataListener.onSensorChanged(null);
        sensorManager.unregisterListener(laccDataListener);
        laccDataListener.onSensorChanged(null);
        sensorManager.unregisterListener(gyrDataListener);
        gyrDataListener.onSensorChanged(null);
        sensorManager.unregisterListener(gravDataListener);
        gravDataListener.onSensorChanged(null);
        sensorManager.unregisterListener(magDataListener);
        magDataListener.onSensorChanged(null);
        sensorManager.unregisterListener(luxDataListener);
        luxDataListener.onSensorChanged(null);

    }

    public String getNumber(){
        return (String) type;
    }

    private void registerAllSensors(){

        if (accDataSensor != null){
            sensorManager.registerListener(accDataListener, accDataSensor, Hz50);
        }
        //sensorManager.registerListener(unaccDataListener, unaccDataSensor, Hz110);
        //sensorManager.registerListener(ungyrDataListener, ungyrDataSensor, Hz110);
        if (unmagDataSensor.getMinDelay() != Hz100){
            //sensorManager.registerListener(unmagDataListener, unmagDataSensor,Hz50,0); //rework 100 is too much for 6p 50hz -> 20000
        } else{
            //sensorManager.registerListener(unmagDataListener, unmagDataSensor, Hz110,0); //rework 100 is too much for 6p 50hz -> 20000
        }
        if (barDataSensor.getMinDelay() == Hz10){
          sensorManager.registerListener(barDataListener, barDataSensor,Hz10); // rework for 6s 200000 -> 5 hz
        } else {
          sensorManager.registerListener(barDataListener, barDataSensor,200000); // rework for 6s 200000 -> 5 hz
        }
        //sensorManager.registerListener(laccDataListener, laccDataSensor, Hz110);

        sensorManager.registerListener(gyrDataListener, gyrDataSensor, Hz50,0);
        if (magDataSensor.getMinDelay() !=Hz100){
            sensorManager.registerListener(magDataListener, magDataSensor,20000); // rework 100 is too much
        }else {
            sensorManager.registerListener(magDataListener, magDataSensor, Hz50); // rework 100 is too much
        }

        //sensorManager.registerListener(gravDataListener, gravDataSensor, Hz110);

        sensorManager.registerListener(luxDataListener,luxDataSensor,Hz5);

    }

    public void addFile(File file){
        MediaScannerConnection.scanFile(this,new String[]{file.getPath()},null,null);
    }

    public void deleteData(MenuItem item) {
        String[] files;
        path = new File(Environment.getExternalStorageDirectory().getPath(),"SensorData");
        deleteDirectory(path);
        path.mkdirs();
        MediaScannerConnection.scanFile(this,new String[]{path.getPath()},null,null);
        //files = path.listFiles();
        /*files = path.list();
        path.
        File file;
        if (files != null){
            for (int i = 0 ; i < files.length; i++){

                file = files[i].getAbsoluteFile();
                file.delete();
                addFile(file);

            }
        }
        */

    }

    /**
     * delets all files and directories of the gievn @param
     * @param file file or directory to delete with underlaying files and folders
     */
    void deleteDirectory(File file){
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
                for (File entry : entries) {
                    deleteDirectory(entry);
                }
        }
        file.setWritable(true);
        file.setWritable(true);
        Log.d(TAG, "deleteDirectoryRecursionJava6: "+file.getName()+file.delete());
        addFile(file);
    }

    synchronized public void setMagnetValue(float[] geoValue) {
        this.magnetValue = geoValue;
    }

    synchronized public void setGravityValue(float[] gravityValue) {
        this.gravityValue = gravityValue;
    }
}
