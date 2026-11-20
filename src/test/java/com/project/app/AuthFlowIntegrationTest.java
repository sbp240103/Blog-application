package com.project.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

// boots the whole application on a random port against the in memory database
// and walks through the real http flow a client would use
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> send(String method, String path, String json, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + path))
                .method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return this.http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String field(String json, String name) {
        Matcher matcher = Pattern.compile("\"" + name + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String registerAndLogin(String email) throws Exception {
        String user = "{\"name\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"secret123\",\"about\":\"testing\"}";
        assertEquals(201, send("POST", "/api/v1/auth/register", user, null).statusCode());

        HttpResponse<String> login = send("POST", "/api/v1/auth/login",
                "{\"email\":\"" + email + "\",\"password\":\"secret123\"}", null);
        assertEquals(200, login.statusCode());
        return field(login.body(), "token");
    }

    @Test
    void registeredUserCanLoginAndReadOwnProfile() throws Exception {
        String email = "it" + System.nanoTime() + "@example.com";
        String token = registerAndLogin(email);

        HttpResponse<String> me = send("GET", "/api/users/me", null, token);
        assertEquals(200, me.statusCode());
        assertTrue(me.body().contains(email));
        assertTrue(!me.body().contains("secret123"), "password must never be returned");
    }

    @Test
    void protectedEndpointNeedsToken() throws Exception {
        assertEquals(401, send("GET", "/api/users/me", null, null).statusCode());
    }

    @Test
    void normalUserCanNotCreateCategories() throws Exception {
        String token = registerAndLogin("cat" + System.nanoTime() + "@example.com");
        HttpResponse<String> response = send("POST", "/api/categories",
                "{\"categoryTitle\":\"Not Allowed\",\"categoryDescription\":\"only admins can do this\"}", token);
        assertEquals(403, response.statusCode());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        String email = "pw" + System.nanoTime() + "@example.com";
        registerAndLogin(email);
        HttpResponse<String> login = send("POST", "/api/v1/auth/login",
                "{\"email\":\"" + email + "\",\"password\":\"wrong123\"}", null);
        assertEquals(401, login.statusCode());
    }

    @Test
    void validationErrorsListTheFields() throws Exception {
        HttpResponse<String> response = send("POST", "/api/v1/auth/register",
                "{\"name\":\"ab\",\"email\":\"not-an-email\",\"password\":\"letters\",\"about\":\"\"}", null);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("fieldErrors"));
        assertTrue(response.body().contains("email"));
    }

    @Test
    void healthCheckIsPublic() throws Exception {
        HttpResponse<String> health = send("GET", "/actuator/health", null, null);
        assertEquals(200, health.statusCode());
        assertTrue(health.body().contains("UP"));
    }

}
