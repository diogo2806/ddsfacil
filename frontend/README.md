# ddsfacil (Frontend)

## Variáveis obrigatórias

Crie um arquivo `.env` (ou `.env.local`) na pasta `frontend` antes de executar `npm run dev` e defina:

```
VITE_URL_BASE_API=https://seu-backend-real
VITE_EMPRESA_ID_PADRAO=1
```

- `VITE_URL_BASE_API`: endereço HTTPS do backend real, sem barras ao final.
- `VITE_EMPRESA_ID_PADRAO`: identificador numérico da empresa utilizado no cabeçalho `X-Empresa-Id`.

O identificador da empresa é sanitizado automaticamente e também pode ser informado via parâmetro `?empresaId=123` ou `?empresa=123` na URL. Nesse caso, o valor é validado, persistido no `localStorage` e removido da barra de endereço após a leitura.
