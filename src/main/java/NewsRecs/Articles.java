package NewsRecs;

import java.util.Date;

public class Articles {
    // Attributes
    private String articleId;
    private String url;
    private String title;
    private String content;
    private String author;
    private String description;
    private String category;
    private Date publishDate;
    private String source;

    // Constructor
    public Articles(String articleId, String url, String title, String content, String author, String description,
                    String category, Date publishDate, String source) {
        this.articleId = articleId;
        this.url = url;             // Set the URL here
        this.title = title;
        this.content = content;
        this.author = author;
        this.description = description;
        this.category = category;
        this.publishDate = publishDate;
        this.source = source;
    }

    // Getter and Setter methods
    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
