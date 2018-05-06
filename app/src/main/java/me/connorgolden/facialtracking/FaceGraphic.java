package me.connorgolden.facialtracking;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import me.connorgolden.facialtracking.ui.camera.GraphicOverlay;


class FaceGraphic extends GraphicOverlay.Graphic {

  private boolean mIsFrontFacing;
  private volatile FaceData mFaceData;
  private Drawable poop_emoji;


  FaceGraphic(GraphicOverlay overlay, Context context, boolean isFrontFacing) {
    super(overlay);
    Resources resources = context.getResources();
    initializeGraphics(resources);
  }

  private void initializeGraphics(Resources resources) {
    poop_emoji = resources.getDrawable(R.drawable.poop);
  }


  void update(FaceData faceData) {
    mFaceData = faceData;
    postInvalidate(); // Trigger a redraw of the graphic (i.e. cause draw() to be called).
  }


  private void drawOverlay(Canvas canvas, Drawable drawState, int left, int right, int bottom, int top) {
    drawState.setBounds(left, top, right, bottom);
    drawState.draw(canvas);
  }

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

    drawOverlay(canvas, poop_emoji, left, right, bottom, top);



        /* if necessary to get position of certain facialfeature
         PointF detectNoseBasePosition = mFaceData.getNoseBasePosition();
        PointF noseBasePosition = new PointF(translateX(detectNoseBasePosition.x),
                translateY(detectNoseBasePosition.y));
         */

  }

}
