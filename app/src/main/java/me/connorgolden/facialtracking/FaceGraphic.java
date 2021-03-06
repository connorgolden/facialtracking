package me.connorgolden.facialtracking;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import me.connorgolden.facialtracking.ui.camera.GraphicOverlay;

/**
 * FaceGraphic class
 * this class handles the intialization of drawables, its size, and the required type.
 */


class FaceGraphic extends GraphicOverlay.Graphic {

  private boolean mIsFrontFacing;
  private volatile FaceData mFaceData;
  private Drawable poop_emoji;
  private Drawable sad_emoji;
  private Drawable happy_emoji;
  private Drawable disappointed_emoji;
  private Drawable tongue_emjoji;
  private Drawable wink_emoji;
  private Drawable frown_emoji;

  private boolean setTracking;


  FaceGraphic(GraphicOverlay overlay, Context context, boolean isFrontFacing) {
    super(overlay);
    Resources resources = context.getResources();
    initializeGraphics(resources);

    this.setTracking = setTracking;

  }

  /**
   * initializes every drawable for each emotional state detected
   * @param resources
   */

  private void initializeGraphics(Resources resources) {
    poop_emoji = resources.getDrawable(R.drawable.poop);
    sad_emoji = resources.getDrawable(R.drawable.sad);
    happy_emoji = resources.getDrawable(R.drawable.happy);
    disappointed_emoji = resources.getDrawable(R.drawable.disappointed);
    tongue_emjoji = resources.getDrawable(R.drawable.tongue);
    wink_emoji = resources.getDrawable(R.drawable.wink);
    frown_emoji = resources.getDrawable(R.drawable.frown);
  }


  /**
   * Updates the face instance from the detection of the most recent frame.  Invalidates the
   * relevant portions of the overlay to trigger a redraw.
   */
  void update(FaceData faceData) {
    mFaceData = faceData;
    postInvalidate(); //Trigger a redraw of the graphic (i.e. cause draw() to be called).
  }

  /**
   * draws the overlay according to face detected
   * @param canvas drawing canvas
   * @param drawState drawable based on detected emotion
   * @param left size parameter
   * @param right size parameter
   * @param bottom size parameter
   * @param top size parameter
   */

  private void drawOverlay(Canvas canvas, Drawable drawState, int left, int right, int bottom, int top) {
    drawState.setBounds(left, top, right, bottom);
    drawState.draw(canvas);
  }

  /**
   * draw first handles sizing and scale of the drawable that is supposed to be overlayed on detected face.
   * secondly, according to it's emotional state it utilizes the correct drawable
   * @param canvas drawing canvas
   */

  @Override
  public void draw(Canvas canvas) {

    if (mFaceData == null) {
      return;
    }

    //get X and Y coordinates
    float x = translateX(mFaceData.getPosition().x + mFaceData.getWidth() / 2.0f);
    float y = translateY(mFaceData.getPosition().y + mFaceData.getHeight() / 2.0f);

    float xOffset = scaleX(mFaceData.getWidth() / 3.0f); //change float to scale differently
    float yOffset = scaleY(mFaceData.getHeight() / 3.0f);
    int left = (int) (x - xOffset);
    int top = (int) (y - yOffset);
    int right = (int) (x + xOffset);
    int bottom = (int) (y + yOffset);


    //states
    boolean smiling = mFaceData.isSmiling();
    boolean leftEyeOpen = mFaceData.isLeftEyeOpen();
    boolean rightEyeOpen = mFaceData.isRightEyeOpen();

    if (smiling){
      if (rightEyeOpen && leftEyeOpen){
        drawOverlay(canvas, happy_emoji, left, right, bottom, top);
      }
      else if (leftEyeOpen){
        drawOverlay(canvas, wink_emoji, left, right, bottom, top);
      }
      else if (rightEyeOpen){
        drawOverlay(canvas, tongue_emjoji, left, right, bottom, top);
      }
    }

    else if (!smiling){

      if (leftEyeOpen && !rightEyeOpen) {
        drawOverlay(canvas, wink_emoji, left, right, bottom, top);
      }

      else if (!leftEyeOpen && !rightEyeOpen){
        drawOverlay(canvas, disappointed_emoji, left, right, bottom, top);
      }
      else {
        drawOverlay(canvas, frown_emoji, left, right, bottom, top);
      }
    }
  }
}
