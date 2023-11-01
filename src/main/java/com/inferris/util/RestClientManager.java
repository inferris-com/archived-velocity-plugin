package com.inferris.util;

import okhttp3.*;

import java.io.IOException;

public class RestClientManager implements AutoCloseable {
    private final OkHttpClient client;

    public RestClientManager() {
        client = new OkHttpClient();
    }

    public RestClientManager(String name) {
        client = new OkHttpClient();
    }


    public Response sendRequest(String url, Method method, String apiKey) {
        Request request = new Request.Builder()
                .url(url)
                .method(method.getType(), null)
                .addHeader(Heading.XF_API.getType(), apiKey)
                .build();

        try {
            return client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendRequest(String url, Method method, String apiKey, MediaType mediaType, String requestBody) {
        RequestBody body = (requestBody != null) ? RequestBody.create(mediaType, requestBody) : null;

        Request request = new Request.Builder()
                .url(url)
                .method(method.getType(), body)
                .addHeader(Heading.XF_API.getType(), apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            // Process the response if needed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Response sendRequest(String url, String apiKey) {

        Request request = new Request.Builder()
                .url(url)
                .addHeader(Heading.XF_API.getType(), apiKey)
                .build();

        try {
            return client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() throws Exception {

    }

    public enum Method {
        POST,
        GET;

        public String getType() {
            return this.name().toUpperCase();
        }
    }

    public enum Heading {
        XF_API("XF-Api-Key");

        public final String type;

        Heading(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
