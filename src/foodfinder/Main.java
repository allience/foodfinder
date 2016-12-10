/**
 * Author: The Alliance
 */

package foodfinder;

import java.util.List;
import java.util.Map;

import foodfinder.data.Ingredient;
import foodfinder.data.Recipe;
import foodfinder.data.RecipeDetails;
import foodfinder.data.TestData;
import foodfinder.data.User;
import foodfinder.recommender.ItemBasedRecommender;

public class Main {

	public static void main(String[] args) {
		
		TestData testData = new TestData();
		//get our test user
		User user = testData.GetTestUser();
		
		//get the list of recipes eaten by our user (history)
		//List<Recipe> userHistory = testData.GetTestUserHistory();
		
		//get the ingredients that he has. [Onion, Potato, Tomato]
		List<Ingredient> userIngredients = testData.GetTestUserIngredients();
		
		//get the list of recipes that contains the ingredients asked by the user
		List<Recipe> possibleRecipes = testData.GetRcipesByIngredients();
		
		for(Recipe rcp : possibleRecipes){
			System.out.println("id = "+rcp.getId());
		}
		
		ItemBasedRecommender itRec = new ItemBasedRecommender(userIngredients);
		Map<Recipe, Double> recommendedRecipes = itRec.recommend(user, possibleRecipes);
		
		List<RecipeDetails> recommendedRecipesDetails = testData.GetRecipesDetailsFromRecipes(recommendedRecipes);
		for(RecipeDetails rcp : recommendedRecipesDetails){
			System.out.println("id = "+rcp.id + " - title = " + rcp.title);
		}
	}

}
