/**
 * 
 */
package org.cloudbus.cloudsim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * @author ley
 *
 */
public class VmAllocationPolicyLite extends VmAllocationPolicy {

	/** The vm table. */
	private Map<String, Host> vmTable;
	
	/**
	 * @param list
	 */	
	public VmAllocationPolicyLite(List<? extends Host> list) {
		super(list);

		setVmTable(new HashMap<String, Host>());
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm)
	 */
	@Override
	public boolean allocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm, org.cloudbus.cloudsim.Host)
	 */
	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host.vmCreate(vm)) { //if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);
			Log.formatLine("%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(), CloudSim.clock());
			return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#optimizeAllocation(java.util.List)
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#deallocateHostForVm(org.cloudbus.cloudsim.Vm)
	 */
	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		int idx = getHostList().indexOf(host);
		if (host != null) {
			host.vmDestroy(vm);
		}
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(org.cloudbus.cloudsim.Vm)
	 */
	@Override
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(int, int)
	 */
	@Override
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}
	
	/**
	 * Gets the vm table.
	 *
	 * @return the vm table
	 */
	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	/**
	 * Sets the vm table.
	 *
	 * @param vmTable the vm table
	 */
	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

}
