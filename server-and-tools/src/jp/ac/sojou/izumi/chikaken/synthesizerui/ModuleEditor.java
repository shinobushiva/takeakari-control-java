package jp.ac.sojou.izumi.chikaken.synthesizerui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ModuleEditor extends JDialog {

	private static final long serialVersionUID = 2818457378573720117L;

	private Module module;
	private MyPanel myPanel;

	public ModuleEditor(Module m, MyPanel mp) {
		this.module = m;
		this.myPanel = mp;

		setModal(true);

		final JTextField nameField = new JTextField(m.name);

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				applyAction(nameField);
			}

		});
		nameField.addKeyListener(new KeyAdapter() {

			public void keyReleased(KeyEvent e) {
				if (KeyEvent.VK_ENTER == e.getKeyCode()) {
					applyAction(nameField);
				}
			}
		});

		JButton deleteButton = new JButton("Delete");
		deleteButton.setForeground(Color.RED);
		deleteButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				myPanel.deleteModule(module);
				setVisible(false);
				myPanel.repaint();
			}
		});

		setLayout(new BorderLayout());
		add(new JLabel("Name:"), BorderLayout.WEST);
		add(nameField, BorderLayout.CENTER);
		JPanel buttons = new JPanel();
		buttons.add(applyButton);
		buttons.add(deleteButton);
		add(buttons, BorderLayout.SOUTH);

		pack();

	}

	private void applyAction(final JTextField nameField) {
		module.name = nameField.getText();
		setVisible(false);
		myPanel.repaint();
	}

}
