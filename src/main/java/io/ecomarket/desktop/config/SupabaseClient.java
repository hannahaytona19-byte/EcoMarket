package io.ecomarket.desktop.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Built-in HTTP client for Supabase PostgREST & Auth REST API.
 */
public class SupabaseClient {

    private static final Logger log = LoggerFactory.getLogger(SupabaseClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;

    public SupabaseClient() {
        this.baseUrl = SupabaseConfig.getSupabaseUrl();
        this.apiKey = SupabaseConfig.getSupabaseAnonKey();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public <T> T get(String path, Class<T> responseType) throws Exception {
        String url = baseUrl + (path.startsWith("/") ? path : "/" + path);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseType);
        }
        throw new RuntimeException("Supabase REST error HTTP " + response.statusCode() + ": " + response.body());
    }

    public <T> T post(String path, Object payload, Class<T> responseType) throws Exception {
        String url = baseUrl + (path.startsWith("/") ? path : "/" + path);
        String body = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseType);
        }
        throw new RuntimeException("Supabase REST error HTTP " + response.statusCode() + ": " + response.body());
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
