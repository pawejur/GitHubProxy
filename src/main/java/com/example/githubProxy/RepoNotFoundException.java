package com.example.githubProxy;

public class RepoNotFoundException extends RuntimeException {
    public RepoNotFoundException(String message) {
        super(message);
    }
}
