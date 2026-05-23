import { clienteHttp } from './clienteHttp';
import type { RespostaAutenticacao } from '../types/autenticacao';

export async function autenticarUsuario(email: string, senha: string): Promise<RespostaAutenticacao> {
  const corpo = { email, senha };
  const resposta = await clienteHttp.post<RespostaAutenticacao>('/api/public/autenticacao/login', corpo);
  return resposta.data;
}

export async function solicitarRedefinicaoSenha(email: string): Promise<void> {
  await clienteHttp.post('/api/public/autenticacao/esqueci-senha', { email });
}

export async function redefinirSenhaComToken(token: string, novaSenha: string): Promise<void> {
  await clienteHttp.post('/api/public/autenticacao/redefinir-senha', { token, novaSenha });
}
