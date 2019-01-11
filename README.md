DataController app records only SensorData.
    The remote app for this application is the RemoteController app which lets you set the time to start the recording as well as the duration for the recording.
    
    To Run the App: (You need at least once internet connection so that the app can sync up the time with an ntp server)
        -Start The app
        -In the upper right corner press on the 3 dots and open the settings
        -In the settings disable the remote controller to set it to the manual start stop mode
        -(Optional) Set the preffered sampling rates for the different sensors
        - press the back button (Upper left corner) or the hardware back button
        - you can now start or stop recording the via the Button in the middle of the screen.
        
    Data can be found on the internal storage under internalStorage/Sensordata/
        
!!!!!We do not reccomend to use the remote app since there seems to appear some anomalies with the sampling of the data as well as the termination of the recording.
    
    
DataControllerV2 app records Sensors, Audio, WIFI and Bluetooth LE data for the phone.
    the remote appfor this application is the RemoteControllerV2 app which lets you set the time to start the recording as well as the duration of the recording.
    
    You can set the App also to Manual Mode by Opening the MainActivity.java file and go in line 517.
        - In the setOnClickListener function put the comments // out in line 527 //startRecording(); -> startRecording();
        - in the line below 528 put the comments in // for remote.discoverServices(); -> //remote.discoverServices();
        repeat the process for line 534 //stopRecording(); -> stopRecording(); 
        and 535 remote.disconnect(); -> //remote.disconnect();
        
        
    To run the App: (You need at least once internet connection so that the app can sync up the time with an ntp server)
        Remote Mode:
        - Start on an other deivce in the same network the RemoteController2 App and set a start Time as well as a duration
        - On the dvice you want to record data on start the RemoteController2 app
        - Press on connect 
        - If both devices connected you will see on the remote app that the device will appear
        - Press on start and the start time and duration will be transmitted to the remote app
        
        Manual Mode:
        -Press on connect to start the recording.