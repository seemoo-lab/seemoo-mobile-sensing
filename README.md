# Data Collector App with remoteController
DataController app records sensors , audio, WIFI and Bluetooth. The recording can either be started via a simple button or a remote app which the target device needs to connect to.
    **Note:** We do not reccomand to use the remote app since there seems to be some problems with the recorded sensor data (it wont record consistently and will drop serevral minutes of recording) .
    
## Pre
    
minSdkVersion 21 \
targetSdkVersion 27 \

Your Android device needs to at least Support Android 5.0.

## Getting Started

### Setting up the Envirorment

1. Download and install Android Studio (https://developer.android.com/studio/)
2. Make sure to have also installed the Android SDK at least with SKD veriosn 21
3. Import the Project to Android Studio: File -> Open  then select the Android app that you want to install, either the RemoteApp or the DataCollector app
4. The Data Collector App supports Crashlytics and can be included (https://fabric.io/kits/android/crashlytics/install) and also linked to googles firebase.
5. Build the project by pressing the green Hammer Button in the Bar or click on Build -> make Project.

### Run the Data Collecor App
To Run the App: (You need at least once internet connection so that the app can sync up the time with an ntp server)
1. Make sure your phone has debuging mode enabled and is connected to your Computer.
2. Start the app by pressing the Green arrow or pressing Shift+F10 and select your device.
3. Accept all permissions -> Data read/write, Audio recording, Bluetooth, Locations (Locations is necessary in order to scan for wifi connections)
4. In the upper right corner can press on the 3 dots and open the settings
    default Settings are: \
	- All sensors set to max Sampling rates
	- Bluetooth , wifi, Audio recording are disabled
	- Remote controller is disabled
    - (Optional) Set the preferred sampling rates for the different sensors
5. press the back button (Upper left corner) or the hardware back button
6. you can now start or stop recording the via the Button in the middle of the screen.


Not Finished yet ...
### Run the Remote Controller App

Data can be found on the internal storage under internalStorage/Sensordata/
    
!!!!!We do not recommend to use the remote app since there seems to appear some anomalies with the sampling of the data as well as the termination of the recording.