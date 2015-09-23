package jp.ac.sojou.izumi.chikaken.xbee;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

public class XBeeSerialServerTamaireTest {

	int SERVER_PORT = 33334;
	private boolean isServerRunning;
	private ServerSocket serverSocekt;

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		XBeeSerialServerTamaireTest s = new XBeeSerialServerTamaireTest();
		s.startServer();

	}

	public void startServer() {
		final Random rand = new Random();

		while (true) {
			try {
				serverSocekt = new ServerSocket(SERVER_PORT);
				System.out.println("Waiting for connection");
				final Socket s = serverSocekt.accept();
				System.out.println("Connection established");
				serverSocekt.close();

				Thread th = new Thread() {
					private BufferedInputStream bis;
					private BufferedOutputStream bos;

					public void run() {
						try {
							// ヘッダ：0x31 メッセージカウンタ：0x8a-0xfe ボールのカウンタ：0x80-0x89
							// ターミナル：0xff
							// 0x31 0x8a 0x80 0x80 0x80 0x80 0x80 0x80 0x80 0x80
							// 0x80 0x80 0x80 0x80
							// 0xff

							byte[] buf = new byte[] { (byte) 0x31, (byte) 0x8a,
									(byte) 0x80, (byte) 0x80, (byte) 0x80,
									(byte) 0x80, (byte) 0x80, (byte) 0x80,
									(byte) 0x80, (byte) 0x80, (byte) 0x80,
									(byte) 0x80, (byte) 0x80, (byte) 0x80,
									(byte) 0xff };

							byte msgCount = 0;
							while (true) {
								Arrays.fill(buf, (byte)0x80);
								buf[0] = (byte)0x31;
								buf[buf.length-1] = (byte)0xff;
								buf[1] = (byte) (((byte) 0x80) | msgCount);
								msgCount++;
								
								buf[rand.nextInt(12) + 2] = (byte) 0x81;

								bis = new BufferedInputStream(
										s.getInputStream(), buf.length);
								bos = new BufferedOutputStream(
										s.getOutputStream(), buf.length);

								bos.write(buf, 0, buf.length);
								// bos.write(0x00);
								bos.flush();

								try {
									Thread.sleep(rand.nextInt(800) + 200);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}

							// bos.write("end".getBytes());
							// bos.flush();

						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								if (bis != null) {
									bis.close();
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
							try {
								if (bos != null) {
									bos.close();
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					};
				};
				th.start();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static byte r() {
		return (byte) ((byte) (Math.round(Math.random() * 127)) & 0x7f | 0x80);
	}
}
