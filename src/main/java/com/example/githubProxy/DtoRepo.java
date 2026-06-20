package com.example.githubProxy;

import java.util.List;

public record DtoRepo(String name, String owner, List<DtoBranch> branches) {
}
