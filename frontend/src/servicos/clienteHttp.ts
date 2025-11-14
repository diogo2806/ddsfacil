import axios from 'axios';
import { URL_BASE_API } from '../configuracao/api';

export const clienteHttp = axios.create({
  baseURL: URL_BASE_API,
  headers: {
    'Content-Type': 'application/json',
  },
});

export function configurarCabecalhoAutorizacaoJwt(tokenJwt: string | null | undefined): void {
  if (typeof tokenJwt !== 'string') {
    delete clienteHttp.defaults.headers.common.Authorization;
    return;
  }

  const tokenLimpo = tokenJwt.replace(/^Bearer\s+/i, '').trim();

  if (tokenLimpo) {
    clienteHttp.defaults.headers.common.Authorization = tokenLimpo;
  } else {
    delete clienteHttp.defaults.headers.common.Authorization;
  }
}
