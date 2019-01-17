# Data Collector App with RemoteController
DataController app records sensors , audio, WIFI and Bluetooth. The recording can either be started via a simple button or a remote app which the target device needs to connect to. \
**Note:** We do not recommend to use the remote app since there seems to be some problems with the recorded sensor data (it won't record consistently and will drop several minutes of recording) .
    
## Requirements
    
minSdkVersion 21 \
targetSdkVersion 27 \

Your Android device needs to at least Support Android 5.0.

## Getting Started

### Setting up the Environment

1. Download and install Android Studio (https://developer.android.com/studio/)
2. Make sure to have also installed the Android SDK at least with SDK version 21
3. Import the Project to Android Studio: File -> Open  then select the Android app that you want to install, either the RemoteApp or the DataCollector app
4. The Data Collector App supports Crashlytics and can be included (https://fabric.io/kits/android/crashlytics/install) and also linked to google's firebase.
5. Build the project by pressing the green Hammer Button in the Bar or click on Build -> make Project.

### Run the DataCollector App
To Run the App: (You need at least once internet connection so that the app can sync up the time with an ntp server)
1. Make sure your phone has debugging mode enabled and is connected to your Computer.
2. Start the app by pressing the green arrow or pressing Shift+F10 and select your device.
3. Accept all permissions -> Data read/write, Audio recording, Bluetooth, Locations (Locations is necessary in order to scan for wifi connections)
4. In the upper right corner can press on the 3 dots and open the settings
    default Settings are: \
	- All sensors set to max Sampling rates
	- Bluetooth , wifi, Audio recording are disabled
	- Remote controller is disabled
    - (Optional) Set the preferred sampling rates for the different sensors
5. press the back button (Upper left corner) or the hardware back button
6. you can now start or stop recording the via the Button in the middle of the screen.

**Note:** You can lock the screens of the phones while recording.

### Run the Remote Controller App
**Note:** We do not recommend in using this approach, snice android has some anomalies while recording the data where you might end up with several sampling holes of a few minutes. \
To record via a remote controller you need at least two android devices.
1. Run the DataCollector app on one device as described above
2. Open the settings and enable Remote control.
3. Go back to the main screen.
4. Open the RemoteController Project in Android Studio.
5. connect the Second device to your computer and have it in debugging mode.
6. Press on the green arrow button or press Shift+F10 and select your device.
7. Make sure both devices are in the same network. (It might be helpful if the device which has the remote controller app installed opens a hotspot where the other devices connect to)
8. On the DataCollector app press on the connect button and wait for both devices to connect successfully to each other.
9. The device will show up on the remote device if they connected successfully and on the DataCollector app you will see above the connect/disconnect button that there is written Connected.
10. On the remote app set now a time when the devices should start recording as well as a duration for how long the devices should record.
11. After the start time and the duration have been set press on the start button to transmit and set the times on the Collector phones.
12. The data collector apps will automatically start and stop at the set time.


### Data Structure
Data can be found on the internal storage under internalStorage/Sensordata/"TimeStamp". \
All data are saved as *.txt files except of the audio which writes the raw date directly in a wav file.
    


