package org.cloudbus.cloudsim.ext.gui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.cloudbus.cloudsim.ext.Constants;
import org.cloudbus.cloudsim.ext.ResSimulation;
import org.cloudbus.cloudsim.ext.event.CloudSimEvent;
import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;
import org.cloudbus.cloudsim.ext.event.CloudSimEvents;

/**
 * The configuration panel of the simulator. This panel is a tabbed panel.
 * 
 * @author Bhathiya Wickremasinghe, Leroy Jiang
 *
 */
public class ConfigureSimulationPanel extends JPanel 
									  implements ActionListener, CloudSimEventListener, Constants {

	private static final String CMD_COPY_MACHINE = "copy_machine";
	private static final String LBL_COPY = "Copy";
	private static final String CMD_REMOVE_VM_ALLOCATION = "remove_vm_allocation";
	private static final String CMD_ADD_VM_ALLOCATION = "add_vm_allocation";
	private static final String CMD_REMOVE_MACHINE = "Remove Machine";
	private static final String CMD_ADD_MACHINE = "add_machine";
	private static final String CMD_SAVE_CONFIG = "save_config_file";
	public static final String CMD_LOAD_CONFIG = "load_config_from_file";
	public static final String CMD_CANCEL_CONFIGURATION = "cancel_configuration";
	public static final String CMD_DONE_CONFIGURATION = "done_ configuration";
	private static final String CMD_REMOVE_DATACENTER = "remove datacenter";
	private static final String CMD_ADD_NEW_DATACENTER = "add new datacenter";
	private static final String CMD_REMOVE_USERBASE = "remove userbase";
	private static final String CMD_ADD_NEW_USERBASE = "add new userbase";
	
	private static final String COL_AVG_OFF_PEAK_USERS = "Avg Off-Peak \nUsers";
	private static final String COL_AVG_PEAK_USERS = "Avg Peak \nUsers";
	
	private static final String LBL_SAVE_CONFIGURATION = "Save Configuration";
	private static final String LBL_LOAD = "Load Configuration";
	private static final String LBL_CANCEL = "Cancel";
	private static final String LBL_DONE = "Done";
	private static final String LBL_REMOVE = "Remove";
	private static final String LBL_ADD_NEW = "Add New";
	private static final int TABLE_HEIGHT = 80;
	private static final Dimension TABLE_DIMENSION = new Dimension(650, TABLE_HEIGHT);
	private static final Dimension BTN_DIMENSION = new Dimension(100, 25);
	private static final String SIM_FILE_EXTENSION = ".sim";
	private static final String TIME_UNIT_DAYS = "days";
	private static final String TIME_UNIT_HOURS = "hours";
	private static final String TIME_UNIT_MIN = "min";
	
	private JTable userBasesTable;
	private ResSimulation simulation;
	private ActionListener screenListener;
	private JTextField txtSimDuration;
	private JComboBox cmbTimeUnit;
	private JComboBox regionCombo;
	
	/** fileChooser is used for both open and save dialog for configurations. */
	private JFileChooser fileChooser;
	private JTable dataCentersTable;
	
	private JPanel machineListPanel;
	private JTable machineTable;
	private JPanel machineListControlsPanel;
	private JComboBox archCombo;
	private JComboBox osCombo;
	private JComboBox vmmCombo;
	private JLabel lblDcName;
	private JPanel machineDetailsPanel;
	private JTable vmAllocTable;
	private JComboBox dcCombo;
	private JTextField txtUserGroupingFactor;
	private JTextField txtDcRequestGroupingFactor;
	private JTextField txtInstructionLength;
	private JComboBox cmbServiceBroker;
	private JComboBox cmbLoadBalancingPolicy;
	

	/** 
	 * Constructor.
	 * 
	 * @param sim
	 * @param screenListener
	 */
	public ConfigureSimulationPanel(ResSimulation sim, ActionListener screenListener){
		this.simulation = sim;
		this.screenListener = screenListener;
		
		initListLocalCopies();
		
		regionCombo = new JComboBox(new Integer[]{0, 1, 2, 3, 4, 5});
		archCombo = new JComboBox(new String[]{DEFAULT_ARCHITECTURE});
		osCombo = new JComboBox(new String[]{DEFAULT_OS});
		vmmCombo = new JComboBox(new String[]{DEFAULT_VMM});
		initUI();
	}
	
	/**
	 * Creates local (deep) copies of the user base and data center lists. Need copies as we
	 * don't want to update the original copies in {@link Simulation} until user clicks 'Done' button. 
	 */
	private void initListLocalCopies(){
		
	}
	
	/** Sets up the main UI elements */
	private void initUI(){
		
		int leftMargin = 50;
		
		setComponentSize(this, new Dimension(900, 700));
		this.setLayout(null);
		int x = leftMargin;
		int y = 0;
		int compW = 500;
		int compH = leftMargin;
		int hGap = 10;
		int vGap = hGap;
		
		JLabel heading = new JLabel("<html><h1>Configure Simulation</h1></html>");
		heading.setBounds(x, y, compW, compH);
		this.add(heading);
		
		y += compH + 20;
		compH = 500;
		compW = 900;
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Main Configuration", createMainTab());
		tabbedPane.addTab("Advanced", createAdvancedTab());
		tabbedPane.setBounds(x, y, compW, compH);
		this.add(tabbedPane);
		
		y += compH + vGap;
		compW = 700;
		compH = 40;
		JPanel controlPanel = createControlPanel();
		controlPanel.setBounds(x, y, compW, compH);
		this.add(controlPanel);
		
		//Init the file chooser as well.
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				if (f.getAbsolutePath().toLowerCase().endsWith(SIM_FILE_EXTENSION)){
					return true;
				} else {
					return false;
				}
			}

			@Override
			public String getDescription() {
				return SIM_FILE_EXTENSION;
			}});
	}
	
	/**
	 * 
	 * @return
	 */
	private JPanel createMainTab(){
		int leftMargin = 10;
		int x = leftMargin;
		int y = 30;
		int compW = 500;
		int compH = leftMargin;
		int hGap = 10;
		int vGap = 20;
		
		JPanel mainTab = new JPanel();
		mainTab.setLayout(null);
		
		compW = 120; 
		compH = 20;
		JLabel lblSimDuration = new JLabel("Simulation Duration:");
		lblSimDuration.setBounds(x, y, compW, compH);
		mainTab.add(lblSimDuration);
		
		x += compW + vGap; 
		compW = 70; 
		txtSimDuration = new JTextField("" + (222333223) / (60000));
		txtSimDuration.setBounds(x, y, compW, compH);
		mainTab.add(txtSimDuration);
		
		x += compW + vGap;
		cmbTimeUnit = new JComboBox(new String[]{TIME_UNIT_MIN, TIME_UNIT_HOURS, TIME_UNIT_DAYS});
		cmbTimeUnit.setBounds(x, y, compW, compH);
		mainTab.add(cmbTimeUnit);
		
		x = leftMargin; 
		y += compH + vGap; 
		compW = 70; 
		JLabel lblUbHeading = new JLabel("User bases:");
		lblUbHeading.setBounds(x, y, compW, compH);
		mainTab.add(lblUbHeading);
		
				
		x = leftMargin;
		y += compH + vGap;
		compW = 80; 
		compH = 60;
		JLabel lblVmHeading = new JLabel("<html>Application<br/>Deployment<br/>Configuration:</html>");
		lblVmHeading.setBounds(x, y, compW, compH);
		mainTab.add(lblVmHeading);
		
		x += compW + hGap * 2;
		compW = 150; 
		compH = 20;
		JLabel lblServiceBroker = new JLabel("Service Broker Policy:");
		lblServiceBroker.setBounds(x, y, compW, compH);
		mainTab.add(lblServiceBroker);
		
		return mainTab;
	}
	

	/**
	 * @return
	 */
	private JPanel createAdvancedTab(){
		int leftMargin = 50;
		int x = leftMargin;
		int y = 50;
		int vGap = 20;
		
		JPanel advancedTab = new JPanel();
		advancedTab.setLayout(null);
		
		int compW = 500;
		int compH = 20;
		
		compW = 240;
		int lastCompH = compH = 60;
		JLabel lblUserGroup = new JLabel("<html>User grouping factor in User Bases:" +
										 "<br/>(Equivalent to number of simultaneous" +
										 "<br/> users from a single user base)</html>");
		lblUserGroup.setBounds(x, y, compW, compH);
		advancedTab.add(lblUserGroup);
		
		x += compW + vGap;
		y += 10;
		compW = 80;
		compH = 20;
		txtUserGroupingFactor = new JTextField("" + 123);
		txtUserGroupingFactor.setBounds(x, y, compW, compH);
		advancedTab.add(txtUserGroupingFactor);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 240;
		lastCompH = compH = 70;
		JLabel lblDcRequestGrouping = new JLabel("<html>Request grouping factor in Data Centers:" +
				                                  "<br/>(Equivalent to number of simultaneous" +
				                                  "<br/> requests a single applicaiton server" +
				                                  "<br/> instance can support.) </html>");
		lblDcRequestGrouping.setBounds(x, y, compW, compH);
		advancedTab.add(lblDcRequestGrouping);
		
		x += compW + vGap;
		y += 10;
		compW = 80;
		compH = 20;
		txtDcRequestGroupingFactor = new JTextField("" + 234);
		txtDcRequestGroupingFactor.setBounds(x, y, compW, compH);
		advancedTab.add(txtDcRequestGroupingFactor);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 240;
		compH = 30;
		JLabel lblInstructionLength = new JLabel("<html>Executable instruction length per request:" +
				                                  "<br/>(bytes)</html>");
		lblInstructionLength.setBounds(x, y, compW, compH);
		advancedTab.add(lblInstructionLength);
		
		x += compW + vGap;
		compW = 80;
		compH = 20;
		txtInstructionLength = new JTextField("" + 222);
		txtInstructionLength.setBounds(x, y, compW, compH);
		advancedTab.add(txtInstructionLength);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 240;
		compH = 30;
		JLabel lblLoadBalancing = new JLabel("<html>Load balancing policy<br/>" +
				                                  "across VM's in a single Data Center:</html>");
		lblLoadBalancing.setBounds(x, y, compW, compH);
		advancedTab.add(lblLoadBalancing);
		
		x += compW + vGap;
		compW = 240;
		compH = 20;
		cmbLoadBalancingPolicy = new JComboBox(new String[]{"EEE", "BBB"
		});
		cmbLoadBalancingPolicy.setSelectedItem("EEE");
		cmbLoadBalancingPolicy.setBounds(x, y, compW, compH);
		advancedTab.add(cmbLoadBalancingPolicy);
		
		return advancedTab;
	}
	
	/**
	 * Used to set size restriction on a component.
	 * @param comp
	 * @param size
	 */
	private void setComponentSize(JComponent comp, Dimension size){
		comp.setPreferredSize(size);
		comp.setMinimumSize(size);
		comp.setMaximumSize(size);
	}
		
	
	
	private JPanel createControlPanel(){
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
		
		JButton btnCancel = addButton(controlPanel, LBL_CANCEL, CMD_CANCEL_CONFIGURATION);
		btnCancel.addActionListener(screenListener);
		JButton btnLoad = addButton(controlPanel, LBL_LOAD, CMD_LOAD_CONFIG);
		btnLoad.addActionListener(screenListener);	
		JButton btnSave = addButton(controlPanel, LBL_SAVE_CONFIGURATION, CMD_SAVE_CONFIG);
		btnSave.addActionListener(screenListener);	
		JButton btnDone = addButton(controlPanel, LBL_DONE, CMD_DONE_CONFIGURATION);
		btnDone.addActionListener(screenListener);
		
		return controlPanel;
	}

	private JButton addButton(JPanel pnlUBControls, String label, String actionCommand) {
		JButton btn = new JButton(label);
		
		FontMetrics fm = this.getFontMetrics(this.getFont());
		int labelWidth = fm.stringWidth(label);
		if (labelWidth < BTN_DIMENSION.getWidth()){
			btn.setPreferredSize(BTN_DIMENSION);
			btn.setMaximumSize(BTN_DIMENSION);
			btn.setMinimumSize(BTN_DIMENSION);
		} else {
			Dimension dimension = new Dimension(labelWidth + 40, (int) BTN_DIMENSION.getHeight());
			btn.setPreferredSize(dimension);
			btn.setMaximumSize(dimension);
			btn.setMinimumSize(dimension);
		}
		btn.setActionCommand(actionCommand);
		btn.addActionListener(this);
		pnlUBControls.add(btn);
		pnlUBControls.add(Box.createVerticalStrut(10));
		
		return btn;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CMD_DONE_CONFIGURATION)){
			finishConfiguration();
		} else if (e.getActionCommand().equals(CMD_CANCEL_CONFIGURATION)){
			initListLocalCopies();
		} else if (e.getActionCommand().equals(CMD_LOAD_CONFIG)){
			loadSimulationFromFile();
		} else if (e.getActionCommand().equals(CMD_SAVE_CONFIG)){
			saveSimulation();
		} 
	}

	private void finishConfiguration() {		
		
	}
	
	public boolean isValidConfiguration(){
		
		return true;
	}	
	
	/**
	 * Saves the simulation to a file.
	 */
	private void saveSimulation() {
		
	}
	
	/**
	 * Loads simulation from a file.
	 */
	private void loadSimulationFromFile() {
				
	}
	
	public void cloudSimEventFired(CloudSimEvent e) {
		
	}
}
