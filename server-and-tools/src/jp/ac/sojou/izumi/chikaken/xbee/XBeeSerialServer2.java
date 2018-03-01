package jp.ac.sojou.izumi.chikaken.xbee;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class XBeeSerialServer2 extends JFrame {

	private static final int SERVER_PORT = 11111;

	private static final long serialVersionUID = 733399969781731274L;

	private BufferedOutputStream[] xbeeBoss;
	private SerialPort[] ports;
	private byte[][] sendBufs;
	private int[] sendLens;
	private int[] groups;
	private int[] speeds;

	private JTextField jtfPort;

	private JButton jbServerOnOff;
	private boolean isServerRunning = false;

	private ServerSocket serverSocekt;

	private Configuration conf;

	private String strModules;
	private String strGroups;
	private String strSpeeds;
	private String strFps;

	public XBeeSerialServer2() {

		String filename = "config.txt";
		File file = new File(filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		conf = new Configuration(file);
		if (conf.getProperty("fps") == null) {
			conf.addProperty("fps", "30");
		}
		if (conf.getProperty("modules") == null) {
			conf.addProperty("modules", "/dev/tty.usbserial-AH01JAVS"
					+ ",/dev/tty.usbserial-AH01JAV3"
					+ ",/dev/tty.usbserial-AH01JAVK"
					+ ",/dev/tty.usbmodemfd1221");
		}
		if (conf.getProperty("groups") == null) {
			conf.addProperty("groups", "13,27,41,100");
		}
		if (conf.getProperty("speeds") == null) {
			conf.addProperty("speeds", "19200,19200,19200,38400");
		}
		conf.store(file, "configurations");
		// conf.storeToXML(file, "configurations");

		strFps = conf.getProperty("fps");
		strModules = conf.getProperty("modules");
		strGroups = conf.getProperty("groups");
		strSpeeds = conf.getProperty("speeds");
		System.out.println("fps:"+strFps);
		System.out.println("modules:"+strModules);
		System.out.println("groups:"+strGroups);
		System.out.println("speeds:"+strSpeeds);

		jtfPort = new JTextField(strModules);

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

			String[] portStrings = jtfPort.getText().split(",");
			xbeeBoss = new BufferedOutputStream[portStrings.length];
			ports = new SerialPort[portStrings.length];
			sendBufs = new byte[portStrings.length][];
			sendLens = new int[portStrings.length];
			groups = new int[portStrings.length];
			speeds = new int[portStrings.length];

			String[] ssss = strSpeeds.split(",");
			for (int i = 0; i < speeds.length; i++) {
				speeds[i] = Integer.parseInt(ssss[i].trim());
			}

			for (int i = 0; i < sendBufs.length; i++) {
				sendBufs[i] = new byte[speeds[i]];
			}

			String[] sss = strGroups.split(",");
			for (int i = 0; i < groups.length; i++) {
				groups[i] = Integer.parseInt(sss[i].trim());
			}

			for (int i = 0; i < portStrings.length; i++) {
				String ps = portStrings[i].trim();
				CommPortIdentifier portId = CommPortIdentifier
						.getPortIdentifier(ps);
				SerialPort port = (SerialPort) portId
						.open("Superman", 5000 + i);
				port.setSerialPortParams(speeds[i], SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
				ports[i] = port;
				xbeeBoss[i] = new BufferedOutputStream(port.getOutputStream());
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean closeSerialPort() {
		if (xbeeBoss == null || ports == null) {
			return false;
		}

		try {
			for (int i = 0; i < ports.length; i++) {
				xbeeBoss[i].close();
				ports[i].close();
			}
			xbeeBoss = null;
			ports = null;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return true;
	}

	public void startServer() {

		while (isServerRunning) {
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

							byte[] buf = new byte[speeds[0] * 2];

							bis = new BufferedInputStream(s.getInputStream(),
									buf.length);
							bos = new BufferedOutputStream(s.getOutputStream(),
									buf.length);

							float lastSentTime = System.currentTimeMillis();
							float fps = Float.parseFloat(strFps);
							while (true) {
								// try {
								// Thread.sleep(1);
								// } catch (InterruptedException e) {
								// e.printStackTrace();
								// }
								int l = bis.read(buf);
								if (l <= 0)
									continue;
//								 System.out.println(l);
								 
								// StringBuilder sb = new StringBuilder();
								// for (int i = 0; i < l; i++) {
								// sb.append(Integer.toHexString(buf[i]))
								// .append(" ");
								// }
								// System.out.println(sb);

								if (buf[0] == 0x65 && buf[1] == 0x6e
										&& buf[2] == 0x64) {
									System.out.println("End Message Received!");
									break;
								}

								Arrays.fill(sendLens, 0);
								for (int i = 0; i < l; i += 5) {
									byte head = buf[i];
									byte r = buf[i + 1];
									byte g = buf[i + 3];
									byte b = buf[i + 2];
									byte id = buf[i + 4];
									int iid = id & 0x7f;

									int n = 0;
									for (int j = 0; j < groups.length; j++) {
										if (iid <= groups[j]) {
											n = j;
											break;
										}
									}

									if (n < sendBufs.length) {
										// // XXX: SecondSight限定で色を交換
										// if (iid >= 42 && iid <= 57) {
										// sendBufs[n][sendLens[n]++] = head;
										// sendBufs[n][sendLens[n]++] = r;
										// sendBufs[n][sendLens[n]++] = g;
										// sendBufs[n][sendLens[n]++] = b;
										// sendBufs[n][sendLens[n]++] = id;
										// } else {
										sendBufs[n][sendLens[n]++] = head;
										sendBufs[n][sendLens[n]++] = r;
										sendBufs[n][sendLens[n]++] = b;
										sendBufs[n][sendLens[n]++] = g;
										sendBufs[n][sendLens[n]++] = id;
										// }
									}
								}

								if (lastSentTime + 1000 / fps > System
										.currentTimeMillis())
									continue;

								// long t = System.currentTimeMillis();
								// System.out.println(sendBufs.length);
								int num = sendBufs.length;
								for (int i = 0; i < num; i++) {
									// System.out.println(sendLens[i]);
									if (xbeeBoss[i] != null) {
										xbeeBoss[i].write(sendBufs[i], 0,
												sendLens[i]);
										xbeeBoss[i].flush();
									}
								}
								// System.out.println("t="
								// + (System.currentTimeMillis() - t));
								bos.write(0xFF);
								bos.flush();
							}
							bis.close();
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

	public static void main(String[] args) {

		XBeeSerialServer2 sc = new XBeeSerialServer2();
		sc.setBounds(100, 100, 640, 480);
		sc.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		sc.setVisible(true);

	}

}