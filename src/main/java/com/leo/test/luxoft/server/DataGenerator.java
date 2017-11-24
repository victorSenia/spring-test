package com.leo.test.luxoft.server;

import com.leo.test.luxoft.model.Comment;
import com.leo.test.luxoft.model.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Senchenko Victor
 */
public class DataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataGenerator.class);

    private static final Random random = new Random();

    private static List<String> words;

    static {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(DataGenerator.class.getClassLoader().getResourceAsStream("lorem ipsum.txt")))) {
            words = buffer.lines().map(String::toLowerCase).map(s -> s.split("[.,]? ")).flatMap(Arrays::stream).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error reading from file {}", e.getLocalizedMessage());
            words = Collections.EMPTY_LIST;
        }
    }

    public static final List<Comment[]> comments = Stream.generate(DataGenerator::generateComments).limit(50).collect(Collectors.toList());

    private static int movieId = 1;

    public static final List<Movie> movies = Stream.generate(DataGenerator::generateMovie).limit(50).collect(Collectors.toList());

    private static Comment[] generateComments() {
        int size = random.nextInt(5) + 5;
        return Stream.generate(DataGenerator::generateComment).limit(size).collect(Collectors.toList()).toArray(new Comment[size]);
    }

    private static Comment generateComment() {
        Comment comment = new Comment();
        comment.setMessage(getWords(10, 20));
        comment.setUsername(getWords(1, 1));
        return comment;
    }

    private static Movie generateMovie() {
        Movie movie = new Movie();
        movie.setId(movieId++);
        movie.setTitle(getWords(2, 3));
        movie.setDescription(getWords(5, 10));
        return movie;
    }

    private static String getWords(int from, int to) {
        int count = random.nextInt(to - from + 1) + from;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(words.get(random.nextInt(words.size())));
            if (i < count - 1)
                builder.append(' ');
        }
        builder.replace(0, 1, builder.substring(0, 1).toUpperCase());
        return builder.toString();
    }
}