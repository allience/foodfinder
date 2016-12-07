/**
 * Author: The Alliance
 */

package foodfinder.recommender;

import java.util.List;
import java.util.Map;

import foodfinder.data.Recipe;
import foodfinder.data.User;
import foodfinder.data.DbContext;

public class UserBasedRecommender implements Recommender {

	private DbContext recommenderDbCtx;
	private String recommenderDb;
	private String server;
	private String username;
	private String password;
	
	public UserBasedRecommender() {
		
		recommenderDb = "foodfinderdb_matrices";
		server = "localhost";
		
		// TODO: KJM: this will work only for me, we should change it
		username = "kjmx";
		password = "pass";
		
	}
	
	@Override
	public Map<Recipe, Double> recommend(User user, List<Recipe> recipes) {
		
		recommenderDbCtx = new DbContext(server, recommenderDb, username, password);
		
		/*
		 * The idea of user based recommendation is to calculate the distance between each
		 * recipe and all the users who ate it
		 */
		
		recommenderDbCtx.dispose();
		
		return null;
	}
	
	
	
	
	
	
	
	

}
