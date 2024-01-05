package com.inferris.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class StatusUpdater implements Runnable {
    private static final String BASE_URI = "https://api.statuspage.io/v1";
    private static final String PAGE_ID = "4g90923glbvb";
    private static final String METRIC_ID = "q1y7y5t91vvp";
    private static final String API_KEY = "e1073a71c4e44664a8407aa7e20759bb";

    public StatusUpdater(){
        Thread updaterThread = new Thread(new StatusUpdater());
        updaterThread.start();
    }

    @Override
    public void run() {
        try {
            int totalPoints = 60 / 5 * 24;
            for (int i = 0; i < totalPoints; i++) {
                long timestamp = System.currentTimeMillis() / 1000 - (i * 5 * 60);
                int value = new Random().nextInt(100);

                String postParams = "data[timestamp]=" + timestamp + "&data[value]=" + value;
                String urlString = BASE_URI + "/pages/" + PAGE_ID + "/metrics/" + METRIC_ID + "/data.json";

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "OAuth " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postParams.getBytes());
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Print the response for debugging purposes
                System.out.println("Response Code: " + responseCode);
                System.out.println("Response Body: " + response.toString());

                Thread.sleep(1000); // Sleep for 1 second
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
