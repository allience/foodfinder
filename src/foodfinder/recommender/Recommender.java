/**
 * Author: The Alliance
 */

package foodfinder.recommender;

import java.util.Map;
import java.util.List;

import foodfinder.data.Recipe;
import foodfinder.data.User;

/*
 * Basic interface for both UserBased and ItemBased recommender methods 
 */
public interface Recommender {

	/*
	 * @Input: A user and a list of recipes with the wanted ingredients
	 * @Output: This method returns a list of recipes and their correlation values
	 */
	Map<Integer, Double> recommend(User user, List<Recipe> recipes);
	
}
