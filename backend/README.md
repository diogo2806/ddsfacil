# ddsfacil (Backend)

This module runs the DDSFacil backend service. The project now requires Java 21 (latest LTS) for building and running.

Quick start:

- Build and test locally (requires Java 21 and Maven):

```
mvn -f pom.xml -U test package
```

- Run with the packaged jar:

```
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

- Build the Docker image (uses Temurin 21):

```
docker build -t ddsfacil-backend .
```

If you don't have Java 21 installed locally, use the provided Dockerfile which builds with a Temurin 21-based Maven image.
