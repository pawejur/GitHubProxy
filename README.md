# GitHub Proxy

A Spring Boot REST API that retrieves a GitHub user's public repositories and
returns each non-fork repository together with its branches and latest commit
SHAs.

## Requirements

- Java 25
- An internet connection for requests to the GitHub API

The Gradle wrapper is included, so a separate Gradle installation is not
required.

## Run locally

On Linux or macOS:

```bash
./gradlew bootRun
```

On Windows:

```powershell
.\gradlew.bat bootRun
```

The application starts at `http://localhost:8080` by default.

## API

### Get repositories for a GitHub user

```http
GET /{username}
```

Example:

```bash
curl http://localhost:8080/octocat
```

Successful response:

```json
[
  {
    "name": "Hello-World",
    "owner": "octocat",
    "branches": [
      {
        "name": "main",
        "sha": "7fd1a60b01f91b314f59955a4e4d92c2c4b3f7d2"
      }
    ]
  }
]
```

Forked repositories are excluded from the response.

If the GitHub user does not exist, the API returns HTTP `404`:

```json
{
  "status": 404,
  "message": "GitHub user 'unknown-user' was not found"
}
```

The API also returns HTTP `404` when the user exists but has no public
repositories:

```json
{
  "status": 404,
  "message": "GitHub user 'some-user' has no public repositories"
}
```

## Configuration

The GitHub API base URL is configured in
`src/main/resources/application.properties`:

```properties
github.api-base-url=https://api.github.com
```

It can be overridden at runtime, for example:

```bash
GITHUB_API_BASE_URL=https://api.github.com ./gradlew bootRun
```

## Tests

Run the integration tests with:

```bash
./gradlew test
```

The test suite uses a local fake GitHub server and does not call the live
GitHub API.

## Build

Create the executable JAR:

```bash
./gradlew bootJar
```

The generated JAR is written to `build/libs/`.
