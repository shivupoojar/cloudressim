package org.cloudbus.cloudsim.ext.gui.screens;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.cloudbus.cloudsim.ext.ResSimulation;

import org.cloudbus.cloudsim.ext.Constants;
import org.cloudbus.cloudsim.ext.gui.utils.SimpleGraph;

/**
 * The Results screen.
 * 
 * @author Bhathiya Wickremasinghe
 *
 */
public class ResultsScreen extends JPanel implements ActionListener {
	//private static final String CMD_EXPORT_RESULTS = "export_results";
	
	private JPanel mainPanel;
	private double avgResponseTime;
	private double minResponseTime;
	private double maxResponseTime;
	private double avgProcessingTime;
	private double minProcessingTime;
	private double maxProcessingTime;
	private DecimalFormat df;
	private Map<String, Object> results;
	
	/** Constructor */
	public ResultsScreen(ResSimulation simulation, Map<String, Object> results){
		df = new DecimalFormat("#0.00");
		
		initUI();
		this.results = results;
		showResults();		
	}
	
	private void initUI(){
		mainPanel = new JPanel();
		this.add(mainPanel, BorderLayout.CENTER);
	}
	
	private void showResults(){
		//this.results = results;
		
		JPanel resultsPanel = new JPanel();
		resultsPanel.setLayout(new BorderLayout());
		
		JPanel mainContentPanel = new JPanel();
		mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));		
		
		resultsPanel.add(mainContentPanel, BorderLayout.CENTER);
		
		resultsPanel.add(createSummaryPanel(), BorderLayout.NORTH);
				
		mainPanel.add(resultsPanel);
		this.revalidate();
	}
	
	private JPanel createSummaryPanel(){
		JPanel summaryPanel = new JPanel();
		summaryPanel.setLayout(new BorderLayout());
		
		JLabel summaryHeading = new JLabel("<html><h2>Overall Summary</h2></html>");
		summaryPanel.add(summaryHeading, BorderLayout.NORTH);
		
		String detailsText = "<html><table>"
							+ "<tr><th></th><th>Host Used (n)</th><th>Network Pressure (5MB/s)</th></tr>"
							+ "<tr><td>Group GA:</td><td>" + (Integer) results.get("gga-host") + "</td><td>"
							+ (Integer) results.get("gga-network") + "</td></tr>"
							+ "<tr><td>First Fit:</td><td>" + (Integer) results.get("ff-host") + "</td><td>"
							+ (Integer) results.get("ff-network") + "</td></tr>"
							+ "</table></html>";							
		JLabel details = new JLabel(detailsText);
					
		summaryPanel.add(details, BorderLayout.CENTER);
		
		File file = new File("data");
		String reportText = "<html><h2>Detailed report: </h2>";
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			line = reader.readLine();
			while (line != null) {
				reportText += "<p>" + line+"</p>";
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		reportText += "</html>";
		
		JLabel report = new JLabel(reportText);
		summaryPanel.add(report, BorderLayout.SOUTH);
		
		/*JButton btnExportResults = new JButton("Export Results");
		btnExportResults.setActionCommand(CMD_EXPORT_RESULTS);
		btnExportResults.addActionListener(this);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new EmptyBorder(0, 30, 0, 0));
		buttonPanel.add(btnExportResults);
		summaryPanel.add(buttonPanel, BorderLayout.EAST);*/
		
		return summaryPanel;
	}
	
	public void actionPerformed(ActionEvent e) {
		
	}
}
