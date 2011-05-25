package org.cloudbus.cloudsim.ext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.*;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Vm;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class WorkLoad {
	private String workMode;
	private String loadFile;
	private int brokerId;
	private List<? extends Vm> vmList;
	
	public WorkLoad(String workMode, String loadFile, int brokerId) {
		this.workMode = workMode;
		this.loadFile = loadFile;
		this.brokerId = brokerId;
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
			//TODO: Gen leases
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> readWorkLoadFromXml(String xmlFile) throws ParserConfigurationException, SAXException, IOException {
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
