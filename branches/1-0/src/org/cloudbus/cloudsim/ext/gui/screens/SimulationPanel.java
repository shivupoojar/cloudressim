package org.cloudbus.cloudsim.ext.gui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cloudbus.cloudsim.ext.Constants;
import org.cloudbus.cloudsim.ext.event.CloudSimEvent;
import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;
import org.cloudbus.cloudsim.ext.gui.SimulationUIElement;
import org.cloudbus.cloudsim.ext.gui.utils.SimpleGraph;

/**
 * The main simulation panel displaying the map of the world.
 *
 * @author Leroy Jiang
 *
 */
public class SimulationPanel extends JPanel implements CloudSimEventListener {
	private final int width = 800;
	private final int height = 460;
	private long[] vals;
	private String[] xAxisLabels;
	private int gen = 0;
	private SimpleGraph graph;

	/** Constructor. */
	public SimulationPanel(int length){
		Dimension dim = new Dimension(width, height);
		this.setPreferredSize(dim);
		this.setMaximumSize(dim);
		this.setMinimumSize(dim);
		this.add(new JLabel("Simulation"), BorderLayout.NORTH);
		
		xAxisLabels = new String[length];
		for (int i = 0; i < length; i++) {
			xAxisLabels[i] = "" + i;
		}

		vals = new long[length];
		vals[0] = 0;
		graph = new SimpleGraph(vals, 
										   xAxisLabels, 
										   new String[] {"Fitness Values (ms)", "Generation" }, 
										   40);
		this.add(graph, BorderLayout.CENTER);
	}
	
	public void cloudSimEventFired(CloudSimEvent e) {
		this.remove(graph);
		
		long fval = (Long) e.getParameter(Constants.PARAM_FVAL);
		fval -= 80;
		vals[gen] = fval;
		graph = new SimpleGraph(vals, 
										   xAxisLabels, 
										   new String[] {"Fitness Values (ms)", "Generation" }, 
										   40);
		this.add(graph, BorderLayout.CENTER);
		gen++;
		this.updateUI();
		this.repaint();
	}

}
