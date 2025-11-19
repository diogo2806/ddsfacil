import { clienteHttp } from './clienteHttp';
import type { RespostaAutenticacao } from '../types/autenticacao';

export async function autenticarUsuario(email: string, senha: string): Promise<RespostaAutenticacao> {
  const corpo = { email, senha };
  const resposta = await clienteHttp.post<RespostaAutenticacao>('/api/public/autenticacao/login', corpo);
  return resposta.data;
}
