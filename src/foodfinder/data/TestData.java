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
	private int userId = 140132;
	
	//a list that contains the recipes rated by our test user, and their ratings
	private List<Map<String, Object>> userResultList;
	
	//a list that contains the ingredients owend by the user (test)
	private List<Map<String, Object>> ingredientsResultList;
	private RecipesService recipeService;
	private void Initialize(){
		
		recipeService = new RecipesService();
		
		
		
	}
	
	public TestData() {
		Initialize();
	}
	
	
	//returns the test user
	public User GetTestUser(){
		User user = new User();
		user.setId(userId);
		return user;
	}
	
	//
	public List<Recipe> GetTestUserHistory(){
		dbContext = new DbContext(server, recommenderDb, username, password);
		return recipeService.GetUserHistory(dbContext, userId);
	}
	
	//returns ingredient ids, not auto increment ids
	public List<Ingredient> GetTestUserIngredients(){
		dbContext = new DbContext(server, dataDb, username, password);
		ArrayList<String> ingredients = new ArrayList<String>();
		ingredients.add("Onion");
		ingredients.add("Potato");
		ingredients.add("Tomato");
		return recipeService.GetIngredients(dbContext, ingredients);
	}
	
}
