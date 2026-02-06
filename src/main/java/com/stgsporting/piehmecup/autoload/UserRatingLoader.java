package com.stgsporting.piehmecup.autoload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UserRatingLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public UserRatingLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Path path = Paths.get(System.getProperty("user.dir") + "/user_rating_view.sql");

        String queriesStr = Files.readString(path);

        String [] queries = queriesStr.split(";");

        for (String query : queries) {
            String q = query.trim();
            if (!q.isEmpty()) {
                jdbcTemplate.execute(q);
            }
        }
    }
}
