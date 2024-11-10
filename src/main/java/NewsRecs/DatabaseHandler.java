package NewsRecs;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHandler {
    private static final String URL = "jdbc:mysql://localhost:3306/news";
    private static final String USER = "shevon@smartengs.com";
    private static final String PASSWORD = "SmarT007Call#";
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                System.out.println("Failed to connect to the database.");
                e.printStackTrace();
            }
        }
        return connection;
    }

    public void saveUser(User user) {
        String insertUserSQL = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                user.setUserID(String.valueOf(keys.getInt(1)));
            }

            System.out.println("User saved successfully with userID: " + user.getUserID());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveUserPreferences(String userId, List<String> preferences) {
        String insertPrefSQL = "INSERT INTO user_preferences (user_id, category) VALUES (?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(insertPrefSQL)) {
            for (String preference : preferences) {
                pstmt.setString(1, userId);
                pstmt.setString(2, preference);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("User preferences saved successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveArticle(Articles article) {
        String insertSQL = "REPLACE INTO articles (url, title, content, author, description, category, publish_date, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(insertSQL)) {
            pstmt.setString(1, article.getUrl());
            pstmt.setString(2, article.getTitle());
            pstmt.setString(3, article.getContent());
            pstmt.setString(4, article.getAuthor());
            pstmt.setString(5, article.getDescription());
            pstmt.setString(6, article.getCategory());
            pstmt.setDate(7, new java.sql.Date(article.getPublishDate().getTime()));
            pstmt.setString(8, article.getSource());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveRating(String userId, String articleId, int rating) {
        String insertRatingSQL = "REPLACE INTO article_ratings (user_id, article_id, rating) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(insertRatingSQL)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, articleId);
            pstmt.setInt(3, rating);
            pstmt.executeUpdate();
            System.out.println("Rating saved successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> getUserRatings(String userId) {
        Map<String, Integer> userRatings = new HashMap<>();
        String selectSQL = "SELECT article_id, rating FROM article_ratings WHERE user_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(selectSQL)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                userRatings.put(rs.getString("article_id"), rs.getInt("rating"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userRatings;
    }

    public User getUser(String username, String password) {
        // Add BINARY to enforce case sensitivity on the username comparison
        String selectSQL = "SELECT * FROM users WHERE BINARY username = ? AND password = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(selectSQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String userId = rs.getString("user_id");
                User user = new User(userId, username, password);
                List<String> preferences = getUserPreferences(userId);
                user.updatePreferences(preferences);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private List<String> getUserPreferences(String userId) {
        List<String> preferences = new ArrayList<>();
        String selectSQL = "SELECT category FROM user_preferences WHERE user_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(selectSQL)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                preferences.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preferences;
    }

    public List<Articles> getArticlesByCategory(String category) {
        List<Articles> articlesList = new ArrayList<>();
        String selectSQL = "SELECT * FROM articles WHERE category = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(selectSQL)) {
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Articles article = createArticleFromResultSet(rs);
                articlesList.add(article);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articlesList;
    }

    public Articles getArticleById(String articleId) {
        String selectSQL = "SELECT * FROM articles WHERE article_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(selectSQL)) {
            pstmt.setString(1, articleId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createArticleFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Articles createArticleFromResultSet(ResultSet rs) throws SQLException {
        String articleId = rs.getString("article_id");
        String url = rs.getString("url");
        String title = rs.getString("title");
        String content = rs.getString("content");
        String author = rs.getString("author");
        String description = rs.getString("description");
        String category = rs.getString("category");
        Date publishDate = rs.getDate("publish_date");
        String source = rs.getString("source");
        return new Articles(articleId, url, title, content, author, description, category, publishDate, source);
    }
}
