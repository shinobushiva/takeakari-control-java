package jp.ac.sojou.izumi.chikaken.synthesizerui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class PartialEditor extends JDialog {

	private static final long serialVersionUID = -6327605090307368189L;

	private Partial partial;
	private MyPanel myPanel;

	public PartialEditor(Partial p, MyPanel mp) {
		this.partial = p;
		this.myPanel = mp;

		setModal(true);

		final JColorChooser jc1 = new JColorChooser(new Color(p.sArgb, true));
		final JColorChooser jc2 = new JColorChooser(new Color(p.eArgb, true));

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		panel.add(jc1);
		panel.add(jc2);

		setLayout(new BorderLayout());

		add(panel, BorderLayout.CENTER);

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				applyAction(jc1, jc2);
			}

		});
		applyButton.addKeyListener(new KeyAdapter() {

			public void keyReleased(KeyEvent e) {
				if (KeyEvent.VK_ENTER == e.getKeyCode()) {
					applyAction(jc1, jc2);
				}
			}
		});

		JButton deleteButton = new JButton("Delete");
		deleteButton.setForeground(Color.RED);
		deleteButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				PartialEditor.this.partial.module.getPartials().remove(
						PartialEditor.this.partial);
				PartialEditor.this.myPanel.repaint();
				setVisible(false);
			}
		});

		JPanel buttons = new JPanel();
		buttons.add(applyButton);
		buttons.add(deleteButton);
		add(buttons, BorderLayout.SOUTH);

		pack();

	}

	private void applyAction(final JColorChooser jc1, final JColorChooser jc2) {
		{
			int rgb = jc1.getColor().getRGB();
			System.out.println(Integer.toHexString(rgb));
			PartialEditor.this.partial.sArgb = rgb;
		}
		{
			int rgb = jc2.getColor().getRGB();
			System.out.println(Integer.toHexString(rgb));
			PartialEditor.this.partial.eArgb = rgb;
		}

		PartialEditor.this.myPanel.repaint();
		setVisible(false);
	}
}
