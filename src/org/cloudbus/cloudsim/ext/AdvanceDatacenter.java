package org.cloudbus.cloudsim.ext;

import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.SimEvent;

public class AdvanceDatacenter extends Datacenter {

	public AdvanceDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		// TODO Auto-generated constructor stub
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

}
