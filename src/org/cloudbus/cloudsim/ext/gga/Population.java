package org.cloudbus.cloudsim.ext.gga;

import java.util.Random;

import org.cloudbus.cloudsim.ext.gga.enums.PackingT;

public class Population {
	static private Random rnd = new Random();
	// Use the default random seed(system time);

	private Genotype population[];
	private GaParamsT gaParams;
	private boolean debug;
	private double totalFitness;
	private int bestIndex;
	private double totalEvaluations;

	public Population()
	// Set the arrays
	{
		population = new Genotype[Constants.MAXPOPSIZE];
		gaParams = new GaParamsT();
	}

	public void Initialize(Problem problem, GaParamsT gaparameters, int nrofobjects,
			boolean debugactive, int k_coloring, PackingT coloringalgorithm)
	// Ask every geno in the population to initialize itself.
	{
		int i;
		bestIndex = 0;
		totalEvaluations = 0;

		debug = debugactive;
		gaParams = gaparameters;

		// Check for some disastrous errors which could ruin the experiments
		if (gaParams.PopulationSize > Constants.MAXPOPSIZE) {
			System.err.println("Error: population size is larger than "
					+ Constants.MAXPOPSIZE);
			System.exit(2);
		}
		if (gaParams.PopulationSize < 4) {
			System.err.println("Error: population size must be larger than 3 ");
			System.exit(2);
		}
		if ((gaParams.N_Crossover % 2) == 1) {
			gaParams.N_Crossover++;
			System.err
					.println("Warning:  crossover should be an even number, using "
							+ gaParams.N_Crossover + " instead");
		}

		//set the problem
		Genotype.setProblem(problem);
		for (i = 0; i < gaParams.PopulationSize; i++) {
			population[i] = new Genotype();
			population[i].Initialize(nrofobjects, gaParams.AllelMutationProb,
					k_coloring, coloringalgorithm);
		}

	} // Initialize ()

	public void Evaluate()
	// Ask fitness of every geno and make a total, note that
	// we do not ask every geno to calculate it's fitness.
	// note: could be faster by letting the evaluation in the
	// Genotype update our bestfitness variable.
	{
		int i;
		int object = 0; // holds first faulty gene

		for (i = 0; i < gaParams.PopulationSize; i++) {
			// This switch adds quite some extra load on the cpu
			/*if (debug)
				switch (population[i].IsValid(object)) {
				case 0:
					break;
				case 1:
					System.err.println("\n"
							+ "Found an invalid genoom! [badcoloring in gene "
							+ object + "]");
					population[i].Print();
					break;
				case 2:
					System.err.println("\n"
							+ "Found an invalid genoom! [duplicatecolor]");
					population[i].Print();
					break;
				case 3:
					System.err
							.println("\n"
									+ "Found an invalid genoom! [illegalcolorused in gene "
									+ object + "]");
					population[i].Print();
					break;
				}*/

			// Look for a better best fitness
			if (population[bestIndex].GetFitness() > population[i].GetFitness())
				bestIndex = i;
		}
	} // Evaluate ()

	public void Reproduce()
	// Use the genetic operators to form a new generation.
	{
		//PlayTwoTournament();
		PlayRoulette();
		ApplyCrossover();
		ApplyMutation();

	} // Reproduce ()

	public void PrintBest()
	// Print out the best geno from the pack, to standard error.
	{
		System.err.println(bestIndex + ": ");
		population[bestIndex].Print(/* cerr */);

	} // PrintBest ()

	public void PrintPop()
	// Give a printout of all genos, warning creates a lot
	// of output. Output goes to standerd error.
	{
		int i;

		for (i = 0; i < gaParams.PopulationSize; i++) {
			System.err.println(i + ": ");
			population[i].Print(/* cerr */);
		}
	} // PrintPop ()

	public double GetBestFitness()
	// Return the fitness value of the best geno, there is no
	// calculation done here.
	{
		return (population[bestIndex].GetFitness());

	} // GetBestFitness ()

	public double GetBinsUsed()
	// Return the fitness value of the best geno, there is no
	// calculation done here.
	{
		return (population[bestIndex].GetBinsUsed());

	} // GetBestFitness ()

	public double GetTotalEvaluations()
	// Return the number of evaluations done so far by this
	// population, no calculation done here.
	{
		return (totalEvaluations);

	} // GetTotalEvaluations ()

	// ------------------------------------------------ Private functions

	private void PlayTwoTournament()
	// Does the noisy-sort algorithm mentioned by E.Falkenhauer
	// in "A New Representation and Operators for GGA applied to
	// Grouping Problems". It takes at random two genos from the
	// poplation. The geno with the best fitness is put back into
	// the population, while the loser goes into a queue. This
	// queue ill replace our population, which leaves us with
	// a noisy-sort on our population.
	{
		int n, i, j;
		Genotype temp[]; // copy of population to use for selection
		boolean removed[]; // holds the losers

		temp = new Genotype[Constants.MAXPOPSIZE];
		removed = new boolean[Constants.MAXPOPSIZE];

		for (i = 0; i < gaParams.PopulationSize; i++) {
			removed[i] = false;
			population[i].Copy(temp[i]);
		}

		// We'll have to do this populationsize times
		for (n = 0; n < gaParams.PopulationSize - 1; n++) {
			// choose two genos for a tournament
			i = rnd.nextInt() % gaParams.PopulationSize;
			while (removed[i])
				i = (i + 1) % gaParams.PopulationSize;
			j = rnd.nextInt() % gaParams.PopulationSize;
			while ((i == j) || (removed[j]))
				j = (j + 1) % gaParams.PopulationSize;

			// pick winner and put it back, put loser in queue
			if ((temp[i].GetFitness() <= temp[j].GetFitness())
					&& (n < gaParams.PopulationSize - 1)) {
				temp[j].Copy(population[n]);
				removed[j] = true;
			} else {
				temp[i].Copy(population[n]);
				removed[i] = true;
			}
		}

		// Don't forget the last one
		i = 0;
		while (removed[i])
			i++;
		temp[i].Copy(population[n]);

	} // PlayTwoTournament ()
	
	private void PlayRoulette()
	// the roulette 
	{
		int n, i, j;
		Genotype temp[]; // copy of population to use for selection
		double probs[]; // holds the losers

		temp = new Genotype[Constants.MAXPOPSIZE];
		probs = new double[gaParams.PopulationSize];

		for (i = 0; i < gaParams.PopulationSize; i++) {
			probs[i] = 0;
			temp[i] = new Genotype();
			population[i].Copy(temp[i]);
		}

		// We'll have to do this populationsize times
		probs[0] = population[0].GetFitness();
		for (i = 1; i < gaParams.PopulationSize; i++) {
			probs[i] = probs[i-1] + population[i].GetFitness();
		}
		//归一化
		double totalP = probs[gaParams.PopulationSize - 1];
		for (i = 0; i < gaParams.PopulationSize; i++) {
			probs[i] = probs[i] / totalP;
		}
		
		double p;
		int selected = 0;
		for (i = 0; i < gaParams.PopulationSize; i++) {
			p = rnd.nextDouble();
			//选出轮盘命中个体
			if (p <= probs[0]) selected = 0;
			else {
				for (j = 1; j < gaParams.PopulationSize; j++) {
					if (p > probs[j-1] && p <= probs[j])
						selected = j;
				}
			}
			temp[selected].Copy(population[i]);
		}
	} // PlayRoulette()

	private void ApplyCrossover()
	// Does crossover operation on N_Crossove individuals.
	{
		int i;

		for (i = 0; i < gaParams.N_Crossover; i = i + 2) {
			population[gaParams.PopulationSize - i - 1].Crossover(
					population[gaParams.PopulationSize - i - 2], population[i],
					population[i + 1]);
			totalEvaluations += 2;
		}
	} // ApplyCrossover ()

	private void ApplyMutation()
	// Choose N_Mutation genos and do a mutation on each of them.
	{
		int i, j;
		boolean mutated[];

		mutated = new boolean[Constants.MAXPOPSIZE];

		for (i = 0; i < gaParams.PopulationSize; i++)
			mutated[i] = false;

		for (i = 0; i < gaParams.N_Mutation; i++) {
			j = rnd.nextInt(99999) % gaParams.PopulationSize;
			while (mutated[j])
				j = (j + 1) % gaParams.PopulationSize;
			mutated[j] = true;
			population[j].Mutation();
			totalEvaluations++;
		}

	} // ApplyMutation ()

	public Genotype getBestGeno() {
		return population[bestIndex];
	}
}
