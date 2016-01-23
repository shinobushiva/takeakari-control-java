package jp.ac.sojou.izumi.chikaken.xbee;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class XBeeSerialServerTest {

	public static void main(String[] args) throws UnknownHostException,
			IOException {

		Socket socket = new Socket("localhost", 33334);

		OutputStream os = socket.getOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(os);
		BufferedInputStream bis = new BufferedInputStream(
				socket.getInputStream());

		byte[] buf = new byte[65535];

		for (int j = 0; j < 1000; j++) {

			byte r = r();
			byte g = r();
			byte b = r();

			int idx = 0;
			for (int i = 0; i <= 28; i++) {

				if (i % 8 == 0)
					continue;

				// r = (byte) 0xff;
				// g = (byte) 0x80;
				// b = (byte) 0xff;

				byte h = (byte) 0x3f;

				byte id = (byte) (((byte) 0x80) | ((byte) i));
				// System.out.println(Integer.toBinaryString(id));

				buf[idx++] = h;
				buf[idx++] = r;
				buf[idx++] = g;
				buf[idx++] = b;
				buf[idx++] = id;
			}

			bos.write(buf, 0, idx);

			bos.flush();
			int read = bis.read();
			System.out.println(read);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		bos.write("end".getBytes());
		bos.flush();

		bos.close();
		socket.close();

	}

	public static byte r() {
		return (byte) ((byte) (Math.round(Math.random() * 127)) & 0x7f | 0x80);
	}
}
