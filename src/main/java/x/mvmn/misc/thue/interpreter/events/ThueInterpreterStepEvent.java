package x.mvmn.misc.thue.interpreter.events;

public class ThueInterpreterStepEvent {

	protected int ruleNumber;
	protected int tokenStartIdx;
	protected int tokenEndIdx;

	public ThueInterpreterStepEvent(int ruleNumber, int tokenStartIdx, int tokenEndIdx) {
		this.ruleNumber = ruleNumber;
		this.tokenStartIdx = tokenStartIdx;
		this.tokenEndIdx = tokenEndIdx;
	}

	public int getRuleNumber() {
		return ruleNumber;
	}

	public int getTokenStartIdx() {
		return tokenStartIdx;
	}

	public int getTokenEndIdx() {
		return tokenEndIdx;
	}
}
