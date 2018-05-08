
package me.connorgolden.facialtracking;

import android.graphics.PointF;

import me.connorgolden.facialtracking.ui.camera.GraphicOverlay;

/**
 * FaceData class stores datamembers needed for face detection and creating overlays
 */

public class FaceData {

  private int mId;

  // Face dimensions
  private PointF mPosition;
  private float mWidth;
  private float mHeight;

  // Head orientation
  private float mEulerY;
  private float mEulerZ;

  // Facial states
  private boolean mLeftEyeOpen;
  private boolean mRightEyeOpen;
  private boolean mSmiling;


  public int getId() {
    return mId;
  }

  public void setId(int id) {
    mId = id;
  }

  public PointF getPosition() {
    return mPosition;
  }

  public void setPosition(PointF position) {
    mPosition = position;
  }

  public float getWidth() {
    return mWidth;
  }

  public void setWidth(float width) {
    mWidth = width;
  }

  public float getHeight() {
    return mHeight;
  }

  public void setHeight(float height) {
    mHeight = height;
  }

  public float getEulerY() {
    return mEulerY;
  }

  public void setEulerY(float eulerY) {
    mEulerY = eulerY;
  }

  public float getEulerZ() {
    return mEulerZ;
  }

  public void setEulerZ(float eulerZ) {
    mEulerZ = eulerZ;
  }

  public boolean isLeftEyeOpen() {
    return mLeftEyeOpen;
  }

  public void setLeftEyeOpen(boolean leftEyeOpen) {
    this.mLeftEyeOpen = leftEyeOpen;
  }

  public boolean isRightEyeOpen() {
    return mRightEyeOpen;
  }

  public void setRightEyeOpen(boolean rightEyeOpen) {
    this.mRightEyeOpen = rightEyeOpen;
  }

  public boolean isSmiling() {
    return mSmiling;
  }

  public void setSmiling(boolean smiling) {
    this.mSmiling = smiling;
  }

}
