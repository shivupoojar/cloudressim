package org.cloudbus.cloudsim.ext;

public interface Constants {
	//TODO: say sth.
	final String PARAM_TIME = "time";
	final String PARAM_FVAL = "fval";
	final String PARAM_RESULT = "gga-result";
	
	final String WORKLOAD_FROM_XML = "from-xml";
	final String WORKLOAD_AUTO_GEN = "auto-gen";
	
	final String SIM_FILE_EXTENSION = ".sim";
	
	//Default Data Center characteristics
	final String DEFAULT_DATA_CENTER_NAME = "DC1";
	final String DEFAULT_ARCHITECTURE = "x86";
	final String DEFAULT_OS = "Linux";
	final String DEFAULT_VMM = "Xen";
	
	final String DEFAULT_WORKLOAD_FILE = "workload.sim";
	final int DEFAULT_WORKLOAD_SIZE = 300;
	final int DEFAULT_HOST_CPU = 2000;
	final int DEFAULT_HOST_RAM = 2000;
	final int DEFAULT_HOST_STORAGE = 1000000;
	final int DEFAULT_HOST_BW = 100;
	
	final int DEFAULT_NETWORK_FIRSTLAYER = 16;
	final int DEFAULT_NETWORK_SECONDLAYER = 48;
	final int DEFAULT_NETWORK_THIRDLAYER = 8;
	
	//Default GGA params
	final int DEFAULT_GGA_GENERAIONS = 200;
	final int DEFAULT_GGA_POPULATION_SIZE = 100;
	final int DEFAULT_GGA_CROSSOVER = 40;
	final int DEFAULT_GGA_MUTATIONS = 20;
	final double DEFAULT_GGA_MUTATION_PROB = 0.25;
}
