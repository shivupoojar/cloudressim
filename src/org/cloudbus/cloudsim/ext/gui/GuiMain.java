package org.cloudbus.cloudsim.ext.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.cloudbus.cloudsim.ext.Constants;
import org.cloudbus.cloudsim.ext.ResSimulation;
import org.cloudbus.cloudsim.ext.event.CloudSimEvent;
import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;
import org.cloudbus.cloudsim.ext.event.CloudSimEvents;
import org.cloudbus.cloudsim.ext.gui.screens.ConfigureSimulationPanel;
import org.cloudbus.cloudsim.ext.gui.screens.ResultsScreen;
import org.cloudbus.cloudsim.ext.gui.screens.SimulationPanel;

import org.cloudbus.cloudsim.ext.gui.utils.SimpleGraph;

/**
 * The main class of the GUI. Sets up the UI, and controls the screen transitions.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class GuiMain extends JFrame implements ActionListener, CloudSimEventListener {

private static final String CMD_ABOUT = "About";
	private static final String CMD_DISPLAY_RESULTS = "display_results";
	private static final String CMD_CANCEL_SIMULATION = "Cancel_simulation";
	private static final String CMD_SHOW_BOUNDARIES = "show_boundaries";
	private static final String HOME_SCREEN = "home screen";
	private static final String CONFIG_SCREEN = "configScreen";
	private static final String CMD_RUN_SIMULATION = "Run Simulation";
	private static final String CMD_EXIT = "Exit";
	private static final String CMD_CONFIGURE_SIMULATION = "Configure Simulation";	
	private static final Dimension MENU_BUTTON_SIZE = new Dimension(120, 40);
	private static final Dimension FRAME_SIZE = new Dimension(800, 600);
	private static final int MENU_BTN_V_GAP = 10;
	
	private CardLayout screenController;
	private JPanel mainPanel;
	private ConfigureSimulationPanel configScreen;
	private SimulationPanel simulationPanel;
	private ResultsScreen resultsScreen;
	private ResSimulation simulation;
	private Map<String, JButton> menuButtons;
	private JProgressBar progressBar;
	private JPanel messagePanel;
	private JPanel busyMessagePnl;
	private JToggleButton btnShowBoundaries;
	private boolean simulationStarted = false;
	private boolean simulationFinished = false;
	private JPanel simulationControlPanel;
	private JButton btnCancelSim;
	private JDialog resultsDlg;
	private JButton btnResults;
	private JButton btnExportResults;
	private JDialog abtDlg;
	
	private Map<String, Object> results;
	
	/** No args constructor */
	public GuiMain(int workloadSize) throws Exception{	
		simulation = new ResSimulation(this, workloadSize);
		
		/* fxxx the gui
		initUI();
		showHomeScreen();
		*/
	}
	
	private void initUI(){		
		this.setTitle("CloudResSim");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(FRAME_SIZE);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		this.setJMenuBar(getSimMenuBar());
		
		menuButtons = new HashMap<String, JButton>();
		this.add(getMenuPanel(), BorderLayout.WEST);
		
		mainPanel = new JPanel();
		screenController = new CardLayout();
		mainPanel.setLayout(screenController);
		
		JScrollPane scrollPanel = new JScrollPane(mainPanel);
		this.add(scrollPanel, BorderLayout.CENTER);
	}
	
	private void showHomeScreen(){
		enableMenuPanel();
		
		if (simulationPanel == null) {//simulationPanel == null){
			JPanel simulationScreen = new JPanel();
			simulationScreen.setLayout(new BorderLayout());
			
			simulationPanel = new SimulationPanel(simulation.getGgaGens());
			simulationScreen.add(simulationPanel, BorderLayout.CENTER);
			
			messagePanel = new JPanel();
			Dimension dimension = new Dimension(500, 60);
			messagePanel.setPreferredSize(dimension);
			messagePanel.setMinimumSize(dimension);
			messagePanel.setMaximumSize(dimension);
			simulationScreen.add(messagePanel, BorderLayout.NORTH);
			
			simulationControlPanel = new JPanel();
			simulationControlPanel.setLayout(new BoxLayout(simulationControlPanel, BoxLayout.X_AXIS));
			btnShowBoundaries = new JToggleButton("Show Region Boundaries");
			btnShowBoundaries.setActionCommand(CMD_SHOW_BOUNDARIES);
			btnShowBoundaries.addActionListener(this);
			simulationControlPanel.add(Box.createHorizontalGlue());
			//simulationControlPanel.add(btnShowBoundaries);
			
			simulationControlPanel.setBorder(new EmptyBorder(5, 5, 25, 25));
			simulationScreen.add(simulationControlPanel, BorderLayout.SOUTH);
			
			mainPanel.add(HOME_SCREEN, simulationScreen);
		}
		
		if (simulationStarted){
			
			if (btnCancelSim == null){
				btnCancelSim = new JButton("Cancel Simulation");
				btnCancelSim.setActionCommand(CMD_CANCEL_SIMULATION);
				btnCancelSim.addActionListener(this);
			}
			simulationControlPanel.remove(btnCancelSim);
			simulationControlPanel.add(Box.createHorizontalStrut(10));
			simulationControlPanel.add(btnCancelSim);
		} else if (simulationFinished) {
			simulationControlPanel.remove(btnCancelSim);
			
			if (btnResults == null){
				btnResults = new JButton("Display Detailed Results");
				btnResults.setActionCommand(CMD_DISPLAY_RESULTS);
				btnResults.addActionListener(this);
				simulationControlPanel.add(Box.createHorizontalStrut(10));
				simulationControlPanel.add(btnResults);
			}
			
		}
		
		screenController.show(mainPanel, HOME_SCREEN);
		this.validate();
		this.repaint();
	}
	
	private void showConfigureScreen(){
		
		disableMenuPanel();
		
		if (configScreen == null){
			configScreen = new ConfigureSimulationPanel(simulation, this);
			mainPanel.add(CONFIG_SCREEN, configScreen);
		}
		screenController.show(mainPanel, CONFIG_SCREEN);
		this.validate();
		this.repaint();
	}
	
	private void showResultsScreen(){
		if (resultsDlg == null){
			resultsDlg = new JDialog(this);
			resultsDlg.setLocationRelativeTo(this);
			resultsDlg.setTitle("Simulation Results");
			
			resultsScreen = new ResultsScreen(simulation, results);
			resultsDlg.getContentPane().add(new JScrollPane(resultsScreen));
						
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			resultsDlg.setSize((int) (screenSize.width * 0.8), (int) (screenSize.height * 0.8));
			resultsDlg.setLocation((int) (screenSize.width * 0.1), (int) (screenSize.height * 0.1));
		}
		
		resultsDlg.setVisible(true);
	}
	
	private JPanel getMenuPanel(){
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
		Border bevelBorder = new BevelBorder(BevelBorder.RAISED);
		Border emptyBoarder = new EmptyBorder(20, 10, 10, 10);
		menuPanel.setBorder(new CompoundBorder(bevelBorder, emptyBoarder));
		
		addMenuButton(menuPanel, CMD_CONFIGURE_SIMULATION);
		menuPanel.add(Box.createVerticalStrut(20));
		addMenuButton(menuPanel, CMD_RUN_SIMULATION);
		menuPanel.add(Box.createVerticalStrut(20));
		addMenuButton(menuPanel, CMD_EXIT);
		
		return menuPanel;
	}
	
	private JMenuBar getSimMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menuHelp = new JMenu("Help");
		menuBar.add(menuHelp);
		
		JMenuItem menuAbout = new JMenuItem(CMD_ABOUT);
		menuAbout.setActionCommand(CMD_ABOUT);
		menuAbout.addActionListener(this);
		menuHelp.add(menuAbout);
		
		return menuBar;
	}

	private void addMenuButton(JPanel pnl, String text) {		
		
		JButton btn = new JButton("<html><p align='center'>" + text + "</p></html>");
		menuButtons.put(text, btn);
		btn.setActionCommand(text);
		btn.setMaximumSize(MENU_BUTTON_SIZE);
		btn.setMinimumSize(MENU_BUTTON_SIZE);
		btn.setPreferredSize(MENU_BUTTON_SIZE);
		
		btn.addActionListener(this);
		
		pnl.add(btn);
		pnl.add(Box.createVerticalStrut(MENU_BTN_V_GAP));
		
	}
	
	private void disableMenuPanel() {
		menuButtons.get(CMD_CONFIGURE_SIMULATION).setEnabled(false);
		menuButtons.get(CMD_RUN_SIMULATION).setEnabled(false);
	}
	
	private void enableMenuPanel() {
		menuButtons.get(CMD_CONFIGURE_SIMULATION).setEnabled(true);
		menuButtons.get(CMD_RUN_SIMULATION).setEnabled(true);
	}
		
	private void showBusyMessage(){
		if (busyMessagePnl == null){
			busyMessagePnl = new JPanel();
			busyMessagePnl.add(new JLabel("<html><h2>Simulation Running...</h2></html>"), BorderLayout.NORTH);
			
			progressBar = new JProgressBar(0, simulation.getGgaGens());
			progressBar.setStringPainted(true);
			busyMessagePnl.add(progressBar, BorderLayout.CENTER);
		}
		
		messagePanel.add(busyMessagePnl, BorderLayout.NORTH);
		messagePanel.revalidate();
	}
	
	private void showSimulationCompleteMessage(){
		messagePanel.removeAll();
		
		JLabel msg = new JLabel("<html><h2>Simulation Complete</h2></html>");
		messagePanel.add(msg);
		
		messagePanel.revalidate();
		this.repaint();
	}
	
	private void showSimulationCancellingMessage(){
		messagePanel.removeAll();
		
		JLabel msg = new JLabel("<html><h2>Cancelling Simulation...</h2></html>");
		messagePanel.add(msg);
		
		messagePanel.revalidate();
		this.repaint();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CMD_CONFIGURE_SIMULATION)){			
			showConfigureScreen();
		} else if (e.getActionCommand().equals(CMD_EXIT)){
			System.exit(0);
		} else if (e.getActionCommand().equals(ConfigureSimulationPanel.CMD_DONE_CONFIGURATION)){
			if (configScreen.isValidConfiguration()){
				showHomeScreen();
			}
		} else if (e.getActionCommand().equals(ConfigureSimulationPanel.CMD_CANCEL_CONFIGURATION)){
			showHomeScreen();
		} else if (e.getActionCommand().equals(CMD_RUN_SIMULATION)){
			if (!simulationFinished){
				showBusyMessage();
				
				//Start simulation in a new thread, because this is the Event-dispatch thread
				Thread t = new Thread(){
					public void run(){
						try {
							simulation.runSimulation();
							showSimulationCompleteMessage();
						} catch (Exception e) {
							JOptionPane.showMessageDialog(GuiMain.this, "Encountered an unexpected error" + e.getMessage());
							e.printStackTrace();
						}
					}
				};
				t.start();
							
				simulationStarted = true;
				showHomeScreen();
								
			} else {
				JOptionPane.showMessageDialog(this, "To re-execute the simulation or run another simulation, " +
													"please re-start the simulator." +
													" \nThis is required due to a limitation in the underlying simulation framework.");
			}
			
		} else if (e.getActionCommand().equals(CMD_CANCEL_SIMULATION)){
			try {
				showSimulationCancellingMessage();
				simulation.cancelSimulation();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage());
			}
		} else if (e.getActionCommand().equals(CMD_DISPLAY_RESULTS)){
			showResultsScreen();
		} else if (e.getActionCommand().equals(CMD_ABOUT)){
			if (abtDlg == null){
				abtDlg = new JDialog();
				abtDlg.setTitle("About CloudSimulator");
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				abtDlg.setLocation((int) screenSize.getWidth()/ 2 - 200, (int) screenSize.getHeight() / 2 - 150);
				abtDlg.setSize(400, 300);
				
				JLabel text = new JLabel("<html><div align='center'><h2>CloudSimulator</h2> v1.0 beta<br/>" +
											"Created by: Bhathiya Wickremasinghe (mkbw@pgrad.unimelb.edu.au)" +
											"</div></html>");
				abtDlg.getContentPane().add(text);
			}
			
			abtDlg.setVisible(true);
		}
	}


	public void cloudSimEventFired(CloudSimEvent e) {
		/* fxxx the gui
		if (e.getId() == CloudSimEvents.EVENT_SIMULATION_ENDED){
			simulationFinished = true;
			simulationStarted = false;
			showHomeScreen();
			
			showOnScreenResults();
			showResultsScreen();
		} else if (e.getId() == CloudSimEvents.EVENT_PROGRESS_UPDATE){
			int currSimTime = (Integer) e.getParameter(Constants.PARAM_TIME);
			progressBar.setValue((int) currSimTime);
		} else if (e.getId() == CloudSimEvents.EVENT_FITNESS_UPDATE){
			simulationPanel.cloudSimEventFired(e);
		} else if (e.getId() == CloudSimEvents.EVENT_GGA_FINISHED){
			results = (Map<String, Object>) e.getParameter(Constants.PARAM_RESULT);
			System.out.println("woooWWW: " + results.size());
		}*/
	}
	
	private void showOnScreenResults(){
		//simulationPanel.setResults(simulation.getResults());
	}
	
	private void runSimulation() {
		this.simulation.runSimulation();
	}
	
	/**
	 * The main method of the application
	 * @param args
	 */
	public static void main(String[] args){
		GuiMain app;
		try {
			int [] sizes = new int[5];
			sizes[0] = 300;
			sizes[1] = 500;
			sizes[2] = 800;
			sizes[3] = 1000;
			sizes[4] = 2000;
			
			File test = new File("outlog.txt"); 
			PrintStream out = new PrintStream(new FileOutputStream(test)); 
			System.setOut(out); 
			
			for (int i=0; i < 5; i++) {
				System.out.println("The size now is:" + sizes[i]);
				
				app = new GuiMain(sizes[i]);			
				app.runSimulation();
			}
			//app.setVisible(true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, 
										  "An error prevented the application from starting properly!",
										  "Error!",
										  JOptionPane.ERROR_MESSAGE);
			System.out.println("Some error occured in ui");
			e.printStackTrace();
		}
		
		
	}	
}
