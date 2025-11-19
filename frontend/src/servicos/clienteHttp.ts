import axios from 'axios';
import { URL_BASE_API } from '../configuracao/api';
import { carregarSessaoUsuario } from '../configuracao/sessaoUsuario';
import { sanitizarTokenJwt } from '../utils/validador';

export const clienteHttp = axios.create({
  baseURL: URL_BASE_API,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para injetar Token e Empresa ID automaticamente
clienteHttp.interceptors.request.use((config) => {
  const sessao = carregarSessaoUsuario();

  // Garante que headers existam
  config.headers = config.headers ?? {};

  if (sessao) {
    // 1. Injeta o Token de Autorização
    const tokenSanitizado = sanitizarTokenJwt(sessao.token);
    if (tokenSanitizado) {
      config.headers.Authorization = `Bearer ${tokenSanitizado}`;
    }

    // 2. Injeta o ID da Empresa vinculado a este usuário/sessão
    // Isso previne o erro 403 onde o header não bate com o token
    if (sessao.empresaId) {
      config.headers['X-Empresa-Id'] = String(sessao.empresaId);
    }
  }

  return config;
});

function removerPrefixoBearer(tokenJwt: string): string {
  return tokenJwt.replace(/^Bearer\s+/i, '').trim();
}

// Esta função é mantida para compatibilidade com o App.tsx antigo,
// mas o interceptor acima agora faz o trabalho pesado.
export function configurarCabecalhoAutorizacaoJwt(tokenJwt: string | null | undefined): void {
  if (typeof tokenJwt !== 'string') {
    delete clienteHttp.defaults.headers.common.Authorization;
    return;
  }

  const tokenSemPrefixo = removerPrefixoBearer(tokenJwt);
  const tokenSanitizado = sanitizarTokenJwt(tokenSemPrefixo);

  if (tokenSanitizado) {
    clienteHttp.defaults.headers.common.Authorization = `Bearer ${tokenSanitizado}`;
  } else {
    delete clienteHttp.defaults.headers.common.Authorization;
  }
}