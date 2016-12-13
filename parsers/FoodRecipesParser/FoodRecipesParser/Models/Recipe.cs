using System.Collections.Generic;
using Newtonsoft.Json;

namespace FoodRecipesParser.Models
{
    public class Recipe
    {
        [JsonProperty("recipe_id")]
        public int Id { get; set; }

        [JsonProperty("main_title")]
        public string Title { get; set; }

        [JsonProperty("main_description")]
        public string Description { get; set; }

        [JsonProperty("recipe_preptime")]
        public int PreTime { get; set; }

        [JsonProperty("recipe_totaltime")]
        public int TotalTime { get; set; }

        [JsonProperty("recipe_cooktime")]
        public int CookTime { get; set; }

        [JsonProperty("record_url")]
        public string Url { get; set; }

        [JsonProperty("main_rating")]
        public double Rating { get; set; }

        [JsonProperty("main_num_ratings")]
        public int NumRatings { get; set; }

        [JsonProperty("num_steps")]
        public int NumSteps { get; set; }

        [JsonProperty("main_username")]
        public string Username { get; set; }

        [JsonProperty("main_userid")]
        public int UserId { get; set; }

        [JsonProperty("recipe_photo_url")]
        public string RecipePhotoUrl { get; set; }

        [JsonProperty("num_recipe_photos")]
        public int NumRecipePhotos { get; set; }

        public int Servings { get; set; }

        public Topic Topic { get; set; }

        public IList<Ingredient> Ingredients { get; set; }

        public IList<string> Directions { get; set; }

        public List<Review> Reviews { get; set; }
    }
}
