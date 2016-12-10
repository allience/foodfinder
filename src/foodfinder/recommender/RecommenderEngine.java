/**
 * Author: The Alliance
 */

package foodfinder.recommender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import foodfinder.MapUtil;
import foodfinder.data.Ingredient;
import foodfinder.data.Recipe;
import foodfinder.data.RecipeDetails;
import foodfinder.data.TestData;
import foodfinder.data.User;

public class RecommenderEngine {
	
	private double itemBasedCoefficient;
	private double userBasedCoefficient;
	
	public RecommenderEngine() {
		
		itemBasedCoefficient = 2.0;
		userBasedCoefficient = 3.0;
		
	}
	
	public void recommend() {
		
		TestData testData = new TestData();
		//get our test user
		User user = testData.GetTestUser();
		
		//get the list of recipes eaten by our user (history)
		//List<Recipe> userHistory = testData.GetTestUserHistory();
		
		//get the ingredients that he has. [Onion, Potato, Tomato]
		List<Ingredient> userIngredients = testData.GetTestUserIngredients();
		
		//get the list of recipes that contains the ingredients asked by the user
		List<Recipe> possibleRecipes = testData.GetRcipesByIngredients();
		
		ItemBasedRecommender itRec = new ItemBasedRecommender(userIngredients);
		Map<Integer, Double> itemBasedRecipes = itRec.recommend(user, possibleRecipes);
		
		UserBasedRecommender userBasedRecommender = new UserBasedRecommender();
		Map<Integer, Double> userBasedRecipes = userBasedRecommender.recommend(user, possibleRecipes);
		
		Map<Recipe, Double> recommendedRecipes = merge(itemBasedRecipes, userBasedRecipes);
		
		recommendedRecipes = MapUtil.sortByValue(recommendedRecipes);
		
		List<RecipeDetails> recommendedRecipesDetails = testData.GetRecipesDetailsFromRecipes(recommendedRecipes);
		
		int count = 1;
		for(RecipeDetails rcp : recommendedRecipesDetails){
			if (count > 20) break;
			System.out.println("RANK = " + count++ + " - id = "+rcp.id + " - title = " + rcp.title);
		}
	}
	
	Map<Recipe, Double> merge(Map<Integer, Double> itemBased, Map<Integer, Double> userBased) {
		
		Map<Recipe, Double> recommendedRecipes = new HashMap<Recipe, Double>();
		
		for (Entry<Integer, Double> recipe : itemBased.entrySet()) {
			
			double itemBasedScore = recipe.getValue();
			double userBasedScore = userBased.get(recipe.getKey());
			
			double newScore = (itemBasedScore * itemBasedCoefficient) + (userBasedScore * userBasedCoefficient);
			
			recommendedRecipes.put(new Recipe(recipe.getKey()), newScore);
		}
		
		return recommendedRecipes;
		
	}

}
