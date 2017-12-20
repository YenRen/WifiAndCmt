package org.opencv.samples.tutorial2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ToggleButton extends View {
	//	switch_bkg_on 是原图   switch_bkg_onSamll是缩放图  此项目取的1/2
	private Bitmap switch_bkg_on,switch_bkg_onSamll;
	private Bitmap switch_bkg_off,switch_bkg_offSamll;
	private Bitmap slip_btn,slip_btnSmall;
	private float currentX;
	private boolean isSlipping = false;//是否处于正在滑动状态
	private boolean isSwitchOn = true;//当前开关的状态
	private boolean proSwitchStatus = true;//原先开关的状态
	public  boolean changePromition = true;//当前状态是否能被改变
	private OnSwitchStatusListener switchStatusListener;//开关监听器

	public ToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ToggleButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	// (绘制控件)
	@Override
	protected void onDraw(Canvas canvas) {//画布
		// 绘制背景
		Matrix matrix = new Matrix();// 指定图像样式
		Paint paint = new Paint();// 画笔

		if(currentX > switch_bkg_offSamll.getWidth()/2){
			//开启状态
			canvas.drawBitmap(switch_bkg_onSamll, matrix, paint);
		} else {
			//关闭状态
			canvas.drawBitmap(switch_bkg_offSamll, matrix, paint);
		}


		// 滑动按钮

		float left_slip = 0;
		if(isSlipping){
			//可以滑动
			if(currentX > switch_bkg_offSamll.getWidth()){
				left_slip = switch_bkg_offSamll.getWidth() - slip_btnSmall.getWidth();
			} else {
				left_slip = currentX - slip_btnSmall.getWidth()/2;
			}

		} else {
			//非滑动
			if(isSwitchOn){
				left_slip = switch_bkg_offSamll.getWidth() - slip_btnSmall.getWidth();
			} else {
				left_slip = 0;
			}
		}


		//开关边缘处理
		if(left_slip < 0){
			left_slip = 0;
		} else if(left_slip > switch_bkg_offSamll.getWidth() - slip_btnSmall.getWidth()){
			left_slip = switch_bkg_offSamll.getWidth() - slip_btnSmall.getWidth();
		}
		canvas.drawBitmap(slip_btnSmall, left_slip, 0, paint);





		super.onDraw(canvas);
	}

	//指定开关样式
	public void setSwitchStyle(int bkgSwitchOn, int bkgSwitchOff, int btnSlip) {
		//开关  开启的背景图
		switch_bkg_on = BitmapFactory.decodeResource(getResources(), bkgSwitchOn);
		switch_bkg_onSamll = Bitmap.createScaledBitmap(switch_bkg_on,switch_bkg_on.getWidth()/2,switch_bkg_on.getHeight()/2,false);
		//开关  关闭的背景图
		switch_bkg_off = BitmapFactory.decodeResource(getResources(), bkgSwitchOff);
		switch_bkg_offSamll = Bitmap.createScaledBitmap(switch_bkg_off,switch_bkg_off.getWidth()/2,switch_bkg_off.getHeight()/2,false);
		//滑动块图片
		slip_btn = BitmapFactory.decodeResource(getResources(), btnSlip);
		slip_btnSmall = Bitmap.createScaledBitmap(slip_btn,slip_btn.getWidth()/2,slip_btn.getHeight()/2,false);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(changePromition){
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN://按下
				currentX = event.getX();

				isSlipping = true;
				break;
			case MotionEvent.ACTION_MOVE://移动

				currentX = event.getX();

				break;
			case MotionEvent.ACTION_UP://松开
				isSlipping = false;

				if (currentX > switch_bkg_offSamll.getWidth() / 2) {
					isSwitchOn = true;
				} else {
					isSwitchOn = false;
				}


				//注册了开关监听器， 同时开关的状态发生了改变的时候
				if (switchStatusListener != null && proSwitchStatus != isSwitchOn) {
					switchStatusListener.onSwitch(isSwitchOn);

					proSwitchStatus = isSwitchOn;
				}

				break;
			}
		}


		invalidate();//重新绘制控件
		return true;
	}

	//指定开关的默认状态
	public void setSwitchStatus(boolean status) {
		isSwitchOn = status;
	}

	public void setChangePromition(boolean bool){
		changePromition = bool;
	}

	//开关状态改变监听器
	public interface OnSwitchStatusListener{
		abstract void onSwitch(boolean state);
	}

	//对外提高回调方法
	public void setOnSwitchStatusListener( OnSwitchStatusListener listener){
		switchStatusListener = listener;
	}

}
