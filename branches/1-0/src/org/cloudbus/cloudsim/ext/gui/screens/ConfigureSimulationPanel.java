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

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.ext.Constants;
import org.cloudbus.cloudsim.ext.ResSimulation;
import org.cloudbus.cloudsim.ext.event.CloudSimEvent;
import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;
import org.cloudbus.cloudsim.ext.event.CloudSimEvents;
import org.cloudbus.cloudsim.ext.gga.Capacity;
import org.cloudbus.cloudsim.ext.utils.IOUtil;

/**
 * The configuration panel of the simulator. This panel is a tabbed panel.
 * 
 * @author Bhathiya Wickremasinghe, Leroy Jiang
 *
 */
public class ConfigureSimulationPanel extends JPanel 
									  implements ActionListener, CloudSimEventListener, Constants {

	private static final String CMD_SAVE_CONFIG = "save_config_file";
	public static final String CMD_LOAD_CONFIG = "load_config_from_file";
	public static final String CMD_LOAD_WORKLOAD = "load_workload_from_file";
	public static final String CMD_CANCEL_CONFIGURATION = "cancel_configuration";
	public static final String CMD_DONE_CONFIGURATION = "done_ configuration";
	
	private static final String LBL_LOAD = "Load Config File";
	private static final String LBL_LOAD_WORKLOAD = "Load WorkLoad";
	private static final String LBL_CANCEL = "Cancel";
	private static final String LBL_DONE = "Done";
	private static final Dimension BTN_DIMENSION = new Dimension(100, 25);
	
	private ResSimulation simulation;
	private ActionListener screenListener;
	
	/** fileChooser is used for both open and save dialog for configurations. */
	private JFileChooser fileChooser;
	
	/** Workload configurations. */
	private JComboBox cmbWorkloadSize;
	private JTextField txtHostCPU;
	private JTextField txtHostRam;
	private JTextField txtHostStorage;
	private JTextField txtHostBw;
	
	/** Network configurations. */
	private JTextField txtNetworkFirstLayer;
	private JTextField txtNetworkSecondLayer;
	private JTextField txtNetworkThirdLayer;
	
	/** GGA configurations. */
	private JTextField txtGGAGenerations;
	private JComboBox cmbGGAPopulationSize;
	private JTextField txtGGACrossover;
	private JTextField txtGGAMutation;
	private JTextField txtGGAMutationProb;
	

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
		tabbedPane.addTab("Main Confuguration", createMainTab());
		tabbedPane.addTab("Network Confuguration", createNetworkTab());
		tabbedPane.addTab("GGA Confuguration", createAdvancedTab());
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
				if (f.getAbsolutePath().toLowerCase().endsWith(Constants.SIM_FILE_EXTENSION)){
					return true;
				} else {
					return false;
				}
			}

			@Override
			public String getDescription() {
				return Constants.SIM_FILE_EXTENSION;
			}});
	}
	
	/**
	 * 
	 * @return
	 */
	private JPanel createMainTab(){
		int leftMargin = 50;
		int x = leftMargin;
		int y = 50;
		int vGap = 20;
		
		JPanel mainTab = new JPanel();
		mainTab.setLayout(null);
		
		int compW = 500;
		int compH = 20;
		
		compW = 150;
		int lastCompH = compH = 30;
		JLabel lblWorkloadSize = new JLabel("<html>Workload Size:<br/>" +
				                                  "workload size:</html>");
		lblWorkloadSize.setBounds(x, y, compW, compH);
		mainTab.add(lblWorkloadSize);
		
		x += compW + vGap;
		compW = 150;
		compH = 20;
		cmbWorkloadSize = new JComboBox(new Integer[]{50, 300, 400, 1000
		});
		cmbWorkloadSize.setSelectedItem(simulation.getWorkloadSize());
		cmbWorkloadSize.setBounds(x, y, compW, compH);
		mainTab.add(cmbWorkloadSize);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 150;
		lastCompH = compH = 30;
		JLabel lblHostRam = new JLabel("<html>Host ram:" +
										 "<br/>units: (MB)</html>");
		lblHostRam.setBounds(x, y, compW, compH);
		mainTab.add(lblHostRam);
		
		x += compW + vGap;
		compW = 80;
		compH = 20;
		txtHostRam = new JTextField("" + simulation.getHostRam());
		txtHostRam.setBounds(x, y, compW, compH);
		mainTab.add(txtHostRam);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 150;
		compH = 30;
		JLabel lblHostCPU = new JLabel("<html>Host CPU:" +
				                                  "<br/>Units: (MHz)</html>");
		lblHostCPU.setBounds(x, y, compW, compH);
		mainTab.add(lblHostCPU);
		
		x += compW + vGap;
		compW = 80;
		compH = 20;
		txtHostCPU = new JTextField("" + simulation.getHostCpu());
		txtHostCPU.setBounds(x, y, compW, compH);
		mainTab.add(txtHostCPU);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 150;
		compH = 30;
		JLabel lblHostStorage = new JLabel("<html>Host Storage:" +
				                                  "<br/>units: (MB)</html>");
		lblHostStorage.setBounds(x, y, compW, compH);
		mainTab.add(lblHostStorage);
		
		x += compW + vGap;
		compW = 80;
		compH = 20;
		txtHostStorage = new JTextField("" + simulation.getHostStorage());
		txtHostStorage.setBounds(x, y, compW, compH);
		mainTab.add(txtHostStorage);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 150;
		compH = 30;
		JLabel lblHostBw = new JLabel("<html>Host bandwidth:" +
				                                  "<br/>units: (MB/sec)</html>");
		lblHostBw.setBounds(x, y, compW, compH);
		mainTab.add(lblHostBw);
		
		x += compW + vGap;
		compW = 80;
		compH = 20;
		txtHostBw = new JTextField("" + simulation.getHostBw());
		txtHostBw.setBounds(x, y, compW, compH);
		mainTab.add(txtHostBw);
		
		return mainTab;
	}
	
	private JPanel createNetworkTab(){
		int leftMargin = 50;
		int x = leftMargin;
		int y = 50;
		int vGap = 20;
		
		JPanel networkTab = new JPanel();
		networkTab.setLayout(null);
		
		int compW = 500;
		int compH = 20;
		
		compW = 240;
		int lastCompH = compH = 60;
		JLabel lblNetworkFirstLayer = new JLabel("<html>Network first layer:" +
										 "<br/>(Equivalent to first layer</html>");
		lblNetworkFirstLayer.setBounds(x, y, compW, compH);
		networkTab.add(lblNetworkFirstLayer);
		
		x += compW + vGap;
		y += 10;
		compW = 80;
		compH = 20;
		txtNetworkFirstLayer = new JTextField("" + simulation.getFirstLayer());
		txtNetworkFirstLayer.setBounds(x, y, compW, compH);
		networkTab.add(txtNetworkFirstLayer);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 240;
		lastCompH = compH = 70;
		JLabel lblNetworkSecondLayer = new JLabel("<html>Network second layer:" +
												"<br/>(Equivalent to second layer</html>");
		lblNetworkSecondLayer.setBounds(x, y, compW, compH);
		networkTab.add(lblNetworkSecondLayer);	
		
		x += compW + vGap;
		y += 10;
		compW = 80;
		compH = 20;
		txtNetworkSecondLayer = new JTextField("" + simulation.getSecondLayer());
		txtNetworkSecondLayer.setBounds(x, y, compW, compH);
		networkTab.add(txtNetworkSecondLayer);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 240;
		compH = 30;
		JLabel lblNetworkThirdLayer = new JLabel("<html>Network third layer:" +
				                                  "<br/>third layer</html>");
		lblNetworkThirdLayer.setBounds(x, y, compW, compH);
		networkTab.add(lblNetworkThirdLayer);
		
		x += compW + vGap;
		compW = 80;
		compH = 20;
		txtNetworkThirdLayer = new JTextField("" + simulation.getThirdLayer());
		txtNetworkThirdLayer.setBounds(x, y, compW, compH);
		networkTab.add(txtNetworkThirdLayer);
		
		return networkTab;
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
		JLabel lblGGAGens = new JLabel("<html>GGA run generations:" +
										 "<br/>(Equivalent to number of generations" +
										 "<br/> the gga runs)</html>");
		lblGGAGens.setBounds(x, y, compW, compH);
		advancedTab.add(lblGGAGens);
		
		x += compW + vGap;
		y += 10;
		compW = 80;
		compH = 20;
		txtGGAGenerations = new JTextField("" + simulation.getGgaGens());
		txtGGAGenerations.setBounds(x, y, compW, compH);
		advancedTab.add(txtGGAGenerations);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 240;
		lastCompH = compH = 70;
		JLabel lblGGACrossover = new JLabel("<html>Crossover times:" +
				                                  "<br/>(Equivalent to number of crossovers" +
				                                  "<br/> of a generation in gga</html>");
		lblGGACrossover.setBounds(x, y, compW, compH);
		advancedTab.add(lblGGACrossover);
		
		x += compW + vGap;
		y += 10;
		compW = 80;
		compH = 20;
		txtGGACrossover = new JTextField("" + simulation.getCrossover());
		txtGGACrossover.setBounds(x, y, compW, compH);
		advancedTab.add(txtGGACrossover);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 240;
		compH = 30;
		JLabel lblGGAMutations = new JLabel("<html>Mutations Per Generation:" +
				                                  "<br/>the mutations</html>");
		lblGGAMutations.setBounds(x, y, compW, compH);
		advancedTab.add(lblGGAMutations);
		
		x += compW + vGap;
		compW = 80;
		compH = 20;
		txtGGAMutation = new JTextField("" + simulation.getMutations());
		txtGGAMutation.setBounds(x, y, compW, compH);
		advancedTab.add(txtGGAMutation);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 240;
		compH = 30;
		JLabel lblGGAMutationProb = new JLabel("<html>Mutation probs:" +
				                                  "<br/>the mutation probs.</html>");
		lblGGAMutationProb.setBounds(x, y, compW, compH);
		advancedTab.add(lblGGAMutationProb);
		
		x += compW + vGap;
		compW = 80;
		compH = 20;
		txtGGAMutationProb = new JTextField("" + simulation.getMutationProb());
		txtGGAMutationProb.setBounds(x, y, compW, compH);
		advancedTab.add(txtGGAMutationProb);
		
		x = leftMargin;
		y += lastCompH + vGap;
		compW = 240;
		compH = 30;
		JLabel lblGGAPopulationSize = new JLabel("<html>Population Size:<br/>" +
				                                  "the whole population size in gga:</html>");
		lblGGAPopulationSize.setBounds(x, y, compW, compH);
		advancedTab.add(lblGGAPopulationSize);
		
		x += compW + vGap;
		compW = 240;
		compH = 20;
		cmbGGAPopulationSize = new JComboBox(new Integer[]{100, 300, 1000
		});
		cmbGGAPopulationSize.setSelectedItem(simulation.getPopulationSize());
		cmbGGAPopulationSize.setBounds(x, y, compW, compH);
		advancedTab.add(cmbGGAPopulationSize);
		
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
		
		JButton btnLoad = addButton(controlPanel, LBL_LOAD_WORKLOAD, CMD_LOAD_WORKLOAD);
		btnLoad.addActionListener(screenListener);
		JButton btnCancel = addButton(controlPanel, LBL_CANCEL, CMD_CANCEL_CONFIGURATION);
		btnCancel.addActionListener(screenListener);
		/*JButton btnLoad = addButton(controlPanel, LBL_LOAD, CMD_LOAD_CONFIG);
		btnLoad.addActionListener(screenListener);*/	
		//JButton btnSave = addButton(controlPanel, LBL_SAVE_CONFIGURATION, CMD_SAVE_CONFIG);
		//btnSave.addActionListener(screenListener);
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
		} else if (e.getActionCommand().equals(CMD_LOAD_WORKLOAD)){
			loadWorkloadFromFile();
		}
	}

	private void loadWorkloadFromFile() {
		simulation.setWorkloadMethod(Constants.WORKLOAD_FROM_XML);
		cmbWorkloadSize.setEnabled(false);
		txtHostCPU.setEnabled(false);
		txtHostRam.setEnabled(false);
		txtHostBw.setEnabled(false);
		txtHostStorage.setEnabled(false);
		
		fileChooser.setDialogTitle("Open Workload File");
		int status =fileChooser.showOpenDialog(this);
		if (status == JFileChooser.APPROVE_OPTION) {
			File simFile = fileChooser.getSelectedFile();
			simulation.setWorkloadFile(simFile.getAbsolutePath());
			
			List<Capacity> workload;
			
			try {
				workload = (List<Capacity>) IOUtil.loadFromXml(simFile);
			} catch (IOException e) {
				e.printStackTrace();
				workload = null;
			}
			
			cmbWorkloadSize.setSelectedItem(workload.size()-1);
			Capacity host = workload.get(0);
			txtHostCPU.setText(""+host.getCpu());
			txtHostRam.setText(""+host.getMem());
			txtHostBw.setText(""+host.getBandwidth());
			txtHostStorage.setText(""+host.getDisk());
			
		} else {
			cmbWorkloadSize.setEnabled(true);
			txtHostCPU.setEnabled(true);
			txtHostRam.setEnabled(true);
			txtHostBw.setEnabled(true);
			txtHostStorage.setEnabled(true);
			simulation.setWorkloadMethod(Constants.WORKLOAD_AUTO_GEN);
		}
	}

	private void finishConfiguration() {
		simulation.setGgaGens(Integer.parseInt(txtGGAGenerations.getText().trim()));
		simulation.setMutations(Integer.parseInt(txtGGAMutation.getText().trim()));
		simulation.setMutationProb(Double.parseDouble(txtGGAMutationProb.getText().trim()));
		simulation.setPopulationSize((Integer)(cmbGGAPopulationSize.getSelectedItem()));
		simulation.setCrossover(Integer.parseInt(txtGGACrossover.getText().trim()));
		
		simulation.setFirstLayer(Integer.parseInt(txtNetworkFirstLayer.getText().trim()));
		simulation.setSecondLayer(Integer.parseInt(txtNetworkSecondLayer.getText().trim()));
		simulation.setThirdLayer(Integer.parseInt(txtNetworkThirdLayer.getText().trim()));
		
		simulation.setWorkloadSize((Integer)cmbWorkloadSize.getSelectedItem());
		simulation.setHostCpu(Integer.parseInt(txtHostCPU.getText().trim()));
		simulation.setHostBw(Integer.parseInt(txtHostBw.getText().trim()));
		simulation.setHostRam(Integer.parseInt(txtHostRam.getText().trim()));
		simulation.setHostStorage(Integer.parseInt(txtHostStorage.getText().trim()));
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
