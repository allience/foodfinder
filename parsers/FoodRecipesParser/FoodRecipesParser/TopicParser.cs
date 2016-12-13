using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using FoodRecipesParser.Models;
using HtmlAgilityPack;

// TODO: remove Take()

namespace FoodRecipesParser
{
    public class TopicParser
    {
        private class TopicPage
        {
            public string Page { get; set; }
            public bool Loaded { get; set; }
        }

        private readonly string _link;
        private readonly string _pageArg;

        private readonly DataExporter _dataExporter;

        public TopicParser(DataExporter dataExporter)
        {
            _dataExporter = dataExporter;
            _link = "http://www.food.com/topics/";
            _pageArg = "?pn=";
        }

        public IList<Topic> GetTopics()
        {
            var topics = new List<Topic>();

            var topicsIndex = GetTopicsIndex();

            foreach (var index in topicsIndex)
            {
                var link = $"{_link}{index}";
                Console.WriteLine(link);
                topics.AddRange(GetLetterTopics(link));
            }

            return topics;
        }

        private IList<Topic> GetLetterTopics(string link)
        {
            var html = HttpClient.LoadHtml(link, "Error while loading topic source.");

            var pages = GetTopicPages(html);
            pages.First().Loaded = true;
            var content = html;

            var topics = new List<Topic>();

            foreach (var page in pages.Take(1))
            {
                if (!page.Loaded)
                {
                    var pageLink = $"{link}{_pageArg}{page.Page}";

                    Console.WriteLine(pageLink);

                    content = HttpClient.LoadHtml(pageLink, "Error while loading topic source.");
                    page.Loaded = true;
                }
                topics.AddRange(GetPageTopics(content, 5));
            }

            return topics;
        }

        private IList<Topic> GetPageTopics(string html)
        {
            var doc = new HtmlDocument();
            doc.LoadHtml(html);

            var sectionNode = GetSectionNode(doc.DocumentNode, "topic-index-items");

            var topics =
                sectionNode?.Descendants("li")
                    .Select(c => c.Descendants("a").First())
                    .Select(a => CreateTopic(a.InnerText.Trim(), a.Attributes["href"].Value))
                    .ToList();

            return topics;
        }

        private IList<Topic> GetPageTopics(string html, int limit)
        {
            var doc = new HtmlDocument();
            doc.LoadHtml(html);

            var sectionNode = GetSectionNode(doc.DocumentNode, "topic-index-items");

            var topics =
                sectionNode?.Descendants("li").Take(limit)
                    .Select(c => c.Descendants("a").First())
                    .Select(a => CreateTopic(a.InnerText.Trim(), a.Attributes["href"].Value))
                    .ToList();

            return topics;
        }

        private Topic CreateTopic(string name, string link)
        {
            var topic = new Topic { Id = GetTopicId(link), Name = name, Link = link };
            if (topic.Id <= 0) return null;

            //_dataExporter.InsertTopic(topic);
            return topic;
        }

        private int GetTopicId(string link)
        {
            string html;
            try
            {
                html = HttpClient.LoadHtml(link, "Error while loading topic source");
            }
            catch (RequestException)
            {
                return 0;
            }

            var pattern = new Regex($@"FD\.Page\.Topic\.init\('(?<id>\d+)','{link}', ''\);");
            var match = pattern.Match(html);

            if (!match.Success)
                return 0;

            var id = match.Groups["id"];

            return int.Parse(id.Value);
        }

        private IList<TopicPage> GetTopicPages(string html)
        {
            var doc = new HtmlDocument();
            doc.LoadHtml(html);

            var sectionNode = GetSectionNode(doc.DocumentNode, "letter-index-pages");

            var pages =
                sectionNode?.Descendants("li")
                    .Where(c => !c.InnerText.Trim().Contains("Previous") && !c.InnerText.Trim().Contains("Next"))
                    .Select(p => new TopicPage { Page = p.InnerText.Trim(), Loaded = false})
                    .ToList() ?? new List<TopicPage> {new TopicPage {Page = "1", Loaded = false}};

            return pages;
        } 

        private IList<string> GetTopicsIndex()
        {
            var html = HttpClient.LoadHtml(_link, "Error while loading topics index source.");

            var doc = new HtmlDocument();
            doc.LoadHtml(html);

            var sectionNode = GetSectionNode(doc.DocumentNode, "letter-index");
            var index = sectionNode.Descendants("li").Select(c => c.InnerText.Trim().ToLower()).ToList();

            return index;
        }

        private HtmlNode GetSectionNode(HtmlNode node, string @class)
        {
            return node.SelectSingleNode($"//section[@class='{@class}']//ul");
        }
        
    }
}
