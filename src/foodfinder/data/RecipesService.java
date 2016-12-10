package foodfinder.data;

import static java.lang.Math.toIntExact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipesService {
	
	public List<Recipe> GetUserHistory(DbContext dbContext, int userId){
		
		List<Map<String, Object>> userResultList = dbContext.selectQuery(
				FoodFinderDbMatrices.Recipes_Users,
				null, 
				"user_id = "+userId
		);
		
		ArrayList<Recipe> userRatedRecipes = new ArrayList<Recipe>();
		for(Map<String, Object> item : userResultList){
			Recipe recipe = new Recipe();
			recipe.setId(toIntExact((Long)item.get("recipe_id")));
			userRatedRecipes.add(recipe);
		}
		return userRatedRecipes;
	}
	
	
	//returns ingredient ids, not auto increment ids
	public List<Ingredient> GetIngredients(DbContext dbContext, List<String> ingredients){
		StringBuilder condition = new StringBuilder();
		for (int i = 0; i < ingredients.size(); i++) {
			
			condition.append("`title`");
			condition.append(" = '" + ingredients.get(i) + "'");
			condition.append(i < ingredients.size() - 1 ? " OR " : "");
		}
		List<Map<String, Object>> ingredientsResultList = dbContext.selectQuery(
				FoodFinderDb.Ingredients,
				null, 
				condition.toString()
		);
		
		ArrayList<Ingredient> ingredientsList = new ArrayList<Ingredient>();
		for(Map<String, Object> item : ingredientsResultList){
			Ingredient ingredient = new Ingredient();
			ingredient.setId((Integer)item.get("ingred_id"));
			ingredientsList.add(ingredient);
		}
		return ingredientsList;
	}
	
	//call with IsExact->false to get all recipes that got at least 1 ingredient of the userIngredients
	public List<Integer> GetRcipesByIngredients(DbContext recommenderDbCtx, List<Ingredient> userIngredients, boolean isExact){
		ArrayList<Integer> recipes = new ArrayList<Integer>();
		String logicalOperator = isExact ? " AND " : " OR ";
		List<String> cols = new ArrayList<String>();
		cols.add("recipe_id");
		String condition = "";
		
		for(int i = 0; i < userIngredients.size(); i++){
			condition += "ingredient_id = " + userIngredients.get(i).getId();
			condition += (i < userIngredients.size() - 1) ? logicalOperator : "";
		}
		condition += " GROUP BY recipe_id";
		
		List<Map<String, Object>> resultList = recommenderDbCtx.selectQuery(
				FoodFinderDb.Recipes_Ingredients,
				cols,
				condition
		);
		
		for(Map<String, Object> item : resultList){
			int recipeId = toIntExact((Long)item.get("recipe_id"));
			recipes.add(recipeId);
		}
		
		return recipes;
	}
	
	
	//returns recipeId, numberOfIngredients
	public Map<Integer, Integer> GetNumberOfIngredientsPerRecipe(DbContext recommenderDbCtx, List<Integer> recipes){
		HashMap<Integer, Integer> ingredientsPerRecipe = new HashMap<Integer, Integer>();
		
		List<Map<String, Object>> result = recommenderDbCtx.CustomQuery(
				"SELECT recipe_id, count(ingredient_id) as nbIngredients"
				+ " from recipes_ingredients group by recipe_id");
		
		for(Map<String, Object> rslt : result){
			int recipeId = toIntExact((Long)rslt.get("recipe_id"));
			int numIngred = toIntExact((Long)rslt.get("nbIngredients"));
			ingredientsPerRecipe.put(recipeId, numIngred);
		}
		
		return ingredientsPerRecipe;
	}
}
