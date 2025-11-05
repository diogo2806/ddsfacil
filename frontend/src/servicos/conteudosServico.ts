// Arquivo: frontend/src/servicos/conteudosServico.ts
import { clienteHttp } from './clienteHttp';
import { TipoConteudo } from '../types/enums';

export type ConteudoDds = {
  id: number;
  titulo: string;
  descricao: string;
  tipo?: TipoConteudo;
  url?: string | null;
  arquivoNome?: string | null;
  arquivoPath?: string | null;
};
export type CadastroConteudo = {
  titulo: string;
  descricao?: string;
  tipo?: TipoConteudo;
  url?: string;
  // Para upload de arquivo, envie um FormData e use criarConteudoComArquivo
  arquivo?: File | null;
};
export async function listarConteudos(): Promise<ConteudoDds[]> {
  const resposta = await clienteHttp.get<ConteudoDds[]>('/api/conteudos');
  return resposta.data;
}

export async function criarConteudo(dados: CadastroConteudo): Promise<ConteudoDds> {
  const resposta = await clienteHttp.post<ConteudoDds>('/api/conteudos', dados);
  return resposta.data;
}

export async function criarConteudoComArquivo(dados: CadastroConteudo): Promise<ConteudoDds> {
  const form = new FormData();
  form.append('titulo', dados.titulo);
  form.append('descricao', dados.descricao ?? '');
  form.append('tipo', TipoConteudo.ARQUIVO);
  if (dados.arquivo) {
    form.append('file', dados.arquivo, dados.arquivo.name);
  }

  // CORREÇÃO: Adicionado o prefixo /api/
  const resposta = await clienteHttp.post<ConteudoDds>('/api/conteudos/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return resposta.data;
}

export async function removerConteudo(id: number): Promise<void> {
  await clienteHttp.delete(`/api/conteudos/${id}`);
}