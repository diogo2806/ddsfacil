import { clienteHttp } from './clienteHttp';

export type ConteudoDds = {
  id: number;
  titulo: string;
  descricao: string;
};

export type CadastroConteudo = {
  titulo: string;
  descricao: string;
};

export async function listarConteudos(): Promise<ConteudoDds[]> {
  const resposta = await clienteHttp.get<ConteudoDds[]>('/conteudos');
  return resposta.data;
}

export async function criarConteudo(dados: CadastroConteudo): Promise<ConteudoDds> {
  const resposta = await clienteHttp.post<ConteudoDds>('/conteudos', dados);
  return resposta.data;
}

export async function removerConteudo(id: number): Promise<void> {
  await clienteHttp.delete(`/conteudos/${id}`);
}
