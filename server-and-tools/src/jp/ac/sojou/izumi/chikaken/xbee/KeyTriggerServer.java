package jp.ac.sojou.izumi.chikaken.xbee;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class KeyTriggerServer extends JFrame {

	private static final long serialVersionUID = 6264489077939226285L;

	private Configuration conf;
	private String strPort;
	private String strSpeed;
	private String strModule;

	private JButton jbServerOnOff;
	private boolean isServerRunning;

	private ServerSocket serverSocket;

	private Robot robot;

	private BufferedOutputStream xbeeBos;

	private SerialPort xbeePort;

	public KeyTriggerServer() {

		try {
			robot = new Robot();
			robot.delay(1000);
		} catch (AWTException e) {
			e.printStackTrace();
		}

		String filename = "keyevent_config.txt";
		File file = new File(filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		conf = new Configuration(file);
		if (conf.getProperty("port") == null) {
			conf.addProperty("port", "33335");
		}
		if (conf.getProperty("speed") == null) {
			conf.addProperty("speed", "38400");
		}
		if (conf.getProperty("module") == null) {
			conf.addProperty("module", "/dev/tty.usbserial-AH016RIU");
		}
		conf.store(file, "configurations");
		// conf.storeToXML(file, "configurations");

		strPort = conf.getProperty("port");
		strSpeed = conf.getProperty("speed");
		strModule = conf.getProperty("module");
		System.out.println(strPort);
		System.out.println(strSpeed);
		System.out.println(strModule);

		jbServerOnOff = new JButton("Start Server [Server is Stop now]");
		jbServerOnOff.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Thread() {
					public void run() {
						if (!isServerRunning) {
							isServerRunning = true;
							openSerialPort();
							startServer();
						} else {
							closeSerialPort();
							try {
								serverSocket.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							isServerRunning = false;
							jbServerOnOff
									.setText("Start Server [Server is Stop now]");
						}
					};
				}.start();
				jbServerOnOff.setText("Stop Server [Server is Running now]");
			}
		});
		add(jbServerOnOff);

	}

	public void keyboardClick(Robot robot, int... args) {
		for (int key : args) {
			robot.keyPress(key);
		}
		robot.delay(100);
		// 同時押しの場合はShift等は後に離したいので逆順
		for (int i = args.length - 1; i >= 0; i--) {
			robot.keyRelease(args[i]);
		}
		robot.delay(1000);
	}

	public void startServer() {

		while (isServerRunning) {
			try {
				serverSocket = new ServerSocket(Integer.parseInt(strPort));

				System.out.println("Waiting for connection");
				final Socket s = serverSocket.accept();
				System.out.println("Connection established");
				serverSocket.close();
				Thread th = new Thread() {
					public void run() {
						try {

							byte[] buf = new byte[512];

							BufferedInputStream bis = new BufferedInputStream(
									s.getInputStream(), buf.length);
							// BufferedOutputStream bos = new
							// BufferedOutputStream(
							// s.getOutputStream(), buf.length);

							while (bis.available() <= 0) {
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}

							int l = bis.read(buf);
							System.out.println(l);
							String cmd = new String(buf, 0, l).trim();
							System.out.println(cmd);
							cmd = cmd.trim();
							byte[] sb = cmd.getBytes();

							if (xbeeBos != null) {
								xbeeBos.write(sb, 0, sb.length);
								xbeeBos.flush();
							}
							bis.close();

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				th.start();

			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

			int speed = Integer.parseInt(strSpeed);

			String ps = strModule.trim();
			System.out.println(ps);
			CommPortIdentifier portId = CommPortIdentifier
					.getPortIdentifier(ps);
			xbeePort = (SerialPort) portId.open("Superman", 6000);
			xbeePort.setSerialPortParams(speed, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			xbeePort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			xbeeBos = new BufferedOutputStream(xbeePort.getOutputStream());

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean closeSerialPort() {
		if (xbeeBos == null) {
			return false;
		}

		try {
			xbeeBos.close();
			xbeePort.close();

			xbeeBos = null;
			xbeePort = null;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return true;
	}

	public static void main(String[] args) {

		JFrame frame = new KeyTriggerServer();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 640, 480);
		frame.setVisible(true);

	}
}