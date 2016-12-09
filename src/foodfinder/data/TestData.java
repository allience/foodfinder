package foodfinder.data;

import static java.lang.Math.toIntExact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//this class handles the management of the test user, his history, and ingredients
public class TestData {

	//later we can put all in 1 db to prevent maknig different dbContexts
	private String recommenderDb = FoodFinderDbMatrices.Name;
	private String dataDb = FoodFinderDb.Name;
	private String server = "localhost";
	private String username = "root";
	private String password = "abc";
	private DbContext dbContext;
	
	//this is a temporary hardcoded user id for test purposes
	private int randomUserId = 75805;
	
	//a list that contains the recipes rated by our test user, and their ratings
	private List<Map<String, Object>> userResultList;
	
	//a list that contains the ingredients owend by the user (test)
	private List<Map<String, Object>> ingredientsResultList;
	
	private void Initialize(){
		dbContext = new DbContext(server, recommenderDb, username, password);
		userResultList = dbContext.selectQuery(
				FoodFinderDbMatrices.Recipes_Users,
				null, 
				"user_id = "+randomUserId
		);
		
		dbContext = new DbContext(server, dataDb, username, password);
		ingredientsResultList = dbContext.selectQuery(
				FoodFinderDb.Ingredients,
				null, 
				"title = 'Onion' OR title = 'Potato' Or title = 'Tomato'"
		);
	}
	
	public TestData() {
		Initialize();
	}
	
	
	//returns the test user
	public User GetTestUser(){
		User user = new User();
		user.setId(randomUserId);
		return user;
	}
	
	//
	public ArrayList<Recipe> GetTestUserHistory(){
		ArrayList<Recipe> userRatedRecipes = new ArrayList<Recipe>();
		for(Map<String, Object> item : userResultList){
			Recipe recipe = new Recipe();
			recipe.setId(toIntExact((Long)item.get("recipe_id")));
			userRatedRecipes.add(recipe);
		}
		return userRatedRecipes;
	}
	
	//returns ingredient ids, not auto increment ids
	public ArrayList<Ingredient> GetTestUserIngredients(){
		ArrayList<Ingredient> ingredientsList = new ArrayList<Ingredient>();
		for(Map<String, Object> item : ingredientsResultList){
			Ingredient ingredient = new Ingredient();
			ingredient.setId((Integer)item.get("ingred_id"));
			ingredientsList.add(ingredient);
		}
		return ingredientsList;
	}
	
}
