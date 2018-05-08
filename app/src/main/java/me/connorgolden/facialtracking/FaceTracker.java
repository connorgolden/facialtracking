package me.connorgolden.facialtracking;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.HashMap;
import java.util.Map;

import me.connorgolden.facialtracking.ui.camera.GraphicOverlay;

/**
 * FaceTracker class
 * creates a tracker for each new face detected, and updates them accordingly
 */

class FaceTracker extends Tracker<Face> {

  private GraphicOverlay mOverlay;
  private Context mContext;
  private boolean mIsFrontFacing;
  private FaceGraphic mFaceGraphic;
  private FaceData mFaceData;

  private boolean mPreviousIsLeftEyeOpen = true;
  private boolean mPreviousIsRightEyeOpen = true;

  private boolean setTracking = true;

  private Map<Integer, PointF> mPreviousLandmarkPositions = new HashMap<>();

  FaceTracker(GraphicOverlay overlay, Context context, boolean isFrontFacing, boolean setTracking) {
    mOverlay = overlay;
    mContext = context;
    mIsFrontFacing = isFrontFacing;
    mFaceData = new FaceData();

    this.setTracking = setTracking;

  }


  /**
   * Start tracking the detected face instance within the face overlay.
   */
  @Override
  public void onNewItem(int id, Face face) {

    //checks if tracking is enabled
    if (!this.setTracking){
      mOverlay.remove(mFaceGraphic);
      return;
    }

    mFaceGraphic = new FaceGraphic(mOverlay, mContext, mIsFrontFacing);
  }

  /**
   * Update the position/characteristics of the face within the overlay.
   * Checks emotional state
   */
  @Override
  public void onUpdate(FaceDetector.Detections detectionResults, Face face) {

    //check if tracking is enabled
    if (!this.setTracking){
      mOverlay.remove(mFaceGraphic);
      return;
    }

    mOverlay.add(mFaceGraphic);

    // Get face dimensions.
    mFaceData.setPosition(face.getPosition());
    mFaceData.setWidth(face.getWidth());
    mFaceData.setHeight(face.getHeight());

    //check if eyes are closed
    final float isEyeClosedMargin = 0.5f;

    //left eye
    float leftEyeOpenProbability = face.getIsLeftEyeOpenProbability();
    if (leftEyeOpenProbability == Face.UNCOMPUTED_PROBABILITY) {
      mFaceData.setLeftEyeOpen(mPreviousIsLeftEyeOpen);
    }
    else {
      mFaceData.setLeftEyeOpen(leftEyeOpenProbability > isEyeClosedMargin);
      mPreviousIsLeftEyeOpen = mFaceData.isLeftEyeOpen();
    }

    //right eye
    float rightEyeOpenProbability = face.getIsRightEyeOpenProbability();
    if (rightEyeOpenProbability == Face.UNCOMPUTED_PROBABILITY) {
      mFaceData.setRightEyeOpen(mPreviousIsRightEyeOpen);
    }
    else {
      mFaceData.setRightEyeOpen(rightEyeOpenProbability > isEyeClosedMargin);
      mPreviousIsRightEyeOpen = mFaceData.isRightEyeOpen();
    }


    // Determine if person is smiling.
    final float isSmilingMargin = 0.3f;
    mFaceData.setSmiling(face.getIsSmilingProbability() > isSmilingMargin);


    mFaceGraphic.update(mFaceData);



  }

  /**
   * When no face detected it hides the graphic overlay from  CameraSourcePreview
   */
  @Override
  public void onMissing(FaceDetector.Detections<Face> detectionResults) {
    mOverlay.remove(mFaceGraphic);
  }


  /**
   * When presumed that a face is gone forgood, it deletes the graphic object
   */
  @Override
  public void onDone() {
    mOverlay.remove(mFaceGraphic);
  }

}
