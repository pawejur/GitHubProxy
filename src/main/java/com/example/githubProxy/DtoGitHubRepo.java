package com.example.githubProxy;

public record DtoGitHubRepo(String name, DtoGitHubOwner owner, boolean fork) {
}
