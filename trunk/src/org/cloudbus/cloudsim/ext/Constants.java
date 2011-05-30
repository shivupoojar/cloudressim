package org.cloudbus.cloudsim.ext;

public interface Constants {
	//TODO: say sth.
	final String PARAM_TIME = "time";
	final String PARAM_FVAL = "fval";
	
	//Default Data Center characteristics
	final String DEFAULT_DATA_CENTER_NAME = "DC1";
	final String DEFAULT_ARCHITECTURE = "x86";
	final String DEFAULT_OS = "Linux";
	final String DEFAULT_VMM = "Xen";
	
	final int DEFAULT_WORKLOAD_SIZE = 300;
	
	//Default GGA params
	final int DEFAULT_GGA_GENERAIONS = 200;
	final int DEFAULT_GGA_POPULATION_SIZE = 100;
	final int DEFAULT_GGA_CROSSOVER = 40;
	final int DEFAULT_GGA_MUTATIONS = 20;
	final double DEFAULT_GGA_MUTATION_PROB = 0.25;
}
