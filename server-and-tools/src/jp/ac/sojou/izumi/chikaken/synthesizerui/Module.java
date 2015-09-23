package jp.ac.sojou.izumi.chikaken.synthesizerui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Module {

	public String name;

	private ArrayList<Partial> partials = new ArrayList<Partial>();

	public void orderPartials() {
		Collections.sort(partials, new Comparator<Partial>() {
			@Override
			public int compare(Partial o1, Partial o2) {
				return o1.start - o2.start;
			}
		});

	}

	public void addPartial(Partial p) {
		partials.add(p);
		orderPartials();
	}

	public ArrayList<Partial> getPartials() {
		return partials;
	}

	public static String modules2String(List<Module> modules) {
		StringBuilder buf = new StringBuilder();

		for (Module m : modules) {
			m.orderPartials();

			buf.append(m.name).append(",");
			for (Partial p : m.getPartials()) {
				buf.append(p.start).append(",");
				buf.append(p.sArgb).append(",");
				buf.append(p.end).append(",");
				buf.append(p.eArgb).append(",");
			}
			buf.append("\n");
		}

		return buf.toString();
	}

}