package com.leo.test.luxoft;

import com.leo.test.luxoft.server.DataGenerator;
import com.leo.test.luxoft.server.MockCommentServer;
import com.leo.test.luxoft.server.MockMovieServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Matches;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.Base64;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Senchenko Victor
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ApplicationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private MockCommentServer commentServer;

    @Autowired
    private MockMovieServer movieServer;

    private static MockHttpServletRequestBuilder postRequest(String url, String user, String body) {
        return post(url).header("Authorization", authorization(user)).contentType(MediaType.APPLICATION_JSON_UTF8).content(body);
    }

    private static MockHttpServletRequestBuilder getRequest(String url, String user) {
        return get(url).header("Authorization", authorization(user));
    }

    private static String authorization(String user) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + user).getBytes());
    }

    private static ResultMatcher movieResponse(int id) {
        return result -> {
            status().isOk().match(result);
            jsonPath("$.id").value(notNullValue()).match(result);
            jsonPath("$.title").value(notNullValue()).match(result);
            jsonPath("$.description").value(notNullValue()).match(result);
            if (id <= DataGenerator.comments.size()) {
                jsonPath("$.comments").value(hasSize(DataGenerator.comments.get(id - 1).length)).match(result);
                jsonPath("$.comments[0].username").value(notNullValue()).match(result);
                jsonPath("$.comments[0].message").value(notNullValue()).match(result);
            } else
                jsonPath("$.comments").value(hasSize(0)).match(result);
        };
    }

    @Test
    public void testGet() throws Exception {
        for (int i = 1; i < 10; i++) {
            mockMvc.perform(getRequest("/movie/" + i, "user")).andExpect(movieResponse(i));
            mockMvc.perform(getRequest("/movie/" + i, "admin")).andExpect(movieResponse(i));
        }
    }

    @Test
    public void testGetCommentFromCache() throws Exception {
        int movie = 20;
        mockMvc.perform(getRequest("/movie/" + movie, "user")).andExpect(movieResponse(movie));
        commentServer.close();
        mockMvc.perform(getRequest("/movie/" + movie, "user")).andExpect(movieResponse(movie));
        commentServer.init();
    }

    @Test
    public void testGetServiceUnavailable() throws Exception {
        movieServer.close();
        mockMvc.perform(getRequest("/movie/30", "user")).andExpect(status().isServiceUnavailable());
        movieServer.init();
    }

    @Test
    public void testGetCache() throws Exception {
        queryTimes(101, 3);
        queryTimes(102, 1);
        queryTimes(103, 3);
        queryTimes(104, 3);
        queryTimes(105, 3);
        movieFromCache(106, false);
        movieFromCache(101, true);
        movieFromCache(102, false);
        movieFromCache(106, false);
    }

    private void queryTimes(int id, int times) throws Exception {
        movieFromCache(id, false);
        for (int i = 0; i < times; i++)
            movieFromCache(id, true);
    }

    private void movieFromCache(int id, boolean fromCache) throws Exception {
        long start = System.currentTimeMillis();
        mockMvc.perform(getRequest("/movie/" + id, "user")).andExpect(movieResponse(id));
        assertThat((int) (System.currentTimeMillis() - start), fromCache ? lessThan(500) : greaterThanOrEqualTo(500));
    }

    @Test
    public void testGetUnauthorized() throws Exception {
        mockMvc.perform(getRequest("/movie/1", "wrong")).
                andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetNotFound() throws Exception {
        mockMvc.perform(getRequest("/movie/999", "user")).
                andExpect(status().isNotFound());
    }

    @Test
    public void testCreate() throws Exception {
        mockMvc.perform(postRequest("/movie", "admin", "{\"title\":\"Lorem ipsum dolor sit amet\",\"description\":\"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam\"}")).
                andExpect(status().isCreated()).
                andExpect(header().string("location", new Matches("http://.+?(:\\d+)?/movie/[1-9]\\d*")));
    }

    @Test
    public void testCreateBadRequest() throws Exception {
        mockMvc.perform(postRequest("/movie", "admin", "")).
                andExpect(status().isBadRequest());
        mockMvc.perform(postRequest("/movie", "admin", "{\"description\":\"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam\"}")).
                andExpect(status().isBadRequest());
        mockMvc.perform(postRequest("/movie", "admin", "{\"title\":\"Lorem ipsum dolor sit amet\"}")).
                andExpect(status().isBadRequest());
        mockMvc.perform(postRequest("/movie", "admin", "{\"title\":\"Lorem ipsum dolor sit amet\", \"description\":\"\"}")).
                andExpect(status().isBadRequest());
        mockMvc.perform(postRequest("/movie", "admin", "{\"title\":\"\",\"description\":\"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam\"}")).
                andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateForbidden() throws Exception {
        mockMvc.perform(postRequest("/movie", "user", "{}")).
                andExpect(status().isForbidden());
    }

    @Test
    public void testCreateUnauthorized() throws Exception {
        mockMvc.perform(postRequest("/movie", "wrong", "{}")).
                andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateComment() throws Exception {
        mockMvc.perform(postRequest("/movie/1/comment", "user", "{\"message\":\"Lorem ipsum dolor sit amet\"}")).
                andExpect(status().isNoContent());
        mockMvc.perform(postRequest("/movie/1/comment", "admin", "{\"message\":\"Lorem ipsum dolor sit amet\"}")).
                andExpect(status().isNoContent());
    }

    @Test
    public void testCreateCommentBadRequest() throws Exception {
        mockMvc.perform(postRequest("/movie/1/comment", "user", "{\"message\":\"\"}")).
                andExpect(status().isBadRequest());
        mockMvc.perform(postRequest("/movie/1/comment", "user", "{}")).
                andExpect(status().isBadRequest());
        mockMvc.perform(postRequest("/movie/1/comment", "user", "")).
                andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateCommentUnauthorized() throws Exception {
        mockMvc.perform(postRequest("/movie/1/comment", "wrong", "{}")).
                andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateCommentNotExist() throws Exception {
        mockMvc.perform(postRequest("/movie/999/comment", "user", "{\"message\":\"Lorem ipsum dolor sit amet\"}")).
                andExpect(status().isNotFound());
    }

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).
                addFilters(springSecurityFilterChain).
                build();
    }
}