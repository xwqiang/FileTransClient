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
 * socket�ͻ��ˣ�
 * ɨ��ѹ���ļ�Ŀ¼����������˷����������󣬽������ӣ������ļ���
 * */
public class FileClient extends Thread {

	private Logger log = Logger.getLogger("FileServer");
	private boolean running = false;
	private boolean check_running = false;
	private int has_connect = 0;// ��ǰ������
	private int max_connect_count = 3;// �涨����������
	long current_time = System.currentTimeMillis();
	private boolean notNull = false;// ѹ���ļ�Ŀ¼���Ƿ����ļ�
	private String srcFilePath;// ѹ���ļ�����Ŀ¼
	private String destFilePath;// ѹ���ļ�����Ŀ¼
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
			/* ɨ��ѹ���ļ�����Ŀ¼����ȡѹ���ļ����� */
			List<File> canDoFiles = null;
			try {
				canDoFiles = doFile.getFiles(srcFilePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
			int i = 0;
			System.out.println("--------file size:" + canDoFiles.size());
			// �����ǰû���ļ�����ô����2��.
			if (canDoFiles.size() == 0) {
				try {
					System.out.println("��ʱû����Ҫ������ļ�");
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
			// ֤�����ļ���Ҫ����
			if (notNull) {
				// �жϵ�ǰ����
				// GetNewsThread news = new GetNewsThread();
				// String netLong = news.createNews();
				int netLong = NetFlow.getNetSize();
				/*
				 * System.out.println("current wide is:"+netLong);
				 * System.out.println("is can trans"+(totalWide-netLong > leaveWide));
				 * System.out.println("------------- leave:"+(totalWide-netLong)+"file leave wide"+leaveWide);
				 */
				// ����ܴ���-�Ѿ�ռ�ô������Ҫ���ʣ�������ʼ�����ļ���
				// System.out.println("running check point 2: "+running);
				while (running) {
					try {
						// �жϵ�ǰ������������������ӣ���ôֱ�ӽ�������Ĳ��������������Ϊ0��ô֤�����Ӳ����ڣ��������ӣ���������1
						// ��������Ҳ�Ǹ������й�ϵ�ġ�
						if (has_connect < max_connect_count) {
							selector = Selector.open();
							SocketChannel socketChannel = SocketChannel.open();
							socketChannel.configureBlocking(false);
							socketChannel.register(selector, SelectionKey.OP_CONNECT);
							socketChannel.connect(new InetSocketAddress("124.205.226.124", 3333));
							has_connect++;
							System.out.println("��������ok");
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
											// ��ȡ�������˷��ص���Ӧ���ж���Ӧ���͡������8��Ϊ��ȷ��Ӧ��������ȷ�Ĵ���,�Ƴ��ļ��������ñ��ݲ�����
											// ���Ϊ6��ô�ļ����������⣬������ļ��ش�������
											if (head.getCommandId() == 8) {
												boolean isOK = doFile.copyFile(canDoFiles.get(i - 1),destFilePath);
												canDoFiles.get(i - 1).renameTo(new File(destFilePath));
												// �ļ������������������һ�δ��䣬��ô���Խ�����һ���ļ��Ĵ��䣬����flagΪtrue
												if (isOK) {
													System.out.println("�����ļ��ɹ�: " + canDoFiles.get(i - 1).getName());
													times = System.currentTimeMillis();
													flag = true;
													System.out.println("trans a file cost time is: "
														    + (System.currentTimeMillis() - check_time));
													check_time = System.currentTimeMillis();
													sleep(100);
												}
												// �жϣ�����ļ������е��ļ�ȫ��������ɣ���ô��ֹѭ����������һ�ε��ļ�ɨ�衣
												if (i == canDoFiles.size()) {
													log.info("1000���ļ�������ɵ�ʱ��" + (System.currentTimeMillis() - time));
													System.out.println("&&&&&&&&&&&&&&&trans completed?");
													flag = false;
													running = false;
													// notNull = false;
													canDoFiles.clear();
												}
											} else if (head.getCommandId() == 6) {
												System.out.println("���´���");
												i = i - 1;
												flag = true;
											}
										}
										// �жϷ�������Ӧ��ʱ����������ʱ�Ͽ����ӣ�����Ҫ���½�������
										long times2 = System.currentTimeMillis() - times;
										if (times2 > 10000) {
											System.out.println("------------------------------��������Ӧ��ʱ");
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
									// �����쳣�����ж�������
									System.out.println("������");
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
