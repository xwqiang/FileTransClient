package com.leon.client.thread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.leon.client.file.FileDeal;


/**
 * 文件压缩线程。。文件处理都调用DoFile类中的方法。。
 * */
public class FilePressThread extends Thread {

	private FileDeal doFile;

	private String srcFilePath;

	private String destFilePath;

	private String zipFilePath;
	
	private String serverId;
	
	private String protocal;
	
	private String version;

	private boolean running = false;
	
	private int max_file_size;
	
	private int file_count;

	private List<File> list = new ArrayList<File>();

	private List<File> minList = new ArrayList<File>();
	
	private List<String> minNameList = new ArrayList<String>();

	public FilePressThread(String srcFilePath, String destFilePath,String zipFilePath,
			String serverId,String protocal,String version ,int max_file_size ,int file_count) {

		doFile = new FileDeal();
		this.srcFilePath = srcFilePath;
		this.destFilePath = destFilePath;
		this.zipFilePath = zipFilePath;
		this.running = true;
		this.serverId = serverId;
		this.protocal = protocal;
		this.version = version ;
		this.file_count = file_count;
		this.max_file_size = max_file_size;
	}

	public void run() {

		while (running) {

			System.out.println("*************src_path is:"+srcFilePath);
			System.out.println("*************dest_path is:"+destFilePath);
			System.out.println("*************zip file path is:"+zipFilePath);
			
			// 第一步获取所有文件
			list = doFile.getFiles(srcFilePath);
			// 第二步进行根据大小分类压缩，压缩到指定目录，供socket线程扫取该目录
			// 规则是如果大文件的少量进行压缩，小文件多个进行压缩
			// 压缩后，将压缩的文件进行转移备份
			if(list.size()<1){
				try {
					System.out.println("当前没有需要压缩的文件");
					sleep(2000);
					continue;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (int i = 0; i < list.size(); i++) {
                  // System.out.println("--------------");
				// 判断是否是最近修改的文件
                //isLastFile返回一个boolean值，判断是否最近几秒或者几十秒内被修改过
				if (doFile.isLastFile(list.get(i))) {
					//判断文件的大小,如果太大的话就单个进行压缩，否则多个文件进行压缩，可修改
					if (list.get(i).length() > max_file_size  * 1024) {

						// 压缩到zipfilePath
						//System.out.println("######################signle press#######################");
						File[] files=doFile.pressAndMoveFile(new Object[] { list.get(i)},
								zipFilePath,srcFilePath,destFilePath,serverId,protocal,version);
						 try {
								sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						doFile.reNameToFile(files, destFilePath);
					} else {
						//如果文件比较小，那么多个文件保存到临时小文件集合minList中
						minList.add(list.get(i));
						//保存文件名字，用于压缩文件。。这里用文件名字的原因，是因为配合tar文件的一个处理方式
						//这种处理方式，是可以忽略文件绝对路径的，压缩的时候只是压缩的文件，而不会存在多重文件夹的情况出现。
						minNameList.add(list.get(i).getName());
						if (minList.size() > file_count||i == list.size()-1) {
							File[] files  = doFile.pressAndMoveFile(minList.toArray(), zipFilePath,srcFilePath,destFilePath,serverId,protocal,version);
							try {
								sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							doFile.reNameToFile(files, destFilePath);
							minNameList.clear();
							minList.clear();
							System.out.println("list has clear   list size is:"+minNameList.size());
							
						}
					}
				} else {
					//从文件集合中移除当前文件
					list.remove(i);
				}
			}
			list.clear();
			try {
				sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	public static void main(String[] args) {
		
		/*FilePressThread thread = new FilePressThread("","","/usr/local/filetest/aaa/");
		thread.start();*/
		
	}

}
