## DDSFacil — Copilot instructions

Short, practical guidance for AI coding agents working in this repository.

- Project overview: monorepo with a Java Spring Boot backend (`/backend`) and a React + Vite frontend (`/frontend`). Java code lives under `src/main/java/br/com/ddsfacil`. Frontend entry is `src/main.tsx`.

- How the app is structured (big picture):
  - Backend: Spring Boot (Java 17). Controllers -> Services -> Repositories (Spring Data). DTOs live in `*/dto/` packages. Global exception handling is implemented in `configuracao/ManipuladorGlobalExcecoes.java` and a custom `RecursoNaoEncontradoException` is used for missing resources.
  - Frontend: Vite + React + Tailwind. HTTP calls go through `src/configuracao/api.ts` and `src/servicos/*` service modules (e.g., `conteudosServico.ts`, `enviosServico.ts`). UI pages live under `src/paginas/`.

- Key files to reference when editing or adding features:
  - Backend main: `backend/src/main/java/br/com/ddsfacil/DdsFacilAplicacao.java`
  - Example controller/service/repository: `conteudo/ConteudoDdsControlador.java`, `conteudo/ConteudoDdsServico.java`, `conteudo/ConteudoDdsRepositorio.java` and DTOs under `conteudo/dto/`.
  - SMS integration: `integracao/sms/IntegracaoSmsPropriedades.java`, `integracao/sms/ServicoSms.java`, `integracao/sms/TwilioServicoSms.java` and asynchronous sender `envio/sms/EnvioSmsAssincrono.java`.
  - Domain models and flows: `envio/EnvioDds.java`, `envio/StatusEnvioDds.java` show how send operations are tracked.
  - Frontend API client: `frontend/src/configuracao/api.ts` and `frontend/src/servicos/clienteHttp.ts` are the canonical places for base URLs and HTTP patterns.

- Common patterns and conventions (do this in this repo):
  - Use DTO classes in `*/dto/` for controller input/output — controllers should not expose entities directly.
  - Follow Controller -> Service -> Repository layering as implemented in `conteudo` and `funcionarioEntity` modules.
  - Exception handling: throw `RecursoNaoEncontradoException` for not-found, let `ManipuladorGlobalExcecoes` convert to HTTP responses.
  - Package prefix: `br.com.ddsfacil` — keep new code under the same package structure.
  - Portuguese identifiers are used throughout (e.g., `ConfirmacaoTrabalhadorServico`). Keep naming consistent with repository language.

- Build / run / debug (verified from repo READMEs):
  - Run backend locally: `mvn -f backend/pom.xml spring-boot:run` (Java 17 required).
  - Run frontend locally: `cd frontend && npm install && npm run dev` (Vite dev server).
  - Docker: `backend/Dockerfile` and `frontend/Dockerfile` exist for container builds; there is no repo-level compose file — inspect Dockerfiles before composing a multi-container setup.

- Integration points and environment:
  - Database configuration files: `backend/src/main/resources/application.properties` and `application-postgres.properties`. Data seeding in `data.sql`.
  - SMS provider: Twilio implementation lives under `integracao/sms`. Properties class `IntegracaoSmsPropriedades` centralizes credentials — prefer using that abstraction rather than hard-coding provider logic.

- Quick examples (where to change when adding a new REST endpoint):
  1. Create DTOs under `module/dto/` (request/response).
  2. Add entity (if needed) under the module package.
  3. Add a Spring Data repository `*Repositorio.java`.
  4. Add a service `*Servico.java` with business logic and transaction boundaries.
  5. Add a controller `*Controlador.java` that converts between DTOs and domain objects.

- Tests and CI: no test framework or CI config is present in the repo root. If you add tests, follow Spring Boot testing patterns and place frontend tests with the existing Vite/React setup.

- When making changes, be conservative:
  - Avoid changing the global package root or moving many files without coordinating across backend and frontend.
  - Keep API shapes stable — the frontend services expect certain endpoints in `servicos/*` modules.

- If anything here is unclear or you want more detail about build scripts, Docker usage, or runtime environment variables (e.g., exact Twilio env var names), tell me which area to expand and I will iterate.
