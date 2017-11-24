package com.leo.test.luxoft.service;

import com.leo.test.luxoft.model.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author Senchenko Victor
 */
@Service
public class MovieService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovieService.class);

    @Value("${spring.my.server.movie}")
    private String server;

    @Autowired
    private RestService restService;

    @Cacheable(value = "movie", key = "#movieId", unless = "#result == null")
    public Movie movie(int movieId) {
        LOGGER.trace("Movie with id={} from server", movieId);
        return restService.getForObject(server, Movie.class, movieId);
    }
}
