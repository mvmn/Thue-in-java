package x.mvmn.misc.thue.gui;

import javax.swing.table.AbstractTableModel;

import x.mvmn.misc.thue.model.ThueRule;


import java.util.List;

public class ThueRuleTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -6939690289669716780L;
	private List<ThueRule> rules;
	private static final String[] COLUMNS_NAMES = { "Token", "Replacement" };

	public ThueRuleTableModel(List<ThueRule> rules) {
		this.rules = rules;
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return rules.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		ThueRule rule = rules.get(rowIndex);
		return (columnIndex == 0) ? rule.getMatch() : rule.getReplace();
	}

	public String getColumnName(int colIdx) {
		String result = "";

		if (colIdx < COLUMNS_NAMES.length)
			result = COLUMNS_NAMES[colIdx];

		return result;
	}

	public void addRule(ThueRule rule) {
		rules.add(rule);
		this.fireTableRowsInserted(rules.size() - 1, rules.size() - 1);
	}

	public void insertRule(ThueRule rule, int index) {
		rules.add(index, rule);
		this.fireTableRowsInserted(index, index);
	}

	public void deleteRule(int index) {
		if (index >= 0 && index < rules.size()) {
			rules.remove(index);
			this.fireTableRowsDeleted(index, index);
		}
	}
}
