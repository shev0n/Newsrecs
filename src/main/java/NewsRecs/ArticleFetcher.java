package NewsRecs;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ArticleFetcher {
    private String apiKey = "74d841d70e1f4747a330a56b696589ae"; // Replace with your actual NewsAPI key
    private String source = "https://newsapi.org/v2/top-headlines";

    public List<Articles> fetchArticles(String category) {
        List<Articles> articlesList = new ArrayList<>();
        try {
            String urlString = source + "?category=" + category + "&apiKey=" + apiKey;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray articlesArray = jsonResponse.getJSONArray("articles");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

                for (int i = 0; i < articlesArray.length(); i++) {
                    JSONObject articleJSON = articlesArray.getJSONObject(i);

                    String articleId = articleJSON.getString("url"); // Unique ID
                    String urlValue = articleJSON.getString("url");
                    String title = articleJSON.getString("title");
                    String content = articleJSON.optString("content", "No content available");
                    String author = articleJSON.optString("author", "Unknown Author");
                    String description = articleJSON.optString("description", "No description available");
                    String sourceName = articleJSON.getJSONObject("source").getString("name");
                    Date publishDate = dateFormat.parse(articleJSON.getString("publishedAt"));

                    Articles article = new Articles(articleId, urlValue, title, content, author, description,
                            category, publishDate, sourceName);
                    articlesList.add(article);
                }
            } else {
                System.out.println("GET request failed: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articlesList;
    }
}
