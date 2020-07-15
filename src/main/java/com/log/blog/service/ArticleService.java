package com.log.blog.service;

import com.log.blog.dto.Range;
import com.log.blog.entity.Article;
import org.springframework.lang.NonNull;

import java.io.OutputStream;
import java.util.List;

public interface ArticleService {
    List<Article> getArticles(@NonNull Range range);
    long getArticlesCount();
    Article getArticle(@NonNull String articleId);
    List<Article> search(@NonNull String keyword, @NonNull Range range);
    List<Article> search(@NonNull Article feature, @NonNull Range range);
    long searchCount(@NonNull String keyword);
    long searchCount(@NonNull Article feature);
    boolean sendImage(@NonNull String image, @NonNull OutputStream outputStream);
}
