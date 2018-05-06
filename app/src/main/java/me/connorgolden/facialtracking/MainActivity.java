package me.connorgolden.facialtracking;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.vision.face.FaceDetector;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.camera)
    CameraView cameraView;

    private int cameraMethod = CameraKit.Constants.METHOD_STANDARD;
    private int cameraFacing = CameraKit.Constants.FACING_BACK;
    private boolean cropOutput = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ButterKnife.bind(this);

        cameraView.setMethod(cameraMethod);
        cameraView.setCropOutput(cropOutput);

        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();


        ImageButton switchCamButton = (ImageButton) findViewById(R.id.switchCameraButton);
        switchCamButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (cameraFacing == CameraKit.Constants.FACING_FRONT){
                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                    cameraFacing = CameraKit.Constants.FACING_BACK;
                } else {
                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                    cameraFacing = CameraKit.Constants.FACING_FRONT;
                }
            }
        });
        
        ImageButton settingButton = (ImageButton) findViewById(R.id.settingsButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSettings();
            }
        });

        FloatingActionButton camButton = (FloatingActionButton) findViewById(R.id.takePictureButton);
        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
                    @Override
                    public void callback(CameraKitImage event) {
                        imageCaptured(event);
                    }
                });
                Log.i("Button", "CameraClick");
            }
        });


    }

    public void imageCaptured(CameraKitImage image) {
        byte[] jpeg = image.getJpeg();

        Log.i("ImageCapture", "ImageCaptured!");

        long callbackTime = System.currentTimeMillis();
        /*ResultHolder.dispose();
        ResultHolder.setImage(jpeg);
        ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());
        ResultHolder.setTimeToCallback(callbackTime - captureStartTime);
        Intent intent = new Intent(getContext(), PreviewActivity.class);
        getContext().startActivity(intent);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }



    protected void launchSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


}