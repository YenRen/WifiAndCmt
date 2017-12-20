package org.opencv.samples.tutorial2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.Face;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class FaceView extends ImageView {
	private static final String TAG = "YanZi";
	private Context mContext;
	private Paint mLinePaint;
	private Face[] mFaces;
	private Matrix mMatrix = new Matrix();
	private RectF mRect = new RectF();
	private Drawable mFaceIndicator = null;

	public FaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initPaint();
		mContext = context;
		mFaceIndicator = getResources().getDrawable(R.drawable.ic_face_find_2);
	}

	public void setFaces(Face[] faces){
		this.mFaces = faces;
		invalidate();
	}

	public Face[] getFaces(){
		return  this.mFaces;
	}

	public void clearFaces(){
		mFaces = null;
		invalidate();
	}
	

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if(mFaces == null || mFaces.length < 1){
			return;
		}
		boolean isMirror = false;
//		int Id = CameraInterface.getInstance().getCameraId();
//		if(Id == CameraInfo.CAMERA_FACING_BACK){
//			isMirror = false; //后置Camera无需mirror
//		}else if(Id == CameraInfo.CAMERA_FACING_FRONT){
//			isMirror = true;  //前置Camera需要mirror
//		}
		prepareMatrix(mMatrix, isMirror, 0, getWidth(), getHeight());
		canvas.save();
		mMatrix.postRotate(0); //Matrix.postRotate默认是顺时针
		canvas.rotate(-0);   //Canvas.rotate()默认是逆时针 
		for(int i = 0; i< mFaces.length; i++){
			mRect.set(mFaces[i].rect);
			mMatrix.mapRect(mRect);
            mFaceIndicator.setBounds(Math.round(mRect.left), Math.round(mRect.top),
                    Math.round(mRect.right), Math.round(mRect.bottom));
//            mFaceIndicator.draw(canvas);
			Log.i(TAG, "人脸识别矩阵1:"+ mRect);
			canvas.drawRect(mRect, mLinePaint);
		}
		canvas.restore();
		super.onDraw(canvas);
	}

	private void initPaint(){
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		int color = Color.rgb(0, 150, 255);
		int color = Color.rgb(255, 255, 255);
//		mLinePaint.setColor(Color.RED);
		mLinePaint.setColor(color);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(4f);
		mLinePaint.setAlpha(255);
	}

	public void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,int viewWidth, int viewHeight) {
		// Need mirror for front camera.
		matrix.setScale(mirror ? -1 : 1, 1);
		// This is the value for android.hardware.Camera.setDisplayOrientation.
		matrix.postRotate(displayOrientation);
		// Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
		// UI coordinates range from (0, 0) to (width, height).
		matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
		matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
	}

	public RectF selectFaceRect(float x, float y){
		if(mFaces == null || mFaces.length < 1){
			Log.i(TAG, "mRect1" );
			return null;
		}
		boolean isMirror = false;
//		int Id = CameraInterface.getInstance().getCameraId();
//		if(Id == CameraInfo.CAMERA_FACING_BACK){
//			isMirror = false; //后置Camera无需mirror
//		}else if(Id == CameraInfo.CAMERA_FACING_FRONT){
//			isMirror = true;  //前置Camera需要mirror
//		}
		RectF rect = null;
		prepareMatrix(mMatrix, isMirror, 0, getWidth(), getHeight());
		mMatrix.postRotate(0); //Matrix.postRotate默认是顺时针
		for(int i = 0; i< mFaces.length; i++){
			mRect.set(mFaces[i].rect);
			mMatrix.mapRect(mRect);
			Log.i(TAG, "人脸识别矩阵2:"+ mRect);
			if (mRect.contains(x,y)){
				rect = mRect;
				return rect;
			}
		}
		Log.i(TAG, "mRect2" );
		return null;
	}
}