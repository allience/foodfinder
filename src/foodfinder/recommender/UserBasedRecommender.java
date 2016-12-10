/**
 * Author: The Alliance
 */

package foodfinder.recommender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import foodfinder.Configuration;
import foodfinder.MapUtil;
import foodfinder.data.Recipe;
import foodfinder.data.User;
import foodfinder.data.UsersService;
import foodfinder.data.DbContext;
import foodfinder.data.Rating;

public class UserBasedRecommender implements Recommender {

	private DbContext recommenderDbCtx;
	private String recommenderDb = "foodfinderdb_matrices";
	
	private String tblReviews = "recipes_users";
	
	private UsersService usersService;
	
	public UserBasedRecommender() {		
		
		usersService = new UsersService();
		
	}
	
	@Override
	public Map<Integer, Double> recommend(User user, List<Recipe> recipes) {
		
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
		
		Map<Integer, Double> userSimilarities = usersService.getUserSimilarities(recommenderDbCtx, user, true);
		Map<Integer, Double> userRecipeSimilarities = new HashMap<Integer, Double>();
		
		// loop through all the recipes
		for (Entry<Recipe, List<Rating>> entry : recipesRatings.entrySet()) {
			
			Recipe recipe = entry.getKey();
			List<Rating> raters = entry.getValue();
			double accumulation = 0.0;
			int commonUsers = 0;
			
			// loop all through the raters
			for (Rating rater : raters) {
				if (!userSimilarities.containsKey(rater.getUser().getId()))
					continue;
				
				// if the current user and this rater have something in common accumulate it...
				accumulation += userSimilarities.get(rater.getUser().getId()) * rater.getRating();
				commonUsers++;
			}
			
			double average = commonUsers > 0 ? accumulation / commonUsers : 0.0;
			
			userRecipeSimilarities.put(recipe.getId(), average);
			
		}
		
		
		recommenderDbCtx.dispose();
		
		//userRecipeSimilarities = MapUtil.sortByValue(userRecipeSimilarities);
		
		return userRecipeSimilarities;
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






