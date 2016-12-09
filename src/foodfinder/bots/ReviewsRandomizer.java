/**
 * Author: The Alliance
 */

package foodfinder.bots;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import foodfinder.Configuration;
import foodfinder.data.DbContext;
import foodfinder.data.Recipe;
import foodfinder.data.User;

public class ReviewsRandomizer {

	private DbContext dbContext;
	private String foodFinderDb = "foodfinderdb";
	private String recommenderDb = "foodfinderdb_matrices";
	private String tblReviews = "recipes_users";
	private String tblUsers = "users";
	private SecureRandom random;
	
	public ReviewsRandomizer() {
		
		random = new SecureRandom(SecureRandom.getSeed(20));
		
	}
	
	public void run() {
		
		List<String> cols = new ArrayList<String>();

		dbContext = new DbContext(Configuration.server, foodFinderDb, Configuration.username, Configuration.password);
		
		Map<Integer, User> users = getUsers(dbContext.selectQuery(tblUsers, cols, null));
		
		dbContext.dispose();
		
		cols.clear();
		
		dbContext = new DbContext(Configuration.server, recommenderDb, Configuration.username, Configuration.password);
		
		cols.add("recipe_id");
		
		List<Map<String, Object>> reviews = dbContext.selectQuery(tblReviews, cols, null);
		
		Map<Integer, Recipe> recipes = getRecipes(reviews);
		
		for (Entry<Integer, Recipe> recipe : recipes.entrySet()) {
			
			int recipeId = recipe.getKey();
			
			int size = getRandomNumber(2000, 30000);
			int start = getRandomNumber(0, users.size() - size - 1);
			
			for (int i = start; i < (start + size + 1); i++) {
				int userId = users.get(getRandomUserId(users.size(), -1)).getId(); // For performance purposes I ignored the diff
				int rating = getRandomNumber(1, 5);
				
				List<Object> values = new ArrayList<Object>();
				values.add(recipeId);
				values.add(userId);
				values.add(rating);
				
				dbContext.insert(tblReviews, values);
			}
	
		}
		
		dbContext.dispose();
		
	}
	
	private Map<Integer, Recipe> getRecipes(List<Map<String, Object>> reviews) {
		
		Map<Integer, Recipe> recipes = new HashMap<Integer, Recipe>();
		
		for (Map<String, Object> review : reviews) {
			int recipeId = Integer.parseInt(review.get("recipe_id").toString());
			if (!recipes.containsKey(recipeId))
				recipes.put(recipeId, new Recipe(recipeId));
		}
		
		return recipes;
	}
	
	private Map<Integer, User> getUsers(List<Map<String, Object>> rawUsers) {
		
		Map<Integer, User> users = new HashMap<Integer, User>();
		int index = 0;
		
		for (Map<String, Object> user : rawUsers) {
			int userId = Integer.parseInt(user.get("id").toString());
			users.put(index++, new User(userId));
		}
		
		return users;
	}
	
	private int getRandomUserId(int usersLen, int diff) {
		
		int id = -1;
		
		do {
			id = random.nextInt(usersLen);
		} while (id == diff);
		
		return id;
	}
	
	private int getRandomNumber(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}
	
}
;