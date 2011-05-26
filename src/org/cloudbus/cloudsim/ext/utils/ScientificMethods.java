package org.cloudbus.cloudsim.ext.utils;

import org.cloudbus.cloudsim.ext.gga.Constants;

public class ScientificMethods {
	static double[] kProbs; // 其实就是rank概率的值，注意，这里的0，对应rank值应该为1
	static {
		// 这个部分计算所有的Rank Probs，因为这个部分是可以复用的，所以用静态构造函数实现
		kProbs = new double[Constants.MAXOBJECTS];
		for (int i = 0; i < Constants.MAXOBJECTS; i++) {
			kProbs[i] = getKProb(i + 1, 2);
		}
	}
	public static double getKProb(int k, double param) {
		if (k <= 0) {
			return -1;
		}
		if (param < 0) {
			return -1;
		}
		
		return Math.pow((double) k, - param);
	}
	
	public static int getRankByProb(double prob, int groupSize) {
		double[] probs = new double[groupSize];

		for (int i = 0; i < groupSize; i++) {
			probs[i] = 0;
		}
		
		// 计算累计概率
		probs[0] = kProbs[0];
		for (int i = 1; i < groupSize; i++) {
			probs[i] = probs[i-1] + kProbs[i];
		}
		
		// 归一化
		double totalP = probs[groupSize - 1];
		for (int i = 0; i < groupSize; i++) {
			probs[i] = probs[i] / totalP;
		}
		
		int selected = 0;
		
		if (prob <= probs[0]) selected = 0;
		else {
			for (int j = 1; j < groupSize; j++) {
				if (prob > probs[j-1] && prob <= probs[j])
					selected = j;
			}
		}
		
		return selected;
	}
}
