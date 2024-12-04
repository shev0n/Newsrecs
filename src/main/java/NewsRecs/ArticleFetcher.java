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
    private String apiKey = "74d841d70e1f4747a330a56b696589ae"; // NewsAPI key
    private String source = "https://newsapi.org/v2/top-headlines";
    private static final String[] CATEGORIES = {"Sports", "Technology", "Politics", "Health", "Entertainment", "Business"};
    private static final String COUNTRY = "us"; // Specify the country code

    public List<Articles> fetchArticles(String category, int limit) {
        List<Articles> articlesList = new ArrayList<>();
        try {
            String urlString = source + "?country=" + COUNTRY + "&category=" + category + "&pageSize=" + limit + "&apiKey=" + apiKey;
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

                    int articleId = i + 1; // Generate a numeric ID (or replace with actual logic to generate unique IDs)
                    String urlValue = articleJSON.getString("url");
                    String title = articleJSON.getString("title");
                    String content = articleJSON.optString("content", "No content available");
                    String author = articleJSON.optString("author", "Unknown Author");
                    String description = articleJSON.optString("description", "No description available");
                    String sourceName = articleJSON.getJSONObject("source").getString("name");
                    Date publishDate = dateFormat.parse(articleJSON.getString("publishedAt"));

                    Articles article = new Articles(
                            articleId, // Ensure this matches the int type expected by the constructor
                            urlValue,
                            title,
                            content,
                            author,
                            description,
                            category,
                            publishDate,
                            sourceName
                    );
                    articlesList.add(article);
                }

            } else {
                System.out.println("GET request failed for category " + category + " with response code: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Exception while fetching articles for category " + category + ": " + e.getMessage());
            e.printStackTrace();
        }
        return articlesList;
    }

    public List<Articles> fetchMultipleCategories(int totalArticles) {
        List<Articles> mixedArticles = new ArrayList<>();
        int articlesPerCategory = Math.max(totalArticles / CATEGORIES.length, 1); // Ensure at least 1 article per category

        // Fetch articles for each category
        for (String category : CATEGORIES) {
            List<Articles> categoryArticles = fetchArticles(category, articlesPerCategory);
            mixedArticles.addAll(categoryArticles);
        }

        // If we didn't fetch enough articles, fetch more by repeating over categories in round-robin
        int currentIndex = 0;
        while (mixedArticles.size() < totalArticles) {
            String category = CATEGORIES[currentIndex % CATEGORIES.length];
            List<Articles> additionalArticles = fetchArticles(category, 1); // Fetch one more article per category as needed
            mixedArticles.addAll(additionalArticles);
            currentIndex++;
        }

        return mixedArticles;
    }
}
