package com.leo.test.luxoft.server;

import org.mockserver.integration.ClientAndServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;

/**
 * @author Senchenko Victor
 */
@Component
@Profile({"test", "presentation"})
public class MockMovieServer implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockMovieServer.class);

    @Autowired
    private MockServerUtil util;

    @Value("${spring.my.server.movie.port}")
    private int moviePort;

    private ClientAndServer movieServer;

    @PostConstruct
    public void init() {
        movieServer = ClientAndServer.startClientAndServer(moviePort);
        for (int i = 0; i < DataGenerator.movies.size(); i++)
            movieServer.when(util.httpRequest("/movie/" + (i + 1))).respond(util.httpResponse(DataGenerator.movies.get(i)));
        for (int i = 0; i < DataGenerator.movies.size(); i++)
            movieServer.when(util.httpRequest("/movie/" + (i + 101))).respond(util.httpResponse(DataGenerator.movies.get(i), 500));
        LOGGER.info("Servers for movie started at {}. Filled with data from 1 to {}", moviePort, DataGenerator.movies.size());
    }

    @Override
    public void close() {
        movieServer.stop();
    }
}