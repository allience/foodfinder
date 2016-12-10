/**
 * Author: The Alliance
 */

package foodfinder;

import java.util.ArrayList;
import java.util.List;

import foodfinder.data.Ingredient;
import foodfinder.data.Recipe;
import foodfinder.data.TestData;
import foodfinder.data.User;
import foodfinder.recommender.ItemBasedRecommender;

public class Main {

	public static void main(String[] args) {
		
		TestData testData = new TestData();
		
		//get our test user
		User user = testData.GetTestUser();
		
		//get the list of recipes eaten by our user (history)
		List<Recipe> userHistory = testData.GetTestUserHistory();
		
		//get the ingredients that he has. [Onion, Potato, Tomato]
		List<Ingredient> userIngredients = testData.GetTestUserIngredients();
		
		ItemBasedRecommender itRec = new ItemBasedRecommender(userIngredients);
		itRec.recommend(user, userHistory);
		
	}

}
