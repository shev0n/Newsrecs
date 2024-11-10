package NewsRecs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationEngine {
    private DatabaseHandler dbHandler;
    private ArticleFetcher articleFetcher;

    public RecommendationEngine(DatabaseHandler dbHandler, ArticleFetcher articleFetcher) {
        this.dbHandler = dbHandler;
        this.articleFetcher = articleFetcher;
    }

    public List<Articles> getRecommendations(User user) {
        List<Articles> allArticles = dbHandler.getArticlesByCategory(user.getPrimaryCategory());
        Map<String, Integer> userRatings = dbHandler.getUserRatings(user.getUserID());
        Map<Articles, Double> scoredArticles = new HashMap<>();

        for (Articles article : allArticles) {
            double score = calculateSimilarityScore(article, user.getPreferences());

            if (userRatings.containsKey(article.getArticleId())) {
                int rating = userRatings.get(article.getArticleId());
                score += rating;
            }
            scoredArticles.put(article, score);
        }

        return scoredArticles.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }

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
