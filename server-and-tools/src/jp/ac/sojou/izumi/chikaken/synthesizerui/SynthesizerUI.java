package jp.ac.sojou.izumi.chikaken.synthesizerui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class SynthesizerUI {

	public static void main(String[] args) {

		final JFrame frame = new JFrame("Synthesizer");
		frame.setLayout(new BorderLayout());
		final MyPanel myPanel = new MyPanel();
		frame.add(myPanel, BorderLayout.CENTER);

		final JFileChooser jfc = new JFileChooser();

		JButton saveButton = new JButton("Save CSV");
		saveButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				int s = jfc.showSaveDialog(frame);
				if (s == JFileChooser.APPROVE_OPTION) {
					File f = jfc.getSelectedFile();
					String csv = myPanel.createCSV();
					System.out.println(csv);
					try {
						BufferedWriter w = new BufferedWriter(new FileWriter(f));
						w.write(csv);
						w.flush();
						w.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					// myPanel.loadCSV(csv);
				}

			}
		});

		JButton loadButton = new JButton("Load CSV");
		loadButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				int s = jfc.showOpenDialog(frame);
				if (s == JFileChooser.APPROVE_OPTION) {
					File f = jfc.getSelectedFile();

					StringBuilder buf = new StringBuilder();
					try {
						BufferedReader r = new BufferedReader(new FileReader(f));
						String line = null;
						while ((line = r.readLine()) != null) {
							buf.append(line).append("\n");
						}
						r.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					myPanel.loadCSV(buf.toString());
				}
			}
		});

		JButton addModuleButton = new JButton("Add Module");
		addModuleButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				myPanel.addModule();
			}
		});

		JPanel buttons = new JPanel();

		buttons.add(addModuleButton);
		buttons.add(new JSeparator());

		buttons.add(loadButton);
		buttons.add(saveButton);
		frame.add(buttons, BorderLayout.SOUTH);

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 640, 480);
		frame.setVisible(true);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.updateComponentTreeUI(frame);
			SwingUtilities.updateComponentTreeUI(jfc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread th = new Thread() {
			public void run() {

				while (true) {
					try {
						ServerSocket ss = new ServerSocket(33333);
						Socket s = ss.accept();
						ss.close();

						BufferedWriter bos = new BufferedWriter(
								new OutputStreamWriter(s.getOutputStream()));
						bos.write(myPanel.createCSV());
						bos.flush();
						bos.close();
						s.close();

					} catch (IOException e) {
						e.printStackTrace();
					}

				}

			}
		};
		th.setDaemon(true);
		th.start();

	}
}
