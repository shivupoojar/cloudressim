package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.ext.gga.GGA;
import org.cloudbus.cloudsim.ext.gga.Genotype;
import org.cloudbus.cloudsim.ext.gga.Problem;

public class AdvanceDatacenter extends Datacenter {
	private List<? extends Vm> vmQueue;

	public AdvanceDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		
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
    	
    	if (getVmQueue().size() == 19) //{
    		allocateVmsWithGGA();
    	/*	for (int i=0; i < 10; i++) {
	    		vm = getVmQueue().remove(0);
	    		boolean result = getVmAllocationPolicy().allocateHostForVm(vm);
	
	     	    if (ack) {
	     	       int[] data = new int[3];
	               data[0] = getId();
	     	       data[1] = vm.getId();
	
	               if (result) {
	            	   data[2] = CloudSimTags.TRUE;
	               } else {
	            	   data[2] = CloudSimTags.FALSE;
	               }
	    		   sendNow(vm.getUserId(), CloudSimTags.VM_CREATE_ACK, data);
	     	    }
	
	     	    if (result) {
	    			double amount = 0.0;
	    			if (getDebts().containsKey(vm.getUserId())) {
	    				amount = getDebts().get(vm.getUserId());
	    			}
	    			amount += getCharacteristics().getCostPerMem() * vm.getRam();
	    			amount += getCharacteristics().getCostPerStorage() * vm.getSize();
	
	    			getDebts().put(vm.getUserId(), amount);
	
	    			getVmList().add(vm);
	
	    			vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
	     	    }
    		}
    	}*/
    }
    
    private void allocateVmsWithGGA() {
    	Problem problem = new Problem();
    	problem.CreateProblem(getVmQueue(), getHostList());
    	
    	GGA gga = new GGA();
    	//TODO: The initialization variable should be well considered
    	gga.Initialize(problem, 500, new Random().nextInt(9999999));
    	
    	Genotype bestGeno = null;
    	//TODO: Times of the reproduce should be a variable
    	//这里的循环次数是尝试的次数
    	for (int i=0; i < 10; i++) {
    		gga.InitializePopulation ();

    		if (gga.Run()) {
    			//TODO: 成功得到结果
    			bestGeno = gga.getBestGeno();
    			break; //得到就不跑了吧
    		} else {
    			//TODO: 如果不成功怎么样
    		}
    		
    		//TODO: 每次run怎么操作
    	}
    	
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
