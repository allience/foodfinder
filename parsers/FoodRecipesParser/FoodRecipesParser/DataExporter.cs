using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using FoodRecipesParser.Data;
using FoodRecipesParser.Models;

namespace FoodRecipesParser
{
    public class DataExporter
    {
        private readonly DbConnect _dbMgr;
        private readonly string _tblCategories;
        private readonly string _tblUsers;
        private readonly string _tblRecipes;
        private readonly string _tblDirections;
        private readonly string _tblIngredients;
        private readonly string _tblReviews;
        private readonly string _tblRecipiesCategories;
        private readonly string _tblRecipesIngredients;

        public DataExporter()
        {
            _dbMgr = new DbConnect("localhost", "foodfinderdb", "root", "");

            _tblCategories = "categories";
            _tblUsers = "users";
            _tblRecipes = "recipes";
            _tblDirections = "directions";
            _tblIngredients = "ingredients";
            _tblReviews = "reviews";
            _tblRecipiesCategories = "recipes_categories";
            _tblRecipesIngredients = "recipes_ingredients";
        }

        public void Export(IList<Topic> topics, IList<Recipe> recipes)
        {
            InsertTopics(topics);
            InsertRecipes(recipes);

            
        }

        public void InsertTopics(IList<Topic> topics)
        {
            foreach (var topic in topics)
            {
                InsertTopic(topic);
            }
        }

        public void InsertTopic(Topic topic)
        {
            _dbMgr.Insert(_tblCategories, new List<object>
            {
                topic.Id,
                topic.Name,
                topic.TotalRecipes
            });
        }

        public void InsertRecipes(IList<Recipe> recipes)
        {
            foreach (var recipe in recipes)
            {
                InsertRecipe(recipe);
            }
        }

        public void InsertRecipe(Recipe recipe)
        {
            var record = _dbMgr.GetById(_tblRecipes, recipe.Id);
            if (record != null)
            {
                InsertRecipeTopic(recipe);
                return;
            }

            // User
            var user = _dbMgr.GetById(_tblUsers, recipe.UserId);
            if (user == null)
            {
                _dbMgr.Insert(_tblUsers, new List<object>
                {
                    recipe.UserId,
                    recipe.Username
                });
            }

            // Recipe
            _dbMgr.Insert(_tblRecipes, new List<object>
            {
                recipe.Id,
                recipe.Title,
                recipe.Description,
                recipe.PreTime,
                recipe.TotalTime,
                recipe.CookTime,
                recipe.Url,
                recipe.Rating.ToString(CultureInfo.InvariantCulture),
                recipe.NumRatings,
                recipe.NumSteps,
                recipe.UserId,
                recipe.RecipePhotoUrl
            });

            // Directions
            InsertDirections(recipe);

            // Ingredients
            InsertIngredients(recipe);

            // Reviews
            InsertReviews(recipe);

            // Topic
            InsertRecipeTopic(recipe);
        }

        private void InsertRecipeTopic(Recipe recipe)
        {
            _dbMgr.Insert(_tblRecipiesCategories, new List<object> {recipe.Id, recipe.Topic.Id});
        }

        private void InsertReviews(Recipe recipe)
        {
            foreach (var review in recipe.Reviews)
            {
                var user = _dbMgr.GetById(_tblUsers, review.MemberId);
                if (user == null)
                {
                    _dbMgr.Insert(_tblUsers, new List<object> {review.MemberId, review.MemberName});
                }

                _dbMgr.Insert(_tblReviews, new List<object>
                {
                    review.Rating, review.Text, recipe.Id, review.MemberId
                });
            }
        }

        private void InsertIngredients(Recipe recipe)
        {
            foreach (var ingredient in recipe.Ingredients)
            {
                var record = _dbMgr.WhereAll(_tblIngredients, new List<Tuple<string, string, object>>
                {
                    new Tuple<string, string, object>("record_url", "=", ingredient.Url)
                }).FirstOrDefault();

                if (record == null) continue;

                _dbMgr.Insert(_tblRecipesIngredients, new List<object>
                {
                    recipe.Id,
                    record["id"],
                    ingredient.QuantitySup,
                    ingredient.QuantitySub
                });
            }
        }

        private void InsertDirections(Recipe recipe)
        {
            foreach (var direction in recipe.Directions)
            {
                _dbMgr.Insert(_tblDirections, new List<object> {direction, recipe.Id});
            }
        }
    }
}
