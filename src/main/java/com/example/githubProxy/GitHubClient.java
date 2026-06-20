package com.example.githubProxy;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class GitHubClient {

    GitHubClient(@Value("${github.api-base-url}") String baseApiUrl){
        this.baseApiUrl = baseApiUrl;
        this.repoApiUrl = baseApiUrl + "/users/{username}/repos";
        this.branchApriUrl = baseApiUrl + "/repos/{username}/{reponame}/branches";
    }

    private final RestClient restClient = RestClient.create();
    private final String baseApiUrl;
    private final String repoApiUrl;
    private final String branchApriUrl;


    public DtoGitHubRepo[] fetchRepos(String username){
        try {
            return restClient.get()
                    .uri(repoApiUrl, username)
                    .retrieve()
                    .body(DtoGitHubRepo[].class);
        } catch (HttpClientErrorException.NotFound exception){
            throw new UserNotFoundException("GitHub user '%s' was not found".formatted(username));
        }
    }

    public DtoGitHubBranch[] fetchBranches(String username, String reponame){
        try {
            return restClient.get()
                    .uri(branchApriUrl, username, reponame)
                    .retrieve()
                    .body(DtoGitHubBranch[].class);
        } catch(HttpClientErrorException exception){
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RepoNotFoundException("GitHub repository '%s' of user '%s' was not found".formatted(reponame,username));
            }
            throw exception;
        }
    }

}
