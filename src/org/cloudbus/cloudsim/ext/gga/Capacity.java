package org.cloudbus.cloudsim.ext.gga;

public class Capacity implements Comparable<Capacity>{
	public int Cpu;
	public int Mem;
	public int Disk;
	public int Bandwidth;
	
	public int id; 
	
	public Capacity(Capacity c) {
		Cpu = c.Cpu;
		Mem = c.Mem;
		Disk = c.Disk;
		Bandwidth = c.Bandwidth;
		id = c.id;
	}

	public Capacity() {
		Cpu = 0;
		Mem = 0;
		Disk = 0;
		Bandwidth = 0;
		id = 0;
	}
	
	public String toString() {
		return "Cpu:" + Cpu + "Mem:" + Mem + "Disk:" + Disk + "Bw:" + Bandwidth;
	}

	public int getCpu() {
		return Cpu;
	}

	public int getMem() {
		return Mem;
	}

	public int getDisk() {
		return Disk;
	}

	public int getBandwidth() {
		return Bandwidth;
	}
	
	public int getId() {
		return id;
	}

	public void setCpu(int cpu) {
		Cpu = cpu;
	}

	public void setMem(int mem) {
		Mem = mem;
	}

	public void setDisk(int disk) {
		Disk = disk;
	}

	public void setBandwidth(int bandwidth) {
		Bandwidth = bandwidth;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	// 实现根据内存的比较
	@Override
	public int compareTo(Capacity other) {
		return (this.Mem - other.Mem);
	}
}
