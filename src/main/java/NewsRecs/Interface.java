package NewsRecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Interface {
    private DatabaseHandler dbHandler;
    private RecommendationEngine recommendationEngine;
    private ArticleFetcher articleFetcher;
    private Scanner scanner;
    private List<Articles> viewedArticles; // List to track fully viewed articles

    public Interface(DatabaseHandler dbHandler, RecommendationEngine recommendationEngine, ArticleFetcher articleFetcher) {
        this.dbHandler = dbHandler;
        this.recommendationEngine = recommendationEngine;
        this.articleFetcher = articleFetcher;
        this.scanner = new Scanner(System.in);
        this.viewedArticles = new ArrayList<>(); // Initialize viewed articles list
    }

    public void start() {
        System.out.println("Welcome to NewsRecs!");
        while (true) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Create Account");
            System.out.println("2. Login to Existing Account");
            System.out.println("3. Exit");

            int choice = readIntInput();

            switch (choice) {
                case 1 -> createAccount();
                case 2 -> login();
                case 3 -> {
                    System.out.println("Thank you for using NewsRecs. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void createAccount() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (dbHandler.isUsernameExists(username)) {
            System.out.println("Username already exists. Please choose a different username.");
            return;
        }

        User newUser = User.createAccount(username, password);
        boolean isSaved = dbHandler.saveUser(newUser);

        if (isSaved) {
            System.out.println("Account created successfully!");

            List<Articles> articlesList = articleFetcher.fetchMultipleCategories(50);
            for (Articles article : articlesList) {
                dbHandler.saveArticle(article);
            }

            selectPreferences(newUser);
            dashboard(newUser);
        } else {
            System.out.println("Account creation failed. Please try again.");
        }
    }

    private void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        User user = dbHandler.getUser(username, password);
        if (user != null) {
            System.out.println("Login successful!");

            List<Articles> articlesList = articleFetcher.fetchMultipleCategories(50);
            for (Articles article : articlesList) {
                dbHandler.saveArticle(article);
            }

            dashboard(user);
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private void selectPreferences(User user) {
        System.out.println("\nSelect your preferences (choose categories): ");
        System.out.println("1. Sports");
        System.out.println("2. Technology");
        System.out.println("3. Politics");
        System.out.println("4. Health");
        System.out.println("5. Entertainment");
        System.out.println("6. Business");

        List<String> preferences = new ArrayList<>();
        System.out.print("Enter your choices separated by commas (e.g., 1,2,3): ");
        String[] choices = scanner.nextLine().split(",");

        for (String choice : choices) {
            switch (choice.trim()) {
                case "1" -> preferences.add("Sports");
                case "2" -> preferences.add("Technology");
                case "3" -> preferences.add("Politics");
                case "4" -> preferences.add("Health");
                case "5" -> preferences.add("Entertainment");
                case "6" -> preferences.add("Business");
                default -> System.out.println("Invalid choice: " + choice);
            }
        }

        user.updatePreferences(preferences);
        dbHandler.saveUserPreferences(user.getUserID(), preferences);
        System.out.println("Preferences updated successfully!");
    }

    private void dashboard(User user) {
        viewedArticles.clear(); // Clear viewed articles on login
        while (true) {
            System.out.println("\nDashboard - Select an option:");
            System.out.println("1. View Recommendations");
            System.out.println("2. Select Category to Read Articles");
            System.out.println("3. Rate an Article");
            System.out.println("4. Logout");

            int choice = readIntInput();

            switch (choice) {
                case 1 -> viewRecommendations(user);
                case 2 -> selectCategoryToRead(user);
                case 3 -> rateArticle(user);
                case 4 -> {
                    System.out.println("Logging out...");
                    viewedArticles.clear(); // Clear viewed articles on logout
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void selectCategoryToRead(User user) {
        System.out.println("Select a category:");
        System.out.println("1. Sports\n2. Technology\n3. Politics\n4. Health\n5. Entertainment\n6. Business");

        int choice = readIntInput();
        String category = switch (choice) {
            case 1 -> "Sports";
            case 2 -> "Technology";
            case 3 -> "Politics";
            case 4 -> "Health";
            case 5 -> "Entertainment";
            case 6 -> "Business";
            default -> {
                System.out.println("Invalid choice.");
                yield null;
            }
        };

        if (category != null) {
            // Fetch articles for the selected category
            List<Articles> articles = dbHandler.getArticlesByCategory(category);

            // Display a maximum of 10 articles
            if (articles.isEmpty()) {
                System.out.println("No articles found in the selected category.");
                return;
            }

            System.out.println("Articles in category: " + category);
            for (int i = 0; i < Math.min(articles.size(), 10); i++) {
                Articles article = articles.get(i);
                System.out.println((i + 1) + ". " + article.getTitle() + " - " + article.getSource() + " [ID: " + article.getArticleId() + "]");
            }

            System.out.print("Enter the number of the article you wish to view, or type 'Exit' to return: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Exit")) {
                System.out.println("Returning to the dashboard...");
                return;
            }

            try {
                int articleChoice = Integer.parseInt(input);
                if (articleChoice > 0 && articleChoice <= Math.min(articles.size(), 10)) {
                    Articles selectedArticle = articles.get(articleChoice - 1);
                    displayFullArticle(selectedArticle);
                    viewedArticles.add(selectedArticle); // Add the article to the viewed list
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number between 1 and 10.");
            }
        }
    }


    private void viewRecommendations(User user) {
        List<Articles> recommendations = recommendationEngine.getRecommendations(user);

        if (recommendations.isEmpty()) {
            System.out.println("No recommendations available at this time.");
            return;
        }

        System.out.println("\nYour Recommendations (showing top 10):");
        for (int i = 0; i < Math.min(recommendations.size(), 10); i++) {
            Articles article = recommendations.get(i);
            System.out.println((i + 1) + ". " + article.getTitle() + " (" + article.getCategory() + ") [ID: " + article.getArticleId() + "]");
        }

        System.out.print("Enter the number of the article you wish to read, or type 'Exit' to return to the dashboard: ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("Exit")) {
            return;
        }

        try {
            int choice = Integer.parseInt(input);
            if (choice > 0 && choice <= Math.min(recommendations.size(), 10)) {
                Articles selectedArticle = recommendations.get(choice - 1);
                displayFullArticle(selectedArticle);
                viewedArticles.add(selectedArticle);
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please try again.");
        }
    }

    private void rateArticle(User user) {
        if (viewedArticles.isEmpty()) {
            System.out.println("No articles available to rate. Please view articles first.");
            return;
        }

        System.out.println("\nArticles you have viewed:");
        for (int i = 0; i < viewedArticles.size(); i++) {
            Articles article = viewedArticles.get(i);
            System.out.println((i + 1) + ". " + article.getTitle() + " [ID: " + article.getArticleId() + "]");
        }

        System.out.print("Enter the number of the article you want to rate: ");
        int articleIndex = readIntInput();

        if (articleIndex < 1 || articleIndex > viewedArticles.size()) {
            System.out.println("Invalid choice. Please try again.");
            return;
        }

        Articles articleToRate = viewedArticles.get(articleIndex - 1);
        System.out.print("Enter rating (1-5): ");
        int rating = readIntInput();

        if (rating < 1 || rating > 5) {
            System.out.println("Invalid rating. Please try again.");
            return;
        }

        dbHandler.saveRating(user.getUserID(), articleToRate.getArticleId(), rating);
        System.out.println("Rating saved successfully!");
    }

    private void displayFullArticle(Articles article) {
        System.out.println("\nTitle: " + article.getTitle());
        System.out.println("Category: " + article.getCategory());
        System.out.println("Description: " + article.getDescription());
        System.out.println("URL: " + article.getUrl());
    }

    private int readIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            return -1; // Returning -1 for invalid input
        }
    }
}
