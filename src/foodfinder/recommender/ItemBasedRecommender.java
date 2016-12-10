package foodfinder.recommender;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import foodfinder.data.DbContext;
import foodfinder.data.FoodFinderDb;
import foodfinder.data.Ingredient;
import foodfinder.data.Recipe;
import foodfinder.data.RecipesService;
import foodfinder.data.TestData;
import foodfinder.data.User;
import foodfinder.stat.RecipesSimilaritiesCalculator;

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
	public Map<Recipe, Double> recommend(User user, List<Recipe> possibleRecipes) {
		recommenderDbCtx = new DbContext(server, recommenderDb, username, password);
		//List<Recipe> possibleRecipes = recipesService.GetRcipesByIngredients(recommenderDbCtx, userIngredients, false);
		System.out.println(possibleRecipes.size());
		Map<Integer, Integer> ingredientsPerRecipe = recipesService.GetNumberOfIngredientsPerRecipe(recommenderDbCtx, possibleRecipes);
		
		TestData testData = new TestData();
		List<Recipe> userHistory = testData.GetTestUserHistory();
		
		//<recipeId, list of its ingredients by column name and value (O-1)>
		Map<Integer, Map<String, Integer>> possibleRecipesIngredientsMatrice = GetRecipesIngredientsMatrice(possibleRecipes);
		Map<Integer, Map<String, Integer>> userHistoryRecipesIngredients = GetRecipesIngredientsMatrice(userHistory);
		
		RecipesSimilaritiesCalculator recipesSimilaritiesCalculator = new RecipesSimilaritiesCalculator();
		
		DbContext ctx = new DbContext(server, "foodfinderdb_matrices", username, password);
		Map<Recipe, Double> userRecipesSimilarities = recipesSimilaritiesCalculator.calculate(ctx, userHistoryRecipesIngredients, possibleRecipesIngredientsMatrice);
		
		
		
		return userRecipesSimilarities;
	}
	
	
	private Map<Integer, Map<String, Integer>> GetRecipesIngredientsMatrice(List<Recipe> recipes){
		Map<Integer, Map<String, Integer>> recipesIngredientsMatrice = new HashMap<Integer, Map<String, Integer>>();
		String condition = "";
		for(int i = 0; i < recipes.size(); i++){
			condition += "recipe_id = " + recipes.get(i).getId();
			condition += (i < recipes.size() - 1) ? " Or " : "";
		}
		List<Map<String, Object>> result = recommenderDbCtx.selectQuery(
						FoodFinderDb.Recipe_Ingredients,
						null,
						condition
				);
		for(Map<String, Object> rslt : result){
			HashMap<String, Integer> row = new HashMap<String, Integer>();
			int recipeId = 0;
			for(Entry<String, Object> entry : rslt.entrySet()){
				String key = entry.getKey();
				int value;
				if(key.equals("recipe_id")){
					recipeId = (int)entry.getValue();
					continue;
				}
				//if(entry.getValue().getClass().getName().equals(Boolean.class.getName()))
					value = (boolean)entry.getValue() ? 1 : 0;
				//else
					//value = (int)entry.getValue();
				row.put(key, value);
			}
			recipesIngredientsMatrice.put(recipeId, row);
		}
		return recipesIngredientsMatrice;
	}
	
}
