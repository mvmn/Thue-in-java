package x.mvmn.misc.thue.interpreter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import x.mvmn.misc.thue.interpreter.events.IThueInterpreterDataChangeListener;
import x.mvmn.misc.thue.interpreter.events.IThueInterpreterOutputHandler;
import x.mvmn.misc.thue.interpreter.events.IThueInterpreterRuntimeListener;
import x.mvmn.misc.thue.interpreter.events.IThueInterpreterStepListener;
import x.mvmn.misc.thue.interpreter.events.ThueInterpreterStepEvent;
import x.mvmn.misc.thue.model.ThueRule;


/**
 * @author Mykola Makhin
 * @version 1.1
 * 
 */
public class ThueInterpreter {

	public static enum TokensProcessingOrder {
		RANDOM, LEFT_TO_RIGHT, RIGHT_TO_LEFT
	};

	public static enum RuleSelectionPolicy {
		RANDOM, RULELIST_ASC, RULELIST_DESC
	};

	public static enum ProcessingMode {
		RUN, TRACE, ONE_STEP
	};

	protected volatile TokensProcessingOrder tokensProcessingOrder = TokensProcessingOrder.RANDOM;
	protected volatile RuleSelectionPolicy ruleSelectionPolicy = RuleSelectionPolicy.RANDOM;
	protected volatile ProcessingMode processingMode = ProcessingMode.RUN;

	protected volatile int xecutionDelay = 100;

	protected List<IThueInterpreterStepListener> stepListeners = new LinkedList<IThueInterpreterStepListener>();
	protected List<IThueInterpreterOutputHandler> outputHandlers = new LinkedList<IThueInterpreterOutputHandler>();
	protected List<IThueInterpreterDataChangeListener> dataChangeListeners = new LinkedList<IThueInterpreterDataChangeListener>();
	protected List<IThueInterpreterRuntimeListener> runtimeListeners = new LinkedList<IThueInterpreterRuntimeListener>();

	protected List<ThueRule> rulesList;
	protected StringBuffer data;

	protected int ruleIndex = 0;

	// protected boolean[] checkedRules;

	protected volatile boolean stopRequested = false;

	protected Random rnd = new Random();

	// --- Public interface -- initializers
	public void init(List<ThueRule> rulesList, String dataInit, TokensProcessingOrder tokensProcessingOrder, RuleSelectionPolicy ruleSelectionPolicy,
			ProcessingMode processingMode) {
		this.rulesList = rulesList;
		this.data = new StringBuffer(dataInit);
		this.tokensProcessingOrder = tokensProcessingOrder;
		this.ruleSelectionPolicy = ruleSelectionPolicy;
		this.processingMode = processingMode;

		System.gc();
	}

	public void init(List<ThueRule> rulesList, String dataInit, ProcessingMode processingMode) {
		this.init(rulesList, dataInit, TokensProcessingOrder.RANDOM, RuleSelectionPolicy.RANDOM, processingMode);
	}

	public void init(List<ThueRule> rulesList, String dataInit) {
		this.init(rulesList, dataInit, ProcessingMode.TRACE);
	}

	public void setDelay(int delay) {
		this.xecutionDelay = delay;
	}

	public int getDelay() {
		return xecutionDelay;
	}

	// --- the job
	public void doRun() throws InterruptedException {
		for (IThueInterpreterRuntimeListener runtimeListener : runtimeListeners)
			runtimeListener.interpreterStarted();
		try {
			x: while (!this.stopRequested && doStep()) {
				switch (this.processingMode) {
				case RUN:
					break;
				case TRACE:
					Thread.sleep(this.xecutionDelay / 2);
					break;
				case ONE_STEP:
					Thread.sleep(this.xecutionDelay / 2);
					break x;
				}
			}
			stopRequested = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (IThueInterpreterRuntimeListener runtimeListener : runtimeListeners)
			runtimeListener.interpreterStopped();
	}

	protected boolean doStep() throws InterruptedException {
		boolean result = false;
		ThueRule rule = findRule();
		int tokenStart = -1;
		int tokenEnd = -1;

		if (rule == null) {
			ruleIndex = -1;
		} else {
			result = true;

			String match = rule.getMatch();
			int idx = data.indexOf(match); // Is there at least one matching
											// token so the rule can be applied?

			switch (this.tokensProcessingOrder) {
			case RANDOM:
				List<Integer> tokenCandidates = new ArrayList<Integer>();
				do {
					tokenCandidates.add(idx);
					idx = data.indexOf(match, idx + 1);
				} while (idx > 0);

				int i = rnd.nextInt(tokenCandidates.size());
				tokenStart = tokenCandidates.get(i);
				break;
			case LEFT_TO_RIGHT:
				tokenStart = idx;
				break;
			case RIGHT_TO_LEFT:
				tokenStart = data.lastIndexOf(match);
				break;
			}
			tokenEnd = tokenStart + match.length();
		}

		if (!this.processingMode.equals(ProcessingMode.RUN)) {
			ThueInterpreterStepEvent stepEvent = new ThueInterpreterStepEvent(ruleIndex, tokenStart, tokenEnd);
			for (IThueInterpreterStepListener stepListener : stepListeners)
				stepListener.handleEvent(stepEvent);
			Thread.sleep(this.xecutionDelay / 2);
		}

		if (rule != null)
			applyRule(rule, tokenStart, tokenEnd);

		return result;
	}

	protected void applyRule(ThueRule rule, int tokenStart, int tokenEnd) {
		data.delete(tokenStart, tokenEnd);
		String replacement = rule.getReplace();
		while (replacement.indexOf(":::") >= 0) {
			String value = JOptionPane.showInputDialog("Enter value");
			replacement = replacement.replaceFirst("\\:\\:\\:", value);
		}
		replacement = replacement.replace("\\n", "\n");
		if (replacement.startsWith("~")) {
			for (IThueInterpreterDataChangeListener dataChangeListener : dataChangeListeners)
				dataChangeListener.dataChanged(this.getData(), tokenStart, tokenStart + 1);
			for (IThueInterpreterOutputHandler outputHandler : outputHandlers)
				outputHandler.output(replacement.substring(1));
		} else {
			data.insert(tokenStart, replacement);
			for (IThueInterpreterDataChangeListener dataChangeListener : dataChangeListeners)
				dataChangeListener.dataChanged(this.getData(), tokenStart, tokenStart + replacement.length());
		}
	}

	protected ThueRule findRule() {
		ThueRule result = null;
		boolean[] checkedRules = new boolean[rulesList.size()];

		int checkruleCounter = 0;
		for (int i = 0; i < checkedRules.length; i++)
			checkedRules[i] = false;

		if (ruleSelectionPolicy.equals(RuleSelectionPolicy.RULELIST_ASC))
			ruleIndex = -1;
		else if (ruleSelectionPolicy.equals(RuleSelectionPolicy.RULELIST_DESC))
			ruleIndex = rulesList.size();

		while (checkruleCounter < rulesList.size()) {
			checkruleCounter++;
			switch (this.ruleSelectionPolicy) {
			case RANDOM:
				do {
					ruleIndex = rnd.nextInt(rulesList.size());
				} while (checkedRules[ruleIndex]);
				break;
			case RULELIST_ASC:
				ruleIndex++;
				break;
			case RULELIST_DESC:
				ruleIndex--;
				break;
			}
			checkedRules[ruleIndex] = true;
			ThueRule rule = rulesList.get(ruleIndex);
			if (data.indexOf(rule.getMatch()) >= 0) {
				result = rule; // this rule is applicable
				break;
			}
		}

		return result;
	}

	// --- Public interface
	public String getData() {
		return data.toString();
	}

	public ThueInterpreter addStepListener(IThueInterpreterStepListener stepListener) {
		this.stepListeners.add(stepListener); // TODO: Synchronize
		return this;
	}

	public ThueInterpreter addOutputHandlers(IThueInterpreterOutputHandler outputHandler) {
		this.outputHandlers.add(outputHandler); // TODO: Synchronize
		return this;
	}

	public ThueInterpreter addDataChangeListener(IThueInterpreterDataChangeListener dataChangeListener) {
		this.dataChangeListeners.add(dataChangeListener); // TODO: Synchronize
		return this;
	}

	public ThueInterpreter addRuntimeListener(IThueInterpreterRuntimeListener runtimeListener) {
		this.runtimeListeners.add(runtimeListener); // TODO: Synchronize
		return this;
	}

	public void removeRuntimeListener(IThueInterpreterRuntimeListener runtimeListener) {
		this.runtimeListeners.remove(runtimeListener); // TODO: Synchronize
	}

	public void removeStepListener(IThueInterpreterStepListener stepListener) {
		this.stepListeners.remove(stepListener); // TODO: Synchronize
	}

	public void removeOutputHandlers(IThueInterpreterOutputHandler outputHandler) {
		this.outputHandlers.remove(outputHandler); // TODO: Synchronize
	}

	public void removeDataChangeListener(IThueInterpreterDataChangeListener dataChangeListener) {
		this.dataChangeListeners.remove(dataChangeListener); // TODO:
																// Synchronize
	}

	public void requestStop() {
		this.stopRequested = true;
	}

	public TokensProcessingOrder getTokensProcessingOrder() {
		return tokensProcessingOrder;
	}

	public void setTokensProcessingOrder(TokensProcessingOrder tokensProcessingOrder) {
		this.tokensProcessingOrder = tokensProcessingOrder;
	}

	public RuleSelectionPolicy getRuleSelectionPolicy() {
		return ruleSelectionPolicy;
	}

	public void setRuleSelectionPolicy(RuleSelectionPolicy ruleSelectionPolicy) {
		this.ruleSelectionPolicy = ruleSelectionPolicy;
	}

	public ProcessingMode getProcessingMode() {
		return processingMode;
	}

	public void setProcessingMode(ProcessingMode processingMode) {
		this.processingMode = processingMode;
	}

	public void setData(String text) {
		this.data = new StringBuffer(text);
	}
}
