package me.connorgolden.facialtracking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.connorgolden.facialtracking.ui.camera.CameraSourcePreview;
import me.connorgolden.facialtracking.ui.camera.GraphicOverlay;

/**
 * @author Connor Golden
 * @author Jochem Dierx
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_CODE = 1;
    @BindView(R.id.camera)
    CameraSourcePreview cameraView;

    @BindView(R.id.faceOverlay)
    GraphicOverlay graphicOverlay;

    private CameraSource cameraSource = null;
    private boolean isFrontFacing = false;
    private boolean showEmoji = true;
    private boolean setTracking = true;


    /**
     * On creation of the app it initializes the UI and handles user interaction.
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        requestStoragePermission();

        createCameraSource();
        startCameraSource();

        ImageButton switchCamButton = findViewById(R.id.switchCameraButton);
        switchCamButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isFrontFacing = !isFrontFacing;

                if (cameraSource != null) {
                    cameraSource.release();
                    cameraSource = null;
                }

                createCameraSource();
                startCameraSource();
            }
        });
        
        final ImageButton settingButton = findViewById(R.id.settingsButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Customization Coming Soon\u2122", Toast.LENGTH_LONG).show();
                //launchSettings();
            }
        });

        Button camButton = findViewById(R.id.takePictureButton);
        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });


        final ToggleButton tggl;

        tggl = findViewById(R.id.trackingButton);
        tggl.setChecked(true);

        tggl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tggl.isChecked()) {
                    setTracking = false;
                    cameraSource.release();
                    createCameraSource();
                    startCameraSource();

                }
                else {
                    setTracking = true;
                    cameraSource.release();
                    createCameraSource();
                    startCameraSource();
                }
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }


    /**
     * This method handles the creation and initialization of a FaceDetector object.
     * This is used to detect faces
     * @param context
     * @return  face tracking detector
     */

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
                    return new FaceTracker(graphicOverlay, context, isFrontFacing, setTracking);
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

    /**
     * createCameraSource creates a CameraSource object needed to utilize the device's camera for image capturing
     */

    private void createCameraSource() {
        Log.d(TAG, "createCameraSource called.");


        Context context = getApplicationContext();
        FaceDetector detector = createFaceDetector(context);


        int facing = CameraSource.CAMERA_FACING_FRONT;
        if (!isFrontFacing) {
            facing = CameraSource.CAMERA_FACING_BACK;
        }

        cameraSource = new CameraSource.Builder(context, detector)
                .setFacing(facing)
                //.setRequestedPreviewSize(320, 240)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    /**
     * startCameraSource checks if CameraSource is able to run
     */

    private void startCameraSource() {
        // Make sure that the device has Google Play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, 9001);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                cameraView.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    /**
     * takePicture does the image capturing for CameraSource
     */


    private void takePicture(){
        try{
            cameraSource.takePicture(null, new CameraSource.PictureCallback() {
                private File imageFile;
                @Override
                public void onPictureTaken(byte[] bytes) {

                    //#1: Load Byte[] to Bitmap.
                    Bitmap loadedImage = null;
                    Bitmap rotatedBitmap = null;
                    loadedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    //#2: Make matrix of the bitmap and rotate image. (Image taken sideways for some reason)
                    Matrix rotateMatrix = new Matrix();
                    rotateMatrix.postRotate(camRotation());
                    rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
                            loadedImage.getWidth(), loadedImage.getHeight(),
                            rotateMatrix, false);

                    //#3: Get location of Camera Roll. Either on internal or external SD card.
                    String state = Environment.getExternalStorageState();
                    File folder = null;

                    if (state.contains(Environment.MEDIA_MOUNTED)) {
                        folder = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/");
                    } else {
                        folder = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/");
                    }


                    //#4: If the folder dosen't exist make one.
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdirs();
                    }

                    //#5: Once folder exists, make an empty JPEG file with unique timestamp.
                    if (success) {
                        java.util.Date date = new java.util.Date();
                        imageFile = new File(folder.getAbsolutePath() + File.separator
                                + new Timestamp(date.getTime()).toString() + ".jpg");
                        try {
                            imageFile.createNewFile();
                        } catch (IOException e) {
                            Log.e("TakePicture", "File Creation Error");
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getBaseContext(), "Error: Image Not Saved", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //#6: Create Byte stream, and compress to JPEG.
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

                    if (showEmoji){
                        //combine pictures
                        overlay(rotatedBitmap,getOverlayBitmap()).compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
                    }else {
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
                    }


                    //#7: Try and stream byteStream to the file created earlier.
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                        fileOutputStream.write(byteStream.toByteArray());
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //#8: Add metadata to file.
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA, imageFile.getAbsolutePath());

                    //  MainActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    //setResult(Activity.RESULT_OK); //add this
                }
            });
        }catch (Exception e){
            Log.e("TakePicture", e.toString());
            Toast.makeText(getBaseContext(), "Error: Image Not Taken", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getOverlayBitmap(){
        View overlay = findViewById(R.id.camera);
        Bitmap bitmap = Bitmap.createBitmap(overlay.getWidth(),overlay.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Drawable bgDrawable =overlay.getBackground();
        if (bgDrawable!=null){
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }else{
            canvas.drawColor(Color.WHITE);
        }

        overlay.draw(canvas);
        return bitmap;
    }

    private Bitmap overlay(Bitmap bitmap1, Bitmap bitmap2) {
        Bitmap overlay = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), bitmap1.getConfig());
        Canvas canvas = new Canvas(overlay);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(bitmap2, new Matrix(), null);
        return overlay;
    }

    private int camRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Log.i("Rotation", String.valueOf(rotation));
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = -90;
                break;
            case Surface.ROTATION_270:
                degree = -180;
                break;

            default:
                break;
        }

        if (isFrontFacing && rotation == Surface.ROTATION_0){
            return degree-180;
        }else{
            return degree;
        }
    }

    private boolean checkStoragePermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    protected void launchSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}