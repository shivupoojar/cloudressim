package org.cloudbus.cloudsim.ext;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.*;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.ext.utils.ScientificMethods;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class WorkLoad {
	private String workMode;
	private String loadFile;
	private int brokerId;
	private int vmsPerRound;
	private List<? extends Vm> vmList;
	private Host host;
	
	private double alfa = 0.2;
	private double beta = 0.2;
	
	public WorkLoad(String workMode, String loadFile, int brokerId, Host host, int vmsPerRound) {
		this.workMode = workMode;
		this.loadFile = loadFile;
		this.brokerId = brokerId;
		this.host = host;
		this.vmsPerRound = vmsPerRound;
		setVmList(new ArrayList<Vm>());
	}
	
	public void genWorkLoad() {
		if (workMode.equals("from-file")) {
			try {
				setVmList(readWorkLoadFromXml(loadFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (workMode.equals("auto-gen")){
			setVmList(autoGenVms(vmsPerRound));
		} else {
			//TODO: 这里是默认参数
			setVmList(autoGenVms(vmsPerRound));
		}		
	}
	
	public void genNetwork(String fileName) {
		FileWriter file = null;
		Random comRnd = new Random();
		int[][] network = new int[300][300];
		int netLoad = -1;
		try {
			file =  new FileWriter(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i=0; i < vmsPerRound; i++) {
			for (int j=0; j < vmsPerRound; j++) {
				if (i == j) {
					netLoad = 0;
				} else if( i < j) {
					while (netLoad < 0 || netLoad > 8) {
						netLoad = (int) ScientificMethods.normDistribution(comRnd, 1, 2);
					}
				} else {
					netLoad = network[j][i];
				}
				network[i][j] = netLoad;
				// 重要，随机生成
				netLoad = -1;
			}
		}
		
		for (int i=0; i < vmsPerRound; i++) {
			for (int j=0; j < vmsPerRound; j++) {
				netLoad = network[i][j]; 
				String postfix = " ";
				if (j == (vmsPerRound - 1)) {
					postfix = "\n";
				} 
				try {
					file.write(Integer.toString(netLoad) + postfix);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Vm> List<T> autoGenVms(int amount) {
		List<Vm> vms = new ArrayList<Vm>();
		
		int hMips = host.getTotalMips();
    	long hSize = host.getStorage(); //image size (MB)
    	int hRam = host.getRam(); //vm memory (MB)
    	long hBw = host.getBw();
    	
    	double meanMips = alfa * hMips;
    	double deMips = beta * hMips;
    	double meanSize = alfa * hSize; 
    	double deSize = beta * hSize;
    	double meanRam = alfa * hRam; 
    	double deRam = beta * hRam;
    	double meanBw = alfa * hBw; 
    	double deBw = beta * hBw;
    	
    	Random rnd1 = new Random();
    	Random rnd2 = new Random(rnd1.nextLong());
    	Random rnd3 = new Random(rnd2.nextLong());
    	Random rnd4 = new Random(rnd3.nextLong());
		
    	int mips = 0;
    	long size = 0; //image size (MB)
    	int ram = 0; //vm memory (MB)
    	long bw = 0;
    	int pesNumber = 1; //number of cpus
    	int vmid = 0;
    	String vmm = "Xen"; //VMM name
    	
    	for (int i=0; i < amount; i++) {
    		while (mips <= 0 || mips > hMips)
    			mips = (int) ScientificMethods.normDistribution(rnd1, meanMips, deMips);
    		while (size <= 0 || size > hSize)
    			size = (long) ScientificMethods.normDistribution(rnd1, meanSize, deSize);
    		while (ram <= 0 || ram > hRam)
    			ram = (int) ScientificMethods.normDistribution(rnd1, meanRam, deRam);
    		while (bw <= 0 || bw > hBw)
    			bw = (long) ScientificMethods.normDistribution(rnd1, meanBw, deBw);
    		
    		Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			System.out.println("VM:: "+vm.getBw()+"**"+vm.getMips()+"**"+vm.getRam()+"**"+vm.getSize());
			vms.add(vm);
			vmid++;
			
			// 清零，下一次while可以进入循环
			mips = 0;
	    	size = 0;
	    	ram = 0;
	    	bw = 0;
    	}
		
    	
    	
		
		return (List<T>) vms;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Vm> List<T> readWorkLoadFromXml(String xmlFile) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
		DocumentBuilder db = dbf.newDocumentBuilder();   
		// read from file
		Document doc = db.parse(new java.io.File(xmlFile));
		List<Vm> vms = new ArrayList<Vm>();
		System.out.println("haha");
		int mips = 0;
    	long size = 0; //image size (MB)
    	int ram = 0; //vm memory (MB)
    	long bw = 0;
    	int pesNumber = 1; //number of cpus
    	int vmid = 0;
    	String vmm = "Xen"; //VMM name
		
		NodeList nodeList = doc.getElementsByTagName("lease");
		for (int i=0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			System.out.println("fa " + node.getNodeName());
			Node lNode = node.getChildNodes().item(1);
			Node nodeSet = lNode.getChildNodes().item(1);
			pesNumber = Integer.parseInt(nodeSet.getAttributes().getNamedItem("numnodes").getNodeValue());
			int Ram = nodeSet.getChildNodes().getLength();
			NodeList ns = nodeSet.getChildNodes();
			for (int j=0; j < ns.getLength(); j++) {
				Node n = ns.item(j);
				if (n.getNodeName().equals("res")) {
					String type = n.getAttributes().getNamedItem("type").getNodeValue();
					if (type.equals("Bandwidth"))
						bw = Long.parseLong(n.getAttributes().getNamedItem("amount").getNodeValue());
					if (type.equals("Disk"))
						size = Long.parseLong(n.getAttributes().getNamedItem("amount").getNodeValue());
					if (type.equals("Memory"))
						ram = Integer.parseInt(n.getAttributes().getNamedItem("amount").getNodeValue());
					if (type.equals("CPU"))
						mips = Integer.parseInt(n.getAttributes().getNamedItem("amount").getNodeValue());
				}
			}
			Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			System.out.println("VM:: "+vm.getBw()+"**"+vm.getMips()+"**"+vm.getRam()+"**"+vm.getSize());
			vms.add(vm);
			vmid++;
		}
		
		return (List<T>) vms;
		
	}
	
	/**
	 * Gets the vm list.
	 *
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 *
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

}
