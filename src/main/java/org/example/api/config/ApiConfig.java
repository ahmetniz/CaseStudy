package org.example.api.config;

import org.example.web.utils.ConfigReader;

public final class ApiConfig {

    public static final String BASE_URI = "https://api.trello.com/1";

    private ApiConfig() {
    }

    public static String apiKey() {
        String key = ConfigReader.get("trello.key");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "Trello API key not found. Set TRELLO_KEY env var or trello.key in config.properties.");
        }
        return key;
    }

    public static String apiToken() {
        String token = ConfigReader.get("trello.token");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "Trello API token not found. Set TRELLO_TOKEN env var or trello.token in config.properties.");
        }
        return token;
    }
}
