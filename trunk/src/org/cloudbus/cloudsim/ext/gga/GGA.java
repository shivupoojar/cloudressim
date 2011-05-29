package org.cloudbus.cloudsim.ext.gga;

import java.io.FileWriter;
import java.io.IOException;

import org.cloudbus.cloudsim.ext.event.CloudSimEventListener;
import org.cloudbus.cloudsim.ext.event.CloudSimEvent;
import org.cloudbus.cloudsim.ext.event.CloudSimEvents;
import org.cloudbus.cloudsim.ext.gga.enums.PackingT;
import org.cloudbus.cloudsim.ext.Constants;

//TODO: Something needs to be done, for get config options of the gga.
//files needs to be implemented or otherwise.

public class GGA {
	private Problem problem;
	
	private GaParamsT gaparams = new GaParamsT();
	private Population population = new Population();
	private FileWriter solutionsFile;
	private FileWriter dataFile;
	private int maxRuntimes;
	private int nrofobjects;
	private int randomseed;
	private boolean debug;
	private boolean plotdata;
	private boolean printsolutions;
	
	private CloudSimEventListener progressListener;
	
	public GGA (CloudSimEventListener progressListener) {
		this.progressListener = progressListener;		
	}
	
	public void Initialize (Problem problem, int maxRuntimes, int seed)
	// Set all genetic algorithm specific parameters and
	// initialize the population.
	{
		this.problem = problem;
		
		int numberOfObjects = problem.getNrOfItems();
		nrofobjects = numberOfObjects;
		this.maxRuntimes = maxRuntimes;
		
		PropertiesReader properties = PropertiesReader.loader();

		//TODO: These properties should be read from a file;
		gaparams.PopulationSize = properties.getInt("populations_size"); //20//inifile.ReadInt ("populationsize");
		gaparams.N_Crossover = properties.getInt("crossover"); //6;//inifile.ReadInt ("crossover");
		gaparams.N_Mutation = properties.getInt("mutations"); // 7;//inifile.ReadInt ("mutation");
		gaparams.AllelMutationProb = properties.getDouble("allelemutation_prob"); //0.88("populations_size");0.8;//inifile.ReadDouble ("allelemutationprob");

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

		debug = properties.getBoolean("debug"); //false;//inifile.ReadBool ("ggadebug");
		plotdata = properties.getBoolean("plotdata"); //true;//inifile.ReadBool ("plotdata");
		printsolutions = properties.getBoolean("printsolutions"); //true;//inifile.ReadBool ("printsolutions");
		
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy/MM/dd HH/mm/ss");
		java.util.Date date = new java.util.Date();

		if (printsolutions)
		{
			try {
				solutionsFile = new FileWriter("solutions");
			} catch (IOException e1) {
				printsolutions = false;
				e1.printStackTrace();
			}// .open (inifile.ReadString("solutionsfile"));
			try {
				solutionsFile.write("New data:" + format.format(date));
			} catch (Exception e) {
				printsolutions = false;
				System.err.println("Warning: could not open solution files");
				e.printStackTrace();
			}
		}

		if (plotdata)
		{
			try {
				dataFile =  new FileWriter("data");
			} catch (IOException e1) {
				plotdata = false;
				e1.printStackTrace();
			}//.open (inifile.ReadString("datafile"));
			
			try {
				dataFile.write("New data:" + format.format(date));
			} catch (Exception e) {
				plotdata = false;
				e.printStackTrace();
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

		population.Initialize (problem, gaparams, nrofobjects, debug, 50, packingAlgorithm);

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
			System.err.print("\n   " +  population.GetBestFitness () + GetBinsUsed ());
		
		while ((population.GetBestFitness () > 0) && (gen < maxRuntimes))
		{
			population.Reproduce ();
			gen++;
			population.Evaluate ();
			if (debug)
				System.out.println("\n   " + population.GetBestFitness () + GetBinsUsed ());

			if (plotdata)
				try {
					dataFile.write(gen + " generation's best: " + population.getBestGeno() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			CloudSimEvent e1 = new CloudSimEvent(CloudSimEvents.EVENT_PROGRESS_UPDATE);
			e1.addParameter(Constants.PARAM_TIME, gen);
			progressListener.cloudSimEventFired(e1);
			
			CloudSimEvent e2 = new CloudSimEvent(CloudSimEvents.EVENT_FITNESS_UPDATE);
			e2.addParameter(Constants.PARAM_FVAL, (long)(population.GetBestFitness()*100));
			progressListener.cloudSimEventFired(e2);
		}

		if (plotdata) {
			//TODO: need!
		}

		// Print some stats
		if (debug) 
			System.out.println("\n   " + "total runs = " + gen + "    bestfitness = " + population.GetBestFitness ());

		if (printsolutions) {
			//TODO: Solution file
		}
		
		//getBestGeno().CompactFromOutSide();
				
		population.PrintBest ();

		if (population.GetBestFitness () > 0)
			return (true);
		else
			return (false);
	} // Run ()


	public void Close ()
	{
		try {
			dataFile.flush();
			solutionsFile.close();
			dataFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return;
	} // Close ()


	public double GetBinsUsed ()
	{
		return (population.GetBinsUsed ());

	} // GetColorsUsed ()

	public Genotype getBestGeno() {
		//return population.getBestGeno();
		return population.getCurBestGeno();
	}

}
