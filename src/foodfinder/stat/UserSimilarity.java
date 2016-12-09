/**
 * Author: The Alliance
 */

package foodfinder.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import foodfinder.data.Review;
import foodfinder.data.User;


/*
 * Calculer le coefficient de corrélation entre deux utilisateur
 */
public class UserSimilarity {

	private List<Review> reviews;
	private Map<Integer, Map<Integer, Integer>> ratedRecipes;
	
	public UserSimilarity(List<Review> reviews) {
		this.reviews = reviews;
		ratedRecipes = new HashMap<Integer, Map<Integer, Integer>>();
	}
	
	public double correlation(User user1, User user2) {
		
		if (!ratedRecipes.containsKey(user1.getId()))
			ratedRecipes.put(user1.getId(), getUserRatedRecipes(user1.getId()));
		
		if (!ratedRecipes.containsKey(user2.getId()))
			ratedRecipes.put(user2.getId(), getUserRatedRecipes(user2.getId()));
		
		Map<Integer, Integer> user1RatedRecipes = ratedRecipes.get(user1.getId());
		
		Map<Integer, Integer> user2RatedRecipes = ratedRecipes.get(user2.getId());
		
		List<Integer> commonRecipes = getCommonRecipes(user1RatedRecipes, user2RatedRecipes);
		
		if (commonRecipes.isEmpty())
			return 0;
		
		// if they have only 1 recipe in common
		// TODO: we should change this
		if (commonRecipes.size() == 1) {
			return 0.5;
		}
		
		int size = commonRecipes.size();
		
		double[] x = new double[size];
		double[] y = new double[size];
		
		for (int i = 0; i < size; i++) {
			x[i] = user1RatedRecipes.get(commonRecipes.get(i));
			y[i] = user2RatedRecipes.get(commonRecipes.get(i));
		}
		
		double correlation = new PearsonsCorrelation().correlation(x, y);
		
		return correlation;
	}
	
	private List<Integer> getCommonRecipes(Map<Integer, Integer> user1, Map<Integer, Integer> user2) {
		
		List<Integer> commonRecipes = new ArrayList<Integer>();
		
		for (Entry<Integer, Integer> entry1 : user1.entrySet()) {
			for (Entry<Integer, Integer> entry2 : user2.entrySet()) {
				if (entry1.getKey() == entry2.getKey())
					commonRecipes.add(entry1.getKey());
			}
		}
		
		return commonRecipes;
		
	}
	
	private Map<Integer, Integer> getUserRatedRecipes(int userId) {
		
		Map<Integer, Integer> ratedRecipes = new HashMap<Integer, Integer>();
		
		for (Review review : reviews) {
			if (review.getUserId() == userId) {
				ratedRecipes.put(review.getRecipeId(), review.getRating());
			}
		}
		
		return ratedRecipes;
	}
	
}
