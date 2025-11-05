// Arquivo: frontend/src/servicos/confirmacaoTrabalhadorServico.ts
import { clienteHttp } from './clienteHttp';
import { StatusEnvio } from '../types/enums';

export type DadosConfirmacaoTrabalhador = {
  titulo: string;
  descricao: string;
};
export type RespostaConfirmacao = {
  status: StatusEnvio;
  momentoConfirmacao: string | null;
};
function sanitizarToken(token: string): string {
  return token.replace(/[^a-zA-Z0-9-]/g, '');
}

export async function consultarConteudoDds(token: string): Promise<DadosConfirmacaoTrabalhador> {
  const tokenLimpo = sanitizarToken(token);
  if (!tokenLimpo) {
    throw new Error('Token inválido.');
  }
  const resposta = await clienteHttp.get<DadosConfirmacaoTrabalhador>(
    `/public/dds/${encodeURIComponent(tokenLimpo)}`,
  );
  return resposta.data;
}

export async function confirmarLeituraDds(token: string): Promise<RespostaConfirmacao> {
  const tokenLimpo = sanitizarToken(token);
  if (!tokenLimpo) {
    throw new Error('Token inválido.');
  }
  const resposta = await clienteHttp.post<RespostaConfirmacao>(
    `/public/dds/${encodeURIComponent(tokenLimpo)}/confirmar`,
    {},
  );
  return resposta.data;
}