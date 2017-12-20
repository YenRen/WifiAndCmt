package org.opencv.samples.tutorial2;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.opencv.samples.tutorial2.FaceDetect.CAMERA_HAS_STARTED_PREVIEW;

/*******************************************************************
 *  Copyright(c) 2016-2017 Company Name
 *  All rights reserved.
 *
 *  文件名称: Tutorial2Activity.cpp
 *  简要描述: 安卓物体视觉识别主程序
 *
 *  当前版本:0.1
 *  作者: 覃杰
 *  日期: 2016年12月05日
 *  说明: 安卓物体视觉识别程序第一个版本
 *
 *  取代版本:无
 *  作者: 无
 *  日期: 无
 *  说明: 无
 ******************************************************************/
public class Tutorial2Activity extends Activity implements CvCameraViewListener2,BluetoothAdapter.LeScanCallback,Runnable {
    //LOG打印标签
    private static final String TAG = "OCVSample::Activity";
    //常规模式
    private static final int VIEW_MODE_RGBA = 0;
    //CMT跟踪模式
    private static final int VIEW_MODE_CMT = 1;
    //开始CMT初始化
    private static final int START_CMT = 2;
    //跟踪分辨率的宽
    static final int WIDTH = 400;//240; // 320; //640 //400
    //跟踪分辨率的高
    static final int HEIGHT = 240;// 135;// ;//240;0; //480 //240
    //CMT是否初始化
    static boolean uno = true;
    //模式状态
    private int mViewMode;
    //彩色图像对象
    private Mat mRgba;
    //灰色图像对象
    private Mat mGray;
    //摄像头预览控件
    private Tutorial3View mOpenCvCameraView;
    //摄像头预览控件持有者
    SurfaceHolder _holder;
    //跟踪矩形框
    private Rect _trackedBox = null;
    //原子变量处理选择点
    final AtomicReference<Point> trackedBox1stCorner = new AtomicReference<Point>();
    //构造画笔对象
    final Paint rectPaint = new Paint();
    Point corner;
    private MainHandler mMainHandler = null;
    FaceDetect faceDetect = null;
    FaceView faceView;

    Point topLeft = new Point(0, 0);
    Point topRight = new Point(0, 0);
    Point bottomLeft = new Point(0, 0);
    Point bottomRight = new Point(0, 0);

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic characteristic;

    private AlertDialog dialog;
    private DeviceAdapter mDeviceAdapter;
    Thread thread;
    boolean isSendData;
    boolean isSendFinishData;
    boolean isSendRunData = true;
    private String mDeviceAddress = "00:15:83:30:7F:EE";//蓝牙设备地址
//    public static String ipname = "192.168.43.79";

    public final static UUID UUID_SERVICE =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");//蓝牙设备的Service的UUID

    public final static UUID UUID_NOTIFY =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");//蓝牙设备的Characteristic的UUID

    private final int CONNECT_BLUETOOTH_SUCCESS = 2;
    private final int DISCONNECT_BLUETOOTH = 3;
    private final int CONTROL_DEVICE = 4;
//    private ImageView opencvShowView;

//    private Button upButton;
//    private Button downButton;
//    private Button leftButton;
//    private Button rightButton;

    private ControlServerThread controlServerThread;

    private int Orientation;
//    private static PowerManager.WakeLock mWakeLock;

    //加载Opencv与CMT算法库
    static {
        //libA.so是用C封装类操作后生成的包，这里可以先随便命名
        System.loadLibrary("mixed_sample");
        //打印加载opencv库成功
        Log.i(TAG, "OpenCV loaded successfully");
    }

    /*******************************************************************
     *  函数名称: Tutorial2Activity
     *  作者: 覃杰
     *  功能：跟踪界面构造方法
     *  输入参数：无
     *  返回值: 无
     *  日期: 2016年12月10日
     ******************************************************************/
    public Tutorial2Activity() {
        //打印进入构造界面方法
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /*******************************************************************
     *  函数名称: onCreate
     *  作者: 覃杰
     *  功能：跟踪界面初始化方法
     *  输入参数：Bundle savedInstanceState
     *  返回值: 无
     *  日期: 2016年12月05日
     ******************************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //打印进入界面初始化
        Log.i(TAG, "called onCreate");
        //调用父类的构造方法
        super.onCreate(savedInstanceState);
        //加载界面
        setContentView(R.layout.tutorial2_surface_view);
        //获取Opencv摄像头控件
        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial2_activity_surface_view);
//        opencvShowView = (ImageView) findViewById(R.id.OpencvShowView);
        //设置摄像头控件监听器
        mOpenCvCameraView.setCvCameraViewListener(this);
        //设置画笔颜色
        rectPaint.setColor(Color.rgb(0, 255, 0));
        //设置画笔宽度
        rectPaint.setStrokeWidth(5);
        //设置画笔样式
        rectPaint.setStyle(Style.STROKE);
        //获取摄像头预览控件持有者
        _holder = mOpenCvCameraView.getHolder();
        //使能Opencv摄像头控件
        mOpenCvCameraView.enableView();
        //开启摄像头帧率
        mOpenCvCameraView.enableFpsMeter();
        //设置选择对象跟踪监听器
        mOpenCvCameraView.setOnTouchListener(onTouchListener);
//        mOpenCvCameraView.setResolution(640,360);

        faceView = (FaceView)findViewById(R.id.face_view);
        faceView.setVisibility(View.INVISIBLE);
        mMainHandler = new MainHandler();
        faceDetect = new FaceDetect(getApplicationContext(), mMainHandler);
        mMainHandler.sendEmptyMessageDelayed(CAMERA_HAS_STARTED_PREVIEW, 1500);

        initDialog();

        controlServerThread = new ControlServerThread(mMainHandler);
        controlServerThread.start();

        PhotoOrientationListener pl = new PhotoOrientationListener(this);
        pl.enable();

//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");

    }

    /*******************************************************************
     *  模块名称: onTouchListener
     *  作者: 覃杰
     *  功能：触摸事件回调方法
     *  输入参数：无
     *  返回值: 无
     *  日期: 2016年12月05日
     ******************************************************************/
    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //获取当前点的对象
            corner = new Point(event.getX() , event.getY());
            //触摸事件处理
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN://触摸按下事件处理
                    //保存第一次触摸按下的点
                    trackedBox1stCorner.set(corner);
                    //打印第一次触摸按下的点
                    Log.i(TAG, "1st corner: " + corner);
                    break;
                case MotionEvent.ACTION_UP://触摸放开事件处理
                    if (mViewMode == VIEW_MODE_CMT){
                        Toast.makeText(Tutorial2Activity.this,"取消跟踪",Toast.LENGTH_SHORT).show();
//                        faceView.setVisibility(View.VISIBLE);
                        isSendFinishData = true;
                        isSendData = false;
                        mViewMode = VIEW_MODE_RGBA;
                        break;
                    }
                    //获取跟踪矩形框
                    _trackedBox = new Rect(trackedBox1stCorner.get(), corner);
                    //判断跟踪矩形框是否大于100
                    if (_trackedBox.area() > 100) {
                        //打印跟踪矩形框
                        Log.i(TAG, "Tracked box DEFINED: " + _trackedBox);
                        Toast.makeText(Tutorial2Activity.this,"开始跟踪",Toast.LENGTH_SHORT).show();
                        //切换跟踪模式为开始初始化CMT
                        mViewMode = START_CMT;
                        faceView.setVisibility(View.INVISIBLE);
                    } else{
                        RectF mRect = faceView.selectFaceRect(event.getX(),event.getY());
                        if (mRect == null){
//                            _trackedBox = null;
                            int x = (int)event.getX();
                            int y = (int)event.getY();
                            int z = 60;

                            mViewMode = START_CMT;
                            Toast.makeText(Tutorial2Activity.this,"开始跟踪",Toast.LENGTH_SHORT).show();
//                          Toast.makeText(Tutorial2Activity.this,"未选择的矩形",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(Tutorial2Activity.this,"开始跟踪人脸",Toast.LENGTH_SHORT).show();
                            _trackedBox = new Rect((int)mRect.left,(int)mRect.top,(int)mRect.width(),(int)mRect.height());
                            mViewMode = START_CMT;
                            faceView.setVisibility(View.INVISIBLE);
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE://触摸移动事件处理
                    //获取跟踪矩形框
                    _trackedBox = new Rect(trackedBox1stCorner.get(), corner);
                    //构造出跟踪矩形框
                    final android.graphics.Rect rect = new android.graphics.Rect(
                            _trackedBox.x,
                            _trackedBox.y,
                            _trackedBox.x + _trackedBox.width,
                            _trackedBox.y + _trackedBox.height);
                    //获取画板
                    final Canvas canvas = _holder.lockCanvas(rect);
                    //设置绘图颜色和模式
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    //画出跟踪矩形框
                    canvas.drawRect(rect, rectPaint);
                    //释放画板
                    _holder.unlockCanvasAndPost(canvas);

//                    //创建RGB存储照片对象
//                    Mat rgba1 = mRgba.clone();
//                    //把BGR照片转换为RGB照片对象
//                    Imgproc.cvtColor(mRgba, rgba1, Imgproc.COLOR_RGBA2BGR, 3);
//                    //把照片写入SD卡
//                    Highgui.imwrite("/storage/sdcard0/1.jpg",rgba1);
//
//                    //获取跟踪矩形四个顶点
//                    Point topLeft = new Point(_trackedBox.x, _trackedBox.y);
//                    Point bottomRight = new Point(_trackedBox.x+_trackedBox.width, _trackedBox.y+_trackedBox.height);
//                    Core.rectangle(mRgba,topLeft,bottomRight,new Scalar(255, 255, 255), 3);
//
//                    //创建RGB存储照片对象
//                    Mat rgba = mRgba.clone();
//                    //把BGR照片转换为RGB照片对象
//                    Imgproc.cvtColor(mRgba, rgba, Imgproc.COLOR_RGBA2BGR, 3);
//                    //把照片写入SD卡
//                    Highgui.imwrite("/storage/sdcard0/2.jpg",rgba);
                    break;
            }

            return true;
        }
    };

    /*******************************************************************
     *  函数名称: onCameraFrame
     *  作者: 覃杰
     *  功能：摄像头每帧回调处理函数
     *  输入参数：CvCameraViewFrame inputFrame
     *  返回值: Mat
     *  日期: 2016年12月05日
     ******************************************************************/
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //获取摄像头RGB图像
        mRgba = inputFrame.rgba();
        Mat rgba = mRgba.clone();
        new MyThread(rgba,"192.168.43.1").start();

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(Tutorial2Activity.this,"拍照成功",Toast.LENGTH_SHORT).show();
//            }
//        });

//        Bitmap bmp = Bitmap.createBitmap(mRgba.width(), mRgba.height(),
//                Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mRgba, bmp);
//        opencvShowView.setImageBitmap(bmp);

        //获取当前模式
        final int viewMode = mViewMode;
        //模式执行
        switch (viewMode) {
            case VIEW_MODE_RGBA://常规模式
                //获取摄像头RGB图像
                mRgba = inputFrame.rgba();
                break;
            case START_CMT://开始CMT初始化
            {
                //获取摄像头彩色图像
                mRgba = inputFrame.rgba();
                //获取摄像头灰色图像
                mGray = Reduce(inputFrame.gray());
                //获取灰度图像的宽
                double w = mGray.width();
                //获取灰度图像的高
                double h = mGray.height();
                //跟踪矩形框为空则定位到中心点
                if (_trackedBox == null)
                    //初始化CMT
                    OpenCMT(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
                            (long) (w / 2 - w / 4), (long) (h / 2 - h / 4),
                            (long) w / 2, (long) h / 2);
                else {
                    //打印跟踪矩形框参数
                    Log.i("TAG", "START DEFINED: " + _trackedBox.x / 2 + " "
                            + _trackedBox.y / 2 + " " + _trackedBox.width / 2 + " "
                            + _trackedBox.height / 2);
                    //获取摄像头预览控件的宽
                    double px = (w) / (double) (mOpenCvCameraView.getWidth());
                    //获取摄像头预览控件的高
                    double py = (h) / (double) (mOpenCvCameraView.getHeight());
                    //初始化CMT
                    OpenCMT(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
                            (long) (_trackedBox.x * px),
                            (long) (_trackedBox.y * py),
                            (long) (_trackedBox.width * px),
                            (long) (_trackedBox.height * py));
                }
                //标识初始化完成
                uno = false;
                //进入CMT跟踪模式
                mViewMode = VIEW_MODE_CMT;
            }
            break;
            case VIEW_MODE_CMT://CMT跟踪模式
            {
                //获取摄像头彩色图像
                mRgba = inputFrame.rgba();
                //获取摄像头灰色图像
                mGray = inputFrame.gray();
                //压缩灰色图像
                mGray = Reduce(mGray);
                //压缩彩色图像
                Mat mRgba2 = ReduceColor(mRgba);
//                Imgproc.cvtColor(mGray,mGray,Imgproc.COLOR_BGR2GRAY,4);
//                //图像滤波
//                Imgproc.GaussianBlur(mRgba2,mRgba2, new Size(3,3),0);
                //CMT是否初始化完成
                if (uno) {
                    //获取灰度图像的宽
                    int w = mGray.width();
                    //获取灰度图像的高
                    int h = mGray.height();
                    //初始化CMT
                    OpenCMT(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
                            (long) w - w / 4, (long) h / 2 - h / 4, (long) w / 2,
                            (long) h / 2);
                    //标识初始化完成
                    uno = false;
                } else {
                    //跟踪目标物体
                    ProcessCMT(mGray.getNativeObjAddr(), mRgba2.getNativeObjAddr());
                    //获取摄像头预览控件的宽
                    double px = (double) mRgba.width() / (double) mRgba2.width();
                    //获取摄像头预览控件的高
                    double py = (double) mRgba.height() / (double) mRgba2.height();
                    //获取跟踪矩形参数
                    int[] l = CMTgetRect();
                    //是否获取到跟踪矩形参数
                    if (l != null) {
                        Scalar scalar = null;

                        if(l[0]>0&&l[1]>0&&l[2]>0&&l[3]>0&&l[3]>0&&l[4]>0&&l[6]>0&&l[7]>0){
//                          if(l[8] > 0 ){
                            //获取跟踪矩形四个顶点
                            topLeft = new Point(l[0] * px, l[1] * py);
                            topRight = new Point(l[2] * px, l[3] * py);
                            bottomLeft = new Point(l[4] * px, l[5] * py);
                            bottomRight = new Point(l[6] * px, l[7] * py);
                            scalar = new Scalar(0, 255, 0);
                            isSendData = true;
                        }else{
                            scalar = new Scalar(255, 0, 0);
                            if (isSendData){
                                isSendFinishData = true;
                            }
                            isSendData = false;
                        }

//                        Scalar scalar = null;
//                        if (l[8] > 15){
//                            scalar = new Scalar(0, 255, 0);
//                        }else{
//                            scalar = new Scalar(255, 0, 0);
//                        }

                        //画出跟踪矩形框
                        Core.line(mRgba, topLeft, topRight, scalar, 3);
                        Core.line(mRgba, topRight, bottomRight, scalar, 3);
                        Core.line(mRgba, bottomRight, bottomLeft, scalar, 3);
                        Core.line(mRgba, bottomLeft, topLeft, scalar, 3);

                        Point point = getCenterPoint(new Rect(topLeft,bottomRight));
                        //打印跟踪物体中心点
                        Log.i(TAG, "CMT.Point:" + point);
                        Log.i(TAG, "Device.Point:{x:" + point.x/mRgba.width()*10000+","+point.y/mRgba.height()*10000+"}");
//                        Log.i(TAG, "CMT.Point:" + l[0] + l[1] + l[2] + l[3] + l[4] + l[5] + l[6] + l[7]);

//                        point.x = point.x/mRgba.width()*10000;
//                        point.y = point.y/mRgba.height()*10000;

//                        Message message = new Message();
//                        message.what = SEND_BLUETOOTH_DATA;
//                        message.obj = point;
//                        mMainHandler.sendMessage(message);
                    }
                    //标识初始化完成
                    uno = false;
                }
            }
            break;
        }

//        //创建RGB存储照片对象
//        Mat rgba = mRgba.clone();
//        //把BGR照片转换为RGB照片对象
//        Imgproc.cvtColor(mRgba, rgba, Imgproc.COLOR_RGBA2BGR, 3);
//        //把照片写入SD卡
//        Highgui.imwrite("/storage/sdcard0/1234.jpg",rgba);
        //返回跟踪帧图像
        return mRgba;
    }

    /*******************************************************************
     *  函数名称: Reduce
     *  作者: 覃杰
     *  功能：灰度图像缩放
     *  输入参数：Mat m
     *  返回值: Mat
     *  日期: 2016年12月05日
     ******************************************************************/
    Mat Reduce(Mat m) {
        // return m;
        Mat dst = new Mat();
        Imgproc.resize(m, dst, new org.opencv.core.Size(WIDTH, HEIGHT));
        return dst;
    }

    /*******************************************************************
     *  函数名称: ReduceColor
     *  作者: 覃杰
     *  功能：彩色图像缩放
     *  输入参数：Mat m
     *  返回值: Mat
     *  日期: 2016年12月05日
     ******************************************************************/
    Mat ReduceColor(Mat m) {
        Mat dst = new Mat();
        Bitmap bmp = Bitmap.createBitmap(m.width(), m.height(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, WIDTH, HEIGHT, false);

        Utils.bitmapToMat(bmp2, dst);
        // Imgproc.resize(m, dst, new Size(WIDTH,HEIGHT), 0, 0,
        // Imgproc.INTER_CUBIC);
        return dst;
    }

    /*******************************************************************
     *  函数名称: onCameraViewStarted
     *  作者: 覃杰
     *  功能：摄像头预览控件开始回调
     *  输入参数：int width, int height
     *  返回值: 无
     *  日期: 2016年12月05日
     ******************************************************************/
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    /*******************************************************************
     *  函数名称: onCameraViewStopped
     *  作者: 覃杰
     *  功能：摄像头预览控件结束回调
     *  输入参数：无
     *  返回值: 无
     *  日期: 2016年12月05日
     ******************************************************************/
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
    }

    /*******************************************************************
     *  函数名称: getCenterPoint
     *  作者: 覃杰
     *  功能：获取矩形中心点
     *  输入参数：Rect rect
     *  返回值: Point
     *  日期: 2016年12月05日
     ******************************************************************/
    public Point getCenterPoint(Rect rect) {
        Point point = new Point(rect.x + rect.width/2, rect.y + rect.height/2.0);
        return point;
    }

    @Override
    public void onPause() {
        super.onPause();
//        mWakeLock.release();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
//        mWakeLock.acquire();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.enableView();
    }

    @Override
    public void onDestroy() {
        controlServerThread.close();
        isSendRunData = false;
        if (mBluetoothGatt!=null){
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case FaceDetect.UPDATE_FACE_RECT:
                    Camera.Face[] faces = (Camera.Face[]) msg.obj;
                    faceView.setFaces(faces);
                    break;
                case FaceDetect.CAMERA_HAS_STARTED_PREVIEW:
                    mOpenCvCameraView.startGoogleFaceDetect(faceDetect);
                    break;
                case CONNECT_BLUETOOTH_SUCCESS:
                    Toast.makeText(Tutorial2Activity.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
//                    Tutorial2Activity.this.characteristic.setValue("1234".getBytes());
//                    mBluetoothGatt.writeCharacteristic(Tutorial2Activity.this.characteristic);
                    break;
                case DISCONNECT_BLUETOOTH:
                    Toast.makeText(Tutorial2Activity.this, "断开蓝牙连接", Toast.LENGTH_SHORT).show();
                    break;
                case CONTROL_DEVICE:
                    String string = (String) msg.obj;
                    if ("TakePicture".equals(string)){
                        //创建RGB存储照片对象
                        Mat rgba = mRgba.clone();
                        //把BGR照片转换为RGB照片对象
                        Imgproc.cvtColor(mRgba, rgba, Imgproc.COLOR_RGBA2BGR, 3);

                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
                        //把照片写入SD卡
                        Highgui.imwrite("/storage/sdcard0/X-CAM_TEST_"+ df.format(new Date()) +".jpg",rgba);

                        Toast.makeText(Tutorial2Activity.this,"拍照成功",Toast.LENGTH_SHORT).show();
                        break;
                    }


                    if("StartPanoman".equals(string)){
                        Thread thread = new Thread(){
                            public void run(){
                                try {
                                    byte[] data = {(byte) 0x24, (byte) 0x53, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00};
                                    data[6] = 0x01;
                                    if (mBluetoothGatt!=null&&characteristic!=null){
                                        characteristic.setValue(data);
                                        mBluetoothGatt.writeCharacteristic(characteristic);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                        break;
                    }
                    if("StopPanoman".equals(string)){
                        Thread thread = new Thread(){
                            public void run(){
                                try {
                                    byte[] data = {(byte) 0x24, (byte) 0x53, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00};
                                    data[6] = 0x11;
                                    if (mBluetoothGatt!=null&&characteristic!=null){
                                        characteristic.setValue(data);
                                        mBluetoothGatt.writeCharacteristic(characteristic);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                        break;
                    }

                    byte[] data = {(byte) 0x24, (byte) 0x53, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00};
                    boolean b = true;
                    if ("DeviceUp".equals(string)){
//                        closeButton();
//                        upButton.setEnabled(true);
                        data[5] = 0x10;
                    }else if ("DeviceDown".equals(string)){
//                        closeButton();
//                        downButton.setEnabled(true);
                        data[5] = 0x01;
                    }else if ("DeviceLeft".equals(string)){
//                        closeButton();
//                        leftButton.setEnabled(true);
                        data[3] = 0x10;
                    }else if ("DeviceRight".equals(string)){
//                        closeButton();
//                        rightButton.setEnabled(true);
                        data[3] = 0x01;
                    }else if ("DeviceUps".equals(string)){
//                        closeButton();
//                        upButton.setEnabled(true);
                        data[5] = 0x10;
                        b = false;
                    }else if ("DeviceDowns".equals(string)){
//                        closeButton();
//                        downButton.setEnabled(true);
                        data[5] = 0x01;
                        b = false;
                    }else if ("DeviceLefts".equals(string)){
//                        closeButton();
//                        leftButton.setEnabled(true);
                        data[3] = 0x10;
                        b = false;
                    }else if ("DeviceRights".equals(string)){
//                        closeButton();
//                        rightButton.setEnabled(true);
                        data[3] = 0x01;
                        b = false;
                    }else if ("DeviceStop".equals(string)){
//                        closeButton();
                    }
                    data[9] = getXor(data);

                    final byte[] fdata = data;
                    if (b){
                        Thread thread = new Thread(){
                            public void run(){
                                try {
                                    for (int i=0;i<20;i++) {
                                        if (mBluetoothGatt!=null&&characteristic!=null){
                                            characteristic.setValue(fdata);
                                            mBluetoothGatt.writeCharacteristic(characteristic);
                                        }

                                        Thread.sleep(50);
                                    }

                                    byte[] data = {(byte) 0x24, (byte) 0x53, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00};
                                    if (mBluetoothGatt!=null&&characteristic!=null){
                                        characteristic.setValue(data);
                                        mBluetoothGatt.writeCharacteristic(characteristic);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                    }else{
                        Thread thread = new Thread(){
                            public void run(){
                                try {
                                    for (int i=0;i< 2;i++) {
                                        if (mBluetoothGatt!=null&&characteristic!=null){
                                            characteristic.setValue(fdata);
                                            mBluetoothGatt.writeCharacteristic(characteristic);
                                        }

                                        Thread.sleep(50);
                                    }

                                    byte[] data = {(byte) 0x24, (byte) 0x53, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00};
                                    if (mBluetoothGatt!=null&&characteristic!=null){
                                        characteristic.setValue(data);
                                        mBluetoothGatt.writeCharacteristic(characteristic);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                    }





                    break;
            }
            super.handleMessage(msg);
        }
    }

//    public void closeButton(){
//        upButton.setEnabled(false);
//        downButton.setEnabled(false);
//        leftButton.setEnabled(false);
//        rightButton.setEnabled(false);
//    }

    public native void OpenCMT(long matAddrGr, long matAddrRgba, long x, long y, long w, long h);

    public native void ProcessCMT(long matAddrGr, long matAddrRgba);

    public native void CMTSave(String Path);

    public native void CMTLoad(String Path);

    private static native int[] CMTgetRect();

    private class PhotoOrientationListener extends OrientationEventListener {

        public PhotoOrientationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int i) {
            if (45 <= i && i < 135) {//右边 竖屏
                Log.i("qwer", "1");
                Orientation = 1;
            } else if (135 <= i && i < 225) {//下边  横屏
                Log.i("qwer", "2");
                Orientation = 2;
            } else if (i==-1) {//左边 竖屏
                Log.i("qwer", "3");
                Orientation = 3;
            }
            else if (315 <= i || i < 45 ) {//左边 竖屏
                Log.i("qwer", "4");
                Orientation = 4;
            } else {//上边 横屏
                Log.i("qwer", "3");
                Orientation = 3;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            mDeviceAdapter.clear();
            startScan();
            dialog.show();
        }
        return false;
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {//连接状态改变
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {//连接成功
                    Log.i("BLE", "连接成功");
                    Message message = new Message();
                    message.what = CONNECT_BLUETOOTH_SUCCESS;
                    message.obj = "连接成功";
                    mMainHandler.sendMessage(message);
                    //搜索Service
                    mBluetoothGatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//断开连接
                    Log.i("BLE", "连接断开");
                    Message message = new Message();
                    message.what = DISCONNECT_BLUETOOTH;
                    message.obj = "连接断开";
                    mMainHandler.sendMessage(message);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {//服务被发现
            characteristic = gatt.getService(UUID_SERVICE).getCharacteristic(UUID_NOTIFY);
            //根据UUID获取Service中的Characteristic,并传入Gatt中
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {//数据改变
//            String data = new String(characteristic.getValue());
            byte[] bytes = characteristic.getValue();
            if(bytes[6] == 0x10){
                //创建RGB存储照片对象
                Mat rgba = mRgba.clone();
                //把BGR照片转换为RGB照片对象
                Imgproc.cvtColor(mRgba, rgba, Imgproc.COLOR_RGBA2BGR, 3);

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
                //把照片写入SD卡
                Highgui.imwrite("/storage/sdcard0/X-CAM_TEST_"+ df.format(new Date()) +".jpg",rgba);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Tutorial2Activity.this,"拍照成功",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Log.i("Read BLE Data:", bytesToHexString(characteristic.getValue()));
//            Log.i("Read BLE Data:", data);
//            Tutorial2Activity.this.characteristic.setValue("Read Success".getBytes());
//            mBluetoothGatt.writeCharacteristic(Tutorial2Activity.this.characteristic);
        }
    };

    /** 注册对话框初始化 */
    private void initDialog() {
        // 借助builder对象构建对话框对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置对话框标题O(∩_∩)O~
        builder.setTitle("是否退出？");
        // 设置界面布局XML
        View view = View.inflate(this, R.layout.connect_bluetooth_dialog, null);
        // builde加载布局
        builder.setView(view);
        // 加载3个按钮
        builder.setPositiveButton("确定", regDialogListener);
        builder.setNeutralButton("远程控制", regDialogListener);
        builder.setNegativeButton("取消", regDialogListener);
        // 创建Dialog
        dialog = builder.create();
        // 设置触摸屏幕不关闭
        dialog.setCancelable(false);

        // BLE check
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不支持BLE蓝牙设备", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取BluetoothManager
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            Toast.makeText(this, "不支持BLE蓝牙设备", Toast.LENGTH_SHORT).show();
        }
        //获取BluetoothAdapter
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "不支持BLE蓝牙设备", Toast.LENGTH_SHORT).show();
            return;
        }
//        //如果蓝牙没有打开 打开蓝牙
//        if (!mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.enable();
//        }
        //获取BluetoothAdapter
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "不支持BLE蓝牙设备", Toast.LENGTH_SHORT).show();
            return;
        }

        // init listview
        ListView deviceListView = (ListView) view.findViewById(R.id.BlueToothListView);
        mDeviceAdapter = new DeviceAdapter(this, R.layout.listitem_device,
                new ArrayList<ScannedDevice>());
        deviceListView.setAdapter(mDeviceAdapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View view, int position, long id) {
                ScannedDevice item = mDeviceAdapter.getItem(position);
                if (item != null) {
                    BluetoothDevice selectedDevice = item.getDevice();
                    mDeviceAddress = selectedDevice.getAddress();
//                    Toast.makeText(Tutorial2Activity.this, "连接蓝牙:"+mDeviceAddress, Toast.LENGTH_SHORT).show();

                    //根据蓝牙地址获取BluetoothDevice
                    BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);

                    //如果Gatt在运行,将其关闭
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                    }
                    //连接蓝牙设备并获取Gatt对象
                    mBluetoothGatt = bluetoothDevice.connectGatt(Tutorial2Activity.this, true, bluetoothGattCallback);
                    stopScan();
                    thread=new Thread(Tutorial2Activity.this);
                    thread.start();
                    dialog.dismiss();
                }
            }
        });

//        startScan();
    }

    private DialogInterface.OnClickListener regDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: // 确定按钮响应
                    Tutorial2Activity.this.finish();
                    break;
                case DialogInterface.BUTTON_NEUTRAL: // 远程控制按钮响应
                    dialog.dismiss();
                    Tutorial2Activity.this.finish();
                    System.gc();
                    Intent intent = new Intent(Tutorial2Activity.this,IPCameraActivity.class);
                    Tutorial2Activity.this.startActivity(intent);
                    break;
                case DialogInterface.BUTTON_NEGATIVE: // 取消按钮响应
                    dialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onLeScan(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
            }
        });
    }

    private void startScan() {
        if ((mBluetoothAdapter != null)) {
            mBluetoothAdapter.startLeScan(this);
//            setProgressBarIndeterminateVisibility(true);
//            invalidateOptionsMenu();
        }
    }

    private void stopScan() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(this);
        }
//        setProgressBarIndeterminateVisibility(false);
//        invalidateOptionsMenu();
    }

    @Override
    public void run()
    {
        while (isSendRunData){
            try {
                Thread.sleep(50);
                if (mBluetoothGatt!=null&&characteristic!=null&& isSendData&& mViewMode==VIEW_MODE_CMT){

                    String string = "";

                    byte[] data = {(byte) 0x24, (byte) 0x53, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00};

                    Point point = getCenterPoint(new Rect(topLeft,bottomRight));
//                    Log.i("1234", "发送数据" +point);
//                        String string = "CMT.Point:{x:" + point.x + ",y:" + point.y + "}\r\n";

                      //坐标发送程序
//                    double x = point.x/mRgba.width()*10000;
//                    double y = point.y/mRgba.height()*10000;
//
//                    byte[] xData = intToByteArray((int) x);
//                    byte[] yData = intToByteArray((int) x);
//
//                    data[2] = xData[3];
//                    data[3] = xData[2];
//                    data[4] = yData[3];
//                    data[5] = yData[2];
//                    data[9] = getXor(data);

                    int halfpx = mRgba.width()/2;
                    int halfpy = mRgba.height()/2;

                    int z = halfpy/4;
                    RectF mRect = new RectF(halfpx-z,halfpy-z,halfpx+z,halfpx+z);
                    float x = (float)point.x;
                    float y = (float)point.y;

                    Log.i("senddata", "senddata:"+Orientation);
                    if (Orientation == 4){
                        if (mRect.contains(x,y)){
                            isSendFinishData = true;
                        }else{
                            RectF xRect = new RectF(halfpx-z,0,halfpx+z,halfpy*2);
                            if (xRect.contains(x,y)){
                                data[5] = 0x00;
                                Log.i("BLE Device", "停止");
                                string = "DeviceStop";
                            }else if (x>halfpx){
                                data[5] = 0x01;
                                Log.i("BLE Device", "向下");
                                string = "DeviceDown";
                            }else if (x<halfpx){
                                data[5] = 0x10;
                                Log.i("BLE Device", "向上");
                                string = "DeviceUp";
                            }

                            RectF yRect = new RectF(0,halfpy-z,halfpx*2,halfpy+z);
                            if (yRect.contains(x,y)){
                                data[3] = 0x00;
                            }else if (y>halfpy){
                                data[3] = 0x10;
                            }else if (y<halfpy){
                                data[3] = 0x01;
                            }
                        }
                    }else {
                        if (mRect.contains(x,y)){
                            isSendFinishData = true;
                        }else{
                            RectF xRect = new RectF(halfpx-z,0,halfpx+z,halfpy*2);
                            if (xRect.contains(x,y)){
                                data[3] = 0x00;
                                Log.i("BLE Device", "停止");
                                string = "DeviceStop";
                            }else if (x>halfpx){
                                data[3] = 0x01;
                                Log.i("BLE Device", "向右");
                                string = "DeviceRight";
                            }else if (x<halfpx){
                                data[3] = 0x10;
                                Log.i("BLE Device", "向左");
                                string = "DeviceLeft";
                            }

//                            Message message = new Message();
//                            message.what = CONTROL_DEVICE;
//                            message.obj = string;
//                            mMainHandler.sendMessage(message);

                            RectF yRect = new RectF(0,halfpy-z,halfpx*2,halfpy+z);
                            if (yRect.contains(x,y)){
                                data[5] = 0x00;
                                Log.i("BLE Device", "停止");
                                string = "DeviceStop";
                            }else if (y>halfpy){
                                data[5] = 0x01;
                                Log.i("BLE Device", "DeviceDown");
                                string = "DeviceDown";
                            }else if (y<halfpy){
                                data[5] = 0x10;
                                Log.i("BLE Device", "DeviceUp");
                                string = "DeviceUp";
                            }

//                            message = new Message();
//                            message.what = CONTROL_DEVICE;
//                            message.obj = string;
//                            mMainHandler.sendMessage(message);
                        }
                    }

                    data[9] = getXor(data);

                    Log.i("Read BLE Data:", bytesToHexString(data));

                    characteristic.setValue(data);
                    mBluetoothGatt.writeCharacteristic(characteristic);

                }

                if (isSendFinishData){
                    isSendFinishData = false;
                    byte[] data = {(byte) 0x24, (byte) 0x53, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x40, (byte) 0x00};
                    characteristic.setValue(data);
                    mBluetoothGatt.writeCharacteristic(characteristic);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    public static byte getXor(byte[] datas){
        byte temp=datas[0];
        for (int i = 1; i <datas.length; i++) {
            temp ^=datas[i];
        }
        return temp;
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}