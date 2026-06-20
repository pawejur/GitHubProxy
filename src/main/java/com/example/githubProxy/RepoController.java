package com.example.githubProxy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@RestController
public class RepoController {
    RepoController(GitHubClient gitHubClient){
        this.gitHubClient = gitHubClient;
    }

    private final GitHubClient gitHubClient;



    @GetMapping("/{username}/repos")
    DtoGitHubRepo[] repositories(@PathVariable String username){
        return gitHubClient.fetchRepos(username);
    }

    @GetMapping("/{username}/{reponame}/branches")
    DtoGitHubBranch[] branches(@PathVariable String username, @PathVariable String reponame){
        return gitHubClient.fetchBranches(username,reponame);
    }

    @GetMapping("/{username}")
    List<DtoRepo> repos(@PathVariable String username) {

        List<DtoRepo> repositories = new ArrayList<>();

        for( DtoGitHubRepo gitHubRepo : gitHubClient.fetchRepos(username) ){
            if(gitHubRepo.fork()){
                continue;
            }

            List<DtoBranch> branches = new ArrayList<>();

            for( DtoGitHubBranch gitHubBranch : gitHubClient.fetchBranches(username,gitHubRepo.name()) ){
                branches.add(
                        new DtoBranch(
                                gitHubBranch.name(),
                                gitHubBranch.commit().sha()
                        ));
            }


            repositories.add(
                    new DtoRepo(
                            gitHubRepo.name(),
                            gitHubRepo.owner().login(),
                            branches
                    ));
        }


        return repositories;
    }
}
