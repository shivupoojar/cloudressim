package org.cloudbus.cloudsim.ext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.cloudbus.cloudsim.ext.gga.Capacity;


public class Topology {
	@SuppressWarnings("unused")
	private String fileName;
	private String volumeFileName;
	private int VMNum;
	private int[] switchLinkNum;
	private int[][] volumeMatrix;
	private boolean ready;
	private boolean readyForGettingVolume;
	private int serverNum;
	private int[][] linkNumBetweenHosts;
	private TopologyParamsT topologyParams;

	public Topology(String fileName, TopologyParamsT topologyParams){
		this.fileName = fileName;
		this.topologyParams = topologyParams;
		if(!this.readFromXML(fileName)){
			this.fileName = null;
			this.volumeFileName = null;
			this.VMNum = 0;
			this.switchLinkNum = null;
			this.volumeMatrix = null;
			this.ready = false;
			this.serverNum = 0;
			this.linkNumBetweenHosts = null;
			this.readyForGettingVolume = false;
		}
	}
	
	public Topology(){
		this.fileName = null;
		this.volumeFileName = null;
		this.VMNum = 0;
		this.switchLinkNum = null;
		this.volumeMatrix = null;
		this.ready = false;
		this.serverNum = 0;
		this.linkNumBetweenHosts = null;
		this.readyForGettingVolume = false;
	}
	
	public boolean readFromXML(String fileName){
		this.fileName = fileName;
		this.volumeFileName = null;
		this.VMNum = 0;
		this.switchLinkNum = null;
		this.volumeMatrix = null;
		this.ready = false;
		this.serverNum = 0;
		this.linkNumBetweenHosts = null;
		this.readyForGettingVolume = false;
		
		InputStream is = this.getClass().getClassLoader()
		.getResourceAsStream(fileName);
		
		if (is == null) {
			System.err.println("Cannot load " + fileName);
			this.ready = false;
			return false;
		}
		
		Properties properties = new Properties();
		try {
			properties.load(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.ready = false;
			return false;
		}		
		
		this.VMNum = topologyParams.vmNums;
		//this.VMNum = Integer.parseInt(properties.getProperty("VMNum"));
		//System.out.println(this.VMNum);
		
		this.volumeMatrix = new int[this.VMNum][this.VMNum];
		
		this.switchLinkNum = new int[3];
		
		this.switchLinkNum[0] = topologyParams.firstLayer;
		this.switchLinkNum[1] = topologyParams.secondLayer;
		this.switchLinkNum[2] = topologyParams.thirdLayer;
		
		/*this.switchLinkNum[0] = Integer.parseInt(properties.getProperty("FirstLayer"));
		System.out.println(this.switchLinkNum[0]);
		this.switchLinkNum[1] = Integer.parseInt(properties.getProperty("SecondLayer"));
		System.out.println(this.switchLinkNum[1]);
		this.switchLinkNum[2] = Integer.parseInt(properties.getProperty("ThirdLayer"));
		System.out.println(this.switchLinkNum[2]);*/
		
		this.volumeFileName = properties.getProperty("VolumeFileName");
		if(!this.volumeFileName.isEmpty()){
			if(!this.genVolumeMatrix(this.volumeFileName)){
				this.ready = false;
				return false;
			}
		}else{
			this.ready = false;
			return false;
		}
		System.out.println(this.volumeFileName);
		
		this.ready = true;
		return true;
	}
	
	private boolean genVolumeMatrix(String volumeFileName){
		File file = new File(volumeFileName);
		if(file.exists()){
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				for(int i = 0;i < this.VMNum;i++){
					for(int j = 0;j < this.VMNum;j++){
						this.volumeMatrix[i][j] = 0;
						int num = reader.read();
						while(num < '0' || num > '9'){
							num = reader.read();
						}
						while(num >= '0' && num <= '9'){
							this.volumeMatrix[i][j] = this.volumeMatrix[i][j] * 10 + num - 48; 
							num = reader.read();
						}						
						//System.out.print("i:");
						//System.out.print(i);
						//System.out.print("j:");
						//System.out.print(j);
						//System.out.print("volume:");
						//System.out.println(this.volumeMatrix[i][j]);
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}					
		}else{
			System.out.println("No file:" + volumeFileName);
			return false;
		}		
		return true;
	}
	
	public boolean genTopology(ArrayList<Capacity> items,Capacity bin){
		if(this.ready == false){
			return false;
		}
		this.serverNum = 0;
		int Cpu = 0;
		int Mem = 0;
		int Disk = 0;
		int Bandwidth = 0;
		for(int i = 0;i < items.size();i++){
			if(items.get(i).Cpu > bin.Cpu || items.get(i).Mem > bin.Mem || items.get(i).Disk > bin.Disk || items.get(i).Bandwidth > bin.Bandwidth){
				System.out.println("The demand of resouce is not enough!");
				return false;
			}
			Cpu += items.get(i).Cpu;
			Mem += items.get(i).Mem;
			Disk += items.get(i).Disk;
			Bandwidth += items.get(i).Bandwidth;
		}
		
		if(Cpu % bin.Cpu == 0){
			this.serverNum = Cpu/bin.Cpu;
		}else{
			this.serverNum = Cpu/bin.Cpu + 1;
		}
		
		if(Mem % bin.Mem == 0){
			if(this.serverNum < Mem/bin.Mem){
				this.serverNum = Mem/bin.Mem;
			}
		}else{
			if(this.serverNum < Mem/bin.Mem + 1){
				this.serverNum = Mem/bin.Mem + 1;
			}
		}
		
		if(Disk % bin.Disk == 0){
			if(this.serverNum < Disk/bin.Disk){
				this.serverNum = Disk/bin.Disk;
			}
		}else{
			if(this.serverNum < Disk/bin.Disk + 1){
				this.serverNum = Disk/bin.Disk + 1;
			}
		}
		
		if(Bandwidth % bin.Bandwidth == 0){
			if(this.serverNum < Bandwidth/bin.Bandwidth){
				this.serverNum = Bandwidth/bin.Bandwidth;
			}
		}else{
			if(this.serverNum < Bandwidth/bin.Bandwidth + 1){
				this.serverNum = Bandwidth/bin.Bandwidth + 1;
			}
		}
		
		this.serverNum = this.serverNum * 2;
		//System.out.println(this.serverNum);
		this.linkNumBetweenHosts = new int[this.serverNum][this.serverNum];
		
		int[] layer1 = new int[this.switchLinkNum.length];
		int[] layer2 = new int[this.switchLinkNum.length];
		int div = 0;
		for(int i = 0;i < this.serverNum;i++){
			for(int num = 0;num < layer1.length;num++){
				div = 1;
				for(int k = 0;k <= num;k++){
					div = div * this.switchLinkNum[k];
				}
				layer1[num] = i / div;
			}
			for(int j =0;j < this.serverNum;j++){
				for(int num = 0;num < layer2.length;num++){
					div = 1;
					for(int k = 0;k <= num;k++){
						div = div * this.switchLinkNum[k];
					}
					layer2[num] = j / div;
				}
				if(i == j){
					this.linkNumBetweenHosts[i][j] = 0;
				}else{
					this.linkNumBetweenHosts[i][j] = 0;
					for(int k = 0;k < layer1.length;k++){						
						if(layer1[k] == layer2[k]){
							this.linkNumBetweenHosts[i][j] = this.linkNumBetweenHosts[i][j] * 2 + 1;
							break;
						}else{
							this.linkNumBetweenHosts[i][j] = this.linkNumBetweenHosts[i][j] * 2 + 1;
						}
					}
				}
			//System.out.print("i:");
			//System.out.print(i);
			//System.out.print("j:");
			//System.out.print(j);
			//System.out.print("LinkNum:");
			//System.out.println(this.linkNumBetweenHosts[i][j]);
			}
		}
		this.readyForGettingVolume = true;
		return true;
	}

	public int getVolumeCostOfNetwork(int[] objects){
		if(this.readyForGettingVolume == false){
			return -1;
		}
		int volume = 0;;
		for(int i = 0;i < VMNum; i++){
			for(int j = 0;j < VMNum; j++){
				volume += this.linkNumBetweenHosts[objects[i]][objects[j]] * this.volumeMatrix[i][j];
			}
		}
		volume = volume / 2;
		return volume;
	}
	
	/*public static void main(String[] args){
		Topology topology = new Topology("topology.properties");
		ArrayList<Capacity> items = new ArrayList<Capacity>();
		Capacity item;
		for(int i = 0;i < 5;i++){
			item = new Capacity();
			item.Cpu = 1;
			item.Mem = 2;
			item.Disk = 1;
			item.Bandwidth = 1;
			items.add(item);
		}
		Capacity bin = new Capacity();
		bin.Cpu = 2;
		bin.Mem = 3;
		bin.Disk = 3;
		bin.Bandwidth = 5;
		if(!topology.genTopology(items, bin)){
			System.out.println("Failed to gen topology!");
		}
		int[] objects = new int[5];
		objects[0] = 0;
		objects[1] = 0;
		objects[2] = 1;
		objects[3] = 2;
		objects[4] = 3;
		int volume = topology.getVolumeCostOfNetwork(objects);
		System.out.print("Volume:");
		System.out.println(volume);
	}*/
}
