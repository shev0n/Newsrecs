package NewsRecs;

public class Main {
    public static void main(String[] args) {
        ArticleFetcher articleFetcher = new ArticleFetcher();
        DatabaseHandler dbHandler = new DatabaseHandler();

        // Initialize RecommendationEngine
        RecommendationEngine recommendationEngine = new RecommendationEngine(dbHandler, articleFetcher);

        // Initialize Interface with ArticleFetcher
        Interface cliInterface = new Interface(dbHandler, recommendationEngine, articleFetcher);

        // Start the CLI Interface
        cliInterface.start();
    }
}
