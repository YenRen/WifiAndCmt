package org.opencv.samples.tutorial2;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2016/12/28.
 */

class ControlServerThread extends Thread {
    private ServerSocket serverSocket;
    private Tutorial2Activity.MainHandler mMainHandler;
    private boolean run = true;

    public ControlServerThread(Tutorial2Activity.MainHandler mMainHandler) {
        this.mMainHandler = mMainHandler;
        try {
            serverSocket = new ServerSocket(6000);
        } catch (IOException e) {
            Log.i("Server", "服务器创建出错");
            e.printStackTrace();
        }
    }

    public void run() {
        while (run){
            try {
                Socket socket = this.serverSocket.accept();
                BufferedReader bufferedReader = SocketUtil.getBufferedReader(socket);
                String string = bufferedReader.readLine();
                Message message = new Message();
                message.what = 4;
                message.obj = string;
                mMainHandler.sendMessage(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close(){
        run = false;
        try {
            serverSocket.close();
        } catch (Exception e) {
            Log.i("Server", "服务器销毁出错");
            e.printStackTrace();
        }
    }
}
