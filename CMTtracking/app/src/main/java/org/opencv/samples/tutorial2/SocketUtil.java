package org.opencv.samples.tutorial2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

/***********************************************************************
 * 文件名称: SocketUtil.java
 * 内容说明: Socket工具
 * 创 建 人: 覃杰
 * 创建日期: 2015-03-13
 * 修改日期:2015-03-13
 * 版 本 号：V0.01
 ***********************************************************************/
public class SocketUtil {

	/*-------------------------------------------------------------------------
	函数名称: getBufferedReader
	使用功能: 从socket获得输入流
	输入参数: Socket
	输出参数: 无
	创建日期: 2015-03-18
	---------------------------------------------------------------------------*/
	public static BufferedReader getBufferedReader(Socket socket) {
		try {
			return new BufferedReader(
					new InputStreamReader(
							socket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/*-------------------------------------------------------------------------
	函数名称: getPrintWriter
	使用功能: 从socket获得输出流
	输入参数: Socket
	输出参数: 无
	创建日期: 2015-03-18
	---------------------------------------------------------------------------*/
	public static PrintWriter getPrintWriter(Socket socket) {
		try {
			return new PrintWriter(socket.getOutputStream(), true);
			//return new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "gbk"),true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/*-------------------------------------------------------------------------
	函数名称: close
	使用功能: 关闭输入流
	输入参数: Reader
	输出参数: 无
	创建日期: 2015-03-18
	---------------------------------------------------------------------------*/
	public static void close(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
				reader = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*-------------------------------------------------------------------------
	函数名称: close
	使用功能: 关闭输出流
	输入参数: Writer
	输出参数: 无
	创建日期: 2015-03-18
	---------------------------------------------------------------------------*/
	public static void close(Writer writer) {
		if (writer != null) {
			try {
				writer.close();
				writer = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*-------------------------------------------------------------------------
	函数名称: close
	使用功能: 关闭Socket连接
	输入参数: Socket
	输出参数: 无
	创建日期: 2015-03-18
	---------------------------------------------------------------------------*/
	public static void close(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}