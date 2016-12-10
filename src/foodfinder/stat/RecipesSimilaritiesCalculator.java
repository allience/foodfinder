package foodfinder.stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import foodfinder.data.DbContext;
import foodfinder.data.FoodFinderDbMatrices;
import foodfinder.data.Recipe;


public class RecipesSimilaritiesCalculator {
	
	//this function returns a Map of :
	//< recipeId , distance from user (correlation with user history) >
	public Map<Recipe, Double> calculate(DbContext ctx, Map<Integer, Map<String, Integer>> userHistory, Map<Integer, Map<String, Integer>> recipesIngredients){
		Map<Recipe, Double> userRecipesSimilarities = new HashMap<Recipe, Double>();
		RecipeSimilarity recipeSimilarity = new RecipeSimilarity();
		
		//go through all possible recipes (those that have at least 1 asked ingredient
		for(Entry<Integer, Map<String, Integer>> recipe : recipesIngredients.entrySet()){
			
			//calculate the correlation with each recipe eaten by the user (his history)
			double x[] = new double[userHistory.size()];
			int index = 0;
			for(Entry<Integer, Map<String, Integer>> historyRecipe : userHistory.entrySet()){
				//x[index] = recipeSimilarity.correlation(recipe.getValue(), historyRecipe.getValue());
				
				String condition = "recipe1_id = " + recipe.getKey();
				condition += " AND recipe2_id = " + historyRecipe.getKey();
				List<String> cols = new ArrayList<String>();
				cols.add("correlation");
				
				List<Map<String, Object>> result = ctx.selectQuery(FoodFinderDbMatrices.Recipes_Similarities,
						cols,
						condition
				);
				x[index] = (double) result.get(0).get("correlation");
				System.out.println(x[index]);
				
				//fill Db
				/*List<Object> values = new ArrayList<Object>();
				values.add(recipe.getKey());
				values.add(historyRecipe.getKey());
				values.add(x[index]);
				ctx.insert(FoodFinderDbMatrices.Recipes_Similarities, values);
				*/
				index++;
			}
			
			//sum correlations and calculate the average distance between the proposed recipe and the entire user history
			double sum = 0;
			for(int i = 0; i<x.length; i++){
				sum += x[i];
			}
			double averageCorrelation = sum / x.length;
			
			userRecipesSimilarities.put(new Recipe(recipe.getKey()), averageCorrelation);
		}
		
		return userRecipesSimilarities;
	}
}
