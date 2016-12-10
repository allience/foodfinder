package foodfinder.data;

import static java.lang.Math.toIntExact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
			ingredient.setId((Integer)item.get("id"));
			ingredientsList.add(ingredient);
		}
		return ingredientsList;
	}
	
	//call with IsExact->false to get all recipes that got at least 1 ingredient of the userIngredients
	public List<Recipe> GetRecipesByIngredients(DbContext recommenderDbCtx, List<Ingredient> userIngredients, boolean isExact){
		ArrayList<Recipe> recipes = new ArrayList<Recipe>();
		String logicalOperator = isExact ? " AND " : " OR ";
		List<String> cols = new ArrayList<String>();
		cols.add("recipe_id");
		String condition = "";
		
		for(int i = 0; i < userIngredients.size(); i++){
			condition += "ingredient_id = " + userIngredients.get(i).getId();
			condition += (i < userIngredients.size() - 1) ? logicalOperator : "";
		}
		
		List<Map<String, Object>> resultList = recommenderDbCtx.selectQuery(
				FoodFinderDb.Recipes_Ingredients,
				cols,
				condition
		);
		
		for(Map<String, Object> item : resultList){
			int recipeId = toIntExact((Long)item.get("recipe_id"));
			recipes.add(new Recipe(recipeId));
		}
		
		return recipes;
	}
	
	
	//returns recipeId, numberOfIngredients
	public Map<Integer, Integer> GetNumberOfIngredientsPerRecipe(DbContext recommenderDbCtx, List<Recipe> recipes){
		HashMap<Integer, Integer> ingredientsPerRecipe = new HashMap<Integer, Integer>();
		String condition = "(";
		for(int i = 0; i<recipes.size(); i++){
			condition += ""+recipes.get(i).getId();
			condition += (i < recipes.size() - 1) ? ", " : ")";
		}
		List<Map<String, Object>> result = recommenderDbCtx.CustomQuery(
				"SELECT recipe_id, count(ingredient_id) as nbIngredients"
				+ " FROM recipes_ingredients WHERE recipe_id in "+ condition +" GROUP BY recipe_id");
		
		for(Map<String, Object> rslt : result){
			int recipeId = toIntExact((Long)rslt.get("recipe_id"));
			int numIngred = toIntExact((Long)rslt.get("nbIngredients"));
			ingredientsPerRecipe.put(recipeId, numIngred);
		}
		
		return ingredientsPerRecipe;
	}
	
	
	public List<RecipeDetails> GetRecipesById(DbContext recommenderDbCtx, Map<Recipe, Double> recipes){
		List<RecipeDetails> recipesDetails = new ArrayList<RecipeDetails>();
		
		List<String> cols = new ArrayList<String>();
		cols.add("id");
		cols.add("title");
		
		for(Entry<Recipe, Double> recipe : recipes.entrySet()){
			String condition = "`id` = " + recipe.getKey().getId();
			
			List<Map<String, Object>> resultList = recommenderDbCtx.selectQuery(
					FoodFinderDb.Recipes,
					cols,
					condition
			);
			
			RecipeDetails recipeDet = new RecipeDetails();
			for(Map<String, Object> rslt : resultList){
				recipeDet.id = toIntExact((Long)rslt.get("id"));
				recipeDet.title = rslt.get("title").toString();
			}
			recipesDetails.add(recipeDet);
		}
		
		return recipesDetails;
	}
}
