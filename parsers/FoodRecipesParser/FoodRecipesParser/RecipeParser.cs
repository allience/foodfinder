using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using FoodRecipesParser.Models;
using HtmlAgilityPack;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace FoodRecipesParser
{
    public class RecipeParser
    {
        private class SearchParameters
        {
            public int NumRecords { get; set; }
            public int Pages { get; set; }
            public int TotalNumRecords { get; set; }
        }

        private class ApiPage
        {
            public int Page { get; set; }
            public bool Loaded { get; set; }
        }

        private readonly int _maxPerTopic;
        private int _counter;
        private ProgressBar _progress;

        private readonly DataExporter _dataExporter;

        public RecipeParser(int maxPerTopic, DataExporter dataExporter)
        {
            _maxPerTopic = maxPerTopic;
            _dataExporter = dataExporter;
            _counter = 0;
        }

        public IList<Recipe> GetTopicRecipes(Topic topic)
        {
            var link = GetLink(topic.Id, 1);
            var json = HttpClient.LoadHtml(link, "Error while loading recipes.", true);

            var recipesResult = JObject.Parse(json);

            var ttrs = recipesResult["response"]["totalResultsCount"]?.Value<string>();
            if (string.IsNullOrEmpty(ttrs))
                ttrs = "0";

            var nr = recipesResult["response"]["parameters"]["numRecords"]?.Value<string>();
            if (string.IsNullOrEmpty(nr))
                nr = "1";

            topic.TotalRecipes = int.Parse(ttrs);
            var numRecords = int.Parse(nr);

            var parameters = new SearchParameters
            {
                NumRecords = numRecords,
                Pages = (int) Math.Ceiling((double) topic.TotalRecipes/numRecords),
                TotalNumRecords = topic.TotalRecipes
            };

            topic.Recipes = new List<Recipe>();
            var recipes = new List<Recipe>();

            _counter = 0;

            using (_progress = new ProgressBar())
            {
                for (var i = 1; i <= parameters.Pages && _counter < _maxPerTopic; i++)
                {
                    var pageRecipes = GetPageRecipes(topic, new ApiPage {Page = i, Loaded = i == 1}, json);
                    topic.Recipes.AddRange(pageRecipes);
                    recipes.AddRange(pageRecipes);
                }
            }

            return recipes;
        }

        public string GetLink(int topicId, int page)
        {
            return $"http://www.food.com/services/mobile/fdc/search/topic?pn={page}&searchTerm=&topicid={topicId}&sortBy=";
        }

        public string GetReviewLink(int recipeId, int page)
        {
            return $"https://api.food.com/external/v1/recipes/{recipeId}/feed/reviews?pn={page}";
        }

        private IList<Recipe> GetPageRecipes(Topic topic, ApiPage page, string json)
        {
            if (!page.Loaded)
            {
                var link = GetLink(topic.Id, page.Page);
                json = HttpClient.LoadHtml(link, "Error while loading recipes.", true);
            }

            var recipesResult = JObject.Parse(json);
            var results = recipesResult["response"]["results"].Children().ToList();

            var recipes = results.Where(r => r["record_type"].Value<string>() == "Recipe").Select(r => GetRecipe(topic, r)).ToList();

            return recipes;
        }

        private Recipe GetRecipe(Topic topic, JToken json)
        {
            var recipe = JsonConvert.DeserializeObject<Recipe>(json.ToString());

            var html = HttpClient.LoadHtml(recipe.Url, "Error while loading Recipe HTML");

            var doc = new HtmlDocument();
            doc.LoadHtml(html);

            GetRecipeIngredients(doc, recipe);
            GetRecipeDirections(doc, recipe);
            GetRecipeReviews(recipe);

            recipe.Topic = topic;

            _dataExporter.InsertRecipe(recipe);

            _counter++;
            _progress.Report((double)_counter / _maxPerTopic);

            return recipe;
        }

        private void GetRecipeIngredients(HtmlDocument doc, Recipe recipe)
        {
            recipe.Ingredients = new List<Ingredient>();

            var ingredientsNode = doc.DocumentNode.SelectSingleNode("//div[@data-module='ingredients']");

            var servingsNode = ingredientsNode?.SelectSingleNode($"//a[@data-target='#{recipe.Id}_servings']//span");
            if (servingsNode != null)
            {
                var pattern = new Regex(@"(?<servings>\d+)(-\d+)*");
                var match = pattern.Match(servingsNode.InnerText);
                var servings = int.Parse(match.Groups["servings"].Value.Length > 0 ? match.Groups["servings"].Value : "0");
                recipe.Servings = servings;
            }

            var listNode = ingredientsNode?.ChildNodes.FirstOrDefault(c => c.Name == "ul");

            if (listNode != null)
            {
                foreach (var node in listNode.ChildNodes.Where(c => c.Name == "li"))
                {
                    var pattern = new Regex(@"(?<quantity>(?<sup>\d+)(.*(?<sub>\d+))*) (?<ingredient>.*)");
                    var match = pattern.Match(node.InnerText);

                    if (!match.Success) continue;

                    string url = null;
                    var anchor = node.Descendants("a");
                    var liNodes = anchor as IList<HtmlNode> ?? anchor.ToList();
                    if (liNodes.Any())
                        url = liNodes.First().Attributes["href"].Value;

                    //var dataName = node.Attributes["data-ingredient"].Value;

                    var ingredient = new Ingredient
                    {
                        //DataName = dataName,
                        FullName = match.Groups["ingredient"].Value,
                        Quantity = match.Groups["quantity"].Value,
                        QuantitySup = int.Parse(match.Groups["sup"].Value),
                        QuantitySub = match.Groups["sub"].Value.Length > 0 ? int.Parse(match.Groups["sub"].Value) : 0,
                        Url = url
                    };

                    recipe.Ingredients.Add(ingredient);
                }
            }
        }

        private void GetRecipeDirections(HtmlDocument doc, Recipe recipe)
        {
            recipe.Directions = new List<string>();

            var directionsNode = doc.DocumentNode.SelectSingleNode("//div[@data-module='recipeDirections']");

            var olNodes = directionsNode?.Descendants("ol");
            var olList = olNodes as IList<HtmlNode> ?? olNodes?.ToList();
            if (olList == null || !olList.Any())
                return;

            var liList = olList.First().Descendants("li").ToList();
            foreach (var node in liList.Take(liList.Count - 1))
            {
                recipe.Directions.Add(node.InnerText);
            }
        }

        private void GetRecipeReviews(Recipe recipe)
        {
            var link = GetReviewLink(recipe.Id, 1);
            var json = HttpClient.LoadHtml(link, "Error while loading reviews.", true);

            var reviewsResult = JObject.Parse(json);

            var total = int.Parse(reviewsResult["total"].Value<string>());
            var numRecords = int.Parse(reviewsResult["params"]["size"].Value<string>());

            var parameters = new SearchParameters
            {
                NumRecords = numRecords,
                Pages = (int)Math.Ceiling((double)total / numRecords),
                TotalNumRecords = total
            };

            recipe.Reviews = new List<Review>();

            for (var i = 1; i <= parameters.Pages; i++)
            {
                var pageReviews = GetRecipePageReviews(recipe, new ApiPage { Page = i, Loaded = i == 1 }, json);
                recipe.Reviews.AddRange(pageReviews);
            }
        }

        private IList<Review> GetRecipePageReviews(Recipe recipe, ApiPage page, string json)
        {
            if (!page.Loaded)
            {
                var link = GetReviewLink(recipe.Id, page.Page);
                json = HttpClient.LoadHtml(link, "Error while loading reviews.", true);
            }

            var reviewsResult = JObject.Parse(json);
            var results = reviewsResult["data"]["items"].Children().ToList();

            var reviews =
                results.Where(r => r["type"].Value<string>() == "review")
                    .Select(r => JsonConvert.DeserializeObject<Review>(r.ToString()))
                    .ToList();

            return reviews;
        } 
    }
}
