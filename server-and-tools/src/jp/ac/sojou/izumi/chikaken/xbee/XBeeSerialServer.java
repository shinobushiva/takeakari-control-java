package jp.ac.sojou.izumi.chikaken.xbee;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * @deprecated use XBeeSerialServer2
 * @author izumi
 * 
 */
public class XBeeSerialServer extends JFrame {

	private static final long serialVersionUID = 733399969781731274L;

	private CommPortIdentifier portId;
	private SerialPort port;
	private BufferedOutputStream xbeeBos;

	private JTextField jtfPort;

	private JButton jbServerOnOff;
	private boolean isServerRunning = false;

	private ServerSocket serverSocekt;

	private int speed = 115200;

	public XBeeSerialServer() {

		jtfPort = new JTextField("/dev/tty.usbmodemfa131");

		jbServerOnOff = new JButton("Start Server [Server is Stop now]");
		jbServerOnOff.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (isServerRunning) {
					closeSerialPort();
					try {
						serverSocekt.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					isServerRunning = false;

					jbServerOnOff.setText("Start Server [Server is Stop now]");
				} else {
					isServerRunning = true;
					openSerialPort();
					new Thread() {
						public void run() {
							startServer();
						};
					}.start();

					jbServerOnOff
							.setText("Stop Server [Server is Running now]");
				}

			}
		});

		setLayout(new BorderLayout());

		add(jtfPort, BorderLayout.NORTH);
		add(jbServerOnOff, BorderLayout.CENTER);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean openSerialPort() {
		try {
			Enumeration e = CommPortIdentifier.getPortIdentifiers();
			ArrayList list = Collections.list(e);
			for (Object object : list) {
				CommPortIdentifier cpi = (CommPortIdentifier) object;
				System.out.println(cpi.getName());
			}

			portId = CommPortIdentifier.getPortIdentifier(jtfPort.getText());
			port = (SerialPort) portId.open("Superman", 5000);
			port.setSerialPortParams(speed, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

			xbeeBos = new BufferedOutputStream(port.getOutputStream());

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean closeSerialPort() {
		if (xbeeBos == null || port == null) {
			return false;
		}

		try {
			xbeeBos.close();
			port.close();
			xbeeBos = null;
			port = null;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return true;
	}

	public void startServer() {

		while (isServerRunning) {
			try {
				serverSocekt = new ServerSocket(33334);
				System.out.println("Waiting for connection");
				final Socket s = serverSocekt.accept();
				System.out.println("Connection established");
				serverSocekt.close();
				Thread th = new Thread() {
					public void run() {
						try {

							byte[] buf = new byte[speed];

							BufferedInputStream bis = new BufferedInputStream(
									s.getInputStream(), buf.length);
							BufferedOutputStream bos = new BufferedOutputStream(
									s.getOutputStream(), buf.length);

							while (true) {
								// try {
								// Thread.sleep(1);
								// } catch (InterruptedException e) {
								// e.printStackTrace();
								// }
								int l = bis.read(buf);
								if (l <= 0)
									continue;
								System.out.println(l);

								// String s = new String(buf, 0, l);
								// System.out.println(s);

								// if (buf[0] == 0x65 && buf[1] == 0x6e
								// && buf[2] == 0x64) {
								if (buf[0] == 0x00 && buf[1] == 0x00
										&& buf[2] == 0x00) {
									break;
								}

								// for (int i = 0; i < l; i++) {
								// byte b = (byte) buf[i];
								// System.out
								// .print(String
								// .format("%8s",
								// Integer.toBinaryString(b & 0b01111111))
								// .replaceAll(" ", "0")
								// + " ");
								// }
								// System.out.println();

								long t = System.currentTimeMillis();
								if (xbeeBos != null) {
									xbeeBos.write(buf, 0, l);
									xbeeBos.flush();
								}
								System.out.println("t="
										+ (System.currentTimeMillis() - t));
								bos.write(0xFF);
								bos.flush();

							}
							bis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					};
				};
				th.start();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {

		XBeeSerialServer sc = new XBeeSerialServer();
		sc.setBounds(100, 100, 640, 480);
		sc.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		sc.setVisible(true);

	}

}