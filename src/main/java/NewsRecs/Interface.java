package NewsRecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Interface {
    private DatabaseHandler dbHandler;
    private RecommendationEngine recommendationEngine;
    private Scanner scanner;
    private List<Articles> viewedArticles;  // Track fully viewed articles

    public Interface(DatabaseHandler dbHandler, RecommendationEngine recommendationEngine) {
        this.dbHandler = dbHandler;
        this.recommendationEngine = recommendationEngine;
        this.scanner = new Scanner(System.in);
        this.viewedArticles = new ArrayList<>();  // Initialize viewed articles list
    }

    public void start() {
        System.out.println("Welcome to NewsRecs!");
        while (true) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Create Account");
            System.out.println("2. Login to Existing Account");
            System.out.println("3. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

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
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User newUser = User.createAccount(username, password);
        dbHandler.saveUser(newUser);

        selectPreferences(newUser);
        dashboard(newUser);
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

    private void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = dbHandler.getUser(username, password);
        if (user != null) {
            System.out.println("Login successful!");
            dashboard(user);
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private void dashboard(User user) {
        viewedArticles.clear(); // Clear viewed articles on each new login session
        while (true) {
            System.out.println("\nDashboard - Select an option:");
            System.out.println("1. View Recommendations");
            System.out.println("2. Select Category to Read Articles");
            System.out.println("3. Rate an Article");
            System.out.println("4. Logout");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 -> viewRecommendations(user);
                case 2 -> selectCategoryToRead(user);
                case 3 -> rateArticle(user);
                case 4 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewRecommendations(User user) {
        List<Articles> recommendations = recommendationEngine.getRecommendations(user);

        System.out.println("\nYour Recommendations (showing top 10):");
        for (int i = 0; i < Math.min(recommendations.size(), 10); i++) {
            Articles article = recommendations.get(i);
            System.out.println((i + 1) + ". " + article.getTitle() + " (" + article.getCategory() + ") [ID: " + article.getArticleId() + "]");
        }

        System.out.print("Enter the number of the article you wish to read, or type 'Exit' to return to the dashboard: ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("Exit")) {
            System.out.println("Returning to the dashboard...");
            return;
        }

        try {
            int choice = Integer.parseInt(input);
            if (choice > 0 && choice <= Math.min(recommendations.size(), 10)) {
                Articles selectedArticle = recommendations.get(choice - 1);
                displayFullArticle(selectedArticle);
                viewedArticles.add(selectedArticle);  // Add to viewed list
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number between 1 and 10, or 'Exit' to return to the dashboard.");
        }
    }

    private void selectCategoryToRead(User user) {
        System.out.println("Select a category:");
        System.out.println("1. Sports\n2. Technology\n3. Politics\n4. Health\n5. Entertainment\n6. Business");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline
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
            List<Articles> articles = dbHandler.getArticlesByCategory(category);
            System.out.println("Articles in " + category + ":");
            for (int i = 0; i < Math.min(articles.size(), 10); i++) {
                Articles article = articles.get(i);
                System.out.println((i + 1) + ". " + article.getTitle() + " [ID: " + article.getArticleId() + "]");
            }

            System.out.print("Enter the number of the article you wish to read, or type 'Exit' to return to the dashboard: ");
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
                    viewedArticles.add(selectedArticle);  // Add to viewed list
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number or 'Exit' to return to the dashboard.");
            }
        }
    }

    private void rateArticle(User user) {
        if (viewedArticles.isEmpty()) {
            System.out.println("No articles have been viewed yet. Please view an article first.");
            return;
        }

        System.out.println("\nArticles you have viewed:");
        for (int i = 0; i < viewedArticles.size(); i++) {
            Articles article = viewedArticles.get(i);
            System.out.println((i + 1) + ". " + article.getTitle() + " [ID: " + article.getArticleId() + "]");
        }

        System.out.print("Enter the article ID of the article you want to rate: ");
        String articleId = scanner.nextLine().trim();  // Read and trim input for comparison

        // Find the article in viewedArticles by articleId
        Articles articleToRate = null;
        for (Articles article : viewedArticles) {
            if (article.getArticleId().equals(articleId)) {  // Compare IDs directly
                articleToRate = article;
                break;
            }
        }

        if (articleToRate == null) {
            System.out.println("Article not found in viewed articles list.");
            return;
        }

        System.out.print("Enter rating (1-5): ");
        int rating = scanner.nextInt();
        scanner.nextLine(); // consume newline

        user.rateArticle(articleToRate, rating, dbHandler);
        System.out.println("Thank you for rating the article!");
    }


    private void displayFullArticle(Articles article) {
        System.out.println("\n--- Full Article ---");
        System.out.println("Title: " + article.getTitle());
        System.out.println("Category: " + article.getCategory());
        System.out.println("Author: " + article.getAuthor());
        System.out.println("Published Date: " + article.getPublishDate());
        System.out.println("Source: " + article.getSource());
        System.out.println("Content: " + article.getContent());
        System.out.println("URL: " + article.getUrl());
        System.out.println("-------------------\n");
    }
}
