package com.leon.client.bean;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.leon.client.file.FileDigest;

/**
 * 客户端推送文件
 * 
 * 
 */
public class FileRequest {

	public static final int FILE_LEN = 4; // 文件名长度

	public static final int commandId = 0x00000002; // 4字节给消息头赋值
	
	private HeadMessage headMessage;
	
	
	public HeadMessage getHeadMessage() {
		return headMessage;
	}


	public void setHeadMessage(HeadMessage headMessage) {
		this.headMessage = headMessage;
	}


	public byte[] getMessage(File file){
		
		headMessage = new HeadMessage();
		byte[] title = file.getName().getBytes();
		int md5_length = 32;
		headMessage.setCommandId(commandId,true);
		int file_length = getFileLength(file).intValue();
		int total_length = md5_length+file_length+title.length+FILE_LEN+HeadMessage.HEAD_LEN;
		
		headMessage.setTotal_length(total_length);
	   
		byte[] head = headMessage.getHead();
		System.out.println("total_length is: "+total_length+"seq is: "+headMessage.getFile_seq());
		return head;
		/*bb.put(head);
		bb.put(integerToByte(title.length));
		bb.put(title);
		bb.flip();
		
		return bb.array();*/
		
	}
	public byte[] getNameInfo(File file){
		String file_MD5 = FileDigest.getMD5(file);
		byte[] title = file.getName().getBytes();
		byte[] file_info = new byte[4+title.length+32];
		byte[] title_length = integerToByte(title.length);
		byte[] file_MD5_array= file_MD5.getBytes();
		System.out.println("===== file_info length is:"+file_info.length+" md5_str length is: "+file_MD5.length());
		System.arraycopy(file_MD5_array, 0, file_info, 0, 32);
		System.arraycopy(title_length, 0, file_info, 32, 4);
		System.arraycopy(title, 0, file_info, 36, title.length);
		return file_info;
	}
	

	
	public Long getFileLength(File file) {
		return file.length();
	}

	/**
	 * 将压缩包放到bytebuffer中
	 */
	public ByteBuffer getFile(File file) {

		// byte[] FileByte = null;
		// byte[] bytes = new byte[(int) file.length()];
		ByteBuffer bb = ByteBuffer.allocate((int) file.length());
		try {
			FileInputStream fis = new FileInputStream(file);
			FileChannel fc = fis.getChannel();
			fc.read(bb);
			fc.close();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bb;
	}

	/**
	 * 
	 * 判断是否将文件写入缓冲区
	 * */
	public static void main(String[] args) {

		FileRequest request = new FileRequest();
		ByteBuffer bb = request.getFile(new File("d:\\index2.zip"));
		File file = new File("d:\\8.zip");

		try {
			FileOutputStream fos = new FileOutputStream(file);
			FileChannel fc = fos.getChannel();

			bb.flip();
			fc.write(bb);

			bb.clear();
			fc.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 将压缩包放到byte数组中
	 */
	public byte[] getFileByte(File file) {

		byte[] fileByte = null;
		byte[] bytes = new byte[30*1024];

		int readbytes=0;
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream in = new BufferedInputStream(fis);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*1024);
			while ((readbytes = in.read(bytes)) >0) {
				bos.write(bytes, 0, readbytes);
			}
			fileByte = bos.toByteArray();
			bos.close();
			in.close();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileByte;
	}

	public static byte[] integerToByte(int n) {
		byte b[] = new byte[4];
		b[0] = (byte) (n >> 24);
		b[1] = (byte) (n >> 16);
		b[2] = (byte) (n >> 8);
		b[3] = (byte) n;
		return b;
	}

}
