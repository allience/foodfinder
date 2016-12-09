/**
 * Author: The Alliance
 */

package foodfinder.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import foodfinder.Configuration;
import foodfinder.data.DbContext;
import foodfinder.data.Recipe;
import foodfinder.data.Review;
import foodfinder.data.User;

public class UsersSimilaritiesCalculator {
	
	private DbContext recommenderDbCtx;
	private String recommenderDb = "foodfinderdb_matrices";
	
	private String tblReviews = "recipes_users";
	private String tblUsersSimilarities = "users_similarities";
	
	public UsersSimilaritiesCalculator() {
		
	}
	
	public void calculate() {
		
		recommenderDbCtx = new DbContext(Configuration.server, recommenderDb, Configuration.username, Configuration.password);
		
		List<Map<String, Object>> rawReviews = recommenderDbCtx.selectQuery(tblReviews, null, "`rating` > 0");
		
		List<Review> reviews = toReviewsList(rawReviews);
		
		Map<Integer, Integer> users = toUsersList(reviews);
		
		UserSimilarity userSimilarity = new UserSimilarity(reviews);
		
		Map<Integer, List<Integer>> done = new HashMap<Integer, List<Integer>>();
		
		int count = 0;
		
		//for (Entry<Integer, Integer> user1Entry : users.entrySet()) {
			
			
			User user1 = new User(140132);
			
			
			// for each user we are going to loop through all the users whom rated something
			for (Entry<Integer, Integer> user2Entry : users.entrySet()) {
				
				User user2 = new User(user2Entry.getKey());
				
				if (user1.getId() == user2.getId())
					continue;
				
				
				
				if ((done.containsKey(user1.getId()) && done.get(user1.getId()).contains(user2.getId()))
					|| (done.containsKey(user2.getId()) && done.get(user2.getId()).contains(user1.getId()))) {
					continue;
				}
				
				double correlation = userSimilarity.correlation(user1, user2);
				
				if (!done.containsKey(user1.getId()))
					done.put(user1.getId(), new ArrayList<Integer>());
				
				done.get(user1.getId()).add(user2.getId());
				
				List<Object> values = new ArrayList<Object>();
				values.add(user1.getId());
				values.add(user2.getId());
				values.add(correlation);
				
				recommenderDbCtx.insert(tblUsersSimilarities, values);
			}
			
			count++;
		//}
		
		recommenderDbCtx.dispose();
		
	}
	
	private List<Review> toReviewsList(List<Map<String, Object>> rawReviews) {
		
		List<Review> reviews = new ArrayList<Review>();
		
		for (Map<String, Object> review : rawReviews) {
			
			int userId = Integer.parseInt(review.get("user_id").toString());
			int recipeId = Integer.parseInt(review.get("recipe_id").toString());
			int rating = Integer.parseInt(review.get("rating").toString());
			
			reviews.add(new Review(recipeId, userId, rating));
		}
		
		return reviews;
	}
	
	private Map<Integer, Integer> toUsersList(List<Review> reviews) {
		
		Map<Integer, Integer> users = new HashMap<Integer, Integer>();
		
		for (Review review : reviews) {
			if (!users.containsKey(review.getUserId()))
				users.put(review.getUserId(), 0);	
			Integer value = users.get(review.getUserId()) + 1;
			users.put(review.getUserId(), value);
		}
		
		return users;
		
	}
	
}
