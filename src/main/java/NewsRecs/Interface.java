package NewsRecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Interface {
    private DatabaseHandler dbHandler;
    private RecommendationEngine recommendationEngine;
    private Scanner scanner;

    public Interface(DatabaseHandler dbHandler, RecommendationEngine recommendationEngine) {
        this.dbHandler = dbHandler;
        this.recommendationEngine = recommendationEngine;
        this.scanner = new Scanner(System.in);
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
        System.out.println("\nYour Recommendations:");
        for (Articles article : recommendations) {
            System.out.println("- " + article.getTitle() + " (" + article.getCategory() + ") [ID: " + article.getArticleId() + "]");
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
            for (Articles article : articles) {
                System.out.println("- " + article.getTitle() + " [ID: " + article.getArticleId() + "]");
            }
        }
    }

    private void rateArticle(User user) {
        System.out.print("Enter article ID to rate: ");
        String articleId = scanner.nextLine();
        System.out.print("Enter rating (1-5): ");
        int rating = scanner.nextInt();
        scanner.nextLine(); // consume newline

        Articles article = dbHandler.getArticleById(articleId);
        if (article != null) {
            user.rateArticle(article, rating, dbHandler);
            System.out.println("Thank you for rating the article!");
        } else {
            System.out.println("Article not found.");
        }
    }
}
