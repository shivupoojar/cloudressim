package org.cloudbus.cloudsim.ext.gga;

import org.cloudbus.cloudsim.ext.gga.enums.PackingT;
import org.cloudbus.cloudsim.ext.utils.ScientificMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Genotype {
	static private int idnum = 0;
	static private Random rnd = new Random();
	static private Problem problem;

	private int idTag; // unique number
	private double fitness; // current fitness
	private double[] gFitness; // current fitness
	private int nrOfObjects; // number of objects
	private int nrOfGroups; // number of groups
	private int nrOfPacks; // number of colors in problem
	private double allEleMutationProb; // chance group is deleted in mutation
	private PackingT packingUsed; // what algo to use for coloring nodes
	private int objects[]; // holds objects
	private int groups[]; // holds groups

	public Genotype() {
		this.objects = new int[Constants.MAXOBJECTS];
		this.groups = new int[Constants.MAXOBJECTS];
		this.packingUsed = PackingT.FIRSTFIT;
		this.gFitness = new double[Constants.MAXOBJECTS];
	}

	public void Initialize(int numberOfObjects,
			double allElemutationProbability, int kBinpacking,
			PackingT packingAlgorithm)
	// Initialize the geno by making a coloring using the
	// PackObject function, and starting at a random node.
	{
		int i, r;

		r = rnd.nextInt(99999);

		idTag = idnum;
		idnum++;
		allEleMutationProb = allElemutationProbability;

		packingUsed = packingAlgorithm;

		if (packingUsed == PackingT.UNDIFINED) {
			System.err.println("Error: No coloring algorithm defined");
			System.exit(2);
		}

		nrOfGroups = 0;
		nrOfPacks = kBinpacking;
		nrOfObjects = numberOfObjects;

		if (nrOfObjects > Constants.MAXOBJECTS) {
			System.err.println("Error: number of objects is larger than "
					+ Constants.MAXOBJECTS);
			System.exit(2);
		}

		for (i = 0; i < nrOfObjects; i++)
			objects[i] = Constants.UNCOLORED;
		for (i = 0; i < nrOfObjects; i++)
			PackObject((i + r) % nrOfObjects);

		Evaluate();

	}

	public void Evaluate() {
		double uRam = 0;
		double uCpu = 0;
		double uDisk = 0;
		double uBw = 0;
		double uAvg = 0;
		int cRam[];
		int cCpu[];
		int cDisk[];
		int cBw[];

		fitness = 0;
		
		//int n = GetBinsUsed();
		// group sequence is no longer continuous
		int n = Constants.MAXOBJECTS;
		cRam = new int[n];
		cCpu = new int[n];
		cBw = new int[n];
		cDisk = new int[n];
		for (int i=0; i < n; i++) {
			cBw[i] = 0;
			cCpu[i] = 0;
			cDisk[i] = 0;
			cRam[i] = 0;
			gFitness[i] = 0;
		}
		
		for (int j=0; j < nrOfObjects; j++) {
			int group = objects[j];
			cBw[group] += problem.getItemRequest(j, 0);
			cCpu[group] += problem.getItemRequest(j, 1);
			cDisk[group] += problem.getItemRequest(j, 2);
			cRam[group] += problem.getItemRequest(j, 3);
		}
		
		int nRam = problem.GetBinSize().Mem;
		int nCpu = problem.GetBinSize().Cpu;
		int nDisk = problem.GetBinSize().Disk;
		int nBw = problem.GetBinSize().Bandwidth;
		
		for (int i=0; i < n; i++) {
			//计算算子结果
			uRam = (double)cRam[i] / nRam;
			//System.out.println("uRam: " + uRam);
			uCpu = (double)cCpu[i] / nCpu;
			uDisk = (double)cDisk[i] / nDisk;
			uBw = (double)cBw[i] / nBw;
			uAvg = (uRam+uCpu+uDisk+uBw) / 4;
			
			//计算FF中单项分母
			double down = 0;
			down += Math.sqrt(Math.abs(uCpu-uAvg));
			down += Math.sqrt(Math.abs(uBw-uAvg));
			down += Math.sqrt(Math.abs(uDisk-uAvg));
			down += Math.sqrt(Math.abs(uRam-uAvg));
			
			//计算单项结果
			if (down != 0) {
				gFitness[i] = Math.sqrt(uAvg / down);
				fitness += gFitness[i];
			}
			
			//算子清零
			uRam = 0;
			uCpu = 0;
			uDisk = 0;
			uBw = 0;
			uAvg = 0;
		}
		
		//得到最后结果
		fitness /= GetBinsUsed();
		//System.err.println("fitdddd!!!ness: "+fitness);		
	}

	public void Mutation()
	// Mutate a geno by eliminating some groups and reinserting
	// the objects using the PackObject function.
	{
		System.out.println("\n\nBeforeMutation:");
		this.Print();
		System.out.println("----BeforeMutation\n\n");
		
		int i;
		Stack<Integer> savedObjects = new Stack<Integer>();			// objects form eliminated groups
		boolean eliminated [] = new boolean[Constants.MAXOBJECTS];	// marker-array for eliminated groups

		//cerr << "mutation " << idtag << endl;

		for (i = 0; i < nrOfGroups; i++)
				eliminated[i] = false;

		// pick colors for elimination
		for (i = 0; i < nrOfGroups; i++)
			if ((rnd.nextInt (99999) % 100) <= (allEleMutationProb * 100))
			{
				eliminated[groups[i]] = true;
				groups[i] = Constants.ELIMINATED;
			}

		// Pick all colors that were in an eliminated group and mark
		// them uncolored
		for (i = 0; i < nrOfObjects; i++)
		{
			if (eliminated[objects[i]])
			{
				objects[i] = Constants.UNCOLORED;
				savedObjects.push (i);
			}
		}

		System.out.println("before compact");
		this.Print();
		System.out.println("before compact");
		// Remove holes in group part created by elimination
		CompactGroupSimple ();
		System.out.println("\nafter compact");
		this.Print();
		System.out.println("after compact");

		// Reinsert uncolored objects with PackObject function
		while (!savedObjects.empty ()) {
			PackObject (savedObjects.pop ());
		}

		// Reevaluate geno
		Evaluate ();
		
		System.out.println("\n\nAfterMutation:");
		this.Print();
		System.out.println("----AfterMutation\n\n");
	} // Mutation ()
	
	// 旧的CrossOver暂时废除
	/*
	public void Crossover(Genotype otherParent, Genotype child1, Genotype child2)
	// Do a crossover operation between the geno and another parent,
	// producing two children. Using the procedure described by
	// E.Falkenhauer and the PackObject function to reinsert objects.
	{
		int i, j;
		int p1cp1, p1cp2;				// crossover-points for parent P1
		int p2cp1, p2cp2;				// crossover-points for parent P2
		boolean eliminated1[] = new boolean[Constants.MAXOBJECTS];	// marker-array for elimination process P1
		boolean eliminated2[] = new boolean[Constants.MAXOBJECTS];	// marker-array for elimination process P2
		Stack<Integer> objects1 = new Stack<Integer>();				// holds objects from eliminated groups P1
		Stack<Integer> objects2 = new Stack<Integer>();				// holds objects from eliminated groups P2
	
		for (i = 0; i < nrOfGroups; i++)
		{
			eliminated1[i] = false;
			eliminated2[i] = false;
		}
	
		//cerr << "crossover " << idtag << " " << otherParent.idtag << " " << child1.idtag << " " << child2.idtag << endl;
	
		// Choose crossover points
		p1cp1 = rnd.nextInt (99999) % nrOfGroups;
		p1cp2 = rnd.nextInt (99999) % nrOfGroups;
		if (p1cp2 < p1cp1)
		{
			i = p1cp1;
			p1cp1 = p1cp2;
			p1cp2 = i;
		}
		p2cp1 = rnd.nextInt (99999) % otherParent.nrOfGroups;
		p2cp2 = rnd.nextInt (99999) % otherParent.nrOfGroups;
		if (p2cp2 < p2cp1)
		{
			i = p2cp1;
			p2cp1 = p2cp2;
			p2cp2 = i;
		}
		
		System.err.println("this.getCrossPoint: " + this.getCrossPoint());
	
		// Copy parents to children
		Copy (child1);
		otherParent.Copy (child2);
	
		// Mark all groups losing at least one object with ELIMINATED
		for (i = 0; i < nrOfObjects; i++)			// look at all objects
			for (j = p1cp1; j <= p1cp2; j++)		// walk through crossing-section
				if (objects[i] == groups[j])		// object is in injected group
				{
					eliminated2[child2.objects[i]] = true;		// mark group affected
					child2.objects[i] = groups[j] + child2.nrOfGroups;	// new color
				}
		for (i = 0; i < otherParent.nrOfObjects; i++)
			for (j = p2cp1; j <= p2cp2; j++)
				if (otherParent.objects[i] == otherParent.groups[j])
				{
					eliminated1[child1.objects[i]] = true;
					child1.objects[i] = otherParent.groups[j] + child1.nrOfGroups;
				}
	
		// Eliminate effected groups
		for (i = 0; i < child1.nrOfGroups; i++)
			if (eliminated1[child1.groups[i]])
					child1.groups[i] = Constants.ELIMINATED;
	
		for (i = 0; i < child2.nrOfGroups; i++)
			if (eliminated2[child2.groups[i]])
					child2.groups[i] = Constants.ELIMINATED;
	
		// Collect objects member of an eliminated group
		for (i = 0; i < child2.nrOfObjects; i++)
			if ((child2.objects[i] < child2.nrOfGroups) && (eliminated2[child2.objects[i]]))
			{
				child2.objects[i] = Constants.UNCOLORED;
				objects2.push (i);
			}
		for (i = 0; i < child1.nrOfObjects; i++)
			if ((child1.objects[i] < child1.nrOfGroups) && (eliminated1[child1.objects[i]]))
			{
				child1.objects[i] = Constants.UNCOLORED;
				objects1.push (i);
			}
	
		// Inject group-part from parents into children
		child2.InsertGroup (groups, p1cp1, p1cp2, p2cp1);
		child1.InsertGroup (otherParent.groups, p2cp1, p2cp2, p1cp1);
	
		// Remove holes in group-array created by the elimination process
		child2.CompactGroupPart ();
		child1.CompactGroupPart ();
	
		// Reinsert objects from eliminted groups
		while (!objects2.empty ())
			child2.PackObject (objects2.pop ());
		while (!objects1.empty ())
			child1.PackObject (objects1.pop ());
	
		// Compute fitness of children
		child2.Evaluate ();
		child1.Evaluate ();
	
	} // Crossover ()
	*/

	public void Crossover(Genotype otherParent, Genotype child1, Genotype child2)
	// 这个CrossOver是单基因位的交换
	{
		int p1cp, p2cp;		//两个父代的交汇点；
		int i;
		boolean eliminated1[] = new boolean[Constants.MAXOBJECTS];	// marker-array for elimination process P1
		boolean eliminated2[] = new boolean[Constants.MAXOBJECTS];	// marker-array for elimination process P2
		Stack<Integer> objects1 = new Stack<Integer>();				// holds objects from eliminated groups P1
		Stack<Integer> objects2 = new Stack<Integer>();				// holds objects from eliminated groups P2
		
		for (i = 0; i < nrOfGroups; i++)
		{
			eliminated1[i] = false;
			eliminated2[i] = false;
		}
		
		// Choose crossover point
		p1cp = getCrossPoint();
		p2cp = otherParent.getCrossPoint();
		
		// Copy parents to children
		Copy (child1);
		otherParent.Copy (child2);
		
		// 将bin_id冲突的item位直接拿出来
		for (i = 0; i < child2.nrOfObjects; i++)
			if (child2.objects[i] == groups[p1cp])
			{
				child2.objects[i] = Constants.UNCOLORED;
				objects2.push (i);
			}
		for (i = 0; i < child1.nrOfObjects; i++)
			if (child1.objects[i] == otherParent.groups[p2cp])
			{
				child1.objects[i] = Constants.UNCOLORED;
				objects1.push (i);
			}

		// Mark all groups losing at least one object with ELIMINATED
		for (i = 0; i < nrOfObjects; i++)			// look at all objects
			if (objects[i] == groups[p1cp])			// object is in injected group
			{
				if (child2.objects[i] != Constants.UNCOLORED) eliminated2[child2.objects[i]] = true;		// mark group affected
				child2.objects[i] = groups[p1cp];	// 这里改动了，变成直接的
			}
		for (i = 0; i < otherParent.nrOfObjects; i++)
			if (otherParent.objects[i] == otherParent.groups[p2cp])
			{
				if (child1.objects[i] != Constants.UNCOLORED) eliminated1[child1.objects[i]] = true;
				child1.objects[i] = otherParent.groups[p2cp];
			}
		
		// 这里把bin_id冲突的标记为eliminated，为了下一步抹去group
		eliminated1[otherParent.groups[p2cp]] = true;
		eliminated2[groups[p1cp]] = true;
		
		// Eliminate effected groups
		// 这里把bin_id冲突的group位抹去
		for (i = 0; i < child1.nrOfGroups; i++)
			if (eliminated1[child1.groups[i]])
					child1.groups[i] = Constants.ELIMINATED;

		for (i = 0; i < child2.nrOfGroups; i++)
			if (eliminated2[child2.groups[i]])
					child2.groups[i] = Constants.ELIMINATED;
		
		// 注意，我们不把和bin_id冲突的标记为eliminated，因为item位已经染色
		eliminated1[otherParent.groups[p2cp]] = false;
		eliminated2[groups[p1cp]] = false;

		// Collect objects member of an eliminated group
		// 这里不标记item位冲突的；
		for (i = 0; i < child2.nrOfObjects; i++)
			if ((child2.objects[i] != Constants.UNCOLORED) && (child2.objects[i] < child2.nrOfGroups) && (eliminated2[child2.objects[i]]))
			{
				child2.objects[i] = Constants.UNCOLORED;
				objects2.push (i);
			}
		for (i = 0; i < child1.nrOfObjects; i++)
			if ((child1.objects[i] != Constants.UNCOLORED) && (child1.objects[i] < child1.nrOfGroups) && (eliminated1[child1.objects[i]]))
			{
				child1.objects[i] = Constants.UNCOLORED;
				objects1.push (i);
			}

		// Inject group-part from parents into children
		child2.InsertGroup (groups, p1cp, p2cp);
		child1.InsertGroup (otherParent.groups, p2cp, p1cp);

		// Remove holes in group-array created by the elimination process
		child2.CompactGroupSimple ();
		child1.CompactGroupSimple ();

		// Reinsert objects from eliminted groups
		while (!objects2.empty ())
			child2.PackObject (objects2.pop ());
		while (!objects1.empty ())
			child1.PackObject (objects1.pop ());

		// Compute fitness of children
		child2.Evaluate ();
		child1.Evaluate ();
		
		System.out.println("\n\nAfterCrossOver:");
		child1.Print();
		child2.Print();
		System.out.println("----AfterCrossOver\n\n");
	}
	
	// 旧的是MulCrossover
	public void CrossoverOld(Genotype otherParent, Genotype child1, Genotype child2)
	// Do a crossover operation between the geno and another parent,
	// producing two children. Using the procedure described by
	// E.Falkenhauer and the PackObject function to reinsert objects.
	{
		int i, j;
		int p1cp1, p1cp2;				// crossover-points for parent P1
		int p2cp1, p2cp2;				// crossover-points for parent P2
		boolean eliminated1[] = new boolean[Constants.MAXOBJECTS];	// marker-array for elimination process P1
		boolean eliminated2[] = new boolean[Constants.MAXOBJECTS];	// marker-array for elimination process P2
		Stack<Integer> objects1 = new Stack<Integer>();				// holds objects from eliminated groups P1
		Stack<Integer> objects2 = new Stack<Integer>();				// holds objects from eliminated groups P2

		for (i = 0; i < nrOfGroups; i++)
		{
			eliminated1[i] = false;
			eliminated2[i] = false;
		}

		//cerr << "crossover " << idtag << " " << otherParent.idtag << " " << child1.idtag << " " << child2.idtag << endl;

		// Choose crossover points
		p1cp1 = rnd.nextInt (99999) % nrOfGroups;
		p1cp2 = rnd.nextInt (99999) % nrOfGroups;
		if (p1cp2 < p1cp1)
		{
			i = p1cp1;
			p1cp1 = p1cp2;
			p1cp2 = i;
		}
		p2cp1 = rnd.nextInt (99999) % otherParent.nrOfGroups;
		p2cp2 = rnd.nextInt (99999) % otherParent.nrOfGroups;
		if (p2cp2 < p2cp1)
		{
			i = p2cp1;
			p2cp1 = p2cp2;
			p2cp2 = i;
		}
		
		System.err.println("this.getCrossPoint: " + this.getCrossPoint());

		// Copy parents to children
		Copy (child1);
		otherParent.Copy (child2);

		// Mark all groups losing at least one object with ELIMINATED
		for (i = 0; i < nrOfObjects; i++)			// look at all objects
			for (j = p1cp1; j <= p1cp2; j++)		// walk through crossing-section
				if (objects[i] == groups[j])		// object is in injected group
				{
					eliminated2[child2.objects[i]] = true;		// mark group affected
					child2.objects[i] = groups[j] + child2.nrOfGroups;	// new color
				}
		for (i = 0; i < otherParent.nrOfObjects; i++)
			for (j = p2cp1; j <= p2cp2; j++)
				if (otherParent.objects[i] == otherParent.groups[j])
				{
					eliminated1[child1.objects[i]] = true;
					child1.objects[i] = otherParent.groups[j] + child1.nrOfGroups;
				}

		// Eliminate effected groups
		for (i = 0; i < child1.nrOfGroups; i++)
			if (eliminated1[child1.groups[i]])
					child1.groups[i] = Constants.ELIMINATED;

		for (i = 0; i < child2.nrOfGroups; i++)
			if (eliminated2[child2.groups[i]])
					child2.groups[i] = Constants.ELIMINATED;

		// Collect objects member of an eliminated group
		for (i = 0; i < child2.nrOfObjects; i++)
			if ((child2.objects[i] < child2.nrOfGroups) && (eliminated2[child2.objects[i]]))
			{
				child2.objects[i] = Constants.UNCOLORED;
				objects2.push (i);
			}
		for (i = 0; i < child1.nrOfObjects; i++)
			if ((child1.objects[i] < child1.nrOfGroups) && (eliminated1[child1.objects[i]]))
			{
				child1.objects[i] = Constants.UNCOLORED;
				objects1.push (i);
			}

		// Inject group-part from parents into children
		child2.InsertGroup (groups, p1cp1, p1cp2, p2cp1);
		child1.InsertGroup (otherParent.groups, p2cp1, p2cp2, p1cp1);

		// Remove holes in group-array created by the elimination process
		child2.CompactGroupPart ();
		child1.CompactGroupPart ();

		// Reinsert objects from eliminted groups
		while (!objects2.empty ())
			child2.PackObject (objects2.pop ());
		while (!objects1.empty ())
			child1.PackObject (objects1.pop ());

		// Compute fitness of children
		child2.Evaluate ();
		child1.Evaluate ();

	} // MulCrossover ()

	public void Print()
	// Print out the geno's genes and it's fitness.
	{
		int i;

		System.out.print("(" + idTag + ") ");

		// Print out objects
		for (i = 0; i < nrOfObjects; i++)
			if (objects[i] == Constants.UNCOLORED)
				System.out.print("X ");
			else
				System.out.print(objects[i]+ " ");

		System.out.print(" : ");

		// Print out groups
		for (i = 0; i < nrOfGroups; i++)
			if (groups[i] == Constants.ELIMINATED)
				System.out.print("X ");
			else
				System.out.print(groups[i]+" ");

		System.out.print(", ");

		// Print out fitness
		System.out.println("fitness: "+fitness);
	}

	public int GetBinsUsed() {
		return nrOfGroups;
	}

	public double GetFitness() {
		return fitness;
	}

	public void Copy(Genotype child) 
	// Copy the geno to the supplied recipiant.
	{
		int i;

		for (i = 0; i < nrOfObjects; i++)
			child.objects[i] = objects[i];
		for (i = 0; i < nrOfGroups; i++)
			child.groups[i] = groups[i];

		child.nrOfObjects = nrOfObjects;
		child.nrOfGroups = nrOfGroups;
		child.fitness = fitness;

	}

	public int IsValid(int object)
	// Check if all objects in the geno do not violate a solution, and
	// check if there are no duplicate colors.
	{
		return 1;
	}

	public static Problem getProblem() {
		return problem;
	}

	public static void setProblem(Problem problem) {
		Genotype.problem = problem;
	}
	
	public int getAllocatedHost(int vm) {
		if (vm < nrOfObjects)
			return objects[vm];
		else {
			System.err.println("Err: Vm sequence exeeded");
			return -1;
		}
	}

	private int ViolatedConstraints (int object)
	// Calculate the number of constraints an object violates.
	{
		int violations = 0;
		boolean success = false;
		
		int group = objects[object];
		
		Capacity size = problem.GetBinSize();
		
		for (int i=0; i < nrOfObjects; i++) {
			if (objects[i] == group) {
				//将第i个item放到bin里头，size记录当前这个bin剩余容量
				success = problem.PutItem(size, i);
				//如果不能放入的话，返回>0的violations，表示这个防止方法不符合要求
				if (!success) return 1;
			}
		}
			
		return violations;
	} // ViolatedConstraints ()

	private void PackObject(int object)
	// Pack an object using the algoritm selected.
	{
		switch (packingUsed) {
		case FIRSTFIT:
			PackObject_FirstFit(object);
			break;
		// These two have to be implemented
		/*
		 * case smallfirst: PackObject_OrderedPacking (object); break; case
		 * largefirst: PackObject_OrderedPacking (object); break;
		 */
		default:
			System.err.println("Error: No coloring algorithm defined");
			System.exit(2);
		}

	} // PackObject ()

	private void PackObject_FirstFit(int object)
	// Packs an object by using a first fit heurist, if no color
	// is available it creates a new group and uses this to color
	// the object with.
	{
		int i = 0;
		int bin = -1;
		boolean find = false;
		boolean[] binUsed = new boolean[Constants.MAXOBJECTS];
		
		// TODO: 这里可以考虑用哈希表存储已经访问过的
		for (int j = 0; j < Constants.MAXOBJECTS; j++) {
			binUsed[j] = false;
		}

		// First Fit Packing
		while (i < nrOfGroups) {
			bin = groups[i];
			binUsed[bin] = true;
			objects[object] = bin;
			if (ViolatedConstraints(object) > 0) {
				i++;
			} else {
				find = true;
				break;
			}
		}
		
		// 如果没有从已用箱子里头找到合适的，则需要重新建一个箱子
		i = 0;
		while (!find) {
			//如果箱子木有用，则试试
			if (!binUsed[i]) {
				objects[object] = i;
				if (ViolatedConstraints(object) > 0) {
					find = false;
				} else {
					find = true;
					nrOfGroups++;
					groups[nrOfGroups - 1] = i;
				}
			} 
			i++;
		}
	} // PackObject_FirstFit ()

	private void InsertGroup(int parentGroups[], int cp1, int cp2, int position)
	// Given the group-part and the crossing-points of another geno,
	// this method inserts the groups between these points on the
	// given position.
	{
		int i;

		// Make room for to-be-inserted-groups
		for (i = nrOfGroups; i > position; i--)
			groups[i + (cp2 - cp1)] = groups[i - 1];

		// Inject groups in the gene
		for (i = cp1; i <= cp2; i++)
			groups[i + position - cp1] = parentGroups[i] + nrOfGroups;

		// Update number of groups
		nrOfGroups = nrOfGroups + (cp2 - cp1) + 1;

	} // InsertGroup ()

	
	// 将另一个父辈cross point上头的插入到自己的position
	private void InsertGroup(int[] parentGroups, int cp, int position) {
		groups[this.nrOfGroups] = parentGroups[cp];
		nrOfGroups ++;
	}

	private void CompactGroupPart()
	// After elimination of groups from the group-part of the gene,
	// this method removes the holes created by elimination and
	// renumbers the remaining groups to create a numbering between
	// 0 and nrOfGroups - 1.
	{
		int i, j;
		boolean found; // used in finding each number 0...(nrfgroups-1)
		int max; // maximum number currently used in group-part

		// Remove eliminated groups
		i = 0;
		while (i < nrOfGroups)
			if (groups[i] == Constants.ELIMINATED) {
				for (j = i; j < nrOfGroups - 1; j++)
					groups[j] = groups[j + 1];
				nrOfGroups--;
			} else
				i++;

		// Renumber to get a nice permutation of 0,1,2,3... again
		i = 0;
		while (i < nrOfGroups) {
			j = 0;
			found = false;
			max = 0;
			// Look for the current (i) number
			while ((j < nrOfGroups) && (!found)) {
				if (groups[j] > groups[max])
					max = j;
				if (groups[j] == i)
					found = true;
				else
					j++;
			}

			// Number (i) not found, give new number to largest number
			if (!found) {
				for (j = 0; j < nrOfObjects; j++)
					if (objects[j] == groups[max])
						objects[j] = i;
				groups[max] = i;
			}
			i++;
		}
	} // CompactGroup ()
	
	private void CompactGroupSimple()
	// 这个过程去掉了传统的压缩操作，仅仅进行移动位的操作；
	{
		int i, j;

		// Remove eliminated groups
		i = 0;
		while (i < nrOfGroups)
			if (groups[i] == Constants.ELIMINATED) {
				for (j = i; j < nrOfGroups - 1; j++)
					groups[j] = groups[j + 1];
				nrOfGroups--;
			} else
				i++;
	} // CompactGroupSimple ()
	
	private int getCrossPoint() {
		List<RankSort> rankList = new ArrayList<RankSort>();
		
		for (int i=0; i < GetBinsUsed(); i++) {
			// TODO: RankSort的groupId部分，应该是序列号，而不是bin_id
			rankList.add(new RankSort(i, gFitness[groups[i]]));
		}
		
		// 进行排序
		Collections.sort(rankList);
		
		// 得到一个随机的rank
		double prob = rnd.nextDouble();
		int selectedRank = ScientificMethods.getRankByProb(prob, GetBinsUsed());
		
		return rankList.get(selectedRank).groupId;
	}
}

class RankSort implements Comparable<RankSort>{
	public int groupId;
	public double fitness;
	
	public RankSort(int groupId, double fitness) {
		this.groupId = groupId;
		this.fitness = fitness;
	}

	@Override
	public int compareTo(RankSort other) {
		if ((fitness - other.fitness) > 0)
			return 1;
		else if ((fitness - other.fitness) < 0)
			return -1;
		else return 0;
	}
}
