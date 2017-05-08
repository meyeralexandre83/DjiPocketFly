package com.dji.DjiPocketFly;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightMode;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

public class FpvActivity extends Activity implements SurfaceTextureListener,OnClickListener{

<<<<<<< HEAD:DjiPocketFly/app/src/main/java/com/dji/DjiPocketFly/FpvActivity.java
    private static final String TAG = FpvActivity.class.getName();
    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    // Codec pour le fpv
    protected DJICodecManager mCodecManager = null;
=======
    private String FILE_NAME = "locations.txt";
    protected File mFile;

    private static final String TAG = MainActivity.class.getName();
    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;


    // Codec for video live view
    protected DJICodecManager mCodecManager = null;
    protected FlightController mflightController;
    protected LocationCoordinate3D mLocation;

>>>>>>> refs/remotes/origin/Finish:DjiPocketFly/DjiPocketFly/app/src/main/java/com/dji/DjiPocketFly/MainActivity.java
    protected TextureView mVideoSurface = null;
    private Button mCaptureBtn;
    private ToggleButton mRecordBtn;
    private TextView recordingTime;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        initUI();
        
        initFile();

        // Callback de récupération des données de la video
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

        Camera camera = DjiPocketFlyApplication.getCameraInstance();

        if (camera != null) {

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemState) {
                    if (null != cameraSystemState) {

                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;

                        final String timeString = String.format("%02d:%02d", minutes, seconds);
                        final boolean isVideoRecording = cameraSystemState.isRecording();

                        FpvActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                recordingTime.setText(timeString);

                                if (isVideoRecording){
                                    recordingTime.setVisibility(View.VISIBLE);
                                }else
                                {
                                    recordingTime.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }
            });

        }

        BaseProduct product = DjiPocketFlyApplication.getProductInstance();

        Log.i("flightController ", "initialize");
        if(product instanceof Aircraft){
            if (product instanceof Aircraft) {
                mflightController = ((Aircraft) product).getFlightController();
                Log.i("flightController : ", "created");
            }

            if (mflightController != null) {
                Log.i("flightController : ", "on update");
                FlightControllerState flightControllerState = mflightController.getState();

                Log.i("flightController : ", "setStateCallback");
                mflightController.setStateCallback(new FlightControllerState.Callback() {
                    @Override
                    public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                    Log.i("flightController : ", "on update");
                    mLocation = flightControllerState.getAircraftLocation();
                    if(mLocation != null){
                        Log.i("flightController : ", "get location");
                        Double latitude = flightControllerState.getHomeLocation().getLatitude();
                        Double longitude = flightControllerState.getHomeLocation().getLongitude();
                        Log.i("---------------", "------------------------------------------------");
                        Log.i("Latitude : ", String.valueOf(latitude));
                        Log.i("Logigtude : ", String.valueOf(longitude));
                        try {
                            registerLocation(latitude,longitude);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    }
                });
            }
        }

    }

    private void initFile() {
        // On crée un fichier qui correspond à l'emplacement extérieur
         mFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/ " + getPackageName() + "/files/" + FILE_NAME);


    }

    private void registerLocation(Double latitude, Double longitude) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if(latitude==null || longitude==null){
            latitude = 43.04;
            longitude = 7.01;
        }
        jsonObject.put("latitude",latitude);
        jsonObject.put("longitude",longitude);

        String chaine = jsonObject.toString();
        //WRITE
        try {
            // Flux interne
            FileOutputStream output = openFileOutput(FILE_NAME, MODE_PRIVATE);

            // On écrit dans le flux interne
            output.write(chaine.getBytes());

            if(output != null)
                output.close();

            // Si le fichier est lisible et qu'on peut écrire dedans
            if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
                // On crée un nouveau fichier. Si le fichier existe déjà, il ne sera pas créé
                mFile.createNewFile();
                output = new FileOutputStream(mFile);
                output.write(chaine.getBytes());
                if(output != null)
                    output.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onProductChange() {
        initPreviewer();
    }

    @Override
    public void onResume() {
        super.onResume();
        initPreviewer();
        onProductChange();

        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void onReturn(View view){
        this.finish();
    }

    @Override
    protected void onDestroy() {
        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {

        // On initialise le fpv

        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);
        recordingTime = (TextView) findViewById(R.id.timer);
        mCaptureBtn = (Button) findViewById(R.id.btn_capture);
        mRecordBtn = (ToggleButton) findViewById(R.id.btn_record);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        mCaptureBtn.setOnClickListener(this);
        mRecordBtn.setOnClickListener(this);


        recordingTime.setVisibility(View.INVISIBLE);

        mRecordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecord();
                } else {
                    stopRecord();
                }
            }
        });
    }

    private void initPreviewer() {

        BaseProduct product = DjiPocketFlyApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                if (VideoFeeder.getInstance().getVideoFeeds() != null
                        && VideoFeeder.getInstance().getVideoFeeds().size() > 0) {
                    VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(mReceivedVideoDataCallBack);
                }
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = DjiPocketFlyApplication.getCameraInstance();
        if (camera != null){
            // On reinitialise la callback
            VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(FpvActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_capture:{
                captureAction();
                break;
            }
            default:
                break;
        }
    }


    // Methode pour prendre une photo

    private void captureAction(){

        final Camera camera = DjiPocketFlyApplication.getCameraInstance();
        if (camera != null) {

            SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE; // Set the camera capture mode as Single mode
            camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback(){
                    @Override
                    public void onResult(DJIError djiError) {
                        if (null == djiError) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                                        @Override
                                        public void onResult(DJIError djiError) {
                                            if (djiError == null) {
                                                showToast("take photo: success");
                                            } else {
                                                showToast(djiError.getDescription());
                                            }
                                        }
                                    });
                                }
                            }, 2000);
                        }
                    }
            });
        }
    }

    // Methode pour lancer l'enregistrement video
    private void startRecord(){

        final Camera camera = DjiPocketFlyApplication.getCameraInstance();
        if (camera != null) {
            camera.startRecordVideo(new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError)
                {
                    if (djiError == null) {
                        showToast("Record video: success");
                    }else {
                        showToast(djiError.getDescription());

                    }
                }
            });
        }
    }

    //  Methode pour arreter l'enregistrement video
    private void stopRecord(){

        Camera camera = DjiPocketFlyApplication.getCameraInstance();
        if (camera != null) {
            camera.stopRecordVideo(new CommonCallbacks.CompletionCallback(){

                @Override
                public void onResult(DJIError djiError)
                {
                    if(djiError == null) {
                        showToast("Stop recording: success");
                    }else {
                        showToast(djiError.getDescription());
                    }
                }
            });
        }

    }
}
