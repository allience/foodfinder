/**
 * Author: The Alliance
 */

package foodfinder;

import java.util.ArrayList;
import java.util.List;

import foodfinder.data.DbContext;
import foodfinder.data.FoodFinderDb;
import foodfinder.data.Ingredient;
import foodfinder.data.Recipe;
import foodfinder.data.RecipesService;
import foodfinder.data.TestData;
import foodfinder.data.User;
import foodfinder.recommender.UserBasedRecommender;

public class UserBasedMain {

	public static void main(String[] args) {
		
		TestData testData = new TestData();
		
		//get our test user
		User user = testData.GetTestUser();
		
		//get the list of recipes eaten by our user (history)
		List<Recipe> userHistory = testData.GetTestUserHistory();
		
		//get the ingredients that he has. [Onion, Potato, Tomato]
		List<Ingredient> userIngredients = testData.GetTestUserIngredients();
		
		DbContext dbContext = new DbContext(Configuration.server, FoodFinderDb.Name, Configuration.username, Configuration.password);
		
		RecipesService recipesSrv = new RecipesService();
		
		//List<Integer> recipeIds = recipesSrv.GetRecipesByIngredients(dbContext, userIngredients, false);
		List<Recipe> recipes = recipesSrv.GetRecipesByIngredients(dbContext, userIngredients, false);
		/*for (int recipeId : recipeIds) {
			recipes.add(new Recipe(recipeId));
		}*/
		
		dbContext.dispose();
		
		UserBasedRecommender userBasedRecommender = new UserBasedRecommender();
		userBasedRecommender.recommend(user, recipes);
		

	}

}
