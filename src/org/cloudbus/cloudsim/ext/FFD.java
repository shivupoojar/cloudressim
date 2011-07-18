package org.cloudbus.cloudsim.ext;

import java.io.FileWriter;
import java.util.ArrayList;

import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;
import org.cloudbus.cloudsim.ext.gga.GaParamsT;
import org.cloudbus.cloudsim.ext.gga.Genotype;
import org.cloudbus.cloudsim.ext.gga.Population;
import org.cloudbus.cloudsim.ext.gga.Problem;

public class FFD {
private Problem problem;

	private Population population = new Population();
	private FileWriter solutionsFile;
	private FileWriter dataFile;
	private int nrofobjects;
	
	public FFD () {
	}
	
	public void Initialize (Problem problem) {
		this.problem = problem;
		
		nrofobjects = problem.getNrOfItems();
	}
	
	public Genotype getResult() {
		return null;
	}

}
