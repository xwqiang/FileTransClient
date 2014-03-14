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
 * Ϊ�˲��ȴ�ÿ��netflow�ű�������֮���ȡֵ��
 * 
 * �ĳ��ˡ�����ȡд�뱣���ֵ���ļ�
 */
public class NetFlow  {

	private static String url="/var/tmp/rs"; // �������ֵ���ļ����ļ�·��
   

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
