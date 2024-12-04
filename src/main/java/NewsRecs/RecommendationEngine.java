package NewsRecs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecommendationEngine {
    private DatabaseHandler dbHandler;
    private ArticleFetcher articleFetcher;

    public RecommendationEngine(DatabaseHandler dbHandler, ArticleFetcher articleFetcher) {
        this.dbHandler = dbHandler;
        this.articleFetcher = articleFetcher;
    }

    public List<Articles> getRecommendations(User user) {
        // Fetch all articles in the user's primary category
        List<Articles> allArticles = dbHandler.getArticlesByCategory(user.getPrimaryCategory());

        // Fetch user ratings, ensuring consistent use of int for article IDs
        Map<Integer, Integer> userRatings = dbHandler.getUserRatings(user.getUserID());
        Map<Articles, Double> scoredArticles = new HashMap<>();

        // Calculate scores for each article based on similarity and user ratings
        for (Articles article : allArticles) {
            double score = calculateSimilarityScore(article, user.getPreferences());

            // If the article is rated by the user, add the rating to the score
            if (userRatings.containsKey(article.getArticleId())) {
                int rating = userRatings.get(article.getArticleId());
                score += rating;
            }

            scoredArticles.put(article, score);
        }

        // Sort articles by their scores in descending order and return the sorted list
        return scoredArticles.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Calculate similarity score based on the article category and user preferences
    private double calculateSimilarityScore(Articles article, List<String> preferences) {
        double score = 0.0;
        for (String preference : preferences) {
            if (article.getCategory().equalsIgnoreCase(preference)) {
                score += 1.0;
            }
        }
        return score;
    }
}
