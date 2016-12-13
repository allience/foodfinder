using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using FoodRecipesParser.Data;
using FoodRecipesParser.Models;

namespace FoodRecipesParser
{
    class Program
    {
        static void Main(string[] args)
        {
            var exporter = new DataExporter();

            var topicParser = new TopicParser(exporter);
            var topics = topicParser.GetTopics();

            var recipeParser = new RecipeParser(128, exporter);
            var recipes = new List<Recipe>();

            foreach (var topic in topics.Skip(46))
            {
                Console.Write($"Parsing topic {topic.Id} recipes... ");
                recipes.AddRange(recipeParser.GetTopicRecipes(topic));
                Console.WriteLine("");
            }
        }
    }
}
