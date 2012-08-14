package x.mvmn.misc.thue.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import x.mvmn.misc.TextFilesHelper;
import x.mvmn.misc.thue.interpreter.ThueInterpreter;
import x.mvmn.misc.thue.interpreter.ThueInterpreter.ProcessingMode;
import x.mvmn.misc.thue.interpreter.ThueInterpreter.RuleSelectionPolicy;
import x.mvmn.misc.thue.interpreter.ThueInterpreter.TokensProcessingOrder;
import x.mvmn.misc.thue.interpreter.events.IThueInterpreterDataChangeListener;
import x.mvmn.misc.thue.interpreter.events.IThueInterpreterOutputHandler;
import x.mvmn.misc.thue.interpreter.events.IThueInterpreterRuntimeListener;
import x.mvmn.misc.thue.interpreter.events.IThueInterpreterStepListener;
import x.mvmn.misc.thue.interpreter.events.ThueInterpreterStepEvent;
import x.mvmn.misc.thue.model.ThueRule;
import x.mvmn.misc.thue.parser.ThueParser;

public class ThueSwing extends JPanel {
	private static final long serialVersionUID = 5216134616832263106L;

	protected static final String STR_INTERPRETER_TAB_NAME = "Thue";
	protected static final String STR_SOURCE_TAB_NAME = "Source";

	protected final JTabbedPane tabMainTabsPane = new JTabbedPane();
	protected final JTextArea txAreaSource = new JTextArea();
	protected final JTextArea txAreaData = new JTextArea();
	protected final JTextArea txAreaOutput = new JTextArea();
	protected final JTable tblRules = new JTable();
	protected final JButton btnStart = new JButton(">");
	protected final JButton btnStop = new JButton("[]");
	protected final JComboBox cbxMode = new JComboBox(ThueInterpreter.ProcessingMode.values());
	protected final JComboBox cbxRuleSelPolicy = new JComboBox(ThueInterpreter.RuleSelectionPolicy.values());
	protected final JComboBox cbxTokenProcOrder = new JComboBox(ThueInterpreter.TokensProcessingOrder.values());
	protected final JSlider sldStepDelay = new JSlider(JSlider.HORIZONTAL, 1, 1000, 100);
	protected final JLabel lblStepDelay = new JLabel();
	protected final JButton btnLoadSrc = new JButton("Load...");
	protected final JButton btnSaveSrc = new JButton("Save...");
	protected final JButton btnAddRule = new JButton("+");
	protected final JButton btnRemoveRule = new JButton("-");
	protected final JTextField fldMatch = new JTextField();
	protected final JTextField fldReplacement = new JTextField();
	protected final JPanel rulesControlPanel = new JPanel(new GridLayout(2, 2));
	protected final JButton btnClearOutput = new JButton("Clear output");

	protected final ThueParser parser = new ThueParser();
	protected final ThueInterpreter interpreter = new ThueInterpreter();
	final JFileChooser fc = new JFileChooser();

	public ThueSwing() {
		super();

		tblRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		txAreaOutput.setEditable(false);
		txAreaOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txAreaData.setFont(new Font("Monospaced", Font.PLAIN, 12));
		tblRules.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txAreaSource.setFont(new Font("Monospaced", Font.PLAIN, 12));

		txAreaSource
				.setText("#::=Sierpinski's triangle\n#::=By Nikita Ayzikovsky\n#::=Updated for Thue in Java by Mykola Makhin\nX::=~ \nY::=~*\nZ::=~\\n\n_.::=._X\n_*::=*_Y\n._|::=.Z-|\n*_|::=Z\n..-::=.-.\n**-::=*-.\n*.-::=*-*\n.*-::=.-*\n@.-::=@_.\n@*-::=@_*\n::=\n@_*...............................|\n");

		btnLoadSrc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				int returnVal = fc.showOpenDialog(ThueSwing.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						String content = TextFilesHelper.load(fc.getSelectedFile());
						txAreaSource.setText(content);
					} catch (FileNotFoundException fnfex) {
						JOptionPane.showMessageDialog(ThueSwing.this, "File not found " + fc.getSelectedFile().getAbsolutePath(), "Error loading file",
								JOptionPane.ERROR_MESSAGE);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(ThueSwing.this, e1.getMessage(), "Error loading file", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		btnSaveSrc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(false);
				int returnVal = fc.showSaveDialog(ThueSwing.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						TextFilesHelper.save(fc.getSelectedFile(), txAreaSource.getText());
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(ThueSwing.this, e1.getMessage(), "Error loading file", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		sldStepDelay.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				interpreter.setDelay(sldStepDelay.getValue());
				lblStepDelay.setText(Integer.toString(interpreter.getDelay()));
			}
		});

		cbxRuleSelPolicy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(cbxRuleSelPolicy.getSelectedItem());
				System.out.println(((JComboBox) e.getSource()).getSelectedItem());
				interpreter.setRuleSelectionPolicy((RuleSelectionPolicy) cbxRuleSelPolicy.getSelectedItem());
			}
		});

		cbxTokenProcOrder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				interpreter.setTokensProcessingOrder((TokensProcessingOrder) cbxTokenProcOrder.getSelectedItem());
			}
		});

		cbxMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				interpreter.setProcessingMode((ProcessingMode) cbxMode.getSelectedItem());
			}
		});
		cbxMode.setSelectedItem(interpreter.getProcessingMode());

		interpreter.addDataChangeListener(new IThueInterpreterDataChangeListener() {
			public void dataChanged(final String data, final int idxStart, final int idxEnd) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							txAreaData.setText(data);
							txAreaData.select(idxStart, idxEnd);
							txAreaData.setSelectedTextColor(Color.BLUE);
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		});

		interpreter.addStepListener(new IThueInterpreterStepListener() {
			public void handleEvent(final ThueInterpreterStepEvent stepEvent) {
				final int ruleIndex = stepEvent.getRuleNumber();
				if (ruleIndex >= 0) {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								tblRules.setRowSelectionInterval(ruleIndex, ruleIndex);
								txAreaData.setSelectionStart(stepEvent.getTokenStartIdx());
								txAreaData.setSelectionEnd(stepEvent.getTokenEndIdx());
								txAreaData.setSelectedTextColor(Color.RED);
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		});

		interpreter.addOutputHandlers(new IThueInterpreterOutputHandler() {

			public void output(final String output) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							txAreaOutput.append(output);
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}

		});

		// try {
		// interpreter.addOutputHandlers(new
		// ThueMidiSoundOutput(MidiSystem.getSynthesizer()));
		// } catch (MidiUnavailableException e2) {
		// e2.printStackTrace();
		// } catch (Exception e2) {
		// e2.printStackTrace();
		// }

		interpreter.addRuntimeListener(new IThueInterpreterRuntimeListener() {

			public void interpreterStarted() {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							btnStart.setEnabled(false);
							btnStop.setEnabled(true);
							tabMainTabsPane.setEnabledAt(0, false);
							txAreaData.setEditable(false);
							btnAddRule.setEnabled(false);
							btnRemoveRule.setEnabled(false);
							fldMatch.setEnabled(false);
							fldReplacement.setEnabled(false);
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}

			public void interpreterStopped() {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							btnStart.setEnabled(true);
							btnStop.setEnabled(false);
							tabMainTabsPane.setEnabledAt(0, true);
							txAreaData.setText(interpreter.getData());
							txAreaData.setEditable(true);
							btnAddRule.setEnabled(true);
							btnRemoveRule.setEnabled(true);
							fldMatch.setEnabled(true);
							fldReplacement.setEnabled(true);
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}

		});

		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				interpreter.requestStop();
			}
		});

		btnStart.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				new Thread() {
					public void run() {
						try {
							interpreter.setData(txAreaData.getText());
							interpreter.doRun();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}.start();
			}

		});

		btnAddRule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String match = fldMatch.getText();
				if (match.length() > 0)
					((ThueRuleTableModel) tblRules.getModel()).addRule(new ThueRule(match, fldReplacement.getText()));
				fldMatch.setText("");
				fldReplacement.setText("");
			}
		});

		btnRemoveRule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((ThueRuleTableModel) tblRules.getModel()).deleteRule(tblRules.getSelectedRow());
			}
		});

		tblRules.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					int idx = tblRules.getSelectedRow();
					if (fldMatch.isEnabled() && fldReplacement.isEnabled() && idx >= 0 && idx < tblRules.getRowCount()) {
						fldMatch.setText(tblRules.getValueAt(idx, 0).toString());
						fldReplacement.setText(tblRules.getValueAt(idx, 1).toString());
					}
				}
			}
		});

		btnClearOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txAreaOutput.setText("");
			}
		});

		this.setLayout(new BorderLayout());
		JPanel btnLoadSavePanel = new JPanel();
		btnLoadSavePanel.add(btnLoadSrc);
		btnLoadSavePanel.add(btnSaveSrc);
		JPanel tab1 = new JPanel(new BorderLayout());
		tab1.add(new JScrollPane(txAreaSource), BorderLayout.CENTER);
		tab1.add(btnLoadSavePanel, BorderLayout.SOUTH);
		tabMainTabsPane.addTab(ThueSwing.STR_SOURCE_TAB_NAME, tab1);
		JPanel interpreterPanel = new JPanel();
		interpreterPanel.setLayout(new BorderLayout());
		JSplitPane sptTextAreas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(txAreaData), new JScrollPane(txAreaOutput));
		sptTextAreas.setDividerLocation(0.5);
		sptTextAreas.setResizeWeight(0.5);
		sptTextAreas.setPreferredSize(new Dimension(400, 300));
		JPanel txAreasPanel = new JPanel(new BorderLayout());
		txAreasPanel.add(sptTextAreas, BorderLayout.CENTER);
		txAreasPanel.add(btnClearOutput, BorderLayout.SOUTH);
		JPanel rulesPanel = new JPanel(new BorderLayout());
		rulesPanel.add(new JScrollPane(tblRules), BorderLayout.CENTER);
		rulesControlPanel.add(fldMatch);
		rulesControlPanel.add(fldReplacement);
		rulesControlPanel.add(btnAddRule);
		rulesControlPanel.add(btnRemoveRule);
		rulesPanel.add(rulesControlPanel, BorderLayout.SOUTH);
		JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, txAreasPanel, rulesPanel);
		splitPane2.setDividerLocation(0.8);
		splitPane2.setResizeWeight(0.8);
		interpreterPanel.add(splitPane2, BorderLayout.CENTER);
		JPanel btnPanel = new JPanel(new BorderLayout());
		JPanel btnPanelUp = new JPanel();
		JPanel btnPanelDown = new JPanel();
		btnPanelUp.add(btnStart);
		btnPanelUp.add(btnStop);
		btnPanelUp.add(new JLabel("Mode:"));
		btnPanelUp.add(cbxMode);
		btnPanelDown.add(new JLabel("Rules selection:"));
		btnPanelDown.add(cbxRuleSelPolicy);
		btnPanelDown.add(new JLabel("Tokens processing:"));
		btnPanelDown.add(cbxTokenProcOrder);
		btnPanelUp.add(new JLabel("Trace step delay:"));
		btnPanelUp.add(lblStepDelay);
		btnPanelUp.add(sldStepDelay);
		sldStepDelay.setPreferredSize(new Dimension(300, 20));
		lblStepDelay.setPreferredSize(new Dimension(40, 20));

		btnPanel.add(btnPanelUp, BorderLayout.NORTH);
		btnPanel.add(btnPanelDown, BorderLayout.SOUTH);
		btnStart.setEnabled(true);
		btnStop.setEnabled(false);
		interpreterPanel.add(btnPanel, BorderLayout.SOUTH);

		tabMainTabsPane.addTab(ThueSwing.STR_INTERPRETER_TAB_NAME, interpreterPanel);
		tabMainTabsPane.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				JTabbedPane tabSource = (JTabbedPane) e.getSource();
				if (tabSource.getTitleAt(tabSource.getSelectedIndex()).equals(ThueSwing.STR_INTERPRETER_TAB_NAME)) {
					parser.parse(txAreaSource.getText());
					ThueRuleTableModel tbModel = new ThueRuleTableModel(parser.getRules());
					tblRules.setModel(tbModel);
					tblRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					txAreaData.setText(parser.getInitialDataState());
					txAreaOutput.setText("");
					interpreter.init(parser.getRules(), parser.getInitialDataState(), (ProcessingMode) cbxMode.getSelectedItem());
					sldStepDelay.setValue(interpreter.getDelay());
					lblStepDelay.setText(Integer.toString(interpreter.getDelay()));
				}
			}

		});
		JLabel label = new JLabel("Thue in Java written by Mykola Makhin 2009-03-12. No rights reserved.");
		JPanel creditsPanel = new JPanel();
		creditsPanel.add(label);
		this.add(creditsPanel, BorderLayout.SOUTH);
		this.add(tabMainTabsPane, BorderLayout.CENTER);
	}
}
