package org.cloudbus.cloudsim.ext.utils;

public class ScientificFunctions {
	public static double formulaOne(int k, double param) {
		if (k <= 0) {
			return -1;
		}
		if (param < 0) {
			return -1;
		}
		
		return Math.pow((double) k, - param);
	}

}
