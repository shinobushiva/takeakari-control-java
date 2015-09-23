package jp.ac.sojou.izumi.chikaken.synthesizerui;

public class Partial extends SelectableRect {

	public int start;
	public int end;
	public int sArgb;
	public int eArgb;

	public Module module;

	public Partial(Module m) {
		this.module = m;
	}

	public Partial(int s, int e, int sArgb, int eArgb, Module m) {
		this(m);

		this.start = s;
		this.end = e;
		this.sArgb = sArgb;
		this.eArgb = eArgb;
	}
}