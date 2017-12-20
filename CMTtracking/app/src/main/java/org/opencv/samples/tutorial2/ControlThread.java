package org.opencv.samples.tutorial2;

import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Administrator on 2016/12/28.
 */

class ControlThread extends Thread {
    private String controlData;
    private String ipname;

    public ControlThread(String controlData, String ipname) {
        this.controlData = controlData;
        this.ipname = ipname;
    }

    public void run() {
        try {
            // 将图像数据通过Socket发送出去
            Socket socket = new Socket(ipname, 6000);

            PrintWriter printWriter = SocketUtil.getPrintWriter(socket);
            printWriter.println(controlData);
            printWriter.flush();
            printWriter.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
