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
    	Problem problem = new Problem();
    	problem.CreateProblem(getVmQueue(), getHostList(), topologyParams);
    	
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
    	
    	//TODO: 怎么利用结果
    	allcateByGenotype(bestGeno);
    }
    
    private void allcateByGenotype(Genotype geno) {
    	int size = getVmQueue().size();
    	for (int i=0; i < size; i++) {
    		Vm vm = getVmQueue().get(i);
    		int host = geno.getAllocatedHost(i);
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
				assert(3==2);
			}
    		
    	}
    	
    	getVmQueue().clear();
    }
}
