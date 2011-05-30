package org.cloudbus.cloudsim.ext.gui;

import java.awt.Graphics;

import org.cloudbus.cloudsim.ext.gui.SimulationUIElement;

public class GGAUIElement extends SimulationUIElement {

	private int ggaGens;
	private int populationSize;
	private int crossover;
	private int mutations;
	private double mutationProb;
	
	@Override
	public void paint(Graphics g) {
	}

	public int getGgaGens() {
		return ggaGens;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public int getCrossover() {
		return crossover;
	}

	public int getMutations() {
		return mutations;
	}

	public double getMutationProb() {
		return mutationProb;
	}

	public void setGgaGens(int ggaGens) {
		this.ggaGens = ggaGens;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public void setCrossover(int crossover) {
		this.crossover = crossover;
	}

	public void setMutations(int mutations) {
		this.mutations = mutations;
	}

	public void setMutationProb(double mutationProb) {
		this.mutationProb = mutationProb;
	}

}
