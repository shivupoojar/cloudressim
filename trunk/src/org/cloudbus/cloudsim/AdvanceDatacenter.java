package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.ext.TopologyParamsT;
import org.cloudbus.cloudsim.ext.gga.GGA;
import org.cloudbus.cloudsim.ext.gga.GaParamsT;
import org.cloudbus.cloudsim.ext.gga.Genotype;
import org.cloudbus.cloudsim.ext.gga.Problem;
import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;

public class AdvanceDatacenter extends Datacenter {
	private List<? extends Vm> vmQueue;
	
	private int vmQueueCapacity;		//vmQueue容量，到了这个值启动一次vm部署
	private int ggaGenerations;
	private CloudSimEventListener progressListener;
	private GaParamsT gaparams;
	private TopologyParamsT topologyParams;

	public AdvanceDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval, int vmQueueCapacity, int totalGens, CloudSimEventListener l, GaParamsT gaparams, TopologyParamsT topologyParams) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		
		this.vmQueueCapacity = vmQueueCapacity;
		this.ggaGenerations = totalGens;
		this.progressListener = l;
		this.gaparams = gaparams;
		this.topologyParams = topologyParams;
		setVmQueue(new ArrayList<Vm>());
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmQueue() {
		return (List<T>) vmQueue;
	}

	protected <T extends Vm> void setVmQueue(List<T> vmQueue) {
		this.vmQueue = vmQueue;
	}

	/**
     * Processes events or services that are available for this PowerDatacenter.
     *
     * @param ev    a Sim_event object
     *
     * @pre ev != null
     * @post $none
     */
    @Override
	public void processEvent(SimEvent ev) {
    	super.processEvent(ev);
    }
    
    /**
     * Process the event for an User/Broker who wants to create a VM
     * in this PowerDatacenter. This PowerDatacenter will then send the status back to
     * the User/Broker.
     *
     * @param ev   a Sim_event object
     * @param ack the ack
     *
     * @pre ev != null
     * @post $none
     */
    @Override
    protected void processVmCreate(SimEvent ev, boolean ack) {
    	Vm vm = (Vm) ev.getData();
    	getVmQueue().add(vm);
    	
    	if (getVmQueue().size() == vmQueueCapacity)
    		allocateVmsWithGGA();
    }
    
    private void allocateVmsWithGGA() {
    	
    	Genotype last = new Genotype("163 172 161 3 147 150 12 106 84 84 234 234 216 231 147 155 45 155 125 43 45 29 6 108 92 13 13 233 234 28 7 7 233 232 155 106 232 47 232 9 9 231 231 230 28 28 58 22 202 37 230 101 58 5 225 5 4 22 4 72 26 58 230 54 229 14 25 11 229 36 15 229 34 25 226 25 228 11 36 65 38 228 227 14 15 26 76 89 19 16 109 227 15 26 16 226 74 18 226 206 36 70 18 18 64 222 53 225 36 19 23 42 145 19 23 37 37 29 115 145 23 33 147 81 81 81 82 225 82 224 82 223 30 29 30 195 157 224 85 85 42 42 223 31 82 63 223 3 32 222 191 215 31 219 32 210 85 222 17 49 32 32 59 53 41 40 221 27 221 33 30 39 66 50 220 53 38 70 220 33 218 34 167 34 52 59 149 35 35 59 87 12 39 17 40 12 219 218 218 217 17 63 83 217 217 38 40 74 201 39 212 43 216 11 67 211 100 216 215 111 215 213 214 214 213 56 70 27 44 204 94 63 41 173 63 40 87 80 41 213 208 17 87 27 27 77 81 118 194 118 61 118 212 103 212 49 50 211 209 48 210 97 111 131 181 211 97 50 111 43 90 43 111 51 48 57 48 54 49 54 74 50 118 55 130 132 106 55 51 60 56 119 61 2 56 153 102 51 57 55 57 60 189 55 134 134 134 79 210 60 209 73 61 209 177 138 88 208 206 204 138 68 208 61 207 134 207 126 135 93 206 75 64 205 64 205 205 65 65 67 67 204 76 171 107 22 116 66 67 93 75 75 86 204 203 100 8 76 192 203 200 199 76 203 202 143 46 44 54 148 24 103 44 202 5 37 46 77 56 157 157 77 100 68 94 77 94 100 201 68 105 78 69 105 105 201 46 96 78 105 47 159 200 69 198 69 200 162 121 162 199 0 26 121 90 95 121 89 89 90 198 196 128 162 109 109 2 199 198 104 198 197 2 52 92 47 98 52 92 72 197 72 197 72 73 75 196 117 62 73 52 102 96 96 112 116 96 115 115 102 195 126 196 126 120 194 8 186 195 157 102 193 193 194 191 193 103 190 107 8 192 13 103 5 8 62 103 104 83 116 190 192 191 104 191 104 149 188 83 190 86 163 123 107 6 159 107 159 83 86 86 108 110 0 0 62 190 66 124 88 131 124 130 0 141 108 127 156 46 66 71 124 130 108 130 143 163 99 120 112 114 112 129 117 112 189 129 101 88 101 145 117 145 131 189 117 150 140 184 78 113 127 188 188 186 187 158 98 136 98 98 188 71 99 84 15 149 149 141 129 187 19 99 177 136 119 158 71 158 187 80 21 186 183 158 142 110 152 110 185 28 23 180 119 186 119 79 79 34 185 185 113 170 93 136 184 80 184 183 13 183 95 80 120 141 120 141 182 178 182 106 123 113 21 123 21 31 133 113 114 45 30 122 1 182 114 181 35 150 181 25 5 137 123 127 179 180 91 70 91 95 127 180 21 74 122 179 160 171 122 133 95 146 178 174 10 133 179 176 177 178 10 148 11 139 156 135 165 177 135 176 156 156 160 152 175 132 132 148 140 140 176 140 152 152 175 175 160 154 1 160 164 174 173 164 10 174 173 142 39 150 172 18 14 68 161 20 173 20 161 172 20 161 172 154 154 7 171 171 150 142 170 137 139 137 91 139 170 6 139 170 151 142 125 168 146 146 151 128 24 125 144 128 151 9 125 144 144 144 166 167 4 128 167 166 166 153 7 153 169 26 6 4 169 168 24 153 168 165 8 10 165 12 165 1 0 2 6 24 3  : 20 58 81 82 85 118 134 138 157 162 159 101 152 21 91 2 47 153 12 154 161 129 126 121 111 97 87 70 26 128 148 141 136 131 116 109 94 36 11 4 74 125 95 80 79 71 66 62 52 46 44 27 17 84 150 1 165 166 167 144 160 156 158 149 145 130 124 115 105 100 93 63 59 53 42 37 28 13 10 0 151 146 142 5 140 132 122 114 113 110 99 98 88 86 83 73 72 69 68 55 51 50 49 40 39 38 32 31 25 22 45 24 155 147 143 163 164 106 8 139 137 135 133 127 123 120 119 117 112 108 107 104 103 102 96 92 90 89 78 77 76 75 67 65 64 61 60 57 56 54 48 43 41 35 34 33 30 29 23 19 18 16 15 14 9 7 6 3 168 169 170 171 172 173 174 175 176 177 178 179 180 181 182 183 184 185 186 187 188 189 190 191 192 193 194 195 196 197 198 199 200 201 202 203 204 205 206 207 208 209 210 211 212 213 214 215 216 217 218 219 220 221 222 223 224 225 226 227 228 229 230 231 232 233 234");
    	
    	Problem problem = new Problem();
    	problem.CreateProblem(getVmQueue(), getHostList(), topologyParams, last);
    	
    	GGA gga = new GGA(progressListener, gaparams);
    	//TODO: The initialization variable should be well considered
    	gga.Initialize(problem, ggaGenerations, new Random().nextInt(9999999));
    	
    	Genotype bestGeno = null;
		gga.InitializePopulation ();

		if (gga.Run()) {
			//TODO: 成功得到结果
			bestGeno = gga.getBestGeno();
		} else {
			//TODO: 如果不成功怎么样
		}
    		
    	gga.Close();
    	
    	/*
    	
    	//TODO: 临时代码，这部分要改的，试试再来一次调度
    	problem = new Problem();
    	problem.CreateProblem(getVmQueue(), getHostList(), topologyParams, bestGeno);
    	//gaparams.PopulationSize = problem.getNrOfItems();
    	//gaparams.N_Crossover = gaparams.PopulationSize / 2;
    	//gaparams.N_Mutation = gaparams.PopulationSize / 2;
    	gga = new GGA(progressListener, gaparams);
    	//TODO: The initialization variable should be well considered
    	gga.Initialize(problem, ggaGenerations, new Random().nextInt(9999999));
    	
    	bestGeno = null;
		gga.InitializePopulation ();

		if (gga.Run()) {
			bestGeno = gga.getBestGeno();
		} else {
		}    		
    	gga.Close();
    	
    	// 临时代码结束
    	*/
    	
    	allcateByGenotype(bestGeno, problem);
    }
    
    private void allcateByGenotype(Genotype geno, Problem problem) {
    	int size = getVmQueue().size();
    	String plan = "";
    	for (int i=0; i < size; i++) {
    		Vm vm = getVmQueue().get(i);
    		int host = problem.getHostAllocated(geno, i);//geno.getAllocatedHost(i);
    		plan += host + " ";
    		System.out.println("Vm " + i + "size" +vm.getMips());
    		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, getHostList().get(host));
    		int[] data = new int[3];
            data[0] = getId();
  	       	data[1] = vm.getId();
    		if (result) {
         	   data[2] = CloudSimTags.TRUE;
            } else {
         	   data[2] = CloudSimTags.FALSE;
            }
 		   	sendNow(vm.getUserId(), CloudSimTags.VM_CREATE_ACK, data);
			if (result) {
				double amount = 0.0;
				if (getDebts().containsKey(vm.getUserId())) {
					amount = getDebts().get(vm.getUserId());
				}
				amount += getCharacteristics().getCostPerMem() * vm.getRam();
				amount += getCharacteristics().getCostPerStorage()
						* vm.getSize();

				getDebts().put(vm.getUserId(), amount);

				getVmList().add(vm);

				vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy()
						.getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
			} else {
				System.err.println("GGA Seems to be failed");
				System.out.println("Host: " + problem.GetBinSize(host));
				//assert(3==2);
			}
    		
    	}
    	
    	System.out.println("VM ALLOCATON: " + plan);
    	
    	getVmQueue().clear();
    }
}
