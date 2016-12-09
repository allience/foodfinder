/**
 * Author: The Alliance
 */

package foodfinder.data;

public class Rating {

	private User user;
	private int rating;
	
	public Rating() {
	}
	
	public Rating(User user, int rating) {
		this.setUser(user);
		this.setRating(rating);
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
