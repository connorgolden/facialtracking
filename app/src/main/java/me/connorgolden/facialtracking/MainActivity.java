package me.connorgolden.facialtracking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.connorgolden.facialtracking.ui.camera.CameraSourcePreview;
import me.connorgolden.facialtracking.ui.camera.GraphicOverlay;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.camera)
    CameraSourcePreview cameraView;

    private CameraSource mCameraSource = null;

    private boolean isFrontFacing = false;
    private GraphicOverlay graphicOverlay;

    private boolean cropOutput = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);


        graphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);


        Context context = getApplicationContext();
        FaceDetector detector = createFaceDetector(context);

        int facing = CameraSource.CAMERA_FACING_FRONT;
        if (!isFrontFacing) {
            facing = CameraSource.CAMERA_FACING_BACK;
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setFacing(facing)
                .setRequestedPreviewSize(320, 240)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build();

        try {
            cameraView.start(mCameraSource, graphicOverlay);
        } catch (IOException e) {
            e.printStackTrace();
        }


        ImageButton switchCamButton = (ImageButton) findViewById(R.id.switchCameraButton);
        switchCamButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               /* if (cameraFacing == CameraKit.Constants.FACING_FRONT){
                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                    cameraFacing = CameraKit.Constants.FACING_BACK;
                    isFrontFacing = false;
                } else {
                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                    cameraFacing = CameraKit.Constants.FACING_FRONT;
                    isFrontFacing = true;
                }*/
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
        /*camButton.setOnClickListener(new View.OnClickListener() {
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
        });*/


    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            cameraView.start(mCameraSource, graphicOverlay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }



    @NonNull
    private FaceDetector createFaceDetector(final Context context) {
        Log.d(TAG, "createFaceDetector called.");

        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(isFrontFacing)
                .setMinFaceSize(isFrontFacing ? 0.35f : 0.15f)
                .build();

        MultiProcessor.Factory<Face> factory = new MultiProcessor.Factory<Face>() {
            @Override
            public Tracker<Face> create(Face face) {
                return new FaceTracker(graphicOverlay, context, isFrontFacing);
            }
        };

        Detector.Processor<Face> processor = new MultiProcessor.Builder<>(factory).build();
        detector.setProcessor(processor);

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");

            // Check the device's storage. Notifies if not enough.
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Log.w(TAG, getString(R.string.low_storage_error));
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.app_name)
                        .setMessage(R.string.low_storage_error)
                        .setPositiveButton(R.string.disappointed_ok, listener)
                        .show();
            }
        }
        return detector;
    }




    protected void launchSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}