package me.connorgolden.facialtracking;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.vision.face.FaceDetector;
import com.wonderkiln.camerakit.CameraKit;
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


        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.switchCameraButton);
        myFab.setOnClickListener(new View.OnClickListener() {
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


}
