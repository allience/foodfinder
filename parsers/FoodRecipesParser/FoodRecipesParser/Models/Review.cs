using Newtonsoft.Json;

namespace FoodRecipesParser.Models
{
    public class Review
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("memberId")]
        public int MemberId { get; set; }

        [JsonProperty("memberName")]
        public string MemberName { get; set; }

        [JsonProperty("rating")]
        public int Rating { get; set; }

        [JsonProperty("text")]
        public string Text { get; set; }
    }
}
