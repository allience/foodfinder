/**
 * Author: The Alliance
 */

package foodfinder.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersService {

	public UsersService() {}
	
	public Map<Integer, Double> getUserSimilarities(DbContext recommenderContext, User user, boolean positive) {
		
		String condition = "`user1_id`=" + user.getId();
		
		if (positive)
			condition += " AND `correlation` > 0";
		
		List<Map<String, Object>> rawSimilarities = 
				recommenderContext.selectQuery(FoodFinderDbMatrices.Users_Similarities,
												null, condition);
		
		Map<Integer, Double> similarities = new HashMap<Integer, Double>();
		
		for (Map<String, Object> similarity : rawSimilarities) {
			
			int user2Id = Integer.parseInt(similarity.get("user2_id").toString());
			double correlation = Double.parseDouble(similarity.get("correlation").toString());
			
			similarities.put(user2Id, correlation);
			
		}
		
		return similarities;
	}
	
}
