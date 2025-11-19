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

## Multi-tenant (Isolamento por Empresa)

Todas as chamadas privadas da API exigem o envio do cabeçalho `X-Empresa-Id` com o identificador numérico da empresa. Esse valor é usado pelo filtro global do Hibernate para garantir que cada consulta retorne apenas os dados da empresa informada. Os endpoints públicos sob `/api/public/**` continuam acessíveis sem o cabeçalho, pois são utilizados pelos trabalhadores via token seguro.
