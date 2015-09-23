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

		for (int i = 0; i < 1000; i++) {
			int idx = 0;

			byte r = r();
			byte g = r();
			byte b = r();
			r = (byte) 0xff;
			g = (byte) 0x80;
			b = (byte) 0xff;
			{
				byte h = (byte) 0x3f;

				// r = (byte) 0b10111111;
				// g = (byte) (0b10000001*i);
				// b = (byte) 0b11111111;
				byte mask = (byte) 0x7f;

				buf[idx++] = h;
				buf[idx++] = r;
				buf[idx++] = g;
				buf[idx++] = b;
				buf[idx++] = mask;

				// bos.write(h);
				// bos.write(r);
				// bos.write(g);
				// bos.write(b);
				// bos.write(mask);

			}
			{
				byte h = (byte) 0x3f;
				// byte r = r();
				// byte g = r();
				// byte b = r();
				// r = (byte) (0b10000001*i);
				// g = (byte) 0b11111111;
				// b = (byte) 0b11111111;

				byte mask = (byte) 0x7f;
				System.out.println(mask & 0x7f);

				buf[idx++] = h;
				buf[idx++] = r;
				buf[idx++] = g;
				buf[idx++] = b;
				buf[idx++] = mask;
			}

			for (int j = 0; j < 40; j++) {
				byte h = (byte) 0x3f;
				// byte r = r();
				// byte g = r();
				// byte b = r();
				r = (byte) 0x80;
				g = (byte) 0xff;
				b = (byte) 0x80;

				byte mask = (byte) 0x80;

				buf[idx++] = h;
				buf[idx++] = r;
				buf[idx++] = g;
				buf[idx++] = b;
				buf[idx++] = mask;
			}

			bos.write(buf, 0, idx);
			// bos.write(0x00);
			bos.flush();
			int read = bis.read();
			System.out.println(read);
			try {
				Thread.sleep(33);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		byte h = (byte) 0x7f;
		byte r = (byte) 0x80;
		byte g = (byte) 0x80;
		byte b = (byte) 0x80;
		byte mask = (byte) 0x70;
		bos.write(h);
		bos.write(r);
		bos.write(g);
		bos.write(b);
		bos.write(mask);
		bos.flush();

		bos.write("end".getBytes());
		bos.flush();

		bos.close();
		socket.close();

	}

	public static byte r() {
		return (byte) ((byte) (Math.round(Math.random() * 127)) & 0x7f | 0x80);
	}
}
