DataController app records only SensorData.
    The remote app for this application is the RemoteController app which lets you record various sensors on the device as well as Audio and scans for near Bluetooth and WIFI.
    
    To Run the App: (You need at least once internet connection so that the app can sync up the time with an ntp server)
        -Start the app
		-Accept all permissions -> Data read/write, Audio recording, Bluetooth, Locations (Locations is necessary in order to scan for wifi connections)
        -In the upper right corner can press on the 3 dots and open the settings
			-default Settings are:
				-All sensors set to max Sampling rates
				-Bluetooth , wifi, Audio recording are disabled
				-Remote controller is disabled
			-(Optional) Set the preferred sampling rates for the different sensors
			- press the back button (Upper left corner) or the hardware back button
        - you can now start or stop recording the via the Button in the middle of the screen.
        
    Data can be found on the internal storage under internalStorage/Sensordata/
        
!!!!!We do not recommend to use the remote app since there seems to appear some anomalies with the sampling of the data as well as the termination of the recording.