package x.mvmn.misc.thue.model;

public class ThueRule {

	protected String match;
	protected String replace;

	public ThueRule(String match, String replace) {
		this.match = match;
		this.replace = replace;
	}

	public String getMatch() {
		return match;
	}

	public String getReplace() {
		return replace;
	}
}
