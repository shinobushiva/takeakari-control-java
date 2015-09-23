package jp.ac.sojou.izumi.chikaken.synthesizerui.midi;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.JFileChooser;

public class Midi2Text {

	public static final int NOTE_ON = 0x90;
	public static final int NOTE_OFF = 0x80;
	public static final String[] NOTE_NAMES = { "C", "C#", "D", "D#", "E", "F",
			"F#", "G", "G#", "A", "A#", "B" };

	public static void main(String[] args) throws Exception {

		JFileChooser jfc = new JFileChooser();
		int c = jfc.showOpenDialog(null);
		if (c != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File f = jfc.getSelectedFile();

		ArrayList<ArrayList<Entry>> values = new ArrayList<ArrayList<Entry>>();

		Sequence sequence = MidiSystem.getSequence(f);

		int tempo = 0;
		int res = sequence.getResolution();

		int trackNumber = 0;
		for (Track track : sequence.getTracks()) {

			ArrayList<Entry> line = new ArrayList<Entry>();
			values.add(line);

			trackNumber++;
			// System.out.println("Track " + trackNumber + ": size = "
			// + track.size());
			// System.out.println();
			for (int i = 0; i < track.size(); i++) {
				MidiEvent event = track.get(i);
//				System.out.print("@" + event.getTick() + " ");
				MidiMessage message = event.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage sm = (ShortMessage) message;
//					System.out.print("Channel: " + sm.getChannel() + " ");
					if (sm.getCommand() == NOTE_ON) {
						int key = sm.getData1();
						int octave = (key / 12) - 1;
						int note = key % 12;
						String noteName = NOTE_NAMES[note];
						int velocity = sm.getData2();
//						System.out.println("Note on, " + noteName + octave
//								+ " key=" + key + " velocity: " + velocity);

						Entry objs = new Entry(event.getTick(), noteName,
								octave, key, velocity);
						line.add(objs);

					} else if (sm.getCommand() == NOTE_OFF) {
						int key = sm.getData1();
						int octave = (key / 12) - 1;
						int note = key % 12;
						String noteName = NOTE_NAMES[note];
						int velocity = sm.getData2();
						// System.out.println("Note off, " + noteName + octave
						// + " key=" + key + " velocity: " + velocity);
					} else {
						// System.out.println("Command:" + sm.getCommand());
					}
				} else {
					System.out.println("Other message: " + message.getClass());
					if (message instanceof MetaMessage) {
						MetaMessage mm = (MetaMessage) message;
						System.out.println("" + mm.getType() + "," + "0x"
								+ Integer.toHexString(mm.getType()));
						if (mm.getType() == 81) {
							int n = mm.getLength();
							byte[] bb = mm.getData();
							for (int ii = 0; ii < bb.length; ii++) {
								System.out.print("," + mm.getData()[ii]);
							}
							System.out.println();

							tempo = 0;
							for (int ii = 0; ii < bb.length; ii++) {
								tempo = (tempo << 8) + (bb[ii] & 0xff);
							}
							System.out.println(tempo);
						}
					}
				}
			}

			System.out.println();
		}

		System.out.println("--");

		Color[] colors = new Color[] {
				//
				new Color(0xffE60012, true),//
				new Color(0xffF39800, true),//
				new Color(0xffFFF100, true),//
				new Color(0xff8FC31F, true),//
				new Color(0xff009944, true),//
				new Color(0xff009E96, true),//
				new Color(0xff00A0E9, true),//
				new Color(0xff0068B7, true),//
				new Color(0xff0068B7, true),//
				new Color(0xff920783, true),//
				new Color(0xffE4007F, true),//
				new Color(0xffE5004F, true) //
		};

		for (ArrayList<Entry> vals : values) {

			Entry pe = null;

			StringBuilder buf = new StringBuilder();
			buf.append("Module").append(",");

			for (Entry e : vals) {
				if (pe == null) {
					pe = e;
					continue;
				}

				if (pe.tick == e.tick) {
					pe = e;
					continue;
				}

				Color c1 = new Color(colors[pe.key % 12].getRGB() & 0x00ffffff
						| ((pe.velocity * 255 / 100) << 24), true);
				Color c2 = new Color(colors[pe.key % 12].getRGB() & 0x00ffffff
						| ((pe.velocity * 255 / 100) << 24), true);

				buf.append(pe.tick * 1000 / res).append(",")
						.append(c1.getRGB()).append(",")
						.append(e.tick * 1000 / res).append(",")
						.append(c2.getRGB()).append(",");

				pe = e;
			}
			System.out.println(buf.toString());
		}

		// res/1000 * tempo/1000_000 でいいような気がするんだが、、、
		
		System.out.println("tempo: " + (tempo / 1000_000.0) + " s");
		System.out.println(sequence.getMicrosecondLength());
		System.out.println(sequence.getDivisionType());
		System.out.println(sequence.getResolution());

	}

	public static class Entry {
		public long tick;
		public String noteName;
		public int octave;
		public int key;
		public int velocity;

		public Entry(long tick, String noteName, int octave, int key,
				int velocity) {
			this.tick = tick;
			this.noteName = noteName;
			this.octave = octave;
			this.key = key;
			this.velocity = velocity;
		}
	}

}
