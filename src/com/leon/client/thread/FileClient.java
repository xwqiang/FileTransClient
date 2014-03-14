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
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import com.leon.client.bean.FileRequest;
import com.leon.client.bean.HeadMessage;
import com.leon.client.file.FileDeal;
import com.leon.client.net.NetFlow;

/**
 * socket客户端：
 * 扫描压缩文件目录，向服务器端发起连接请求，建立连接，传输文件。
 * */
public class FileClient extends Thread {

	private Logger log = Logger.getLogger("FileServer");
	private boolean running = false;
	private boolean check_running = false;
	private int has_connect = 0;// 当前连接数
	private int max_connect_count = 3;// 规定连接数限制
	long current_time = System.currentTimeMillis();
	private boolean notNull = false;// 压缩文件目录中是否有文件
	private String srcFilePath;// 压缩文件保存目录
	private String destFilePath;// 压缩文件备份目录
	private int totalWide;
	private int leaveWide;
	private long check_time = 0;
	private boolean flag = false;

	public FileClient(String srcFilePath, String destFilePath, int totalWide, int leaveWide) {
		this.totalWide = totalWide;
		this.leaveWide = leaveWide;
		this.srcFilePath = srcFilePath;
		this.destFilePath = destFilePath;
		check_running = true;
		new checkResponseTime().start();

	}

	@Override
	public void run() {
		Selector selector = null;
		FileDeal doFile = new FileDeal();
		try {
			selector = Selector.open();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SelectionKey key = null;
		while (true) {
			long time = System.currentTimeMillis();
			System.out.println("thread is still alive");
			/* 扫描压缩文件保存目录，获取压缩文件集合 */
			List<File> canDoFiles = null;
			try {
				canDoFiles = doFile.getFiles(srcFilePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
			int i = 0;
			System.out.println("--------file size:" + canDoFiles.size());
			// 如果当前没有文件，那么休眠2秒.
			if (canDoFiles.size() == 0) {
				try {
					System.out.println("暂时没有需要传输的文件");
					sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				continue;
			} else {
				notNull = true;
				running = true;
				flag = true;
			}
			// System.out.println("running check point 1: "+running);
			// 证明有文件需要传输
			if (notNull) {
				// 判断当前带宽
				// GetNewsThread news = new GetNewsThread();
				// String netLong = news.createNews();
				int netLong = NetFlow.getNetSize();
				/*
				 * System.out.println("current wide is:"+netLong);
				 * System.out.println("is can trans"+(totalWide-netLong > leaveWide));
				 * System.out.println("------------- leave:"+(totalWide-netLong)+"file leave wide"+leaveWide);
				 */
				// 如果总带宽-已经占用带宽大于要求的剩余带宽，则开始传输文件。
				// System.out.println("running check point 2: "+running);
				while (running) {
					try {
						// 判断当前连接数，如果存在连接，那么直接进行下面的操作，如果连接数为0那么证明连接不存在，建立连接，连接数加1
						// 断线重连也是跟这里有关系的。
						if (has_connect < max_connect_count) {
							selector = Selector.open();
							SocketChannel socketChannel = SocketChannel.open();
							socketChannel.configureBlocking(false);
							socketChannel.register(selector, SelectionKey.OP_CONNECT);
							socketChannel.connect(new InetSocketAddress("124.205.226.124", 3333));
							has_connect++;
							System.out.println("建立连接ok");
						}
						boolean abc = (selector.select() > 0);
						// System.out.println("running check point 3:  select is not empty:"+abc);
						if (abc) {
							// System.out.println("running check point 4: "+running);
							Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
							while (keys.hasNext()) {
								try {
									key = keys.next();
									SocketChannel socketChannel = (SocketChannel) key.channel();
									boolean isconnect = key.isConnectable();
									// System.out.println("running check point 8:  key is connectable: "+socketChannel.finishConnect());
									if (isconnect) {
										if (socketChannel.finishConnect()) {
											socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
											flag = true;
											continue;
										}
									}
									boolean isread = key.isReadable();
									// System.out.println("running check point 9:  key is read: "+isread);
									if (isread) {
										// System.out.println("running check point 5: key is readable ");
										ByteBuffer getBuffer = ByteBuffer.allocate(8);
										long times = 0;
										if (socketChannel.read(getBuffer) > 0) {
											HeadMessage head = new HeadMessage(getBuffer.array());
											// 读取服务器端返回的响应，判断响应类型。如果是8则为正确响应，进行正确的处理,移除文件，并做好备份操作。
											// 如果为6那么文件传输有问题，则进行文件重传操作。
											if (head.getCommandId() == 8) {
												boolean isOK = doFile.copyFile(canDoFiles.get(i - 1),destFilePath);
												canDoFiles.get(i - 1).renameTo(new File(destFilePath));
												// 文件传输正常，并且完成一次传输，那么可以进行下一个文件的传输，设置flag为true
												if (isOK) {
													System.out.println("传输文件成功: " + canDoFiles.get(i - 1).getName());
													times = System.currentTimeMillis();
													flag = true;
													System.out.println("trans a file cost time is: "
														    + (System.currentTimeMillis() - check_time));
													check_time = System.currentTimeMillis();
													sleep(100);
												}
												// 判断，如果文件集合中的文件全部传输完成，那么终止循环，进行下一次的文件扫描。
												if (i == canDoFiles.size()) {
													log.info("1000个文件传输完成的时间" + (System.currentTimeMillis() - time));
													System.out.println("&&&&&&&&&&&&&&&trans completed?");
													flag = false;
													running = false;
													// notNull = false;
													canDoFiles.clear();
												}
											} else if (head.getCommandId() == 6) {
												System.out.println("重新传输");
												i = i - 1;
												flag = true;
											}
										}
										// 判断服务器响应超时情况，如果超时断开连接，，需要重新建立连接
										long times2 = System.currentTimeMillis() - times;
										if (times2 > 10000) {
											System.out.println("------------------------------服务器相应超时");
											if (has_connect > 0)
												has_connect--;
											running = false;
											key.channel().close();
											key.cancel();
										}
									}
									boolean iswrite = key.isWritable();
									// System.out.println("running check point 10:  key is write: "+iswrite+"  flag is:"+ flag);
									if (iswrite && flag && (totalWide - netLong > leaveWide)) {
										System.out.println("running check point 6: key is writeable array size is: "
												+ canDoFiles.size());
										long aaa = System.currentTimeMillis();
										FileRequest request = new FileRequest();
										// ByteBuffer headBuffer = request.getMessage(canDoFiles.get(i));
										ByteBuffer contentByte = ByteBuffer.allocate(1024);
										ByteBuffer bb = ByteBuffer.allocate(contentByte.capacity());
										FileInputStream fis = new FileInputStream(canDoFiles.get(i));
										FileChannel fc = fis.getChannel();
										// bb.put(headBuffer);
										while (fc.read(contentByte) > 0) {
											contentByte.flip();
											bb.put(contentByte);
											bb.flip();
											socketChannel.write(bb);
											contentByte.clear();
											bb.clear();
											sleep(10);
										}
										System.out.println("write time is: " + (System.currentTimeMillis() - aaa));
										fc.close();
										fis.close();
										i++;
										flag = false;
										check_time = System.currentTimeMillis();
									}
								} catch (Exception e) {
									// 捕获异常，进行断线重连
									System.out.println("出错了");
									e.printStackTrace();
									if (has_connect > 0)
										has_connect--;
									running = false;
									key.channel().close();
									key.cancel();
								} finally {
									keys.remove();
								}
								sleep(1);
							}
						} else {
							try {
								sleep(1000);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			try {
				sleep(1000);
			} catch (Exception e) {
               // TODO
			}
		}

	}

	public void doStart() {
		this.running = true;
		start();
	}

	public void shutdown() {
		running = false;
		interrupt();
	}

	private class checkResponseTime extends Thread {
		public void run() {
			while (check_running) {
				if (System.currentTimeMillis() - check_time > 30000) {
					flag = true;
					System.out.println("======time out accored======");
					try {
						sleep(1000);
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}
		}

	}
	
	/*
	 * public static void main(String[] args) { try { Properties pro = new
	 * Properties(); InputStream is =
	 * FileClient.class.getResourceAsStream("/client.properties"); pro.load(is);
	 * System.out.println(pro.getProperty("srcPressPath")); FileClient client =
	 * new
	 * FileClient(pro.getProperty("srcPressPath"),pro.getProperty("destFilePath"
	 * )); client.start(); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } }
	 */

}
