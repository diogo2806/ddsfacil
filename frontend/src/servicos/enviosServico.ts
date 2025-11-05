// Arquivo: frontend/src/servicos/enviosServico.ts
import { clienteHttp } from './clienteHttp';
import { StatusEnvio } from '../types/enums';

export type EnvioDds = {
  id: number;
  funcionarioId: number;
  nomeFuncionario: string;
  obra: string;
  conteudoId: number;
  tituloConteudo: string;
  status: StatusEnvio;
  dataEnvio: string;
  momentoEnvio: string;
  momentoConfirmacao: string | null;
};
export type CadastroEnvio = {
  conteudoId: number;
  funcionariosIds: number[];
  dataEnvio?: string;
};
export async function listarEnviosPorData(data?: string): Promise<EnvioDds[]> {
  const resposta = await clienteHttp.get<EnvioDds[]>('/api/envios', {
    params: data ? { data } : undefined,
  });
  return resposta.data;
}

export async function criarEnvios(dados: CadastroEnvio): Promise<EnvioDds[]> {
  const resposta = await clienteHttp.post<EnvioDds[]>('/api/envios', dados);
  return resposta.data;
}