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
public class MockCommentServer implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockCommentServer.class);

    @Autowired
    private MockServerUtil util;

    @Value("${spring.my.server.comment.port}")
    private int commentPort;

    private ClientAndServer commentServer;

    @PostConstruct
    public void init() {
        commentServer = ClientAndServer.startClientAndServer(commentPort);
        for (int i = 0; i < DataGenerator.comments.size(); i++)
            commentServer.when(util.httpRequest("/comment/" + (i + 1))).respond(util.httpResponse(DataGenerator.comments.get(i)));
        LOGGER.info("Server for comments started at {}. Filled with data from 1 to {}", commentPort, DataGenerator.comments.size());
    }

    @Override
    public void close() {
        commentServer.stop();
    }
}