package com.example.datacollector;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.instacart.library.truetime.TrueTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;

public class MyAudioRecorder {


    private static final String TAG = "AUDIOREC";
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private Thread recordThread;

    private File path;
    private File audioDataFile;
    private File timeStampFile;
    private FileOutputStream stampStream;
    private FileOutputStream fileOutputStream;
    int bufferSize = 0;
    AudioRecord audioRecord;
    MainActivity mainActivity;
    MediaRecorder mediaRecorder;
    private SimpleDateFormat timeFormat;
    private boolean isRecording = false;
    private boolean isEnabled = false;

    public MyAudioRecorder(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mediaRecorder = new MediaRecorder();
        timeFormat = new SimpleDateFormat("yyyy-MM-dd/kk:mm:ss.SSS");
        bufferSize = AudioRecord.getMinBufferSize(16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
    }

    public void stop() {
        if (!isEnabled){
            return;
        }
        isRecording = false;
        int i = audioRecord.getState();
        if (i == 1)
            audioRecord.stop();

        try {
            int channels = ((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1
                    : 2);
            long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

            long totalAudioLen = fileOutputStream.getChannel().size()-44; // remove already written fake header
            long totalDataLen = totalAudioLen + 36;
            writeWaveFileHeader(fileOutputStream, totalAudioLen, totalDataLen,
                    RECORDER_SAMPLERATE, channels, byteRate);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        mainActivity.addFile(audioDataFile);
        mainActivity.addFile(timeStampFile);


        /*mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = new MediaRecorder();
        */

    }

    public void start() {
        if (!isEnabled){
            return;
        }
        path = new File(mainActivity.parentPath,"audio");
        audioDataFile = createFile(mainActivity.getNumber()+".wav");
        timeStampFile = createFile("audio.time");
        path.setReadable(true);
        path.setWritable(true);
        path.mkdirs();
        try {
            stampStream = new FileOutputStream(timeStampFile);
            fileOutputStream = new FileOutputStream(audioDataFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mainActivity.writeToFile(stampStream,timeFormat.format(TrueTime.now()).replace("/"," "));
        writeFakeHeader();
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,bufferSize);

        int state = audioRecord.getState();
        if (state == 1){
            audioRecord.startRecording();
            isRecording = true;
            recordThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    writeAudioToFile();
                }
            },"AudioRecorder Thread");
            recordThread.start();
        }

        /*mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        mediaRecorder.setOutputFile(audioDataFile.getPath());
        //mediaRecorder.setAudioSamplingRate();

        try{
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
*/
    }

    private void writeAudioToFile() {
        byte data[] = new byte[bufferSize];
        int read = 0;
        while(isRecording){
            read = audioRecord.read(data,0,bufferSize);

            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                try {
                    fileOutputStream.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeFakeHeader(){
        byte [] header = new byte[44];
        for (int i = 0; i < header.length; i++){
            header[i] = 0;
        }
        try {
            fileOutputStream.write(header,0,44);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add ending to Filename since it depends on which audio file we have
     * @param fileName <filename>.<ending of File>
     * @return File in the directory for this output
     */
    public File createFile(String fileName) {


        File file = new File(path,fileName);
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

    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        FileChannel editor = fileOutputStream.getChannel();

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1
                : 2) * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);



        int written = editor.write(ByteBuffer.wrap(header),0);
        Log.d(TAG, "writeWaveFileHeader: "+written);

    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
}
