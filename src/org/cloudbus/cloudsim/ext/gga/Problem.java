package org.cloudbus.cloudsim.ext.gga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.ext.Topology;
import org.cloudbus.cloudsim.ext.TopologyParamsT;

public class Problem {
	private ArrayList<Capacity> items;
	private Capacity cBin = new Capacity();
	private int nrOfItems;
	private int nrOfBins;
	private Topology topology;
	private boolean[] taken;
	private int[] remainedItems;
	
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
		if (item.Cpu > bin.Cpu)
			return false;
		else
			bin.Cpu -= item.Cpu;
		if (item.Mem > bin.Mem)
			return false;
		else
			bin.Mem -= item.Mem;
		if (item.Disk > bin.Disk)
			return false;
		else
			bin.Disk -= item.Disk;
		if (item.Bandwidth > bin.Bandwidth)
			return false;
		else
			bin.Bandwidth -= item.Bandwidth;
			
		return true;		
	}
	public void CreateProblem(List<? extends Vm> vmList, List<? extends Host> hostList, TopologyParamsT topologyParams, Genotype old) 
	// Create the problem.
	{
		items = new ArrayList<Capacity>();
		nrOfItems = vmList.size();
		nrOfBins = hostList.size();
		
		Host host = hostList.get(0);
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
			c.id = i;
			items.add(c);
		}
		
		if (old != null)
			arrangeOld(old);
		
		
		/* 拓扑的代码暂时不用了		
		topology = new Topology("topology.properties", topologyParams);
		if (!topology.genTopology(items, cBin)) {
			System.err.println("Network Generation Failure");
			System.exit(1);
		}*/
	}
	
	//TODO: 这个借口用int表示type，应当改成enum
	public int getItemRequest(int seq, int type) {
		int retVal;
		switch (type) {
		case 0:
			retVal = items.get(seq).Bandwidth;
			break;
		case 1:
			retVal = items.get(seq).Cpu;
			break;
		case 2:
			retVal = items.get(seq).Disk;
			break;
		case 3:
			retVal = items.get(seq).Mem;
			break;
		default:
			retVal = items.get(seq).Mem;
		}
		return retVal;
	}
	public Topology getTopology() {
		return topology;
	}
	
	// 用于针对上一轮的放置方案进行解析
	private void arrangeOld(Genotype old) {
		int [] groups = old.getGroups();
		remainedItems = new int[nrOfItems];
		
		// 标记已经用了的group号
		taken = new boolean[nrOfBins];
		for (int i=0; i < nrOfBins; i++) {
			taken[i] = false;
		}
		for (int i=0; i < old.GetBinsUsed(); i++) {
			taken[groups[i]] = true;
		}
		
		// 得到Bin的安排
		List<Bin> bins = new ArrayList<Bin>();
		for (int i=0; i < nrOfBins; i++) {
			bins.add(new Bin(i, cBin));
		}
		
		// 放入到合适的Bin中
		for (int i=0; i < nrOfItems; i++) {
			int binId = old.getAllocatedHost(i);
			bins.get(binId).add(items.get(i));
			// 复制上一次的放置信息
			remainedItems[i] = binId;
		}
		
		// 对每个Bin，剔除内存条件不满足的item
		for (int i=0; i < nrOfBins; i++) {
			// bin被占用了，才做进一步的判断
			if (taken[i]) {
				Bin bin = bins.get(i);
				List<Integer> removed = bin.getRemoved();
				if (removed.size() == bin.items.size()) {
					taken[i] = false;
				}
				for (int j=0; j < removed.size(); j++) {
					remainedItems[removed.get(j)] = Constants.UNCOLORED;
					System.out.println("Bin-"+i+"  Item removed: "+removed.get(j)+"  Mem:"+items.get(removed.get(j)).Mem);
				}
			}
		}
		
		for (int i=0; i < nrOfItems; i++) {
			System.out.println("Item-"+i+"  Bins: "+remainedItems[i]);
		}
		System.exit(0);
	}
}

class Bin {
	int binId;
	List<Capacity> items;
	Capacity size;
	
	public Bin(int id, Capacity c) {
		this.binId = id;
		this.items = new ArrayList<Capacity>();
		this.size = c;
	}
	
	public void add(Capacity item) {
		this.items.add(item);
	}
	
	public List<Integer> getRemoved() {
		List<Integer> retVal = new ArrayList<Integer>();
		Collections.sort(this.items);
		Collections.reverse(this.items);
		
		int Mem = 0;
		int fit = 0;
		Capacity total = new Capacity(size);
		// 找到最大的填充点
		for (fit = 0; fit < items.size(); fit++) {
			Mem += items.get(fit).Mem;
			//TODO: 这里的0.5要编程可调的
			if ((float)Mem / size.Mem > 0.5 && putItem(total, items.get(fit))) {
				break;
			} else {
				System.out.println("Bin-"+this.binId+"  Item remained: "+items.get(fit).id+"  Mem:"+items.get(fit).Mem);
			}
		}
		// 返回填充的东西
		for (int i = fit; i < items.size(); i++) {
			retVal.add(items.get(i).id);
		}
		return retVal;
	}
	
	private boolean putItem(Capacity bin, Capacity item) {
		if (item.Cpu > bin.Cpu)
			return false;
		else
			bin.Cpu -= item.Cpu;
		if (item.Mem > bin.Mem)
			return false;
		else
			bin.Mem -= item.Mem;
		if (item.Disk > bin.Disk)
			return false;
		else
			bin.Disk -= item.Disk;
		if (item.Bandwidth > bin.Bandwidth)
			return false;
		else
			bin.Bandwidth -= item.Bandwidth;
			
		return true;		
	}
}
