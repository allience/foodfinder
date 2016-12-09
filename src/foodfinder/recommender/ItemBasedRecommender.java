package foodfinder.recommender;

import static java.lang.Math.toIntExact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foodfinder.data.DbContext;
import foodfinder.data.FoodFinderDb;
import foodfinder.data.Ingredient;
import foodfinder.data.Recipe;
import foodfinder.data.User;

public class ItemBasedRecommender implements Recommender{

	private DbContext recommenderDbCtx;
	private String recommenderDb;
	private String server;
	private String username;
	private String password;
	private List<Ingredient> userIngredients;
	private RecipesService recipesService;
	
	public ItemBasedRecommender(List<Ingredient> ingredients) {
		userIngredients = new ArrayList<Ingredient>();
		
		userIngredients = ingredients;
		
		recommenderDb = "foodfinderdb";
		server = "localhost";
		
		username = "root";
		password = "abc";
		
		recipesService = new RecipesService();
	}
	
	@Override
	public Map<Recipe, Double> recommend(User user, List<Recipe> userRecipes) {
		recommenderDbCtx = new DbContext(server, recommenderDb, username, password);
		List<Integer> possibleRecipes = recipesService.GetRcipesByIngredients(recommenderDbCtx, userIngredients, false);
		//System.out.println(possibleRecipes.size());
		Map<Integer, Integer> ingredientsPerRecipe = recipesService.GetNumberOfIngredientsPerRecipe(recommenderDbCtx, possibleRecipes);
		
		
		return null;
	}
	
	//get all the recipes that contain the ingredients, by the exact number of ingredients, 
	//or at least one of them.
	// Map < recipe_id, numberOfIngredients>
	private List<Integer> GetRcipesByIngredients(boolean isExact){
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
	
	private Map<Integer, Integer> GetNumberOfIngredientsPerRecipe(List<Integer> recipes){
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
	
	private List<Map<String, Integer>> GetRecipesIngredientsMatrice(List<Integer> recipes){
		List<Map<String, Integer>> recipesIngredientsMatrice = new ArrayList<Map<String, Integer>>();
		String condition = "";
		for(int i = 0; i < recipes.size(); i++){
			condition += "recipe_id = " + recipes.get(i);
			condition += (i < recipes.size() - 1) ? " Or " : "";
		}
		List<Map<String, Object>> result = recommenderDbCtx.selectQuery(
						FoodFinderDb.Recipes_Ingredients,
						null,
						null
				);
		for(Map<String, Object> rslt : result){
			
		}
		return recipesIngredientsMatrice;
	} 
}
