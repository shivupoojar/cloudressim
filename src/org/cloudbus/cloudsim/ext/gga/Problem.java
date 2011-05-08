package org.cloudbus.cloudsim.ext.gga;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

public class Problem {
	private ArrayList<Capacity> items;
	private Capacity cBin;
	private int nrOfItems;
	private int nrOfBins;
	
	public int getNrOfItems() {
		return nrOfItems;
	}
	public int getNrOfBins() {
		return nrOfBins;
	}
	public Capacity GetBinSize()
	// return the size of the bin
	{
		Capacity bin = new Capacity(cBin);
		return bin;
	}
	public boolean PutItem(Capacity bin, int object) 
	// to see if the bin fit the item
	{
		Capacity item = items.get(object);
		if (item.Cpu < bin.Cpu)
			return false;
		else
			bin.Cpu -= item.Cpu;
		if (item.Mem < bin.Mem)
			return false;
		else
			bin.Mem -= item.Mem;
		if (item.Disk < bin.Disk)
			return false;
		else
			bin.Disk -= item.Disk;
		if (item.Bandwidth < bin.Bandwidth)
			return false;
		else
			bin.Bandwidth -= item.Bandwidth;
			
		return true;		
	}
	public void CreateProblem(List<? extends Vm> vmList, List<? extends Host> hostList) 
	// Create the problem.
	{
		items = new ArrayList<Capacity>();
		nrOfItems = vmList.size();
		nrOfBins = hostList.size();
		
		Host host = hostList.get(0);
		//TODO: the size of the variable
		cBin.Bandwidth = (int) host.getBw();
		cBin.Cpu = (int) host.getMaxAvailableMips();
		cBin.Disk = (int) host.getStorage();
		cBin.Mem = host.getRam();
		
		for (int i=0; i < nrOfItems; i++) {
			Vm vm = vmList.get(i);
			Capacity c = new Capacity();
			//TODO: Find a way to evaluate the requested resources
			c.Bandwidth = (int) vm.getBw();
			c.Cpu = (int) vm.getMips();
			c.Disk = (int) vm.getSize();
			c.Mem = vm.getRam();
			items.add(c);
		}
	}
}
