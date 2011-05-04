package org.cloudbus.cloudsim.ext.gga;

public class Capacity {
	public int Cpu;
	public int Mem;
	public int Disk;
	public int Bandwidth;
	
	public Capacity(Capacity c) {
		Cpu = c.Cpu;
		Mem = c.Mem;
		Disk = c.Disk;
		Bandwidth = c.Bandwidth;
	}
}
