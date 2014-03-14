package com.leon.client.thread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.leon.client.file.FileDeal;


/**
 * �ļ�ѹ���̡߳����ļ���������DoFile���еķ�������
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
			
			// ��һ����ȡ�����ļ�
			list = doFile.getFiles(srcFilePath);
			// �ڶ������и��ݴ�С����ѹ����ѹ����ָ��Ŀ¼����socket�߳�ɨȡ��Ŀ¼
			// ������������ļ�����������ѹ����С�ļ��������ѹ��
			// ѹ���󣬽�ѹ�����ļ�����ת�Ʊ���
			if(list.size()<1){
				try {
					System.out.println("��ǰû����Ҫѹ�����ļ�");
					sleep(2000);
					continue;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (int i = 0; i < list.size(); i++) {
                  // System.out.println("--------------");
				// �ж��Ƿ�������޸ĵ��ļ�
                //isLastFile����һ��booleanֵ���ж��Ƿ����������߼�ʮ���ڱ��޸Ĺ�
				if (doFile.isLastFile(list.get(i))) {
					//�ж��ļ��Ĵ�С,���̫��Ļ��͵�������ѹ�����������ļ�����ѹ�������޸�
					if (list.get(i).length() > max_file_size  * 1024) {

						// ѹ����zipfilePath
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
						//����ļ��Ƚ�С����ô����ļ����浽��ʱС�ļ�����minList��
						minList.add(list.get(i));
						//�����ļ����֣�����ѹ���ļ������������ļ����ֵ�ԭ������Ϊ���tar�ļ���һ������ʽ
						//���ִ���ʽ���ǿ��Ժ����ļ�����·���ģ�ѹ����ʱ��ֻ��ѹ�����ļ�����������ڶ����ļ��е�������֡�
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
					//���ļ��������Ƴ���ǰ�ļ�
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
