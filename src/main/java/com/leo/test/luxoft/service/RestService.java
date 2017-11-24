package com.leo.test.luxoft.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author Senchenko Victor
 */
@Service
public class RestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestService.class);

    @Autowired
    private RestTemplate restTemplate;

    public <T> T getForObject(String url, Class<T> tClass, Object... uriVariables) {
        URI uri = restTemplate.getUriTemplateHandler().expand(url, uriVariables);
        try {
            return restTemplate.getForObject(uri, tClass);
        } catch (ResourceAccessException e) {
            LOGGER.info("Resource {} is not available", uri);
            throw e;
        } catch (RestClientException e) {
            LOGGER.info("Resource {} return {}", uri, e.getLocalizedMessage());
            return null;
        }
    }
}
