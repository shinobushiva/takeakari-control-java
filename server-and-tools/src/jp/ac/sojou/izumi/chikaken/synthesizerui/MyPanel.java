package jp.ac.sojou.izumi.chikaken.synthesizerui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;

public class MyPanel extends JPanel {

	private static final int Y_OFFSET = 40;
	private static final int STEP = 10; // 1000ms
	private static final int X_OFFSET = 100;
	private static final int Y_GAP = 40;

	private static final long serialVersionUID = -4973735383820676654L;

	private ArrayList<Module> modules = new ArrayList<Module>();
	private Map<Rectangle, SelectableRect> clickMap = new HashMap<Rectangle, SelectableRect>();

	private SelectableRect selected;
	private Rectangle selectedRect;

	private Point dragOffset;
	private Point dragStart;

	private Map<Rectangle, Module> moduleClicks = new HashMap<Rectangle, Module>();

	private Point scroll = new Point();

	private enum DragType {
		Drag, SL, SR, DragView
	}

	private DragType dragType = DragType.Drag;

	public MyPanel() {

		Thread th = new Thread() {
			public void run() {
				while (true) {
					LABEL: if (dragStart != null) {

						Point mp = MouseInfo.getPointerInfo().getLocation();

						if (dragType == DragType.DragView) {

							int xd = dragStart.x - mp.x;

							if (Point.distance(dragStart.x, dragStart.y, mp.x,
									mp.y) < 5) {
								break LABEL;
							}

							xd = (int) (0.05 * xd);
							int yd = (int) (0.05 * (dragStart.y - mp.y));

							scroll.x -= xd;// xd;
							if (scroll.x > 0)
								scroll.x = 0;

							scroll.y -= yd;
							if (scroll.y > 0) {
								scroll.y = 0;
							}
							if (scroll.y < -(modules.size() - 1) * Y_GAP) {
								scroll.y = -(modules.size() - 1) * Y_GAP;
							}

							// System.out.println(scroll);

							// dragStart = e.getPoint();
							repaint();
						}
					}

					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}
		};
		th.setDaemon(true);
		th.start();

		{
			Module m = new Module();
			m.name = "Module 1";
			Partial p1 = new Partial(1000, 2000, 0xffff0000, 0xff0000ff, m);
			m.addPartial(p1);
			Partial p2 = new Partial(3000, 4000, 0xff00ff00, 0xff000077, m);
			m.addPartial(p2);

			modules.add(m);
		}
		{
			Module m = new Module();
			m.name = "Module 2";
			Partial p1 = new Partial(1000, 2000, 0xffff0000, 0xff00ffff, m);
			m.addPartial(p1);
			Partial p2 = new Partial(3000, 4000, 0xffffff00, 0xff660077, m);
			m.addPartial(p2);

			modules.add(m);
		}

		setBackground(Color.WHITE);

		addMouseMotionListener(new MouseMotionListener() {

			public void mouseMoved(MouseEvent e) {

			}

			public void mouseDragged(MouseEvent e) {
				if (dragStart != null) {

					int xd = dragStart.x - e.getPoint().x;
					// if (dragStart.x + xd <= xOffset) {
					// xd = xOffset;
					// return;
					// }
					if (selectedRect != null) {
						if (dragType == DragType.Drag) {
							selectedRect.x -= xd;
						}
						if (dragType == DragType.SL) {
							selectedRect.x -= xd;
							selectedRect.width += xd;
						}

						if (dragType == DragType.SR) {
							selectedRect.width -= xd;
						}

						if (selectedRect.width < 10) {
							selectedRect.width = 10;
						}

						dragStart = e.getPoint();
						repaint();
					}
					// else {
					// if (dragType == DragType.DragView) {
					//
					// int speed = (int) (0.1 * xd);
					//
					// int yd = speed; // dragStart.y - e.getPoint().y;
					//
					// scroll.x -= speed;// xd;
					// if (scroll.x > 0)
					// scroll.x = 0;
					//
					// scroll.y -= yd;
					// if (scroll.y > 0) {
					// scroll.y = 0;
					// }
					// if (scroll.y < -(modules.size() - 1) * Y_GAP) {
					// scroll.y = -(modules.size() - 1) * Y_GAP;
					// }
					//
					// System.out.println(scroll);
					//
					// // dragStart = e.getPoint();
					// repaint();
					// }
					// }

				}
			}
		});

		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				MyPanel.this.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				if (selected != null) {

					Partial p = (Partial) selected;

					selectedRect.x = Math.round(selectedRect.x / 10f) * 10;
					selectedRect.width = Math.round(selectedRect.width / 10f) * 10;

					p.start = (selectedRect.x - X_OFFSET - scroll.x) * STEP;
					if (p.start <= 0)
						p.start = 0;
					p.end = p.start + selectedRect.width * STEP;

					p.module.orderPartials();
					repaint();
				} else {
					if (dragType == DragType.DragView) {
						scroll.x = (scroll.x / STEP) * STEP;
					}
				}

				dragStart = null;
				selectedRect = null;
				selected = null;

				repaint();
			}

			public void mousePressed(MouseEvent e) {

				if (dragStart != null)
					return;

				selected = null;

				for (Rectangle rect : clickMap.keySet()) {
					if (rect.contains(e.getPoint())) {
						SelectableRect selectable = clickMap.get(rect);
						if (selectable instanceof Partial) {

							if (rect.x + 10 > e.getPoint().x) {
								MyPanel.this
										.setCursor(Cursor
												.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
								dragType = DragType.SL;
							} else if (rect.x + rect.width - 10 < e.getPoint().x) {
								MyPanel.this
										.setCursor(Cursor
												.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
								dragType = DragType.SR;
							} else {
								MyPanel.this
										.setCursor(Cursor
												.getPredefinedCursor(Cursor.HAND_CURSOR));
								dragType = DragType.Drag;
							}

							selected = selectable;
							selectedRect = rect;
							repaint();
							break;
						}
					}
				}

				if (selected != null) {
					dragStart = e.getPoint();
					dragOffset = new Point(dragStart);
					dragOffset.x -= selectedRect.x;
					dragOffset.y -= selectedRect.y;
				} else {
					dragType = DragType.DragView;
					MyPanel.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.HAND_CURSOR));

					dragStart = MouseInfo.getPointerInfo().getLocation();
				}
			}

			public void mouseExited(MouseEvent e) {
				// System.out.println(":MOUSE_EXITED_EVENT:");
			}

			public void mouseEntered(MouseEvent e) {
				// System.out.println(":MOUSE_ENTER_EVENT:");
			}

			public void mouseClicked(MouseEvent e) {

				if (e.getClickCount() >= 2) {

					for (Rectangle rect : clickMap.keySet()) {
						if (rect.contains(e.getPoint())) {
							SelectableRect selectable = clickMap.get(rect);
							if (selectable instanceof Partial) {
								selected = selectable;
								selectedRect = rect;
								repaint();

								JDialog d = new PartialEditor(
										(Partial) selectable, MyPanel.this);
								d.setVisible(true);
								return;
							}
						}
					}

					for (Rectangle rect : moduleClicks.keySet()) {
						if (rect.contains(e.getPoint())) {
							Module m = moduleClicks.get(rect);
							System.out.println(m.name);

							JDialog d = new ModuleEditor(m, MyPanel.this);
							d.setVisible(true);
							return;
						}
					}

					int n = (e.getPoint().y - Y_OFFSET) / Y_GAP;
					if (e.getPoint().y > Y_OFFSET && n < modules.size()) {
						Module m = modules.get(n);

						int s = (e.getPoint().x - X_OFFSET - scroll.x) * STEP;

						Partial p = new Partial(s, s + STEP * 30, 0xffff0000,
								0xff0000ff, m);
						m.addPartial(p);
						repaint();
					}
				}
			}
		});
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		clickMap.clear();
		moduleClicks.clear();

		Rectangle r = g.getClipBounds();

		int y = 20;

		int counter = 0;

		for (int i = 0; i < -scroll.x; i += STEP) {
			counter++;
		}

		for (int i = X_OFFSET - (-scroll.x - STEP * counter); i < r.width; i += STEP) {

			if (counter % STEP == 0) {
				g2d.drawString("" + (counter / 10f) + "s", i - 4, y - 5);
			}

			if (counter % STEP == 0) {
				g2d.drawLine(i, y, i, y + 10);
			}
			if (counter % (STEP / 2) == 0) {
				g2d.drawLine(i, y, i, y + 8);
			} else {
				g2d.drawLine(i, y, i, y + 5);
			}
			counter++;
		}

		y = Y_OFFSET + scroll.y;

		for (Module m : modules) {
			g2d.setColor(Color.BLACK);
			g2d.setPaint(null);

			if (y < Y_OFFSET - 10) {
				y += Y_GAP;
				continue;
			}

			moduleClicks.put(new Rectangle(10, y, X_OFFSET - 10, Y_GAP), m);

			g2d.drawString(m.name, 10, y + Y_GAP - 15);
			g2d.drawLine(X_OFFSET, y + Y_GAP / 2, r.width - 10, y + Y_GAP / 2);

			for (Partial p : m.getPartials()) {

				int x1 = X_OFFSET + scroll.x + p.start / STEP;
				int y1 = y + 10;
				int x2 = X_OFFSET + scroll.x + p.end / STEP;
				int y2 = y + Y_GAP - 10;

				GradientPaint gradient = new GradientPaint(x1, y1, new Color(
						p.sArgb, true), x2, y1, new Color(p.eArgb, true), true);
				g2d.setPaint(gradient);

				clickMap.put(new Rectangle(x1, y1, x2 - x1, y2 - y1), p);

				if (x1 < X_OFFSET) {
					x1 = X_OFFSET;
				}
				g2d.fillRect(x1, y1, x2 - x1, y2 - y1);

				g2d.setColor(Color.LIGHT_GRAY);
				g2d.drawRect(x1, y1, x2 - x1, y2 - y1);

			}
			y += Y_GAP;
		}

		if (selected != null) {

			Rectangle rr = selectedRect;

			g2d.setColor(Color.GREEN);
			g2d.drawRoundRect(rr.x - 2, rr.y - 2, rr.width + 4, rr.height + 4,
					5, 5);
		}

		if (dragType == DragType.DragView && dragStart != null) {

			g2d.setColor(Color.GRAY);
			int size = 40;

			Point pp1 = relP(dragStart);
			g2d.drawOval(pp1.x - size / 2, pp1.y - size / 2, size, size);

			Point pp2 = relP(MouseInfo.getPointerInfo().getLocation());
			g2d.drawLine(pp1.x, pp1.y, pp2.x, pp2.y);
		}
	}

	private Point relP(Point absolute) {
		Point compCoords = getLocationOnScreen();
		return new Point(absolute.x - compCoords.x, absolute.y - compCoords.y);
	}

	public void loadCSV(String csv) {

		modules.clear();
		try {
			BufferedReader br = new BufferedReader(new StringReader(csv));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().length() <= 0)
					continue;

				System.out.println("a:" + line);
				Module m = new Module();
				modules.add(m);

				String[] vals = line.split(",");

				int idx = 0;
				m.name = vals[idx++];
				while (idx < vals.length) {
					int s = Integer.parseInt(vals[idx++]);
					int sArgb = Integer.parseInt(vals[idx++]);
					int e = Integer.parseInt(vals[idx++]);
					int eArgb = Integer.parseInt(vals[idx++]);
					Partial p = new Partial(s, e, sArgb, eArgb, m);
					m.addPartial(p);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		repaint();

	}

	public String createCSV() {

		return Module.modules2String(modules);
	}

	public void addModule() {
		Module m = new Module();
		m.name = "New Module";
		modules.add(m);
		repaint();
	}

	public void deleteModule(Module m) {
		modules.remove(m);
		repaint();
	}
}
