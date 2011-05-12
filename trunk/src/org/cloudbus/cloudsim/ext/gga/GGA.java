package org.cloudbus.cloudsim.ext.gga;

import org.cloudbus.cloudsim.ext.gga.enums.PackingT;

//TODO: Something needs to be done, for get config options of the gga.
//files needs to be implemented or otherwise.

public class GGA {
	private Problem problem;
	
	private GaParamsT gaparams = new GaParamsT();
	private Population population = new Population();
	private java.io.File solutionsFile;
	private java.io.File dataFile;
	private int maxevals;
	private int nrofobjects;
	private int randomseed;
	private boolean debug;
	private boolean plotdata;
	private boolean printsolutions;
	
	public void Initialize (Problem problem, int maxEvaluations, int seed)
	// Set all genetic algorithm specific parameters and
	// initialize the population.
	{
		this.problem = problem;
		
		int numberOfObjects = problem.getNrOfItems();
		nrofobjects = numberOfObjects;
		maxevals = maxEvaluations;

		//TODO: These properties should be read from a file;
		gaparams.PopulationSize = 20;//inifile.ReadInt ("populationsize");
		gaparams.N_Crossover = 6;//inifile.ReadInt ("crossover");
		gaparams.N_Mutation = 7;//inifile.ReadInt ("mutation");
		gaparams.AllelMutationProb = 0.8;//inifile.ReadDouble ("allelemutationprob");

		if (gaparams.PopulationSize < gaparams.N_Crossover * 2)
		{
			System.err.println("Error: the population size (" + gaparams.PopulationSize + ") is smaller than twice the amount of crossovers (" + gaparams.N_Crossover + ") per generation");
			System.exit(2);
		}
		if (gaparams.PopulationSize < gaparams.N_Mutation)
		{
			System.err.println("Error: the population size (" + gaparams.PopulationSize + ") is smaller than the amount of mutations (" + gaparams.N_Mutation + ") per generation");
			System.exit(2);
		}
		if ((gaparams.AllelMutationProb < 0) || (gaparams.AllelMutationProb > 1))
		{
			System.err.println("Error: the parameter AllelMutationProb (" + gaparams.AllelMutationProb + ") should lie between 0 and 1");
			System.exit(2);

		}

		debug = false;//inifile.ReadBool ("ggadebug");
		plotdata = true;//inifile.ReadBool ("plotdata");
		printsolutions = true;//inifile.ReadBool ("printsolutions");

		if (printsolutions)
		{
			solutionsFile = new java.io.File("solutions");// .open (inifile.ReadString("solutionsfile"));
			if (!solutionsFile.exists())
			{
				printsolutions = false;
				System.err.println("Warning: could not open solution files");
			}
		}

		if (plotdata)
		{
			dataFile =  new java.io.File("data");//.open (inifile.ReadString("datafile"));
			if (!dataFile.exists())
			{
				plotdata = false;
				System.err.println("Warning: could not open data files");
			}
		}

	} // Initialize ()


	public void InitializePopulation ()
	{
		randomseed++;
		PackingT packingAlgorithm = PackingT.UNDIFINED;
		
		packingAlgorithm = PackingT.FIRSTFIT;
		//TODO: Other strategies, max packing num;

		population.Initialize (problem, gaparams, nrofobjects, false, 50, packingAlgorithm);

	} // InintializePupulation ()


	public boolean Run ()
	// Run the genetic algorithm until it solves the problem or until
	// it has reached _maxevals_ evaluations. Return true if a solution
	// is found.
	{
		int gen;

		population.Evaluate ();
		gen = 0;
		if (debug)
			System.err.print("\b\b\b\b\b\b\b\b\b\b" +  population.GetBestFitness () + GetBinsUsed ());
		
		while ((population.GetBestFitness () > 0) && (population.GetTotalEvaluations ()) < maxevals)
		{
			population.Reproduce ();
			gen++;
			population.Evaluate ();
			if (debug)
				System.err.println("\b\b\b\b\b\b\b\b\b\b" + population.GetBestFitness () + GetBinsUsed ());

			if (plotdata)
				//TODO: need!//dataFile. population.GetTotalEvaluations () + " " +\ population.GetBestFitness () << endl;
				System.err.println("lack of codes1!");
		}

		if (plotdata)
			//TODO: need!
			System.err.println("lack of codes2!");

		// Print some stats
		if (debug) 
			System.err.println("\b\b\b\b\b" + "total evals = " + population.GetTotalEvaluations () + "    bestfitness = " + population.GetBestFitness ());

		if (printsolutions)
			//TODO: Solution file
			//population.PrintBest (solutionsfile);
			population.PrintBest ();
		
		population.PrintBest ();

		if (population.GetBestFitness () > 0)
			return (true);
		else
			return (false);
	} // Run ()


	public void Close ()
	{
		//TODO: close files
		return;
	} // Close ()


	public double GetBinsUsed ()
	{
		return (population.GetBinsUsed ());

	} // GetColorsUsed ()


	public double GetTotalEvaluations ()
	// Return the totalevaluations done so far by the
	// genetic algorithm, no calculations are done.
	{
		return (population.GetTotalEvaluations ());

	} // GetTotalEvaluation ()


	public Genotype getBestGeno() {
		//return population.getBestGeno();
		return population.getCurBestGeno();
	}

}
