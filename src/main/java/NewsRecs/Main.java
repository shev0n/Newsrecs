package NewsRecs;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        ArticleFetcher articleFetcher = new ArticleFetcher();
        DatabaseHandler dbHandler = new DatabaseHandler();

        // Fetch articles from the API
        List<Articles> articlesList = articleFetcher.fetchArticles("Sports");

        // Save each article to the database
        for (Articles article : articlesList) {
            dbHandler.saveArticle(article);
        }

        System.out.println("All articles have been fetched and saved to the database.");

        // Start the CLI Interface
        RecommendationEngine recommendationEngine = new RecommendationEngine(dbHandler, articleFetcher);
        Interface cliInterface = new Interface(dbHandler, recommendationEngine);

        // Call the start method to launch the CLI
        cliInterface.start();
    }
}
