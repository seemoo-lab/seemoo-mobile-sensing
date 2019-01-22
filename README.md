# Android DataCollector App (with RemoteController)

This folder contains the source code of the Anroid apps used for data collection (audio, sensor data, WiFi and BLE beacons) in the paper "Perils of Zero-Interaction Security in the Internet of Things", by Mikhail Fomichev, Max Maass, Lars Almon, Alejandro Molina, Matthias Hollick, in Proceedings of the ACM on Interactive, Mobile, Wearable and Ubiquitous Technologies, vol. 3, Issue 1, 2019.

The *DataController* app contains the core data collection functionality. The data collection can either be started by pressing a button in *DataController* (RECOMMENDED) or via the remote app (*RemoteController*) to which a target sensing device needs to connect to.

**Note:** While we provide the source code for the *RemoteController*, we do not recommend using it in combination with *DataController* to start it remotely (star the *DataController* by pressing a button)—with this setup we experienced some unexpected errors, jeopardizing reliable data collection. 

In our experimetns we used *Samsung Galaxy S6* phones with *Android 7.0* to collect the following sensor modalities (The *DataController* app was also tested with *Nexus 5X* and *Nexus 6P* both with *Android 8.1.0*):

| **Hardware**      | **Sensors**       | **Sampling rate**  | **Comments** |
| ------------- |:-------------:| -----:|:-----------------------:|
| Galaxy S6  | Barometric pressure (hPa), luminosity (lux);  movement -> accelerometer (*m/s^2*), gyroscope (*deg/s*), magnetometer (*uT*) | 5 Hz / 50 Hz |        5 Hz for pressure and lux, 50 Hz for movement sensors            |
| Galaxy S6  | Bluetooth low energy (BLE) and WiFi beacons      |   0.1 Hz |  Scan visible BLE and WiFi access points (APs) for 10 seconds     |
| Galaxy S6  | Raw audio stream   |    16 kHz |     The audio is stored in a **.WAV* file 


## Requirements

```
minSdkVersion 21 
targetSdkVersion 27 
Android version >= Android 5.0
```

## Getting Started

### Setting up the Environment

1. Download and install *Android Studio* (https://developer.android.com/studio/).
2. Make sure to install the Android SDK with the minimum *SDK version 21*.
3. Import the Project to Android Studio: ```File -> Open```, and then select the Android app you wish to install, i.e., *DataCollector* or *RemoteController*.
4. The *DataCollector* app supports *Crashlytics*, which can be included (https://fabric.io/kits/android/crashlytics/install) and can also be linked to Google's firebase.
5. Build the project by pressing the corresponding button or using ```Build -> make Project```.

### Run DataCollector App

Before running the app make sure that the phone has Internet connection (required for the NTF sync):

1. Make sure your phone has debugging mode enabled and connect it to the computer.
2. Start the app by pressing the corresponding button or hitting *Shift+F10* to select select your device, install the *DataCollector*. 
3. Accept all permissions -> *Data read/write*, *Audio recording*, *Bluetooth*, *Locations* (required for performing WiFi scans).
4. The app settings are entered by clicking 3-dot symbol in the upper right corner, the *Default Settings* are:
	- All sensors are set to their *Max* sampling rates permitted by the phone.
	- *BLE*, *WiFi* and *audio* collection is disabled.
	- The option to launch the app via *RemoteController* is disabled.
    - (Optional) Here you can set the required sampling rates for different sensors.
5. The app is started/stopped by pressing the *START/STOP* button in the center of the screen. 

**Notes:** 
* Each time the *START/STOP* button is pressed a new folder with recorded sensor data is created. 
* You can lock the screen on the phone while the data collection is running. However, on *Samsung Glaxy S6* the app **MUST NOT** be put in the background as the OS will eventually kill the app!

### Run RemoteController App

To start *DataCollector* via *RemoteController* two Android phones are required (NOT RECOMMENDED, see above):

1. Start the *DataCollector* app on the first phone as described above.
2. Open the settings in *DataCollector* and enable *Remote control*.
4. Open the *RemoteController* Project in *Android Studio*.
5. Connect the second phone (must be in a debugging mode) to your computer.
6. Press the green arrow button or hit *Shift+F10* to select your device, install the *RemoteController*. 
7. Make sure that both phones are in the same network.
8. In the *DataCollector* app (first phone) press the *CONNECT* button and wait for both phones to establish a connection.
9. If the connection is successful the first phone will pop up in the *RemoteController* (running on the second phone) and in the *DataCollector* (running on the first phone) the status will change to *CONNECTED*.
10. In the *RemoteController* set up the time at which the *DataCollector* starts recording, as well as the duration of the data collection. 
11. After the start time and duration have been set, press the *START* button in the *RemoteController* to transimt these paramters to the *DataCollector*, which displays the received start time and data collection duration.
12. The *DataCollector* will automatically start the data collection at the specified start time and stop it after the specified duration. 


### Data Structure

The collected data is stored on the phone running the *DataCollector* app in the following location *internalStorage/SensorData/Timestamp*:

```
Timestamp/              # Root folder of the sensor data, corresponds the start time of data collection
  + audio/
  | + XX.wav            # Encoded audio data, XX is the sensor number, e.g., 01, 02, etc. set in the DataCollector app
  | + audio.time        # Time when the audio started
  + ble/
  | + ble.txt           # BLE data
  + sensors/
  | + accData.txt       # Accelerometer data
  | + barData.txt       # Barometric data
  | + gyrData.txt       # Gyroscope data
  | + luxData.txt       # Lluminosity data
  | + magData.txt       # Magnetometer data
  + wifi/
    + wifi.txt          # WiFi data
```

