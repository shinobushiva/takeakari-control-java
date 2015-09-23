package jp.ac.sojou.izumi.chikaken.synthesizerui.imageread;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import jp.ac.sojou.izumi.chikaken.synthesizerui.Module;
import jp.ac.sojou.izumi.chikaken.synthesizerui.Partial;

public class Images2SequenceUI extends JFrame {

	private static final long serialVersionUID = 7254045089184445404L;

	private List<File> imageFiles;

	private List<Point> points;

	private BufferedImage img;
	private int imgPointer = 0;

	private Point dragging = null;

	private final static int FPS = 30;

	private JFileChooser jfc;

	public static void main(String[] args) {

		JFrame f = new Images2SequenceUI();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 640, 480);

		f.setVisible(true);

	}

	public Images2SequenceUI() {

		points = new ArrayList<Point>();

		final JPanel jp = new JPanel() {

			private static final long serialVersionUID = -5301404619289111518L;

			protected void paintComponent(java.awt.Graphics g) {
				if (img != null) {
					Rectangle r = g.getClipBounds();
					g.drawImage(img, 0, 0, r.width, r.height, 0, 0,
							img.getWidth(), img.getHeight(), this);

					int count = 0;
					for (Point p : points) {
						int x = (int) (p.x * g.getClipBounds().getWidth() / img
								.getWidth());
						int y = (int) (p.y * g.getClipBounds().getHeight() / img
								.getHeight());
						g.setColor(Color.GREEN);
						g.drawOval(x - 5, y - 5, 10, 10);
						g.drawString("" + count, x, y - 10);
						count++;

						int argb = img.getRGB(p.x, p.y);
						// int aa = argb & 0xff000000 >> 24;
						// int rr = argb & 0x00ff0000 >>16;
						// int gg = argb & 0x0000ff00 >>8;
						// int bb = argb & 0x000000ff;
						g.drawString(Integer.toHexString(argb), x + 7, y + 5);
					}
				}
			};
		};

		jp.addMouseMotionListener(new MouseAdapter() {

			public void mouseDragged(MouseEvent e) {
				// System.out.println(dragging);

				if (dragging != null) {
					Point p = e.getPoint();
					int x = (int) (p.x * img.getWidth() / jp.getWidth());
					int y = (int) (p.y * img.getHeight() / jp.getHeight());
					dragging.x = x;
					dragging.y = y;
					jp.repaint();
				}
			}
		});
		jp.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				for (Point p : points) {
					int x = (int) (p.x * jp.getWidth() / img.getWidth());
					int y = (int) (p.y * jp.getHeight() / img.getHeight());
					if (e.getPoint().distance(x, y) < 10) {
						dragging = p;
						break;
					}
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (dragging == null) {
					Point p = e.getPoint();
					p.x = (int) (p.x * img.getWidth() / (double) jp.getWidth());
					p.y = (int) (p.y * img.getHeight() / (double) jp
							.getHeight());
					points.add(p);
				}

				dragging = null;
				jp.repaint();

			}
		});

		JButton openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				jfc = new JFileChooser();

				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int s = jfc.showOpenDialog(Images2SequenceUI.this);
				if (s == JFileChooser.APPROVE_OPTION) {
					File d = jfc.getSelectedFile();
					File[] files = d.listFiles(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String name) {
							if (name.toLowerCase().endsWith(".png")) {
								return true;
							}
							return false;
						}
					});
					imageFiles = new ArrayList<File>(Arrays.asList(files));

					Collections.sort(imageFiles, new Comparator<File>() {
						@Override
						public int compare(File o1, File o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});

					for (File file : files) {
						System.out.println(file.getName());
					}
					setImgPointer(0);
				}

				jp.repaint();
			}
		});

		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imgPointer = Math.min(imageFiles.size() - 1, imgPointer + 1);
				// System.out.println(imgPointer);
				setImgPointer(imgPointer);
				jp.repaint();
			}
		});

		JButton prevButton = new JButton("Prev");
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imgPointer = Math.max(0, imgPointer - 1);
				// System.out.println(imgPointer);
				setImgPointer(imgPointer);
				jp.repaint();
			}
		});

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int s = jfc.showSaveDialog(Images2SequenceUI.this);
				if (s == JFileChooser.APPROVE_OPTION) {
					File f = jfc.getSelectedFile();
					try {
						BufferedWriter w = new BufferedWriter(new FileWriter(f));

						for (Point p : points) {
							w.write("" + p.x + "," + p.y);
							w.write(Character.LINE_SEPARATOR);
						}

						w.flush();
						w.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			}
		});

		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int s = jfc.showOpenDialog(Images2SequenceUI.this);
				if (s == JFileChooser.APPROVE_OPTION) {
					File f = jfc.getSelectedFile();
					points.clear();
					try {
						BufferedReader r = new BufferedReader(new FileReader(f));
						String str = null;
						while ((str = r.readLine()) != null) {
							String[] sa = str.split(",");
							if (sa.length >= 2) {
								Point p = new Point(Integer.parseInt(sa[0]),
										Integer.parseInt(sa[1]));
								points.add(p);
							}
						}
						r.close();
						jp.repaint();

					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JButton readButton = new JButton("Read");
		readButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread th = new Thread() {
					public void run() {

						ArrayList<Module> modules = new ArrayList<Module>();
						ArrayList<ArrayList<Integer>> vals = new ArrayList<ArrayList<Integer>>();
						for (int i = 0; i < points.size(); i++) {
							Module module = new Module();
							module.name = "module_" + (i + 1);
							modules.add(module);
							vals.add(new ArrayList<Integer>());
						}

						for (int i = 0; i < imageFiles.size(); i++) {
							setImgPointer(i);
							System.out.println("Image Pointer = " + i
									+ ",File=" + imageFiles.get(i).getName());
							jp.repaint();

							int count = 0;
							for (Point p : points) {

								int argb = img.getRGB(p.x, p.y);
								// int aa = argb & 0xff000000 >> 24;
								// int rr = argb & 0x00ff0000 >>16;
								// int gg = argb & 0x0000ff00 >>8;
								// int bb = argb & 0x000000ff;
								// g.drawString(Integer.toHexString(argb), x +
								// 7,
								// y + 5);
								vals.get(count).add(argb);

								Module m = modules.get(count);
								Partial part = null;
								if (m.getPartials().size() > 0) {
									part = m.getPartials().get(
											m.getPartials().size() - 1);

									if (part.sArgb != argb) {

										part.eArgb = part.sArgb;
										part.end = (i - 1) * FPS;

										part = new Partial(m);
										m.addPartial(part);
										part.sArgb = argb;
										part.start = i * FPS;
										part.eArgb = argb;
										part.end = i * FPS;
									}
								} else {
									part = new Partial(m);
									m.addPartial(part);
									part.sArgb = argb;
									part.start = i * FPS;
									part.eArgb = argb;
									part.end = i * FPS;
								}

								count++;

							}

							try {
								Thread.sleep(1);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						int s = jfc.showSaveDialog(Images2SequenceUI.this);
						if (s == JFileChooser.APPROVE_OPTION) {
							File f = jfc.getSelectedFile();
							try {
								BufferedWriter w = new BufferedWriter(
										new FileWriter(f));

								for (ArrayList<Integer> val : vals) {
									System.out.println(val);
									for (Integer i : val) {
										w.write("" + i);
										w.write(",");
									}
									w.write(Character.LINE_SEPARATOR);
								}

								w.write(Character.LINE_SEPARATOR);
								w.write(Module.modules2String(modules));

								w.flush();
								w.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					};
				};
				th.start();
			}
		});

		JPanel buttons = new JPanel();
		buttons.add(openButton);
		buttons.add(new JSeparator(SwingConstants.VERTICAL));
		buttons.add(prevButton);
		buttons.add(nextButton);
		buttons.add(new JSeparator(SwingConstants.VERTICAL));
		buttons.add(loadButton);
		buttons.add(saveButton);
		buttons.add(new JSeparator(SwingConstants.VERTICAL));
		buttons.add(readButton);

		setLayout(new BorderLayout());

		add(buttons, BorderLayout.SOUTH);
		add(jp, BorderLayout.CENTER);
	}

	private void setImgPointer(int p) {

		File f = imageFiles.get(p);
		try {
			img = ImageIO.read(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
