package com.leon.client.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileDigest {
	
	/**
	   * 获取单个文件的MD5值！
	   * @param file
	   * @return
	   */
	  public static String getFileMD5(File file) {
	    if (!file.isFile()){
	      return null;
	    }
	    MessageDigest digest = null;
	    FileInputStream in=null;
	    byte buffer[] = new byte[1024];
	    int len;
	    try {
	      digest = MessageDigest.getInstance("MD5");
	      in = new FileInputStream(file);
	      while ((len = in.read(buffer, 0, 1024)) != -1) {
	        digest.update(buffer, 0, len);
	      }
	      in.close();
	    } catch (Exception e) {
	      e.printStackTrace();
	      return null;
	    }
	    BigInteger bigInt = new BigInteger(1, digest.digest());
	    return bigInt.toString(16);
	  }
	  
	  
	  public static String getMD5(File file) {
	        FileInputStream fis = null;
	        try {
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            fis = new FileInputStream(file);
	            byte[] buffer = new byte[8192];
	            int length = -1;
	            
	            while ((length = fis.read(buffer)) != -1) {
	                md.update(buffer, 0, length);
	            }
	            
	            return bytesToString(md.digest());
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        } catch (NoSuchAlgorithmException ex) {
	            ex.printStackTrace();
	        } finally {
	            try {
	                fis.close();
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	        return null;
	    }
	  
	  public static String bytesToString(byte[] data) {
	        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
	                'e', 'f'};
	        char[] temp = new char[data.length * 2];
	        for (int i = 0; i < data.length; i++) {
	            byte b = data[i];
	            temp[i * 2] = hexDigits[b >>> 4 & 0x0f];
	            temp[i * 2 + 1] = hexDigits[b & 0x0f];
	        }
	        return new String(temp);

	    }

}
