package org.cloudbus.cloudsim.ext.gga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.ext.Topology;
import org.cloudbus.cloudsim.ext.TopologyParamsT;
import org.cloudbus.cloudsim.ext.utils.ScientificMethods;

public class Problem {
	private ArrayList<Capacity> items;
	private ArrayList<Capacity> leftItems;
	private Capacity cBin = new Capacity();
	private List<Capacity> cBins = new ArrayList<Capacity>();
	private int nrOfItems;
	private int nrOfBins;
	private Topology topology;
	private boolean[] taken;
	private int[] remainedItems;
	private Genotype old;
	
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
	public Capacity GetBinSize(int binId)
	// return the size of the bin by id of bin.
	{
		// 从cBins得到容量，用new方法返回副本；
		Capacity bin = new Capacity(cBins.get(binId));
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
		
		this.old = old;
		if (old != null) {
			arrangeOld(old);
		}
		else {
			taken = new boolean[nrOfItems];
			for (int i=0; i < nrOfItems; i++) {
				taken[i] = true;
			}
		}
		
		
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
	
	public boolean isTaken(int binId) {
		return taken[binId];
	}
	
	public int getDistance(Genotype gen) {
		if (old == null) {
			return 0;
		}
		
		int[] otherItems = new int[remainedItems.length];
		for (int i=0; i<remainedItems.length; i++) {
			otherItems[i] = remainedItems[i];
		}
		
		for (int i=0; i<items.size(); i++) {
			Capacity c = items.get(i);
			// DEBUG用
			//System.out.println("item-id: " + c.id +"  box: " + remainedItems[c.id] + 
			//		"  tobe: " + gen.getAllocatedBin(c.id) );
			//otherItems[i] = gen.getAllocatedBin(c.id);
			
			//System.out.println("item-id: " + c.id +"  box: " + remainedItems[c.id] + 
			//		"  tobe: " + gen.getAllocatedBin(i) );
			otherItems[c.id] = gen.getAllocatedBin(i);
		}
		
		String oldGeno = "";
		String newGeno = "";
		
		for (int i = 0; i < otherItems.length; i++) {
			oldGeno += (char)('0' + old.getAllocatedBin(i));
			newGeno += (char)('0' + otherItems[i]);
		}
		
		//System.out.println("new: " + newGeno + "\nold" + oldGeno);
		
		return ScientificMethods.getLdistance(oldGeno, newGeno);
	}
	
	public int getHostAllocated(Genotype geno, int vm) {
		if (old == null) {
			return geno.getAllocatedBin(vm);
		}
		if (remainedItems[vm] != Constants.UNCOLORED) {
			return remainedItems[vm];			
		} else {
			for (int i=0; i<items.size(); i++) {
				Capacity c = items.get(i);
				if (c.id == vm) {
					System.out.println("NOTREMAIN: " + vm);
					return geno.getAllocatedBin(i);
				}
			}
		}
		
		return 0;
	}
	
	// 用于针对上一轮的放置方案进行解析
	private void arrangeOld(Genotype old) {
		int [] groups = old.getGroups();
		remainedItems = new int[nrOfItems];
		leftItems = new ArrayList<Capacity>();
		
		// 标记已经用了的group号
		taken = new boolean[nrOfBins];
		for (int i=0; i < nrOfBins; i++) {
			taken[i] = false;
		}
		for (int i=0; i < old.GetBinsUsed(); i++) {
			taken[groups[i]] = true;
		}
		
		for (int i=0; i < nrOfBins; i++) {
			System.out.println("bins-" + i + " " + taken[i]);
		}
		
		// 得到Bin的安排
		List<Bin> bins = new ArrayList<Bin>();
		for (int i=0; i < nrOfBins; i++) {
			bins.add(new Bin(i, cBin));
		}
		
		// 放入到合适的Bin中
		for (int i=0; i < nrOfItems; i++) {
			int binId = old.getAllocatedBin(i);//old.getAllocatedHost(i);
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
				// 记录剩余的容量
				cBins.add(new Capacity(bin.left));
				System.out.println("id-" + i +" has left: " + bin.left);
				if (removed.size() == bin.items.size()) {
					taken[i] = false;
				}
				for (int j=0; j < removed.size(); j++) {
					remainedItems[removed.get(j)] = Constants.UNCOLORED;
					System.out.println("Bin-"+i+"  Item removed: "+removed.get(j)+"  Mem:"+items.get(removed.get(j)).Mem);
				}
			} else {
				cBins.add(new Capacity(cBin));
			}
		}
		
		for (int i=0; i < nrOfItems; i++) {
			System.out.println("Item-"+i+"  Bins: "+remainedItems[i]);
		}
		
		// 最后要对所有的VM进行处理，那些留下的，还有木有留下来的；
		int counter = 0;		//计数，当前减少的个数
		for (int i=0; i < nrOfItems; i++) {
			if (remainedItems[i] != Constants.UNCOLORED) {
				leftItems.add(items.remove(i - counter));
				counter ++;
			}
		}
		
		for (int i=0; i < leftItems.size(); i++) {
			Capacity c = leftItems.get(i);
			System.out.print("Left: " + c);
			System.out.println(" At bin" + remainedItems[c.id]);
		}
		for (int i=0; i < items.size(); i++) {
			Capacity c = items.get(i);
			System.out.println("NotLeft: " + c);
		}
		
		for (int i=0; i < nrOfBins; i++) {
			System.out.println("Bin-" + i +"  has: " + this.GetBinSize(i));
		}
		
		nrOfItems -= counter;
		//System.exit(0);
	}
}

class Bin {
	int binId;
	List<Capacity> items;
	Capacity size;
	public Capacity left;
	
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
			boolean isFull = false;
			//TODO: 这里的0.5要编程可调的
			if ((float)Mem / size.Mem > 0.5) {
				break;
			} else {
				// 如果放置返回错误，那么说明满了
				isFull = !putItem(total, items.get(fit));
				if (isFull) {
					total.Bandwidth += items.get(fit).Bandwidth;
					total.Cpu += items.get(fit).Cpu;
					total.Mem += items.get(fit).Mem;
					total.Disk += items.get(fit).Disk;
					break;
				} else {
					// 这种情况下可以继续填充，该item被remain下来了
					System.out.println("Bin-"+this.binId+"  Item remained: "+items.get(fit).id+"  Mem:"+items.get(fit).Mem);
				}
			}
		}
		// 返回不能填充的东西
		for (int i = fit; i < items.size(); i++) {
			retVal.add(items.get(i).id);
		}
		left = new Capacity(total);
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
