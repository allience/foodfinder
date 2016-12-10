package foodfinder.recommender;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import foodfinder.data.DbContext;
import foodfinder.data.FoodFinderDb;
import foodfinder.data.Ingredient;
import foodfinder.data.Recipe;
import foodfinder.data.RecipesService;
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
