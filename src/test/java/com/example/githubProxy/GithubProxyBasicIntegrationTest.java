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

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.proxyAllTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class GithubProxyBasicIntegrationTest {

    @RegisterExtension
    static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Autowired
    private RestTestClient restClient;

    @Test
    void ReturnRepositoriesWithBranchesForExistingGitHubUser() {
        restClient.get()
                .uri("/pawejur")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DtoRepo[].class)
                .value(repositories -> assertThat(repositories)
                        .anySatisfy(repository -> {
                            assertThat(repository.name()).isEqualTo("GitHubProxy");
                            assertThat(repository.owner()).isEqualTo("pawejur");
                            assertThat(repository.branches()).isNotEmpty();
                        }));
    }

    @Test
    void ReturnNotFoundErrorForGitHubUserWithoutPublicRepositories() {
        String username = "this-user-does-not-exists-for-sure-123456789";

        restClient.get()
                .uri("/{username}", username)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(DtoError.class)
                .value(error -> {
                    assertThat(error.status()).isEqualTo(404);
                    assertThat(error.message())
                            .isEqualTo("GitHub user '%s' was not found"
                                    .formatted(username));
                });
    }
}
