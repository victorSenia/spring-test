package com.leo.test.luxoft.controller;

import com.leo.test.luxoft.model.Comment;
import com.leo.test.luxoft.model.Movie;
import com.leo.test.luxoft.service.CommentService;
import com.leo.test.luxoft.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Senchenko Victor
 */
@RestController
@RequestMapping("/movie")
public class MovieController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovieController.class);

    @Autowired
    private MovieService movieService;

    @Autowired
    private ExecutorService executor;

    @Autowired
    private CommentService commentService;

    @GetMapping(value = "/{movieId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getMovie(@PathVariable int movieId) {
        Future<Movie> movieFuture = executor.submit(() -> movieService.movie(movieId));
        Future<Comment[]> commentsFuture = executor.submit(() -> commentService.comments(movieId));
        try {
            Movie movie = movieFuture.get();
            if (movie == null) {
                commentsFuture.cancel(true);
                return ResponseEntity.notFound().build();
            }
            Comment[] comments = commentsFuture.get();
            movie.setComments(comments != null ? comments : new Comment[0]);
            return ResponseEntity.ok(movie);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted {}", e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RestClientException)
                throw (RestClientException) e.getCause();
            LOGGER.info("Execution exception {}", e.getLocalizedMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> createMovie(@Valid @RequestBody Movie movie, BindingResult result) {
        if (result.hasErrors())
            return ResponseEntity.badRequest().build();
        //TODO storing movie is not implemented
        movie.setId(new Random().nextInt(9999));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{movieId}").buildAndExpand(movie.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(value = "/{movieId}/comment", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> createComment(@PathVariable int movieId, @Valid @RequestBody Comment comment, BindingResult result, Principal principal) {
        if (result.hasErrors())
            return ResponseEntity.badRequest().build();
        if (movieService.movie(movieId) == null)
            return ResponseEntity.notFound().build();
        comment.setMovieId(movieId);
        comment.setUsername(principal.getName());
        //TODO storing comment is not implemented
        return ResponseEntity.noContent().build();
    }
}
