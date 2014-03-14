package com.leon.client.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
/**
 * �ļ����� FileDeal
 * @author 1207265
 *
 */
public class FileDeal {
	
	private Logger log = Logger.getLogger("pressFile");
	private List<File> list = new ArrayList<File>();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private int count=0;
	
	/**
	 * ����Ŀ¼��ȡĿ¼�µ��ļ��� �ݹ�ѭ������ֹ�ļ����°������ļ��С�
	 * @filesPath  �ļ�Ŀ¼�������ļ�·��
	 * */
	public List<File> getFiles(String filesPath) {
		try {
			File file = new File(filesPath);
			File[] files = file.listFiles();
			for (int i = 0; files!=null && i < files.length; i++) {
				if (files[i].isFile()) {
					list.add(files[i]);
				} else if (files[i].isDirectory()) {
					getFiles(files[i].getPath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * ɾ���ļ���file.delete()
     * public boolean delete()
     * ɾ���˳���·������ʾ���ļ���Ŀ¼�������·������ʾһ��Ŀ¼�����Ŀ¼����Ϊ�ղ���ɾ����
     * ���أ����ҽ����ɹ�ɾ���ļ���Ŀ¼ʱ������ true�����򷵻� false��
     * �׳���SecurityException - ������ڰ�ȫ������������ SecurityManager.checkDelete(java.lang.String) �����ܾ����ļ�����ɾ�����ʡ�
	 * */
	public void deleteFile(String filePath) {
		try {
			File file = new File(filePath);
			file.delete();
		} catch (Exception e) {
			System.out.println("ɾ���ļ�ʧ��");
		}
	}

	/**
	 * copy��ʽ�ƶ��ļ���
	 * ���ַ�ʽ��renameTo��ʽ��ȣ� renameTo��ʽ�����Ŀ����ڣ�����Ŀ¼����䶯�������������ظ����䡣
	 * ������Ҫ�ĳ����ַ�ʽ������copy��֮�󣬵���delete����Ŀ¼�µĸ��ļ�����ɾ����
	 * File.separator��
     * public static final String separator
     * ��ϵͳ�йص�Ĭ�����Ʒָ��������ڷ��㿼�ǣ�������ʾΪһ���ַ��������ַ���ֻ����һ���ַ����� separatorChar��
	 * */
	public void copyFile(String srcFilePath, String disFilePath) {
		try {
			File srcFile = new File(srcFilePath);
			ByteBuffer bb = ByteBuffer.allocate(1024);
			FileInputStream fis = new FileInputStream(srcFile);
			FileChannel fc = fis.getChannel();
			File disFile = new File(disFilePath + File.separator + srcFile.getName());
			FileOutputStream fos = new FileOutputStream(disFile);
			FileChannel dfc = fos.getChannel();
			while (fc.read(bb) > 0) {
				bb.flip();
				dfc.write(bb);
				bb.clear();
			}
			dfc.close();
			fos.close();
			fc.close();
			fis.close();
			// �ò���ɾ��srcFile.delete()������
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("û�в鵽���ļ�");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("��ȡ�д���");
			e.printStackTrace();
		}
	}

	/**
	 * copy��ʽ�ƶ��ļ���
	 * ���ַ�ʽ��renameTo��ʽ��ȣ� renameTo��ʽ�����Ŀ����ڣ�����Ŀ¼����䶯�������������ظ����䡣
	 * ������Ҫ�ĳ����ַ�ʽ������copy��֮�󣬵���delete����Ŀ¼�µĸ��ļ�����ɾ����
	 * File.separator��
     * public static final String separator
     * ��ϵͳ�йص�Ĭ�����Ʒָ��������ڷ��㿼�ǣ�������ʾΪһ���ַ��������ַ���ֻ����һ���ַ����� separatorChar��
	 * */
	public boolean copyFile(File srcFile, String disFilePath) {
		try {
			ByteBuffer bb = ByteBuffer.allocate(1024);
			FileInputStream fis = new FileInputStream(srcFile);
			FileChannel fc = fis.getChannel();
			File disFile = new File(disFilePath + File.separator + srcFile.getName());
			FileOutputStream fos = new FileOutputStream(disFile);
			FileChannel dfc = fos.getChannel();
			while (fc.read(bb) > 0) {
				bb.flip();
				dfc.write(bb);
				bb.clear();
			}
			dfc.close();
			fos.close();
			fc.close();
			fis.close();
			srcFile.delete();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("û�в鵽���ļ�");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("��ȡ�д���");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * copy��ʽ�ƶ��ļ���
	 * ���ַ�ʽ��renameTo��ʽ��ȣ� renameTo��ʽ�����Ŀ����ڣ�����Ŀ¼����䶯�������������ظ����䡣
	 * ������Ҫ�ĳ����ַ�ʽ������copy��֮�󣬵���delete����Ŀ¼�µĸ��ļ�����ɾ����
	 * 
	 * pro.getInputStream():
     * public abstract InputStream getInputStream()
     * ����ӽ��̵�������������������ɸ� Process �����ʾ�Ľ��̵ı�׼�������
     * ʵ��ע����������������л�����һ�������⡣
     * ���أ����ӵ��ӽ��������������������
	 * */
	public boolean copyFile(Object[] files, String disFilePath) {
		try {
			File temp = null;
			for (int i = 0; i < files.length; i++) {
				temp = (File) files[i];
				String cmd = " mv " + temp.getAbsolutePath() + " " + disFilePath + "_" + temp.getName();
				System.out.println(cmd);
				Process pro = Runtime.getRuntime().exec(cmd);// ִ��linux����
				BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
				if (br.readLine() != null) {
					return true;
				}
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("û�в鵽���ļ�");
			e.printStackTrace();
			return false;
		}
	}
      
	/**
	 * Windows window ����winrar������������ѹ�����ԡ���
	 */
	public boolean pressWinFiles(Object[] files, String targetPath) {
		count++;
		targetPath = targetPath + count + ".rar";
		Process pro = null;
		String cmd = "D:\\Program Files\\WinRAR\\WinRAR.exe" + " a "
               + "-ibck -ep1 " + targetPath + " " + StringUtils.join(files, " ");
		try {
			pro = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			if (br.readLine() != null) {
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("�ļ�ѹ��ʧ��");
			e.printStackTrace();
		} finally {
			pro.destroy();
		}
		return false;
	}
	
	/**
	 * ѹ�����ƶ��ļ�
	 * @param files
	 * @param targetPath
	 * @param srcFilePath
	 * @param destFilePath
	 * @param serverId
	 * @param protocal
	 * @param version
	 * @return
	 */
	public File[] pressAndMoveFile(Object[] files, String targetPath,
			String srcFilePath, String destFilePath, String serverId,
			String protocal, String version) {
		File[] tmpFiles = new File[files.length];
		File file = null;
		File tempFile = null;
		String[] fileNames = new String[files.length];
		try {
			for (int i = 0; i < files.length; i++) {
				tempFile = (File) files[i];
				if (!tempFile.getName().startsWith(serverId + protocal + version + "_")) {
					file = new File(tempFile.getPath().substring(0,tempFile.getPath().lastIndexOf("/") + 1)
							+ serverId + protocal + version + "_" + tempFile.getName());
					System.out.println("change name result:" + tempFile.renameTo(file));
				} else {
					file = tempFile;
				}
				tmpFiles[i] = file;
				fileNames[i] = file.getName();
			}
			// zipFile(tmpFiles, targetPath);
			targetPath = targetPath + serverId + System.currentTimeMillis() + ".tar.gz";
			String cmd = "tar cvfz " + targetPath + " -C  " + srcFilePath + "   " + StringUtils.join(fileNames, " ");
			System.out.println("press file cmd is: " + cmd);
//			log.info("press file cmd is: " + cmd);
			Process pro = Runtime.getRuntime().exec(cmd);
			pro.waitFor();
			BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			if (br.readLine() != null) {
				System.out.println("++++++++++++++++++++++tar success++++++++++++++++++++++++");
				pro.destroy();
				br.close();
			}
			/*
			 * for(int m = 0 ;m<tmpFiles.length;m++){ File file_mv = new
			 * File(destFilePath+sdf.format(System.currentTimeMillis())+"/");
			 * if(!file_mv.exists()){ file_mv.mkdir(); }
			 * System.out.println("press end and mv result :"
			 * +tmpFiles[m].renameTo(new
			 * File(destFilePath+sdf.format(System.currentTimeMillis
			 * ())+"/"+tmpFiles[m].getName()))); }
			 */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmpFiles;
	}
  	
  	/**
  	 * �����ļ����������ļ��е����ʹ�ã����Ǵ�����ɺ����ջ��ǻᵽһ��Ŀ¼�£�
  	 * �ƻ�ԭ���ķ����ڴ洢�Ľṹ
  	* @Title: pressAndMoveFileByRealPath 
  	* @Description: TODO
  	* @param @param files
  	* @param @param targetPath
  	* @param @param srcFilePath
  	* @param @param destFilePath
  	* @param @param serverId
  	* @param @param protocal
  	* @param @param version  
  	* @return void 
  	* @throws
  	 */
	public void pressAndMoveFileByRealPath(Object[] files, String targetPath,
			String srcFilePath, String destFilePath, String serverId,
			String protocal, String version) {
		File[] tmpFiles = new File[files.length];
		File file = null;
		File tempFile = null;
		String[] fileNames = new String[files.length];
		StringBuffer cmd_str = new StringBuffer("tar cvfz " + targetPath + " ");
		try {
			for (int i = 0; i < files.length; i++) {
				tempFile = (File) files[i];
				if (!tempFile.getName().startsWith(serverId + protocal + version + "_")) {
					file = new File(tempFile.getPath() + serverId + protocal + version + "_" + tempFile.getName());
					System.out.println("change name result:" + tempFile.renameTo(file));
				} else {
					file = tempFile;
					// System.out.println("Not change name result:"+tempFile.renameTo(file));
				}
				tmpFiles[i] = file;
				cmd_str.append(file.getAbsolutePath() + " ");
				fileNames[i] = file.getName();
			}
			targetPath = targetPath + System.currentTimeMillis() + ".tar.gz";
			// String cmd = "tar cvfz " + targetPath +
			// " -C  "+srcFilePath+"   "+ StringUtils.join(fileNames, " ");
			System.out.println("press command :" + cmd_str.toString());
			Process pro = Runtime.getRuntime().exec(cmd_str.toString());
			BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			if (br.readLine() != null) {
				System.out.println("++++++++++++++++++++tar success++++++++++++++++++++++++");
			}
			for (int m = 0; m < tmpFiles.length; m++) {
				System.out.println("press end and mv result :"
						+ tmpFiles[m].renameTo(new File(destFilePath + tmpFiles[m].getName())));
			}
			pro.destroy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	}
	
      /*public void zipFile(File[] files,String targetPath) throws FileNotFoundException,IOException{
  		ZipOutputStream  zos = new ZipOutputStream(new FileOutputStream(targetPath+System.currentTimeMillis()+".zip"));
  		FileInputStream fin = null;
  		for(int i = 0  ; i  < files.length ; i++){
  			ZipEntry zip = new ZipEntry(files[i].getName());
  			zos.putNextEntry(zip);
  			fin = new FileInputStream(files[i]);
  			byte[] buffer = new byte[2048];
  			int len = 0;
  			while((len=fin.read(buffer))!=-1){
  				zos.write(buffer, 0, len);
  				zos.flush();
  			}
  		}
  		zos.close();
  		fin.close();
  	}
  	
  	public void tarFile(File[] files,String targetPath) throws Exception {
  		String file_str = targetPath+System.currentTimeMillis()+".zip";
  		GzipCompressorOutputStream gos = new GzipCompressorOutputStream(new FileOutputStream(file_str));
  		for(int i = 0  ; i  < files.length ; i++){
  	  		FileInputStream fin = null;
  			fin = new FileInputStream(files[i]);
  			byte[] buffer = new byte[1024];
  			int len = 0;
  			while((len=fin.read(buffer))!=-1){
  				gos.write(buffer, 0, len);
  				gos.flush();
  			}
  			gos.close();
  	  		fin.close();
  		}
  	}
  	
  	public void  utar(File file,String targetPath)throws Exception {
  		GzipCompressorInputStream  gis = new GzipCompressorInputStream(new FileInputStream(file));
  		int count = 0;
  		byte[] buffer = new byte[1024];
  		while((count = gis.read(buffer, 0, 1024))!=-1){
  			
  		}
  	}*/
  	
	/**
	 * Linux ����linux tar�������ѹ���ļ� files�Ƕ���ļ������Ǽ����� Ҳ�����Ǽ�ʮ�������ļ���С������
	 * targetPath ��Ҫѹ������ָ��Ŀ¼��
	 * files�������ֻ���ļ�������Ϊ-c�Ѿ��л�����Ŀ¼�£���Ҫ��ֻ���ļ������ɣ�������Ǿ���·���Ļ���-c���𲻵����á�
	 */
	public void pressFiles(Object[] files, String targetPath,
			String srcFilePath, String destFilePath, String serverId,
			String protocal, String version) {
		String[] tmpFiles = new String[files.length];
		System.out.println("***************serverId: " + serverId
				+ " protocal: " + protocal + " version : " + version
				+ "****************");
		for (int i = 0; i < tmpFiles.length; i++) {
			String cmd = " mv " + srcFilePath + (String) files[i] + " "
					+ srcFilePath + serverId + protocal + version + "_"
					+ (String) files[i];
			System.out.println(cmd);
			try {
				Process pro = Runtime.getRuntime().exec(cmd);
				tmpFiles[i] = serverId + protocal + version + "_" + (String) files[i];
				pro.destroy();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("*****************************end*****************************");
		targetPath = targetPath + System.currentTimeMillis() + ".tar.gz";
		String cmd = "tar cvfz " + targetPath + " -C  " + srcFilePath + "   " + StringUtils.join(tmpFiles, " ");
		StringBuffer buffer = new StringBuffer("");
		buffer.append("mv ");
		for (int i = 0; i < tmpFiles.length; i++) {
			buffer.append(srcFilePath + tmpFiles[i] + " ");
		}
		buffer.append(" " + destFilePath);
		System.out.println(buffer.toString());
		try {
			// System.out.println(cmd);
			Process pro = Runtime.getRuntime().exec(cmd);
			Process pro1 = Runtime.getRuntime().exec(buffer.toString());
			BufferedReader br = new BufferedReader(new InputStreamReader(pro1.getInputStream()));
			if (br.readLine() != null) {
				System.out.println("+++++++++++++++++++mv success++++++++++++++++++++++");
			}
			pro.destroy();
			pro1.destroy();
		} catch (IOException e) {
			System.out.println("ѹ���ļ�ʧ��");
			e.printStackTrace();
		}
	}
	
	/**
	 * renameTo��ʽ�������ļ�
	 * �����ļ���files��ѹ����ɺ�Ҫ���м��в������ӵ�ǰ�ļ�����ɾ�����ƶ�����һ���ļ����¡�
	 * ���õĵط������Ŀ����ڣ���ô�Ͳ������copy��һ����ʱ�����������ļ����ظ���
	 */
	public void reNameToFile(File[] files, String targetPath) {
		for (int m = 0; m < files.length; m++) {
			File file_mv = new File(targetPath + sdf.format(System.currentTimeMillis()) + "/");
			if (!file_mv.exists()) {
				file_mv.mkdir();
			}
			boolean mv_ret = files[m].renameTo(new File(targetPath
					+ sdf.format(System.currentTimeMillis()) + "/"
					+ files[m].getName()));
			if (!mv_ret) {
				System.out.println("mv fail & path is :" + files[m].getAbsolutePath());
				if (new File(
						"/usr/local/smgpSend/smgpSendWriteFile/file_info_send_matchDisposed/"
						+ sdf.format(System.currentTimeMillis()) + "/" + files[m].getName()).exists()) {
					files[m].renameTo(new File("/usr/local/trans_err/send/" + files[m].getName()));
				}
			}
		}
	}
	
	/**
	 * renameTo��ʽ�������ļ�
	 * �����ļ���files��ѹ����ɺ�Ҫ���м��в������ӵ�ǰ�ļ�����ɾ�����ƶ�����һ���ļ����¡�
	 * ���õĵط������Ŀ����ڣ���ô�Ͳ������copy
	 */
	public boolean reNameToFile(File file, String targetPath) {
		File file2;
		try {
			file2 = new File(targetPath + file.getName());
			file.renameTo(file2);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * �жϸ��ļ��Ƿ����30���ڱ��޸Ĺ���
	 * ����޸Ĺ���֤������ļ����ڱ�д�룬���߱�������������ʱ��ѹ��
	 * */
	public boolean isLastFile(File files) {
		long doFileTime = 0;
		doFileTime = files.lastModified();
		long time = System.currentTimeMillis() - doFileTime;
		if (time > 30000) {
			return true;
		}
		return false;
	}
	
	//����
	public static void main(String[] args) {
	 // TODO
	}
	
}
