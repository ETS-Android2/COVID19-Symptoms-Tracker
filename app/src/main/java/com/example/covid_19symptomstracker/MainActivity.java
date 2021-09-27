package com.example.covid_19symptomstracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    private boolean isRecording = false;
    MediaRecorder mediaRecorder;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Camera mCamera;
    private CameraPreview mPreview;
    public static File mediaFile;

    SensorManager sensor;
    Sensor accel;

    TextView heartRate_view;

    float respiratoryRate;
    float heartRate;

    int index = 0;
    List<Float> accel_X = new ArrayList<>();
    List<Float> accel_Y = new ArrayList<>();
    List<Float> accel_Z = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermissions();

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);

        //requestPermissions
        mCamera = getCameraInstance();

        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);


        Button symptomsButton = findViewById(R.id.symptomsButton);

        symptomsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent int1 = new Intent(getApplicationContext(), SecondScreen.class);
                startActivity(int1);
            }
        });

        heartRate_view = findViewById(R.id.heartRateView);
        TextView respiratoryRate_view = findViewById(R.id.respiratoryRateView);
        TextView warningMessage = findViewById(R.id.message);

        Button heartRate_button = findViewById(R.id.heartRateButton);

        heartRate_button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                //startRecording();
                heartRate_view.setText("Recording video for 45s...");
                if (isRecording) {
                    // stop recording and release camera
                    mediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object
                    mCamera.lock();         // take camera access back from MediaRecorder

                    // inform the user that recording has stopped
                    //setCaptureButtonText("Capture");
                    isRecording = false;
                } else {
                    // initialize video camera
                    if (prepareVideoRecorder()) {
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        mediaRecorder.start();

                        // inform the user that recording has started
                        //setCaptureButtonText("Stop");
                        isRecording = true;
                    } else {
                        // prepare didn't work, release the camera
                        releaseMediaRecorder();
                        // inform user
                    }
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isRecording) {
                                Toast.makeText(getApplicationContext(),"Recording finished!", Toast.LENGTH_LONG).show();
                                // stop recording and release camera
                                mediaRecorder.stop();  // stop the recording
                                releaseMediaRecorder(); // release the MediaRecorder object
                                mCamera.lock();         // take camera access back from MediaRecorder

                                // inform the user that recording has stopped
                                //setCaptureButtonText("Capture");
                                isRecording = false;
                            }
                            heartRate_view.setText("Calculating Heart Rate, please wait...");

                            heartRate = getHeartRate();
                            if (heartRate<45) {
                                warningMessage.setText("Heart rate value is not accurate" +
                                        "\nMake sure to stay still and keep your finger on the camera for the entire duration.");
                            }
                            heartRate_view.setText("Heart Rate is: " + new DecimalFormat("##.##").format(heartRate));
                            Toast.makeText(getApplicationContext(),"Heart Rate data collected!", Toast.LENGTH_LONG).show();
                        }
                    }, 45000);
                }
            }
        });

        Button respiratoryRate_button = findViewById(R.id.respiratoryRateButton);

        respiratoryRate_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                respiratoryRate_view.setText("Calculating Respiratory rate 45s...");
                index = 0;
                sensor = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                accel = sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if(accel!=null){
                    final SensorEventListener mSensor = new SensorEventListener(){

                        @Override
                        public void onSensorChanged(SensorEvent sensorEvent) {
                            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                                index++;

                                if(index>128){
                                    sensor.unregisterListener(this);
                                }

                                if(index <= 128) {
                                    accel_X.add(sensorEvent.values[0]);
                                    accel_Y.add(sensorEvent.values[1]);
                                    accel_Z.add(sensorEvent.values[2]);
                                }
                            }
                        }

                        @Override
                        public void onAccuracyChanged(Sensor sensor, int i) {

                        }
                    };
                    sensor.registerListener(mSensor, accel, SensorManager.SENSOR_DELAY_NORMAL);

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            respiratoryRate = getRespiratoryRate(accel_X, accel_Y, accel_Z);
                            respiratoryRate_view.setText("Respiratory Rate is: " + new DecimalFormat("##.##").format(respiratoryRate));
                            Toast.makeText(getApplicationContext(),"Respiratory Rate data collected!", Toast.LENGTH_LONG).show();
                        }
                    }, 45000);
                }
            }
        });

        Button uploadSigns_button = findViewById(R.id.uploadSignsButton);
        uploadSigns_button.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {

                if(databaseHelper.getData().getCount() == 0){
                    if(databaseHelper.insertData("HRrate", heartRate)){
                        Log.d("DB", "HRrate uploaded");
                    }
                } else {
                    if(databaseHelper.updateData("HRrate", heartRate)){
                        Log.d("DB", "HRrate updated");
                    }
                }

                if(databaseHelper.getData().getCount() == 0){
                    if (databaseHelper.insertData("RRrate", respiratoryRate)) {
                        Log.d("DB", "RRrate uploaded");
                    }
                } else {
                    if(databaseHelper.updateData("RRrate", respiratoryRate)){
                        Log.d("DB", "RRrate updated");
                    }
                }
                Toast.makeText(getApplicationContext(),"Signs data saved to database.", Toast.LENGTH_LONG).show();

            }
        });
    }


    private boolean getPermissions() {

        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                };

        boolean permissionFlag = false;

        while(!permissionFlag) {

            int totalPermissions = 0;

            for (String permission : permissions) {

                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, 1);
                } else {
                    totalPermissions++;
                }
            }

            if(totalPermissions == 4) {
                permissionFlag = true;
            }
        }

        return true;
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder(){

        mCamera = getCameraInstance();
        Camera.Parameters p = mCamera.getParameters();
        if(!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
            return false;
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(p);

        mediaRecorder = new MediaRecorder();


        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

        // Step 4: Set output file
        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("TAG", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("TAG", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(p);
            mCamera.lock();           // lock camera for later use
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Resume: ", "Resuming activity");
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    protected void onPause() {
        super.onPause();
        Log.i("Pause: ", "Pausing activity");
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected  void onDestroy(){

        super.onDestroy();
        Log.i("Destroy: ", "Destroying activity");
        releaseCamera();
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    public float getHeartRate(){

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mediaFile.toString());

        //Log.e("PATH", " " + mediaFile.toString());

        float WIDTH = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        float HEIGHT = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        float FRAME_COUNT = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));

        float FRAME_RATE = FRAME_COUNT/45;

        List<Float> signalArray = new ArrayList<>();

        float zeroCrossing;

        int INCREMENT = 1000000/(int)FRAME_RATE;


        for (int sec=0; sec<45000000; sec+=INCREMENT) {
            int sum=0;
            Bitmap bm = retriever.getFrameAtTime(sec, MediaMetadataRetriever.OPTION_CLOSEST);
            for (int i=0; i<WIDTH; i++){
                for (int j=0; j<HEIGHT; j++){

                    sum+=bm.getPixel(i, j);

                }
            }
            signalArray.add(sum/(HEIGHT*WIDTH));

            //Log.e("color"," " + sum + ", " + sec);
        }

        retriever.release();

        List<Float> smoothedArray = Utilities.simpleMovingAverage(signalArray);

        zeroCrossing = Utilities.zeroCrossing(Utilities.simpleMovingAverage(Utilities.differential(smoothedArray)));
        float number_of_peaks = zeroCrossing/2;

        // return number the of heart beats in one minute
        return (number_of_peaks/45)*60;
    }

    public float getRespiratoryRate(List<Float> x, List<Float> y, List<Float> z){

        List<Float> smoothed_X = Utilities.simpleMovingAverage(x);
        List<Float> smoothed_Y = Utilities.simpleMovingAverage(y);
        List<Float> smoothed_Z = Utilities.simpleMovingAverage(z);

        float zeroCrossing_X = Utilities.zeroCrossing(Utilities.differential(smoothed_X));
        float zeroCrossing_Y = Utilities.zeroCrossing(Utilities.differential(smoothed_Y));
        float zeroCrossing_Z = Utilities.zeroCrossing(Utilities.differential(smoothed_Z));

        float peaks = ((zeroCrossing_X+zeroCrossing_Y+zeroCrossing_Z)/3)/2;

        // return number the of breaths in one minute
        return (peaks/45)*60;
    }
}