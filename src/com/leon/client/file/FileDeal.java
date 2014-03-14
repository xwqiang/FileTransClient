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
 * 文件处理： FileDeal
 * @author 1207265
 *
 */
public class FileDeal {
	
	private Logger log = Logger.getLogger("pressFile");
	private List<File> list = new ArrayList<File>();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private int count=0;
	
	/**
	 * 根据目录获取目录下的文件： 递归循环，防止文件夹下包含子文件夹。
	 * @filesPath  文件目录并不是文件路径
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
	 * 删除文件：file.delete()
     * public boolean delete()
     * 删除此抽象路径名表示的文件或目录。如果此路径名表示一个目录，则此目录必须为空才能删除。
     * 返回：当且仅当成功删除文件或目录时，返回 true；否则返回 false。
     * 抛出：SecurityException - 如果存在安全管理器，且其 SecurityManager.checkDelete(java.lang.String) 方法拒绝对文件进行删除访问。
	 * */
	public void deleteFile(String filePath) {
		try {
			File file = new File(filePath);
			file.delete();
		} catch (Exception e) {
			System.out.println("删除文件失败");
		}
	}

	/**
	 * copy方式移动文件：
	 * 这种方式跟renameTo方式相比， renameTo方式是如果目标存在，本地目录不会变动，这样就容易重复传输。
	 * 所以需要改成这种方式，在我copy完之后，调用delete将本目录下的该文件进行删除。
	 * File.separator：
     * public static final String separator
     * 与系统有关的默认名称分隔符，出于方便考虑，它被表示为一个字符串。此字符串只包含一个字符，即 separatorChar。
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
			// 该不该删除srcFile.delete()？？？
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("没有查到该文件");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("读取有错误");
			e.printStackTrace();
		}
	}

	/**
	 * copy方式移动文件：
	 * 这种方式跟renameTo方式相比， renameTo方式是如果目标存在，本地目录不会变动，这样就容易重复传输。
	 * 所以需要改成这种方式，在我copy完之后，调用delete将本目录下的该文件进行删除。
	 * File.separator：
     * public static final String separator
     * 与系统有关的默认名称分隔符，出于方便考虑，它被表示为一个字符串。此字符串只包含一个字符，即 separatorChar。
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
			System.out.println("没有查到该文件");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("读取有错误");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * copy方式移动文件：
	 * 这种方式跟renameTo方式相比， renameTo方式是如果目标存在，本地目录不会变动，这样就容易重复传输。
	 * 所以需要改成这种方式，在我copy完之后，调用delete将本目录下的该文件进行删除。
	 * 
	 * pro.getInputStream():
     * public abstract InputStream getInputStream()
     * 获得子进程的输入流。输入流获得由该 Process 对象表示的进程的标准输出流。
     * 实现注意事项：对输入流进行缓冲是一个好主意。
     * 返回：连接到子进程正常输出的输入流。
	 * */
	public boolean copyFile(Object[] files, String disFilePath) {
		try {
			File temp = null;
			for (int i = 0; i < files.length; i++) {
				temp = (File) files[i];
				String cmd = " mv " + temp.getAbsolutePath() + " " + disFilePath + "_" + temp.getName();
				System.out.println(cmd);
				Process pro = Runtime.getRuntime().exec(cmd);// 执行linux命令
				BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
				if (br.readLine() != null) {
					return true;
				}
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("没有查到该文件");
			e.printStackTrace();
			return false;
		}
	}
      
	/**
	 * Windows window 调用winrar软件的命令进行压缩测试。。
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
			System.out.println("文件压缩失败");
			e.printStackTrace();
		} finally {
			pro.destroy();
		}
		return false;
	}
	
	/**
	 * 压缩并移动文件
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
  	 * 用于文件夹内有子文件夹的情况使用，但是处理完成后最终还是会到一个目录下，
  	 * 破坏原来的分日期存储的结构
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
	 * Linux 调用linux tar命令进行压缩文件 files是多个文件可能是几个， 也可能是几十个，看文件大小而定。
	 * targetPath 是要压缩到的指定目录。
	 * files数组必须只是文件名，因为-c已经切换到该目录下，需要的只是文件名即可，如果还是绝对路径的话，-c就起不到作用。
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
			System.out.println("压缩文件失败");
			e.printStackTrace();
		}
	}
	
	/**
	 * renameTo方式处理多个文件
	 * 处理文件：files被压缩完成后，要进行剪切操作。从当前文件夹下删除，移动到另一个文件夹下。
	 * 不好的地方：如果目标存在，那么就不会进行copy（一般用时间命名避免文件名重复）
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
	 * renameTo方式处理单个文件
	 * 处理文件：files被压缩完成后，要进行剪切操作。从当前文件夹下删除，移动到另一个文件夹下。
	 * 不好的地方：如果目标存在，那么就不会进行copy
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
	 * 判断该文件是否最近30秒内被修改过，
	 * 如果修改过，证明这个文件正在被写入，或者被其他操作，暂时不压缩
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
	
	//测试
	public static void main(String[] args) {
	 // TODO
	}
	
}
