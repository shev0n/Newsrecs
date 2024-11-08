package NewsRecs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseHandler {
    private static final String URL = "jdbc:mysql://localhost:3306/news";
    private static final String USER = "shevon@smartengs.com";
    private static final String PASSWORD = "SmarT007Call#";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
        }
        return connection;
    }

    public void saveArticle(Articles article) {
        // Standard INSERT statement without IGNORE to allow duplicates
        String insertSQL = "INSERT INTO articles (url, title, content, author, description, category, publish_date, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, article.getUrl());
            pstmt.setString(2, article.getTitle());
            pstmt.setString(3, article.getContent());
            pstmt.setString(4, article.getAuthor());
            pstmt.setString(5, article.getDescription());
            pstmt.setString(6, article.getCategory());
            pstmt.setDate(7, new java.sql.Date(article.getPublishDate().getTime()));
            pstmt.setString(8, article.getSource());

            pstmt.executeUpdate();
            System.out.println("Article saved successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
