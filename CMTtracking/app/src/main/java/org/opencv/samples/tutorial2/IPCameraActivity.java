package org.opencv.samples.tutorial2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPCameraActivity extends Activity {

    private static final int UPDATE_VIEW = 0; //监控
    private static boolean IS_PHOTO_MODE = true; //当前模式
    private MyThread myThread;
    private ImageView imageView,ImageMode;
    private MainHandler mMainHandler = null;
    private String ipname = "192.168.43.1";
    private TextView mTextMode;
    private ToggleButton mToBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipcamera);
        findViewById(R.id.img_left).setOnTouchListener(mOnTouchListener);
        findViewById(R.id.img_right).setOnTouchListener(mOnTouchListener);
        //mTextMode = (TextView) findViewById(R.id.choose_mode);
        //ImageMode = (ImageView) findViewById(R.id.choose_mode);
        mToBtn = (ToggleButton) findViewById(R.id.myToBtn);
        //指定开关样式
        mToBtn.setSwitchStyle(R.drawable.bkg_switch, R.drawable.bkg_switch, R.drawable.btn_slip);

        //指定开关的默认状态   false拍摄模式  true全景拍摄模式
        mToBtn.setSwitchStatus(false);
        mToBtn.setOnSwitchStatusListener(mOnSwitchListener);

        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setOnTouchListener(onTouchListener);
        mMainHandler = new MainHandler();

        Log.i("Server", "服务器IP："+getHostIP());
        myThread = new MyThread(mMainHandler);
        myThread.start();
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case UPDATE_VIEW:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    imageView.setImageBitmap(bitmap);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //当手指按下的时候
                    x1 = event.getX();
                    y1 = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    //当手指离开的时候
                    x2 = event.getX();
                    y2 = event.getY();
                    if(y1 - y2 > 50) {
//                        Toast.makeText(IPCameraActivity.this, "向上滑", Toast.LENGTH_SHORT).show();
                        new ControlThread("DeviceUp",ipname).start();
                    } else if(y2 - y1 > 50) {
//                        Toast.makeText(IPCameraActivity.this, "向下滑", Toast.LENGTH_SHORT).show();
                        new ControlThread("DeviceDown",ipname).start();
                    } else if(x1 - x2 > 50) {
//                        Toast.makeText(IPCameraActivity.this, "向左滑", Toast.LENGTH_SHORT).show();
                        new ControlThread("DeviceLeft",ipname).start();
                    } else if(x2 - x1 > 50) {
//                        Toast.makeText(IPCameraActivity.this, "向右滑", Toast.LENGTH_SHORT).show();
                        new ControlThread("DeviceRight",ipname).start();
                    }else{
//                        Toast.makeText(IPCameraActivity.this, "点击事件", Toast.LENGTH_SHORT).show();
                        new ControlThread("TakePicture",ipname).start();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
            }
            return true;
        }
    };

    public String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }

    @Override
    public void onDestroy() {
        myThread.close();
        super.onDestroy();
    }

    private class MyThread extends Thread {

//        private ServerSocket serverSocket;
        private DatagramSocket datagramSocket;
        private DatagramPacket datagramPacket;
        private Bitmap bmp = null;
        private MainHandler mMainHandler;
        private boolean run = true;
        private byte[] rawImagebuff = new byte[204800];

        public MyThread(MainHandler mMainHandler) {
            this.mMainHandler = mMainHandler;
            try {
                this.datagramSocket = new DatagramSocket(6000);
                this.datagramPacket = new DatagramPacket(rawImagebuff, rawImagebuff.length);
            } catch (IOException e) {
                Log.i("Server", "服务器创建出错");
                e.printStackTrace();
            }
        }

        public void run() {
            while (run){
                try {
                    Log.i("Server", "等待图片数据接入");
                    // 准备接收数据
                    datagramSocket.receive(datagramPacket);
                    InetAddress address = datagramPacket.getAddress();// 接收的地址
                    ipname = address.toString().substring(1);
                    Log.i("Server", ipname);

                    byte[] rawImage = datagramPacket.getData();
                    bmp = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);

//                    Socket s = this.serverSocket.accept();
//                    InputStream is = s.getInputStream();
//
//                    bmp =  BitmapFactory.decodeStream(is);
//                    is.close();
//                    s.close();

                    Message message = new Message();
                    message.what = 0;
                    message.obj = bmp;
                    mMainHandler.sendMessage(message);
                    //            imageView.setImageBitmap(bmp);
                } catch (Exception e) {
                    Log.i("Server", "服务器创建失败");
                    e.printStackTrace();
                }
            }
        }

        public void close(){
            run = false;
            try {
                datagramSocket.close();
            } catch (Exception e) {
                Log.i("Server", "服务器销毁出错");
                e.printStackTrace();
            }
        }
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (view.getId()){
                case R.id.img_left:
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_MOVE:
                            new ControlThread("DeviceLeft",ipname).start();
                           break;
                    }
                    break;
                case R.id.img_right:
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_MOVE:
                            new ControlThread("DeviceRight",ipname).start();
                            break;
                    }
                    break;
            }
            return true;
        }
    };
    boolean flg = false; //全景开始
    public void takePhoto(View view){
        if(IS_PHOTO_MODE) {
            new ControlThread("TakePicture", ipname).start(); //拍照
            Toast.makeText(this, "拍摄成功", Toast.LENGTH_SHORT).show();
        }else {
            if (!flg) {
                new ControlThread("StartPanoman", ipname).start();//开始全景
                mToBtn.setOnSwitchStatusListener(null);
                mToBtn.setChangePromition(false); //正在全景模式则不能更改状态
                Toast.makeText(this, "开始全景拍摄", Toast.LENGTH_SHORT).show();
                flg = !flg;
            } else {
                new ControlThread("StopPanoman", ipname).start();//结束全景
                mToBtn.setOnSwitchStatusListener(mOnSwitchListener);
                mToBtn.setChangePromition(true);
                Toast.makeText(this, "全景拍摄已结束", Toast.LENGTH_SHORT).show();
                flg = !flg;
            }
        }
    }
    /*
    public void doChooseMode(View view){
        IS_PHOTO_MODE = !IS_PHOTO_MODE;
    //    mTextMode.setText(IS_PHOTO_MODE?"当前模式：拍摄模式":"当前模式：全景模式");
        ImageMode.setImageResource(IS_PHOTO_MODE?R.mipmap.icon_off:R.mipmap.icon_on);
        flg = false;
    }
    */
    private ToggleButton.OnSwitchStatusListener mOnSwitchListener = new ToggleButton.OnSwitchStatusListener() {
        @Override
        public void onSwitch(boolean state) {
            IS_PHOTO_MODE = !state;
            flg = false;
            if(state){
                //开启
                Toast.makeText(getApplicationContext(), "全景模式", Toast.LENGTH_SHORT).show();
            } else {
                //关闭
                Toast.makeText(getApplicationContext(), "拍摄模式", Toast.LENGTH_SHORT).show();
            }
        }
    };

}

