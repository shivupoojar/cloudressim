package org.cloudbus.cloudsim.ext;

import java.io.FileWriter;
import java.util.ArrayList;

import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;
import org.cloudbus.cloudsim.ext.gga.GaParamsT;
import org.cloudbus.cloudsim.ext.gga.Genotype;
import org.cloudbus.cloudsim.ext.gga.Population;
import org.cloudbus.cloudsim.ext.gga.Problem;
import org.cloudbus.cloudsim.ext.gga.enums.PackingT;

public class XFD {
private Problem problem;

	private Population population = new Population();
	private FileWriter solutionsFile;
	private FileWriter dataFile;
	private int nrofobjects;
	
	public XFD () {
	}
	
	public void Initialize (Problem problem) {
		this.problem = problem;
		
		nrofobjects = problem.getNrOfItems();
	}
	
	public Genotype getResult(PackingT packingAlgo) {
		Genotype geno = new Genotype();
		
		Genotype.setProblem(problem);
		
		System.out.println("algo: " + packingAlgo + PackingT.UNDIFINED);
		
		geno.xfdInitialize(nrofobjects, packingAlgo);
		
		System.out.println(geno.getStatics());
		
		return geno;
	}

}
