package com.log.blog.service.impl;

import com.log.blog.dto.Range;
import com.log.blog.entity.Article;
import com.log.blog.mapper.ArticleMapper;
import com.log.blog.service.ArticlePublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Service
public class ArticlePublicServiceImpl implements ArticlePublicService {
    private ArticleMapper articleMapper;
    private String uploadRootPath;
    private String imagesDir;

    @Autowired
    public void init(ArticleMapper articleMapper, Environment environment) {
        this.articleMapper = articleMapper;
        this.uploadRootPath = environment.getProperty("upload.rootPath");
        this.imagesDir = environment.getProperty("upload.article.images");
    }

    @Override
    public Article getArticle(String articleId) {
        try {
            return articleMapper.getArticle(articleId);
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public List<Article> getArticles(@NonNull Range range) {
        try {
            return articleMapper.getAllArticle(range);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Article> search(@NonNull String keyword, @NonNull Range range) {
        Article feature = new Article();
        feature.setTitle(keyword);
        feature.setContent(keyword);
        return search(feature, range);
    }

    @Override
    public List<Article> search(@NonNull Article feature, @NonNull Range range) {
        try {
            return articleMapper.findArticles(feature, range);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean sendImage(String image, OutputStream outputStream) {
        File file = new File(uploadRootPath + imagesDir + image);
        if (!file.exists())
            return false;
        try {
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.transferTo(outputStream);
            inputStream.close();
            outputStream.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}