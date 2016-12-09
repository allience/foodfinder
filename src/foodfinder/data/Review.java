/**
 * Author: The Alliance
 */

package foodfinder.data;

public class Review {

	private int recipeId;
	private int userId;
	private int rating;
	
	public Review() {
	}
	
	public Review(int recipeId, int userId, int rating) {
		this.recipeId = recipeId;
		this.userId = userId;
		this.rating = rating;
	}
	
	public int getRecipeId() {
		return recipeId;
	}
	public void setRecipeId(int recipeId) {
		this.recipeId = recipeId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	
}
