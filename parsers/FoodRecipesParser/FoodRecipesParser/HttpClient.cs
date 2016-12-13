using System;
using System.IO;
using System.Net;
using System.Runtime.Serialization;

namespace FoodRecipesParser
{
    public class HttpClient
    {
        public enum HttpStatus
        {
            Ok,
            Error
        }

        public class RequestData
        {
            public HttpStatus Status { get; set; }
            public string Source { get; set; }
        }

        public static RequestData Get(string url, bool api = false)
        {
            var httpWebRequest = InitializeWebRequest(url);

            if (api)
            {
                httpWebRequest.Accept = "text/html,application/json;q=0.9,*/*;q=0.8";
                httpWebRequest.ContentType = "application/json";
            }
            else
                httpWebRequest.Accept = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

            try
            {
                httpWebRequest.Proxy = null;
                using (var httpWebResponse = (HttpWebResponse)httpWebRequest.GetResponse())
                {
                    if (httpWebResponse.StatusCode != HttpStatusCode.OK)
                        return new RequestData { Status = HttpStatus.Error };

                    var response = httpWebResponse.GetResponseStream();
                    if (response == null)
                        return new RequestData { Status = HttpStatus.Error };

                    using (var content = new StreamReader(response))
                    {
                        return new RequestData { Status = HttpStatus.Ok, Source = content.ReadToEnd() };
                    }
                }
            }
            catch (WebException)
            {
                return new RequestData {Status = HttpStatus.Error};
            }
            
        }

        public static HttpWebRequest InitializeWebRequest(string url)
        {
            var httpWebRequest = (HttpWebRequest)WebRequest.Create(url);
            httpWebRequest.Method = "GET";
            httpWebRequest.UserAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";

            httpWebRequest.Proxy = null;
            
            return httpWebRequest;
        }

        public static string LoadHtml(string link, string errorMsg, bool api = false)
        {
            var html = Get(link, api);
            if (html.Status != HttpStatus.Ok)
                throw new RequestException(errorMsg);

            return html.Source;
        }
    }

    [Serializable]
    internal class RequestException : Exception
    {
        public RequestException()
        {
        }

        public RequestException(string message) : base(message)
        {
        }

        public RequestException(string message, Exception innerException) : base(message, innerException)
        {
        }

        protected RequestException(SerializationInfo info, StreamingContext context) : base(info, context)
        {
        }
    }
}
