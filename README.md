# DDS Facil

Aplicação composta por frontend em React com Vite e backend em Spring Boot para gerenciamento de conteúdos de DDS.

## Estrutura

- `backend`: projeto Java 17 com Spring Boot.
- `frontend`: aplicação React com Tailwind CSS e React Query.

## Configuração

1. Execute `mvn -f backend/pom.xml spring-boot:run` para iniciar a API.
2. Instale as dependências do frontend com `npm install` na pasta `frontend`.
3. Inicie o frontend com `npm run dev`.

### Variáveis de ambiente para login no painel

No backend, configure as credenciais de acesso do usuário administrador pelo ambiente:

- `DDSFACIL_ADMIN_EMAIL`
- `DDSFACIL_ADMIN_SENHA`

Essas variáveis são aplicadas automaticamente no usuário `ADMIN` da empresa padrão durante a inicialização da API.
