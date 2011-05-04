package org.cloudbus.cloudsim.ext.gga;

import java.util.ArrayList;

public class Problem {
	private ArrayList<Capacity> items;
	private Capacity cBin;
	
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
	public void CreateProblem() 
	// Create the problem.
	{
		items = new ArrayList<Capacity>();
	}
}
