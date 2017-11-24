package com.leo.test.luxoft.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author Senchenko Victor
 */
@Component
@Profile({"test", "presentation"})
public class MockServerUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockServerUtil.class);

    @Autowired
    private ObjectMapper mapper;

    public HttpRequest httpRequest(String path) {
        return HttpRequest.request().withMethod("GET").withPath(path);
    }

    public HttpResponse httpResponse(Object o, int delay) {
        try {
            return HttpResponse.response().withStatusCode(200).withHeaders(new Header("Content-Type", "application/json; charset=utf-8")).withBody(mapper.writeValueAsString(o)).withDelay(TimeUnit.MILLISECONDS, delay);
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not write object to String. {}", e.getLocalizedMessage());
            return HttpResponse.response().withStatusCode(500);
        }
    }

    public HttpResponse httpResponse(Object o) {
        return httpResponse(o, 100);
    }

}