package foodfinder.stat;

import java.util.Map;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class RecipeSimilarity {
	
	public double correlation(Map<String, Integer> recipe1Ingredients, Map<String, Integer> recipe2Ingredients){
		int size = recipe1Ingredients.size();
		
		double[] x = new double[size];
		double[] y = new double[size];
		
		for (int i = 1; i < size; i++) {
			x[i] = recipe1Ingredients.get(Integer.toString(i));
			y[i] = recipe2Ingredients.get(Integer.toString(i));
		}
		double correlation = new PearsonsCorrelation().correlation(x, y);
		
		return correlation;
	}
}
