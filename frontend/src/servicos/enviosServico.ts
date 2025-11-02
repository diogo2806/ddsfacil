import { clienteHttp } from './clienteHttp';

export type StatusEnvioDds = 'ENVIADO' | 'CONFIRMADO';

export type EnvioDds = {
  id: number;
  funcionarioId: number;
  nomeFuncionario: string;
  obra: string;
  conteudoId: number;
  tituloConteudo: string;
  status: StatusEnvioDds;
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
  const resposta = await clienteHttp.get<EnvioDds[]>('/envios', {
    params: data ? { data } : undefined,
  });
  return resposta.data;
}

export async function criarEnvios(dados: CadastroEnvio): Promise<EnvioDds[]> {
  const resposta = await clienteHttp.post<EnvioDds[]>('/envios', dados);
  return resposta.data;
}

