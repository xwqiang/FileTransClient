package com.leon.client.bean;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 消息头
 * */
public class HeadMessage {

	public static final int HEAD_LEN = 12;//消息头长度

	private int commandId; // 通过消息体给类型赋值

	private int total_length; // 4字节文件的大小长度，用来服务器建立缓冲区设置大小
	
	private static int file_seq;
	
	private static int squence_index;
	private final int MIN_SEQ = 0;
	private final int MAX_SEQ = 0x7fffffff;

	private static Lock lock = new ReentrantLock(); 
	
	
	private void createSequence(){
		lock.lock();
		try{
			if(squence_index == MAX_SEQ){
				squence_index = MIN_SEQ;
			}else{
				squence_index++;
			}
			file_seq = squence_index;
		}finally{
			lock.unlock();
		}
	}
	

	public int getFile_seq() {
		return file_seq;
	}

	public void setFile_seq(int file_seq) {
		this.file_seq = file_seq;
	}

	public int getTotal_length() {
		return total_length;
	}

	public void setTotal_length(int total_length) {
		System.out.println();
		this.total_length = total_length;
	}

	public int getCommandId() {
		return commandId;
	}
	
	




	public void setCommandId(int commandId,boolean isCreateSeq) {
		this.commandId = commandId;
		if(isCreateSeq){
			createSequence();
		}
	}

	public HeadMessage() {}
	
    /**
     * 解析消息头
     * */
	public HeadMessage(byte[] buffer) {
		
		this.commandId = byte4ToInteger(buffer, 0);
		this.total_length = byte4ToInteger(buffer, 4);
		this.file_seq = byte4ToInteger(buffer,8);
	}
    /**
     * 获取消息头
     * */
	public byte[] getHead1() {
		byte[] bytes = new byte[HEAD_LEN];
		byte[] byteId = integerToByte(commandId);
		byte[] bytetotal = integerToByte(total_length);
		byte[] byteseq = integerToByte(file_seq);
		
		System.arraycopy(byteId, 0, bytes, 0, 4);
		System.arraycopy(bytetotal, 0, bytes, 4, 4);
		System.arraycopy(byteseq, 0, bytes, 8, 4);
		return bytes;
	}
	/*public ByteBuffer getHead() {
		ByteBuffer buffer = ByteBuffer.allocate(HEAD_LEN);
		buffer.putInt(commandId);
		buffer.putInt(total_length);
		buffer.putInt(file_seq);
		
		return buffer;
	}*/
	public byte[] getHead() {
		byte[] head = new byte[12];
		byte[] command_id  = integerToByte(commandId); 
		byte[] length = integerToByte(total_length);
		byte[] seq = integerToByte(file_seq);
		System.arraycopy(command_id, 0, head, 0, 4);
		System.arraycopy(length, 0, head, 4, 4);
		System.arraycopy(seq, 0, head, 8, 4);
		return head;
		
	}
    /**
     * 整型转字节
     * */
	public static byte[] integerToByte(int n) {
		byte b[] = new byte[4];
		b[0] = (byte) (n >> 24);
		b[1] = (byte) (n >> 16);
		b[2] = (byte) (n >> 8);
		b[3] = (byte) n;
		return b;
	}
    /**
     * 字节转整型
     * */
	public static int byte4ToInteger(byte[] b, int offset) {
		return (0xff & b[offset]) << 24 | (0xff & b[offset + 1]) << 16
				| (0xff & b[offset + 2]) << 8 | (0xff & b[offset + 3]);
	}
	
	public static void debugData(String desc,byte[] data){
		System.out.println("消息总长:"+data.length +" "+desc);		
		int count=0;
	      for(int i=0;i<data.length;i++){
	    	 int b=data[i];
	    	  if(b<0){b+=256;}
	    	 String hexString= Integer.toHexString(b);
	hexString = (hexString.length() == 1) ? "0" + hexString : hexString;
	    	 System.out.print(hexString+"  ");
	    	 count++;
	    	 if(count%4==0){
	    		 System.out.print( "  ");
	    	 }
	    	 if(count%16==0){
	    		 System.out.println();
	    	 }
	      }
	      System.out.println();
    }
}
