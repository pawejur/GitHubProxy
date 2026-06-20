package com.example.githubProxy;


import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class GithubProxyBasicIntegrationTest {

    @Autowired
    private RestTestClient restClient;



    private static final HttpServer githubServer = startGithubServer();

    private static HttpServer startGithubServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/", GithubProxyBasicIntegrationTest::handleGithubRequest);
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not start fake GitHub server",
                    exception
            );
        }
    }

    @DynamicPropertySource
    static void githubProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "github.api-base-url",
                () -> "http://localhost:" + githubServer.getAddress().getPort()
        );
    }

    private static void handleGithubRequest(HttpExchange exchange)
            throws IOException {

        String path = exchange.getRequestURI().getPath();

        String body = switch (path) {
            case "/users/test/repos" -> """
                  [
                    {
                      "name": "project-one",
                      "owner": {"login": "test"},
                      "fork": false
                    },
                    {
                      "name": "forked-project",
                      "owner": {"login": "test"},
                      "fork": true
                    }
                  ]
                  """;

            case "/repos/test/project-one/branches" -> """
                  [
                    {
                      "name": "main",
                      "commit": {"sha": "abc123"}
                    }
                  ]
                  """;

            default -> null;
        };

        if (body == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        byte[] response = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set("Content-Type", "application/json");

        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }


    @Test
    void returnsOnlyNonForkRepositories() {
        restClient.get()
                .uri("/test")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("""
                      [
                        {
                          "name": "project-one",
                          "owner": "test",
                          "branches": [
                            {
                              "name": "main",
                              "sha": "abc123"
                            }
                          ]
                        }
                      ]
                      """);
    }

    @AfterAll
    static void stopGithubServer() {
        githubServer.stop(0);
    }

}
