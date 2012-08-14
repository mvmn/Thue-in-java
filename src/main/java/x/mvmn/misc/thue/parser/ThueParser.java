package x.mvmn.misc.thue.parser;

import java.util.ArrayList;
import java.util.List;

import x.mvmn.misc.thue.model.ThueRule;


public class ThueParser {
	protected List<ThueRule> rules = new ArrayList<ThueRule>();
	protected String initialDataState = "";

	public String parse(String text) {
		String error = null;

		ArrayList<ThueRule> rules = new ArrayList<ThueRule>();
		initialDataState = "";

		String[] lines = text.split("\n");
		String match = null;
		StringBuffer replacement = new StringBuffer();
		StringBuffer initialDataState = new StringBuffer();
		boolean idsStarted = false;
		for (String line : lines) {
			String lReplacement;
			int eqTokenIndex = line.indexOf("::=");
			if (eqTokenIndex >= 0) {
				if (match != null) {
					if (replacement == null) {
						error = "Replacement is null";
						break;
					}
					rules.add(new ThueRule(match, replacement.toString()));
					match = null;
					replacement = new StringBuffer();
				}
				String lMatch = line.substring(0, eqTokenIndex);
				if (lMatch.trim().length() > 0)
					match = lMatch;
				else {
					idsStarted = true;
					match = null;
				}
				lReplacement = line.substring(eqTokenIndex + "::=".length());
			} else
				lReplacement = line;
			if (idsStarted)
				initialDataState.append(lReplacement);
			else
				replacement.append(lReplacement);
		}

		this.rules = rules;
		this.initialDataState = initialDataState.toString();

		return error;
	}

	public List<ThueRule> getRules() {
		return rules;
	}

	public String getInitialDataState() {
		return initialDataState;
	}

	public static void main(String[] args) { // test
		String source = "#::=Sierpinski's triangle, HTML version\n" + "#::=By Nikita Ayzikovsky\n" + "X::=~&nbsp;\n" + "Y::=~*\n" + "Z::=~<br>\n"
				+ "_.::=._X\n" + "_*::=*_Y\n" + "._|::=.Z-|\n" + "*_|::=Z\n" + "..-::=.-.\n" + "**-::=*-.\n" + "*.-::=*-*\n" + ".*-::=.-*\n" + "@.-::=@_.\n"
				+ "@*-::=@_*\n" + "::=\n" + "@_*...............................|\n";

		ThueParser parser = new ThueParser();
		String error = parser.parse(source);
		if (error == null) {
			List<ThueRule> rules = parser.getRules();
			String ids = parser.getInitialDataState();
			for (ThueRule rule : rules)
				System.out.println("['" + rule.getMatch() + "'] ::= ['" + rule.getReplace() + "']");
			System.out.println("IDS: " + ids);
		} else {
			System.out.println("Parse error " + error);
		}
	}
}
