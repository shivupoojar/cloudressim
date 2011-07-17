package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.ext.TopologyParamsT;
import org.cloudbus.cloudsim.ext.gga.GGA;
import org.cloudbus.cloudsim.ext.gga.GaParamsT;
import org.cloudbus.cloudsim.ext.gga.Genotype;
import org.cloudbus.cloudsim.ext.gga.Problem;
import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;

public class AdvanceDatacenter extends Datacenter {
	private List<? extends Vm> vmQueue;
	
	private int vmQueueCapacity;		//vmQueue容量，到了这个值启动一次vm部署
	private int ggaGenerations;
	private CloudSimEventListener progressListener;
	private GaParamsT gaparams;
	private TopologyParamsT topologyParams;

	public AdvanceDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval, int vmQueueCapacity, int totalGens, CloudSimEventListener l, GaParamsT gaparams, TopologyParamsT topologyParams) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		
		this.vmQueueCapacity = vmQueueCapacity;
		this.ggaGenerations = totalGens;
		this.progressListener = l;
		this.gaparams = gaparams;
		this.topologyParams = topologyParams;
		setVmQueue(new ArrayList<Vm>());
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmQueue() {
		return (List<T>) vmQueue;
	}

	protected <T extends Vm> void setVmQueue(List<T> vmQueue) {
		this.vmQueue = vmQueue;
	}

	/**
     * Processes events or services that are available for this PowerDatacenter.
     *
     * @param ev    a Sim_event object
     *
     * @pre ev != null
     * @post $none
     */
    @Override
	public void processEvent(SimEvent ev) {
    	super.processEvent(ev);
    }
    
    /**
     * Process the event for an User/Broker who wants to create a VM
     * in this PowerDatacenter. This PowerDatacenter will then send the status back to
     * the User/Broker.
     *
     * @param ev   a Sim_event object
     * @param ack the ack
     *
     * @pre ev != null
     * @post $none
     */
    @Override
    protected void processVmCreate(SimEvent ev, boolean ack) {
    	Vm vm = (Vm) ev.getData();
    	getVmQueue().add(vm);
    	
    	if (getVmQueue().size() == vmQueueCapacity)
    		allocateVmsWithGGA();
    }
    
    private void allocateVmsWithGGA() {
    	
    	Genotype last = new Genotype("12 15 15 14 13 14 1 9 13 4 3 6 2 3 5 1 6 10 14 2 3 13 7 10 10 0 7 6 4 12 5 8 0 8 5 4 1 13 9 1 8 11 9 2 12 12 3 11 5 11  : 2 8 3 1 9 10 6 4 7 5 0 11 12 13 14 15");
    	
    	Problem problem = new Problem();
    	problem.CreateProblem(getVmQueue(), getHostList(), topologyParams, last);
    	
    	GGA gga = new GGA(progressListener, gaparams);
    	//TODO: The initialization variable should be well considered
    	gga.Initialize(problem, ggaGenerations, new Random().nextInt(9999999));
    	
    	Genotype bestGeno = null;
		gga.InitializePopulation ();

		if (gga.Run()) {
			//TODO: 成功得到结果
			bestGeno = gga.getBestGeno();
		} else {
			//TODO: 如果不成功怎么样
		}
    		
    	gga.Close();
    	
    	/*
    	
    	//TODO: 临时代码，这部分要改的，试试再来一次调度
    	problem = new Problem();
    	problem.CreateProblem(getVmQueue(), getHostList(), topologyParams, bestGeno);
    	//gaparams.PopulationSize = problem.getNrOfItems();
    	//gaparams.N_Crossover = gaparams.PopulationSize / 2;
    	//gaparams.N_Mutation = gaparams.PopulationSize / 2;
    	gga = new GGA(progressListener, gaparams);
    	//TODO: The initialization variable should be well considered
    	gga.Initialize(problem, ggaGenerations, new Random().nextInt(9999999));
    	
    	bestGeno = null;
		gga.InitializePopulation ();

		if (gga.Run()) {
			bestGeno = gga.getBestGeno();
		} else {
		}    		
    	gga.Close();
    	
    	// 临时代码结束
    	*/
    	
    	allcateByGenotype(bestGeno, problem);
    }
    
    private void allcateByGenotype(Genotype geno, Problem problem) {
    	int size = getVmQueue().size();
    	String plan = "";
    	for (int i=0; i < size; i++) {
    		Vm vm = getVmQueue().get(i);
    		int host = problem.getHostAllocated(geno, i);//geno.getAllocatedHost(i);
    		plan += host + " ";
    		System.out.println("Vm " + i + "size" +vm.getMips());
    		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, getHostList().get(host));
    		int[] data = new int[3];
            data[0] = getId();
  	       	data[1] = vm.getId();
    		if (result) {
         	   data[2] = CloudSimTags.TRUE;
            } else {
         	   data[2] = CloudSimTags.FALSE;
            }
 		   	sendNow(vm.getUserId(), CloudSimTags.VM_CREATE_ACK, data);
			if (result) {
				double amount = 0.0;
				if (getDebts().containsKey(vm.getUserId())) {
					amount = getDebts().get(vm.getUserId());
				}
				amount += getCharacteristics().getCostPerMem() * vm.getRam();
				amount += getCharacteristics().getCostPerStorage()
						* vm.getSize();

				getDebts().put(vm.getUserId(), amount);

				getVmList().add(vm);

				vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy()
						.getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
			} else {
				System.err.println("GGA Seems to be failed");
				System.out.println("Host: " + problem.GetBinSize(host));
				//assert(3==2);
			}
    		
    	}
    	
    	System.out.println("VM ALLOCATON: " + plan);
    	
    	getVmQueue().clear();
    }
}
