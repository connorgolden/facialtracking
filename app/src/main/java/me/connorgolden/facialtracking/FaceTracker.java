package me.connorgolden.facialtracking;

import android.content.Context;
import android.graphics.PointF;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.util.HashMap;
import java.util.Map;

class FaceTracker extends Tracker<Face> {

  private GraphicOverlay mOverlay;
  private Context mContext;
  private boolean mIsFrontFacing;
  private FaceGraphic mFaceGraphic;
  private FaceData mFaceData;

  private boolean mPreviousIsLeftEyeOpen = true;
  private boolean mPreviousIsRightEyeOpen = true;

  private Map<Integer, PointF> mPreviousLandmarkPositions = new HashMap<>();

  FaceTracker(GraphicOverlay overlay, Context context, boolean isFrontFacing) {
    mOverlay = overlay;
    mContext = context;
    mIsFrontFacing = isFrontFacing;
    mFaceData = new FaceData();
  }


  @Override
  public void onNewItem(int id, Face face) {
    mFaceGraphic = new FaceGraphic(mOverlay, mContext, mIsFrontFacing);
  }

  @Override
  public void onUpdate(FaceDetector.Detections detectionResults, Face face) {
    mOverlay.add(mFaceGraphic);

    // Get face dimensions.
    mFaceData.setPosition(face.getPosition());
    mFaceData.setWidth(face.getWidth());
    mFaceData.setHeight(face.getHeight());
    mFaceGraphic.update(mFaceData);
  }


  @Override
  public void onMissing(FaceDetector.Detections<Face> detectionResults) {
    mOverlay.remove(mFaceGraphic);
  }

  @Override
  public void onDone() {
    mOverlay.remove(mFaceGraphic);
  }

}
