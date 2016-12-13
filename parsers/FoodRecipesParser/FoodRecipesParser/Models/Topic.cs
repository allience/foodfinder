using System.Collections.Generic;

namespace FoodRecipesParser.Models
{
    public class Topic
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public string Link { get; set; }
        public int TotalRecipes { get; set; }
        public List<Recipe> Recipes { get; set; }
    }
}
