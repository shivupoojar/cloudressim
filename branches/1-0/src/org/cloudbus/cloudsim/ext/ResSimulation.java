/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.ext;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.AdvanceDatacenter;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicyLite;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.ext.event.CloudSimEvent;
import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;
import org.cloudbus.cloudsim.ext.event.CloudSimEvents;
import org.cloudbus.cloudsim.ext.gga.GaParamsT;
import org.cloudbus.cloudsim.ext.TopologyParamsT;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;


/**
 * A simple example showing how to create
 * a datacenter with one host and run two
 * cloudlets on it. The cloudlets run in
 * VMs with the same MIPS requirements.
 * The cloudlets will take the same time to
 * complete the execution.
 */
public class ResSimulation {

	/** The cloudlet list. */
	private List<Cloudlet> cloudletList;

	/** The vmlist. */
	private List<Vm> vmlist;
	
	private String workloadMethod;
	private String workloadFile;
	private int workloadSize;
	private int hostCpu;
    private int hostRam;
    private int hostStorage;
    private int hostBw;
    
    private int firstLayer;
    private int secondLayer;
    private int thirdLayer;    
	
	private int ggaGens;
	private int populationSize;
	private int crossover;
	private int mutations;
	private double mutationProb;
	
	private CloudSimEventListener guiListener;
	
	public ResSimulation(CloudSimEventListener gui) {
		this.guiListener = gui;
		
		this.workloadMethod = Constants.WORKLOAD_AUTO_GEN;
		this.workloadFile = Constants.DEFAULT_WORKLOAD_FILE;
		this.workloadSize = Constants.DEFAULT_WORKLOAD_SIZE;
		this.hostCpu =  Constants.DEFAULT_HOST_CPU;
		this.hostRam =  Constants.DEFAULT_HOST_RAM;
		this.hostStorage = Constants.DEFAULT_HOST_STORAGE;
		this.hostBw = Constants.DEFAULT_HOST_BW;
		
		this.firstLayer = Constants.DEFAULT_NETWORK_FIRSTLAYER;
		this.secondLayer = Constants.DEFAULT_NETWORK_SECONDLAYER;
		this.thirdLayer = Constants.DEFAULT_NETWORK_THIRDLAYER;
		
		this.ggaGens = Constants.DEFAULT_GGA_GENERAIONS;
		this.populationSize = Constants.DEFAULT_GGA_POPULATION_SIZE;
		this.crossover = Constants.DEFAULT_GGA_CROSSOVER;
		this.mutations = Constants.DEFAULT_GGA_MUTATIONS;
		this.mutationProb = Constants.DEFAULT_GGA_MUTATION_PROB;		
	}

	/**
	 * Creates main() to run this example
	 */
	public void runSimulation() {

		Log.printLine("Starting CloudResSim...");

	        try {
	        	// First step: Initialize the CloudSim package. It should be called
	            	// before creating any entities.
	            	int num_user = 1;   // number of cloud users
	            	Calendar calendar = Calendar.getInstance();
	            	boolean trace_flag = false;  // mean trace events

	            	// Initialize the CloudSim library
	            	CloudSim.init(num_user, calendar, trace_flag);

	            	// Second step: Create Datacenters
	            	//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
	            	AdvanceDatacenter datacenter0 = createDatacenter("Datacenter_0");

	            	//Third step: Create Broker
	            	DatacenterBroker broker = createBroker();
	            	int brokerId = broker.getId();

	            	//Fourth step: Create one virtual machine
	            	vmlist = new ArrayList<Vm>();

	            	//VM description
	            	int vmid = 0;
	            	int mips = 250;
	            	long size = 10000; //image size (MB)
	            	int ram = 512; //vm memory (MB)
	            	long bw = 1000;
	            	int pesNumber = 1; //number of cpus
	            	String vmm = "Xen"; //VMM name

	            	//Create VMs
	            	//for (int i=0; i < 10; i++) {
	            		//vmlist.add(new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared()));
	            		//vmid++;
	            	//}
	            	
	            	WorkLoad workload = new WorkLoad(workloadMethod, workloadFile, brokerId, datacenter0.getHostList().get(0), workloadSize);
	            	workload.genWorkLoad();
	            	//workload.genNetwork("VolumeFile.txt");
	            	vmlist = workload.getVmList();

	            	//submit vm list to the broker
	            	broker.submitVmList(vmlist);


	            	//Fifth step: Create two Cloudlets
	            	cloudletList = new ArrayList<Cloudlet>();

	            	//Cloudlet properties
	            	int id = 0;
	            	pesNumber=1;
	            	long length = 250000;
	            	long fileSize = 300;
	            	long outputSize = 300;
	            	UtilizationModel utilizationModel = new UtilizationModelFull();

	            	Cloudlet cloudlet1 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            	cloudlet1.setUserId(brokerId);

	            	id++;
	            	Cloudlet cloudlet2 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            	cloudlet2.setUserId(brokerId);

	            	//add the cloudlets to the list
	            	cloudletList.add(cloudlet1);
	            	cloudletList.add(cloudlet2);

	            	//submit cloudlet list to the broker
	            	broker.submitCloudletList(cloudletList);


	            	//bind the cloudlets to the vms. This way, the broker
	            	// will submit the bound cloudlets only to the specific VM
	            	broker.bindCloudletToVm(cloudlet1.getCloudletId(),vmlist.get(0).getId());
	            	broker.bindCloudletToVm(cloudlet2.getCloudletId(),vmlist.get(1).getId());

	            	// Sixth step: Starts the simulation
	            	CloudSim.startSimulation();


	            	// Final step: Print results when simulation is over
	            	List<Cloudlet> newList = broker.getCloudletReceivedList();

	            	CloudSim.stopSimulation();

	            	printCloudletList(newList);

	            	//Print the debt of each user to each datacenter
	    	    	datacenter0.printDebts();

	            	Log.printLine("CloudSimExample2 finished!");
	            	CloudSimEvent e1 = new CloudSimEvent(CloudSimEvents.EVENT_SIMULATION_ENDED);
	    			guiListener.cloudSimEventFired(e1);
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	            Log.printLine("Unwanted errors happen");
	        }
	    }

		private AdvanceDatacenter createDatacenter(String name){

	        // Here are the steps needed to create a PowerDatacenter:
	        // 1. We need to create a list to store
	    	//    our machine
	    	List<Host> hostList = new ArrayList<Host>();

	        // 2. A Machine contains one or more PEs or CPUs/Cores.
	    	// In this example, it will have only one core.
	    	List<Pe> peList = new ArrayList<Pe>();

	    	int mips = hostCpu;

	        // 3. Create PEs and add these into a list.
	    	peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

	        //4. Create Host with its id and list of PEs and add them to the list of machines
	        int hostId=0;
	        int ram = hostRam; //host memory (MB)
	        long storage = hostStorage; //host storage
	        int bw = hostBw;

	        // Host数量跟workload一样大，足够用
	        for (int i=0; i < workloadSize; i++) {
	        	hostList.add(
		    			new Host(
		    				hostId,
		    				new RamProvisionerSimple(ram),
		    				new BwProvisionerSimple(bw),
		    				storage,
		    				peList,
		    				new VmSchedulerTimeShared(peList)
		    			)
		    		); // This is our machine
	        	hostId++;
	        }

	        // 5. Create a DatacenterCharacteristics object that stores the
	        //    properties of a data center: architecture, OS, list of
	        //    Machines, allocation policy: time- or space-shared, time zone
	        //    and its price (G$/Pe time unit).
	        String arch = "x86";      // system architecture
	        String os = "Linux";          // operating system
	        String vmm = "Xen";
	        double time_zone = 10.0;         // time zone this resource located
	        double cost = 3.0;              // the cost of using processing in this resource
	        double costPerMem = 0.05;		// the cost of using memory in this resource
	        double costPerStorage = 0.001;	// the cost of using storage in this resource
	        double costPerBw = 0.0;			// the cost of using bw in this resource
	        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

	        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
	                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


	        // 6. Finally, we need to create a PowerDatacenter object.
	        GaParamsT gaparams = new GaParamsT();
	        gaparams.PopulationSize = populationSize;
	        gaparams.N_Crossover = crossover;
	        gaparams.N_Mutation = mutations;
	        gaparams.AllelMutationProb = mutationProb;
	        
	        TopologyParamsT topology = new TopologyParamsT();
	        topology.vmNums = workloadSize;
	        topology.firstLayer = firstLayer;
	        topology.secondLayer = secondLayer;
	        topology.thirdLayer = thirdLayer;
	        
	        AdvanceDatacenter datacenter = null;
	        try {
	            datacenter = new AdvanceDatacenter(name, characteristics, new VmAllocationPolicyLite(hostList), storageList, 0, workloadSize, ggaGens, guiListener, gaparams, topology);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return datacenter;
	    }

	    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	    //to the specific rules of the simulated scenario
	    private DatacenterBroker createBroker(){

	    	DatacenterBroker broker = null;
	        try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	    	return broker;
	    }

	    /**
	     * Prints the Cloudlet objects
	     * @param list  list of Cloudlets
	     */
	    private void printCloudletList(List<Cloudlet> list) {
	        int size = list.size();
	        Cloudlet cloudlet;

	        String indent = "    ";
	        Log.printLine();
	        Log.printLine("========== OUTPUT ==========");
	        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
	                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

	        DecimalFormat dft = new DecimalFormat("###.##");
	        for (int i = 0; i < size; i++) {
	            cloudlet = list.get(i);
	            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

	            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
	                Log.print("SUCCESS");

	            	Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
	                     indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                             indent + indent + dft.format(cloudlet.getFinishTime()));
	            }
	        }

	    }

		public int getGgaGens() {
			return ggaGens;
		}

		public void setGgaGens(int ggaGens) {
			this.ggaGens = ggaGens;
		}

		public String getWorkloadMethod() {
			return workloadMethod;
		}

		public void setWorkloadMethod(String workloadMethod) {
			this.workloadMethod = workloadMethod;
		}

		public String getWorkloadFile() {
			return workloadFile;
		}

		public void setWorkloadFile(String workloadFile) {
			this.workloadFile = workloadFile;
		}

		public int getFirstLayer() {
			return firstLayer;
		}

		public void setFirstLayer(int firstLayer) {
			this.firstLayer = firstLayer;
		}

		public int getSecondLayer() {
			return secondLayer;
		}

		public void setSecondLayer(int secondLayer) {
			this.secondLayer = secondLayer;
		}

		public int getThirdLayer() {
			return thirdLayer;
		}

		public void setThirdLayer(int thirdLayer) {
			this.thirdLayer = thirdLayer;
		}

		public int getWorkloadSize() {
			return workloadSize;
		}

		public int getHostCpu() {
			return hostCpu;
		}

		public int getHostRam() {
			return hostRam;
		}

		public int getHostStorage() {
			return hostStorage;
		}

		public int getHostBw() {
			return hostBw;
		}

		public void setHostCpu(int hostCpu) {
			this.hostCpu = hostCpu;
		}

		public void setHostRam(int hostRam) {
			this.hostRam = hostRam;
		}

		public void setHostStorage(int hostStorage) {
			this.hostStorage = hostStorage;
		}

		public void setHostBw(int hostBw) {
			this.hostBw = hostBw;
		}

		public int getPopulationSize() {
			return populationSize;
		}

		public int getCrossover() {
			return crossover;
		}

		public int getMutations() {
			return mutations;
		}

		public double getMutationProb() {
			return mutationProb;
		}

		public CloudSimEventListener getGuiListener() {
			return guiListener;
		}

		public void setWorkloadSize(int workloadSize) {
			this.workloadSize = workloadSize;
		}

		public void setPopulationSize(int populationSize) {
			this.populationSize = populationSize;
		}

		public void setCrossover(int crossover) {
			this.crossover = crossover;
		}

		public void setMutations(int mutations) {
			this.mutations = mutations;
		}

		public void setMutationProb(double mutationProb) {
			this.mutationProb = mutationProb;
		}

		public void setGuiListener(CloudSimEventListener guiListener) {
			this.guiListener = guiListener;
		}

		public void cancelSimulation() {
			CloudSim.stopSimulation();			
		}
}
