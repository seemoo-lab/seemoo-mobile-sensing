package com.example.datacollector;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 *  Main method for DataCollection app to record SensorData on the device. All data will be stored on the device under @{@link Environment#getExternalStorageDirectory()} path
 *  in the directory "SensorData", each recording is Tagged with its timestamp the recording happened
 *  The App uses @{@Link TrueTime} to sync the time with a time server and therefore needs internet connection for an initial synchronisation.
 *
 * @author Timm Lippert
 *
 */


public class MainActivity extends AppCompatActivity {


    public static final String SMAPLE_EXTRA = "SAMPLE_RATE";
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
    File accDataFile;


    /**
     * Senors to register Listener to.
     */
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
                writeHeader(luxDataStream,s);
                firstrun = false;
            }

            writeToFile(luxDataStream,printEventFirst(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    public Integer sampleAccel;
    public Integer sampleAccelUn;
    public Integer sampleAccelLin;
    public Integer sampleGyr;
    public Integer sampleGyrUn;
    public Integer sampleGrav;
    public Integer sampleMag;
    public Integer sampleMagUn;
    public Integer sampleLux;
    public Integer sampleBar;
    private boolean remoteSwitch = true;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //TODO: Implement what to do when switch for remote control is toggled
            if (key.equals(getString(R.string.pref_title_remote_switch))){
                if (sharedPreferences.getBoolean(key,false)){
                    remoteSwitch = true;
                }else{
                    remoteSwitch = false;
                }
                setRemoteSwitch(remoteSwitch);
            }
            updatePreferences();
        }
    };
    private boolean isRunning = false;



    public int[] getSampleValues(){
        return new int[] {
                sampleAccel,
                sampleAccelUn,
                sampleAccelLin,
                sampleGyr,
                sampleGyrUn,
                sampleMag,
                sampleMagUn,
                sampleGrav,
                sampleLux,
                sampleBar
        };
    }

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


    private SensorEventListener accDataListener = new SensorEventListener() {
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

    private int Hz100 = 10000; // 100HZ in µs
    private int Hz10 = 100000;
    private int Hz50 = 20000;
    private int Hz40 = 25000;
    private int Hz44 = 22500;
    private int Hz110 = 9000;
    private int Hz5 = 200000;
    private int divisor = 1000000;


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
            }
            writeToFile(gyrDataStream,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
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
    public com.example.datacollector.BluetoothScanner bluetoothScanner;
    public com.example.datacollector.WifiScanner wifiScanner;
    public com.example.datacollector.MyAudioRecorder myAudioRecorder;



    /**
     * Time and duration set for Recording over Remote app
     */
    private int hour;
    private int mins;
    private int dur;
    private int durMin = 0;

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

    SharedPreferences preferences;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        ctx = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(prefListener);


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

        //Set the current saved status to the remoteswitch
        updatePreferences();
        setRemoteSwitch(remoteSwitch);

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
        updatePreferences();
        //sensorManager.registerListener(testListener,unaccDataSensor,2500);
        //createFile("sensorTest");
        remote = new RemoteController(this) {
            @Override
            public void gotMessage(String s) {
                Log.d(TAG, "gotMessage: " +s );
                Calendar date = GregorianCalendar.getInstance();
                //Calendar date = Calendar.getInstance();
                if (s.contains("SetDate")){
                    if (startStopTimer != null){
                        startStopTimer.cancel();
                        startStopTimer.purge();
                    }
                    wl.acquire();
                    String times[] = s.split(":");
                    hour = Integer.parseInt(times[1]);
                    mins = Integer.parseInt(times[2]);
                    dur = Integer.parseInt(times[3]);
                    durMin = Integer.parseInt(times[4]);
                    startStopTimer = new Timer();
                    Log.d(TAG, "gotMessage: Time : "+ timeFormat.format(date.getTime()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textDur = findViewById(R.id.date_dur);
                            textDur.setText(String.valueOf(dur));
                            if (durMin < 10){
                                textDur.setText(dur+":0"+durMin);
                            } else{
                                textDur.setText(dur+":"+durMin);
                            }
                            TextView textStart = findViewById(R.id.date_start);
                            if (mins < 10){
                                textStart.setText(hour+":0"+mins);
                            } else{
                                textStart.setText(hour+":"+mins);
                            }
                        }
                    });
                    date.set(Calendar.HOUR_OF_DAY,hour);
                    date.set(Calendar.MINUTE,mins);
                    date.set(Calendar.SECOND,0);
                    final Calendar startDate = date;
                    Log.d(TAG, "gotMessage: Start Time : "+ timeFormat.format(date.getTime()));
                    startStopTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    startRecording();

                                    setStopTimer(startDate);
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

    private void setRemoteSwitch(final boolean remoteSwitch) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (remoteSwitch){

                    button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked){
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //startRecording();
                                        wl.acquire();
                                        remote.discoverServices();
                                    }
                                }).start();

                            } else {
                                remote.disconnect();
                            }
                        }
                    });
                    //Set default paramter for recording via Remote app
                    button.setText("Connect");
                    TextView textDur = findViewById(R.id.date_dur);
                    textDur.setVisibility(View.VISIBLE);
                    TextView textStart = findViewById(R.id.date_start);
                    textStart.setVisibility(View.VISIBLE);
                    findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                    findViewById(R.id.textView3).setVisibility(View.VISIBLE);
                } else {
                    button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked){
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //startRecording()
                                        startRecording();
                                    }
                                }).start();
                            } else {
                                //stop recording
                                stopRecording();
                            }
                        }
                    });
                    //Set default parameters for Recording via Button
                    if (isRunning){
                        button.setChecked(true);
                        button.setText("Stop");
                    } else {
                        button.setChecked(false);
                        button.setText("Start");
                    }
                    TextView textDur = findViewById(R.id.date_dur);
                    textDur.setVisibility(View.INVISIBLE);
                    TextView textStart = findViewById(R.id.date_start);
                    textStart.setVisibility(View.INVISIBLE);
                    findViewById(R.id.textView2).setVisibility(View.INVISIBLE);
                    findViewById(R.id.textView3).setVisibility(View.INVISIBLE);
                    button.setTextOn("Stop");
                    button.setTextOff("Start");
                }
            }
        });

    }

    private void setStopTimer(Calendar calendar) {
        wl.acquire();
        startStopTimer = new Timer();
        final Calendar date = calendar;
        date.add(Calendar.HOUR_OF_DAY,dur);
        date.add(Calendar.MINUTE,durMin);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.text_stop_view)).setText(timeFormat.format(date.getTime()));
            }
        });
        Log.d(TAG, "gotMessage: Stop Time : "+ timeFormat.format(date.getTime()));
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
        isRunning = false;

        addFile(accDataFile);
        addFile(unaccDataFile);
        addFile(ungyrDataFile);
        addFile(unmagDataFile);
        addFile(barDataFile);
        addFile(laccDataFile);
        addFile(gyrWDataFile);
        addFile(gravDataFile);
        addFile(magDataFile);
        addFile(gyrDataFile);
        addFile(luxDataFile);
        t.cancel();
        t.purge();
        if (wl != null && wl.isHeld()){
            try{
                wl.release();
            } catch (Throwable throwable){
                //ignore if releaseing a wakelock causes a problem, mih be already released
            }
        }


    }

    private void closeAllStreams() {
        try {
            if (unaccDataStream != null) unaccDataStream.close();
            if (ungyrDataStream != null) ungyrDataStream.close();
            if (unmagDataStream != null) unmagDataStream.close();
            if (barDataStream != null) barDataStream.close();
            if (laccDataStream != null) laccDataStream.close();
            if (gyrWDataStream != null) gyrWDataStream.close();
            if (magDataStream != null) magDataStream.close();
            if (gravDataStream != null) gravDataStream.close();
            if (gyrDataStream != null) gyrDataStream.close();
            if (luxDataStream != null) luxDataStream.close();
        } catch (IOException e) {
            e.printStackTrace();

        } catch (NullPointerException e){
            Log.d(TAG, "closeAllStreams: No Files written because streams are null");
        }
    }

    private void startRecording() {
        wl.acquire();
        updatePreferences();

        isRunning = true;

        syncTime(0);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd/kk_mm_ss");

        parentPath = Environment.getExternalStorageDirectory().getPath()+"/SensorData/"+format.format(TrueTime.now()).replace("/","_");
        //preparation for sensors
        path = new File(parentPath,"sensors");
        path.setReadable(true);
        path.setWritable(true);
        path.mkdirs();
        try {
            if (accDataSensor != null && sampleAccel >= 0){
                accDataFile = createFile("accData");
                accDataStream = new FileOutputStream(accDataFile);
            }
            if (luxDataSensor != null && sampleLux >= 0){
                luxDataFile = createFile("luxData");
                luxDataStream = new FileOutputStream(luxDataFile);
            }
            if (unaccDataSensor != null && sampleAccelUn >= 0){
                unaccDataFile = createFile("unaccData");
                unaccDataStream = new FileOutputStream(unaccDataFile);
            }
            if (ungyrDataSensor != null & sampleGyrUn >= 0){
                ungyrDataFile = createFile("ungyrData");
                ungyrDataStream = new FileOutputStream(ungyrDataFile);
            }
            if (unmagDataSensor != null && sampleMagUn >= 0){
                unmagDataFile = createFile("unmagData");
                unmagDataStream = new FileOutputStream(unmagDataFile);
            }
            if (barDataSensor != null && sampleBar >= 0){
                barDataFile = createFile("barData");
                barDataStream = new FileOutputStream(barDataFile);
            }
            if (laccDataSensor != null && sampleAccelLin >= 0){
                laccDataFile = createFile("laccData");
                laccDataStream = new FileOutputStream(laccDataFile);
            }
            if (gyrDataSensor != null & sampleGyr >= 0){
                gyrWDataFile = createFile("gyrWData");
                gyrDataFile = createFile("gyrData");
                gyrWDataStream = new FileOutputStream(gyrWDataFile);
                gyrDataStream =new FileOutputStream(gyrDataFile);
            }
            if (magDataSensor != null && sampleMag >= 0){
                magDataFile = createFile("magData");
                magDataStream = new FileOutputStream(magDataFile);
            }
            if (gravDataSensor!= null && sampleGrav >= 0){
                gravDataFile = createFile("gravData");
                gravDataStream = new FileOutputStream(gravDataFile);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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

    private void updatePreferences() {
        if (accDataSensor != null){
            sampleAccel = Integer.parseInt(preferences.getString(getString(R.string.accel),String.valueOf(divisor/accDataSensor.getMinDelay())));
        } else sampleAccel = Integer.parseInt(preferences.getString(getString(R.string.accel),"100"));
        if (unaccDataSensor != null){
            sampleAccelUn = Integer.parseInt(preferences.getString(getString(R.string.accelUnC),String.valueOf(divisor/unaccDataSensor.getMinDelay())));
        }else sampleAccelUn = Integer.parseInt(preferences.getString(getString(R.string.accelUnC),"100"));
        if (laccDataSensor != null){
            sampleAccelLin = Integer.parseInt(preferences.getString(getString(R.string.accelLin),String.valueOf(divisor/laccDataSensor.getMinDelay())));
        } else sampleAccelLin = Integer.parseInt(preferences.getString(getString(R.string.accelLin),"100"));
        if (gyrDataSensor != null){
            sampleGyr = Integer.parseInt(preferences.getString(getString(R.string.gyr),String.valueOf(divisor/gyrDataSensor.getMinDelay())));
        }else {
            sampleGyr = Integer.parseInt(preferences.getString(getString(R.string.gyr),"100"));
        }
        if (ungyrDataSensor != null){
            sampleGyrUn = Integer.parseInt(preferences.getString(getString(R.string.gyrUnC),String.valueOf(divisor/ungyrDataSensor.getMinDelay())));
        }else {
            sampleGyrUn = Integer.parseInt(preferences.getString(getString(R.string.gyrUnC),"100"));
        }
        if (gravDataSensor != null){
            sampleGrav = Integer.parseInt(preferences.getString(getString(R.string.grav),String.valueOf(divisor/gravDataSensor.getMinDelay())));
        }else {
            sampleGrav = Integer.parseInt(preferences.getString(getString(R.string.grav),"100"));
        }
        if (magDataSensor != null){
            sampleMag = Integer.parseInt(preferences.getString(getString(R.string.mag),String.valueOf(divisor/magDataSensor.getMinDelay())));
        }else {
            sampleMag = Integer.parseInt(preferences.getString(getString(R.string.mag),"100"));
        }
        if (luxDataSensor != null && luxDataSensor.getMinDelay() != 0){
            sampleLux = Integer.parseInt(preferences.getString(getString(R.string.lux),String.valueOf(divisor/luxDataSensor.getMinDelay())));
        }else {
            sampleLux = Integer.parseInt(preferences.getString(getString(R.string.lux),"100"));
        }
        if (unmagDataSensor != null){
            sampleMagUn = Integer.parseInt(preferences.getString(getString(R.string.magUn),String.valueOf(divisor/unmagDataSensor.getMinDelay())));
        }else {
            sampleMagUn = Integer.parseInt(preferences.getString(getString(R.string.magUn),"100"));
        }
        if (barDataSensor != null){
            sampleBar = Integer.parseInt(preferences.getString(getString(R.string.bar),String.valueOf(divisor/barDataSensor.getMinDelay())));
        } else {
            sampleBar = Integer.parseInt(preferences.getString(getString(R.string.bar),"100"));
        }
        remoteSwitch = preferences.getBoolean(getString(R.string.pref_title_remote_switch), false);
        myAudioRecorder.setEnabled(preferences.getBoolean(getString(R.string.pref_title_remote_switch_audio), false));
        wifiScanner.setEnabled(preferences.getBoolean(getString(R.string.pref_title_remote_switch_wifi), false));
        bluetoothScanner.setEnabled(preferences.getBoolean(getString(R.string.pref_title_remote_switch_ble), false));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wl != null && wl.isHeld()){
            wl.release();
        }
    }

    public void writeToFile(final OutputStream stream, final String text){
        try {
            stream.write(text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHeader(OutputStream stream, String[] headers){
        StringBuilder sb = new StringBuilder();
        for (String s: headers){
            sb.append(s);
            sb.append(" ");
        }
        sb.append("TS \r\n");
        writeToFile(stream,sb.toString());
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

        if (accDataSensor != null && sampleAccel >= 0){
            sensorManager.registerListener(accDataListener, accDataSensor, divisor/sampleAccel);
        }
        if (luxDataSensor != null && sampleLux >= 0){
            sensorManager.registerListener(luxDataListener,luxDataSensor,divisor/sampleLux);
        }
        if (unaccDataSensor != null && sampleAccelUn >= 0){
            sensorManager.registerListener(unaccDataListener, unaccDataSensor, divisor/sampleAccelUn);
        }
        if (ungyrDataSensor != null & sampleGyrUn >= 0){
            sensorManager.registerListener(ungyrDataListener, ungyrDataSensor, divisor/sampleGyrUn);
        }
        if (unmagDataSensor != null && sampleMagUn >= 0){
            sensorManager.registerListener(unmagDataListener, unmagDataSensor,divisor/sampleMagUn); //rework 100 is too much for 6p 50hz -> 20000
        }
        if (barDataSensor != null && sampleBar >= 0){
            sensorManager.registerListener(barDataListener, barDataSensor,divisor/sampleBar);
        }
        if (laccDataSensor != null && sampleAccelLin >= 0){
            sensorManager.registerListener(laccDataListener, laccDataSensor, divisor/sampleAccelLin);
        }
        if (gyrDataSensor != null & sampleGyr >= 0){
            sensorManager.registerListener(gyrDataListener, gyrDataSensor, divisor/sampleGyr);
        }
        if (magDataSensor != null && sampleMag >= 0){
            sensorManager.registerListener(magDataListener, magDataSensor,divisor/sampleMag); // rework 100 is too much
        }
        if (gravDataSensor!= null && sampleGrav >= 0){
            sensorManager.registerListener(gravDataListener, gravDataSensor, divisor/sampleGrav);
        }
    }

    public void addFile(File file){
        if (file != null){
            MediaScannerConnection.scanFile(this,new String[]{file.getPath()},null,null);
        }
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

    public void showSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SMAPLE_EXTRA,getSampleValues());
        startActivity(intent);
    }

    public void testSamplingRate(MenuItem item) {
        startTestForSamplingRate();

    }

    AlertDialog testDialog;
    Timer testTimer;
    private int testTime = 0;
    private ArrayList<TestSensorListener> testListenerList;

    private TimerTask testTask = new TimerTask() {
        @Override
        public void run() {
            if (testTime == 60) {
                stopTest();
                testTimer.cancel();
                testTimer.purge();

            } else {
                testTime++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        testDialog.setMessage("Time: "+testTime);
                    }
                });
            }
        }
    };

    private void stopTest() {
        for (TestSensorListener listener : testListenerList){
            sensorManager.unregisterListener(listener);
        }

        final StringBuilder sb = new StringBuilder();

        for (TestSensorListener listener : testListenerList){
            sb.append(listener.name).append(": ").append(String.valueOf(listener.getSamples()/testTime)).append("Hz ").append(listener.samples).append("\n");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testDialog.setMessage(sb.toString());
            }
        });


    }


    private void startTestForSamplingRate() {
        testTime = 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (testDialog == null){
                    testDialog = new AlertDialog.Builder(getContext()).setMessage("Time: 0").setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            dialog.cancel();
                        }
                    }).create();
                }
                testDialog.show();
                new Thread(new Runnable() {
                     @Override
                     public void run() {
                         testTimer = new Timer();
                         testTimer.scheduleAtFixedRate(testTask,1000,1000);
                         registerAllSensorsTest();
                     }
                 }).start();

            }
        });
    }

    private void registerAllSensorsTest() {
        testListenerList = new ArrayList<>();
        if (accDataSensor != null && sampleAccel >= 0){
            testListenerList.add(new TestSensorListener("Accelerometer"));
            sensorManager.registerListener(testListenerList.get(testListenerList.size()-1), accDataSensor, divisor/sampleAccel);
        }
        if (luxDataSensor != null && sampleLux >= 0){
            testListenerList.add(new TestSensorListener("Lux"));
            sensorManager.registerListener(testListenerList.get(testListenerList.size()-1),luxDataSensor,divisor/sampleLux);
        }
        if (unaccDataSensor != null && sampleAccelUn >= 0){
            testListenerList.add(new TestSensorListener("AccelerometerUN"));
            sensorManager.registerListener(testListenerList.get(testListenerList.size()-1), unaccDataSensor, divisor/sampleAccelUn);
        }
        if (ungyrDataSensor != null & sampleGyrUn >= 0){
            testListenerList.add(new TestSensorListener("Gyroscope UN"));
            sensorManager.registerListener(testListenerList.get(testListenerList.size()-1), ungyrDataSensor, divisor/sampleGyrUn);
        }
        if (unmagDataSensor != null && sampleMagUn >= 0){
            testListenerList.add(new TestSensorListener("Magnetometer UN"));
            sensorManager.registerListener(testListenerList.get(testListenerList.size()-1), unmagDataSensor,divisor/sampleMagUn); //rework 100 is too much for 6p 50hz -> 20000
        }
        if (barDataSensor != null && sampleBar >= 0){
            testListenerList.add(new TestSensorListener("Barometer"));
            sensorManager.registerListener(testListenerList.get(testListenerList.size()-1), barDataSensor,divisor/sampleBar);
        }
        if (laccDataSensor != null && sampleAccelLin >= 0){
            testListenerList.add(new TestSensorListener("Accelerometer Lin"));
            sensorManager.registerListener(testListenerList.get(testListenerList.size()-1), laccDataSensor, divisor/sampleAccelLin);
        }
        if (gyrDataSensor != null & sampleGyr >= 0){
            testListenerList.add(new TestSensorListener("Gyroscope"));
            sensorManager.registerListener(testListenerList.get(testListenerList.size()-1), gyrDataSensor, divisor/sampleGyr);
        }
        if (magDataSensor != null && sampleMag >= 0){
            testListenerList.add(new TestSensorListener("Magnetometer"));
            sensorManager.registerListener(testListenerList.get(testListenerList.size()-1), magDataSensor,divisor/sampleMag); // rework 100 is too much
        }
        if (gravDataSensor!= null && sampleGrav >= 0){
            testListenerList.add(new TestSensorListener("Gravity"));
            sensorManager.registerListener(testListenerList.get(testListenerList.size()-1), gravDataSensor, divisor/sampleGrav);
        }
    }

    public Context getContext() {
        return this;
    }
}
