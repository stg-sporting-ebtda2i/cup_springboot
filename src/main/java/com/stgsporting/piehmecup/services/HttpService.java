package com.stgsporting.piehmecup.services;

import com.stgsporting.piehmecup.helpers.Http;
import com.stgsporting.piehmecup.helpers.Response;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HttpService {

    @Value("${quizzes.api.key}")
    private String API_KEY;

    @Value("${quizzes.api.secret}")
    private String API_SECRET;

    @Value("${quizzes.api.url}")
    private String BASE_URL;

    public Response get(String url) {
        return new Http(BASE_URL + url, API_KEY, API_SECRET).get();
    }

    public Response post(String url, JSONObject body) {
        return new Http(BASE_URL + url, API_KEY, API_SECRET).post(body);
    }

    public Response patch(String url, JSONObject body) {
        return new Http(BASE_URL + url, API_KEY, API_SECRET).patch(body);
    }

    public Response delete(String url) {
        return new Http(BASE_URL + url, API_KEY, API_SECRET).delete();
    }

    public String getBaseUrl() {
        return BASE_URL;
    }
}
