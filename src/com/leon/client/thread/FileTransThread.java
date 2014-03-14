package com.leon.client.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.leon.client.bean.FileRequest;
import com.leon.client.bean.HeadMessage;
import com.leon.client.file.FileDeal;
import com.leon.client.file.FileDigest;
import com.leon.client.net.NetFlow;


public class FileTransThread extends Thread{
	private Logger log = Logger.getLogger("transErr");
	private boolean running = true;
	private int totalWide;
	private int leaveWide;
	private String srcFilePath;
	private String destFilePath;
	private int window_size;
	private boolean check_running;
	private int has_connect =0;//当前连接数
	private int max_connect_count=15;//规定连接数限制
	private int port;
	private HashMap<Integer,String> waitFileRespMap = new HashMap<Integer,String>();
	private HashMap<String,Long> has_put_file = new HashMap<String,Long>();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	public FileTransThread(String srcFilePath,String destFilePath,int totalWide,int leaveWide,int port,int window_size){
		this.totalWide=totalWide;
		this.leaveWide= leaveWide;
		this.srcFilePath = srcFilePath;
		this.destFilePath = destFilePath;
		this.port = port;
		this.window_size = window_size;
		check_running = true;
		new timeoutMapThread().start();
	}
	
	public void run(){
		FileDeal doFile = new FileDeal();
		Selector selector = null;
		SelectionKey key = null;
		System.out.println(this.getName()+"has start !!!");
			try {
				selector = Selector.open();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		while(true){
			List<File> canDoFiles = null;
			try{
				canDoFiles = doFile.getFiles(srcFilePath);//从源目录检索需要传送的文件列表
			}catch(Exception e){
				e.printStackTrace();
			}
			
			if(canDoFiles.size()>0){
				int i = 0;
				int m = 0;
		        this.running=true;
		        long last_transTime=0;
		        int current_net_flow =0;
		       // while(running&& (i <canDoFiles.size()||(i>=canDoFiles.size()&&waitFileRespMap.size()>0))){ 
		             //i <canDoFiles.size()表示i还小于本次检索出来的文件数量，
		        	//表明本次检索出来的文件仍然有需要传输的  i>=canDoFiles.size()&&waitFileRespMap.size()>0 表示i已经大于了本次检索文件数量，但是可能还有已经传输完成，有需要等待响应的交互。
		        while(running&&i <canDoFiles.size()){
		            current_net_flow =  NetFlow.getNetSize();
		        	System.out.println("current leave width :" + (totalWide-current_net_flow));
		        	try{
		        	if(has_connect<max_connect_count){
						
						SocketChannel socketChannel = SocketChannel.open();
				        socketChannel.configureBlocking(false);
				        socketChannel.register(selector,SelectionKey.OP_CONNECT);
				        //socketChannel.connect(new InetSocketAddress("124.127.184.89", port));
				        socketChannel.connect(new InetSocketAddress("192.168.5.225", port));
				        //socketChannel.connect(new InetSocketAddress("localhost", port));
				        has_connect++;
				        System.out.println("Establish Connection success! has_connect: "+ has_connect);
					}
		        	
		        	if(selector.select()>0){
		        	     
		        		Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
		        		while(keys.hasNext()){
		        			try{
		        				key = keys.next();
		        				if(key.attachment() == null){
									m++;
		        					key.attach(m);
		        					System.out.println(" key attch Object success! :"+m);
								}
		        				SocketChannel socketChannel = (SocketChannel) key.channel();
		        				if(key.isConnectable()){
		        					if (socketChannel.finishConnect()){
		        						socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		        			            	
		        						continue;
		        					}
		        				}
		        				sleep(10);
		        				if(key.isReadable()){
		        					
		        					ByteBuffer getBuffer = ByteBuffer.allocate(12);
		        					if(socketChannel.read(getBuffer)>0){
		        						HeadMessage head = new HeadMessage(getBuffer.array());
		        						//debugData("rev head byte array :",getBuffer.array());
		        						if(head.getCommandId()==8){
		        							try{
		        							String file_path = waitFileRespMap.remove(head.getFile_seq());
		        							System.out.println(" after response  get key from map key is :"+head.getFile_seq()+" value is : "+file_path);
		        							File file = new File(file_path.split("#")[0]);
		        							File patch_file = new File(destFilePath+sdf.format(System.currentTimeMillis())+"/");
		        							if(!patch_file.exists()){
		        								patch_file.mkdir();
		        							}
		        							System.out.println("================= total net cost time is: "+(System.currentTimeMillis()-Long.parseLong(file_path.split("#")[1])));
		        							boolean rmv = file.renameTo(new File(destFilePath+sdf.format(System.currentTimeMillis())+"/"+file.getName()));
		        							
		        							has_put_file.remove(file.getAbsolutePath());
		        							}catch(Exception e){
		        								e.printStackTrace();
		        							}
		        							}
		        						if(head.getCommandId()==6){
		        							String file_path = waitFileRespMap.remove(head.getFile_seq());
		        							File file = new File(file_path.split("#")[0]);
		        							log.info("file  trans error,path is: "+file.getAbsolutePath());
		        							has_put_file.remove(file.getAbsolutePath());
		        						}
		        						//socketChannel.register(selector,  SelectionKey.OP_WRITE);
		        					}
		        				}
		        				sleep(10);
		        				boolean abc = key.isWritable();
		        				//System.out.println("file resp map size is: "+waitFileRespMap.size());
		        				
		        				if(abc&& waitFileRespMap.size()<30&&(totalWide-current_net_flow>leaveWide)){
		        				//if(abc&& waitFileRespMap.size()<30&&(System.currentTimeMillis()-last_transTime>100)){
		        					 if(i<canDoFiles.size()){
		        						 if(!has_put_file.containsKey(canDoFiles.get(i).getAbsolutePath())&&canDoFiles.get(i).exists()){//加入这个判断是因为有缓存的存在，
		        							 System.out.println("**************has_put_file size is : "+has_put_file.size()+"*******************");
		        					 long aaa = System.currentTimeMillis();
		        					 System.out.println("file resp map size is: "+waitFileRespMap.size());
		        					 //System.out.println("  =========================== write key seq is: "+(Integer)key.attachment()+"---------------------------");
		        					 FileRequest request = new FileRequest();
		        					 byte[]  file_header = request.getMessage(canDoFiles.get(i));
		        					 byte[]  file_info = request.getNameInfo(canDoFiles.get(i));
		        					 ByteBuffer  buffer =  ByteBuffer.allocate(5*1024);
		        					 FileInputStream fis = new FileInputStream(canDoFiles.get(i));
  					    		     FileChannel fc = fis.getChannel();
						    		 buffer.put(file_header);
						    		 buffer.put(file_info);
						    		 buffer.flip();
						    		 socketChannel.write(buffer);
						    		 buffer.clear();
						    		  while(fc.read(buffer)>0){
						    			  buffer.flip();
						    			  socketChannel.write(buffer);
						    			  buffer.clear();
						    		  }
						    		 
						    		  waitFileRespMap.put(request.getHeadMessage().getFile_seq(), canDoFiles.get(i).getAbsolutePath()+"#"+System.currentTimeMillis());
						    		  System.out.println("============put sended file to map  key is:"+request.getHeadMessage().getFile_seq()+" value is : "+canDoFiles.get(i).getAbsolutePath()+"#"+System.currentTimeMillis());
						    		  has_put_file.put(canDoFiles.get(i).getAbsolutePath(), System.currentTimeMillis());
						    		   // fc.close();
						    		  fis.close();
						    		  i++;
						    		  last_transTime = System.currentTimeMillis();
						    		 // socketChannel.register(selector, SelectionKey.OP_READ );
						    		  System.out.println("write to socket stream cost time is: "+(System.currentTimeMillis()-aaa));
						    		  sleep(10);
		        					 }else if(has_put_file.containsKey(canDoFiles.get(i).getAbsolutePath())){
		        						 long put_time = has_put_file.get(canDoFiles.get(i).getAbsolutePath());
		        						 if(System.currentTimeMillis()-put_time>1000*60*3){
		        							 has_put_file.remove(canDoFiles.get(i).getAbsolutePath());
		        						 }
		        						 System.out.println("has this file in sending file map! array size is: "+canDoFiles.size()+" i: "+i+" has_put_file size is: "+has_put_file.size());
		        						 i++;
		        					 }else{
		        						 i++;
		        					 }
		        					 }
		        				}
		        				
		        			}catch(Exception e){
		        				e.printStackTrace();
		        				if(has_connect>0)
									 has_connect--;
								     key.channel().close();
									 key.cancel();
		        			}finally{
		        				
		        				keys.remove();
		        			}
		        		}
		        	}
		        	
		        }catch(Exception e){
		         e.printStackTrace();	
		        }
		        }
		        try{
		        	canDoFiles.clear();
		     
		        }catch(Exception e){
		        	e.printStackTrace();
		        }
			}else{
				System.out.println("no file need to trans!");
			    try {
					sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}  
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
	private class timeoutMapThread  extends Thread{
		public void run(){
			while(true){
			try{
				
				Set<Integer> set = waitFileRespMap.keySet();  
		        Iterator<Integer> its = set.iterator(); 
				while(its.hasNext()){
					Integer seq = its.next();
					
					
					String timeStr = waitFileRespMap.get(seq);
					
					long file_time = Long.parseLong(timeStr.split("#")[1]);
					if(System.currentTimeMillis()-file_time>90000){
						String[] value = timeStr.split("#");
						its.remove();
						has_put_file.remove(value[0]);
						System.out.println("remove from map ,key is : "+seq);
					}
				}
				sleep(2000);
			}catch(Exception e){
				e.printStackTrace();
			}
			try{
				sleep(2000);
			}catch(Exception e){
				e.printStackTrace();
			}
			}
		}
	}
}
