package com.leo.test.luxoft.service;

import com.leo.test.luxoft.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

/**
 * @author Senchenko Victor
 */
@Service
public class CommentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

    @Value("${spring.my.server.comment}")
    private String server;

    @Autowired
    private RestService restService;

    @Autowired
    private CacheManager cacheManager;

    @CachePut(value = "comment", key = "#movieId", unless = "#result == null")
    public Comment[] comments(int movieId) {
        LOGGER.trace("Comments to movie with id={} from server", movieId);
        try {
            Comment[] comments = restService.getForObject(server, Comment[].class, movieId);
            if (comments != null)
                return comments;
        } catch (ResourceAccessException ignored) {
        }
        LOGGER.trace("Comments to movie with id={} from cache", movieId);
        Cache.ValueWrapper valueWrapper = cacheManager.getCache("comment").get(movieId);
        return valueWrapper != null ? (Comment[]) valueWrapper.get() : null;
    }
}
