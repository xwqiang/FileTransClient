package com.leon.client.net;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 * 
 * 
 * 为了不等待每次netflow脚本运行完之后获取值，
 * 
 * 改成了。。读取写入保存该值的文件
 */
public class NetFlow  {

	private static String url="/var/tmp/rs"; // 保存带宽值的文件的文件路径
   

	public static int getNetSize() {

		File file = new File(url);
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));

			String str = null;
			int netLength  = 0;
			while ((str = dis.readLine()) != null) {
				netLength = Integer.parseInt(str);
			}
			dis.close();
			
			
            return netLength;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			
		}

		return 0;
	}
	


}
