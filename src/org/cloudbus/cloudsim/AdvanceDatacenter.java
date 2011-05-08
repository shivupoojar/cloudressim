package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

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
    	
    	if (getVmQueue().size() == 2) {
    		for (int i=0; i < 2; i++) {
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
    	}
    }

}
