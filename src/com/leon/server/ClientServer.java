package com.leon.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.leon.client.thread.FilePressThread;
import com.leon.client.thread.FileTransThread;

public class ClientServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties pro = new Properties();
			InputStream is = ClientServer.class.getClassLoader().getResourceAsStream("client.properties");
			//InputStream is = FileClient.class.getResourceAsStream("./client.properties");
			pro.load(is);
			/**
			 * 2.启动文件压缩线程
			 */
			new FilePressThread(pro.getProperty("srcPressPath"),pro.getProperty("destFilePath"),pro.getProperty("pressFilePath"),
					pro.getProperty("serverId"),pro.getProperty("protocal"),pro.getProperty("version"),
					Integer.parseInt(pro.getProperty("max_file_size")),Integer.parseInt(pro.getProperty("file_count"))).start();
			System.out.println(" zip src path is:" + pro.getProperty("pressFilePath"));
			System.out.println(" zip dest path is:" + pro.getProperty("pressFileDestPath"));
			/**
			 * 3.启动文件传输线程
			 */
			FileTransThread client = new FileTransThread(pro.getProperty("pressFilePath"),pro.getProperty("pressFileDestPath"),
					Integer.parseInt(pro.getProperty("TotalWide")),Integer.parseInt(pro.getProperty("LeaveWide")),
					Integer.parseInt(pro.getProperty("port")),Integer.parseInt(pro.getProperty("window_size")));
			client.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
