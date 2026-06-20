package com.example.githubProxy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class GithubProxyBasicIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Autowired
    private RestTestClient restClient;

    @DynamicPropertySource
    static void githubProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api-base-url", wireMock::baseUrl);
    }

    @BeforeEach
    void stubGitHubApi() {
        wireMock.stubFor(get(urlEqualTo("/users/test/repos"))
                .willReturn(okJson("""
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
                          """)));

        wireMock.stubFor(get(urlEqualTo(
                "/repos/test/project-one/branches"))
                .willReturn(okJson("""
                          [
                            {
                              "name": "main",
                              "commit": {"sha": "abc123"}
                            }
                          ]
                          """)));
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

        wireMock.verify(getRequestedFor(
                urlEqualTo("/users/test/repos")));

        wireMock.verify(getRequestedFor(
                urlEqualTo("/repos/test/project-one/branches")));
    }
}