/**
 * Author: The Alliance
 */

package foodfinder.recommender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foodfinder.Configuration;
import foodfinder.data.Recipe;
import foodfinder.data.User;
import foodfinder.data.DbContext;
import foodfinder.data.Rating;

public class UserBasedRecommender implements Recommender {

	private DbContext recommenderDbCtx;
	private String recommenderDb = "foodfinderdb_matrices";
	
	private String tblReviews = "recipes_users";
	
	public UserBasedRecommender() {		
	}
	
	@Override
	public Map<Recipe, Double> recommend(User user, List<Recipe> recipes) {
		
		recommenderDbCtx = new DbContext(Configuration.server, recommenderDb, Configuration.username, Configuration.password);
		
		/*
		 * The idea of user based recommendation is to calculate the distance between each
		 * recipe and all the users who ate it by calculating the similarity between the global user
		 * and all the users.
		 * 
		 * 1- Get all the users who rated the list of recipes
		 * 2- Calculate similarity between user and all users
		 */
		
		/*
		 * 1- Get all the users who rated the list of recipes
		 */
		Map<Recipe, List<Rating>> recipesRatings = new HashMap<Recipe, List<Rating>>();
		
		for (Recipe recipe : recipes) {
			
			recipesRatings.put(recipe, getRecipeRatings(recommenderDbCtx, recipe));
			
		}
		
		/*
		 * 2- Calculate similarity between user and all users
		 */
		
		
		
		recommenderDbCtx.dispose();
		
		return null;
	}
	
	public List<Rating> getRecipeRatings(DbContext dbContext, Recipe recipe)
				throws NullPointerException {
		
		if (dbContext == null)
			throw new NullPointerException("dbContext");
		
		List<Map<String, Object>> reviews = dbContext.selectQuery(tblReviews, null, "`recipe_id` = " + recipe.getId() + " AND `rating` > 0");
		
		List<Rating> ratings = new ArrayList<Rating>();
		
		for (Map<String, Object> review : reviews) {
			
			User user = new User();
			user.setId(Integer.parseInt(review.get("user_id").toString()));
			
			int rating = Integer.parseInt(review.get("rating").toString());
			
			ratings.add(new Rating(user, rating));
			
		}
		
		return ratings;
	}
	
}






