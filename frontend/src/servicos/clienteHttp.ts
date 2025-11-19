import axios from 'axios';
import { URL_BASE_API } from '../configuracao/api';
import { obterEmpresaIdAtualOpcional } from '../configuracao/empresa';
import { sanitizarTokenJwt } from '../utils/validador';

export const clienteHttp = axios.create({
  baseURL: URL_BASE_API,
  headers: {
    'Content-Type': 'application/json',
  },
});

clienteHttp.interceptors.request.use((config) => {
  const empresaId = obterEmpresaIdAtualOpcional();
  if (empresaId) {
    config.headers = config.headers ?? {};
    config.headers['X-Empresa-Id'] = String(empresaId);
  }
  return config;
});

function removerPrefixoBearer(tokenJwt: string): string {
  return tokenJwt.replace(/^Bearer\s+/i, '').trim();
}

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
