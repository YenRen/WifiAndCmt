package org.opencv.samples.tutorial2;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Administrator on 2016/12/27.
 */

class MyThread extends Thread {
    private Mat mat;
    private String ipname;

    public MyThread(Mat mat, String ipname) {
        this.mat = mat;
        this.ipname = ipname;
    }

    public void run() {
        Log.i("sendimage", "发送图片线程1");
        try {
            Log.i("sendimage", "发送图片线程2");

            Bitmap bmp = Bitmap.createBitmap(mat.width(), mat.height(),
                    Bitmap.Config.RGB_565);
            Utils.matToBitmap(mat, bmp);
            Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, 640, 360, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp2.compress(Bitmap.CompressFormat.JPEG, 50, baos);

            DatagramSocket udpSocket = new DatagramSocket();
            InetAddress local = InetAddress.getByName(ipname);
            DatagramPacket p = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, local, 6000);
            udpSocket.send(p);
            Log.i("sendimage", "发送图片数据长度："+baos.toByteArray().length);
        } catch (IOException e) {
            Log.i("sendimage", "发送图片线程3");
            e.printStackTrace();
        }
    }
}