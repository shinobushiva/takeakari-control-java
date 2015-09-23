package jp.ac.sojou.izumi.chikaken.xbee;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * @author izumi
 * 
 */
public class XBeeDirectSerialServerTest extends JFrame {

	private static final long serialVersionUID = 733399969781731274L;

	private CommPortIdentifier portId;
	private SerialPort port;
	private BufferedOutputStream xbeeBos;

	private JTextField jtfPort;

	private JButton jbServerOnOff;
	private boolean isServerRunning = false;

	private int speed = 19200;

	public XBeeDirectSerialServerTest() {

		jtfPort = new JTextField("/dev/tty.usbserial-AH01J9HD");

		jbServerOnOff = new JButton("Start Server [Server is Stop now]");
		jbServerOnOff.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (isServerRunning) {
					isServerRunning = false;
					closeSerialPort();

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

	byte r = 0;

	public void startServer() {

		while (isServerRunning) {

			byte[] buf = new byte[1 + 3 * 30];
			buf[0] = 0x7f;

			r = (byte) ((r + 1) % 127);

			for (int i = 1; i < buf.length; i += 3) {
				buf[i] = (byte) (r | 0x80);
				buf[i + 1] = (byte) (0x81);
				buf[i + 2] = (byte) (0x81);
			}

			try {
				xbeeBos.write(buf);
				xbeeBos.flush();

				Thread.sleep(1);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {

		XBeeDirectSerialServerTest sc = new XBeeDirectSerialServerTest();
		sc.setBounds(100, 100, 640, 480);
		sc.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		sc.setVisible(true);

	}

}