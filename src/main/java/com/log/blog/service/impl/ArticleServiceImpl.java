package com.log.blog.service.impl;

import com.log.blog.dto.ArticleParam;
import com.log.blog.entity.Article;
import com.log.blog.mapper.ArticleMapper;
import com.log.blog.service.ArticleService;
import com.log.blog.utils.PageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service("articleBasicService")
public class ArticleServiceImpl implements ArticleService {
    private final ArticleMapper articleMapper;
    private final String imagesDir;

    public ArticleServiceImpl(ArticleMapper articleMapper,
                              @Value("${upload.article.images}") String imagesDir) {
        this.articleMapper = articleMapper;
        this.imagesDir = imagesDir;
    }

    @Override
    public Article getArticle(@NonNull String articleId) {
        return articleMapper.getArticle(articleId);
    }

    @Override
    public List<Article> listArticles(@NonNull ArticleParam feature) {
        PageUtils.startPage(feature.getPageRange());
        if (feature.isFuzzySearch())
            return articleMapper.findArticles(feature);
        return articleMapper.listArticles(feature);
    }

    @Override
    public boolean sendImage(@NonNull String image, @NonNull OutputStream outputStream) {
        File file = new File(imagesDir + image);
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
