// Arquivo: frontend/src/servicos/localTrabalhoServico.ts
import { clienteHttp } from './clienteHttp';

/**
 * DTO de resposta do backend (LocalTrabalhoResponse.java)
 */
export type LocalTrabalho = {
  id: number;
  nome: string;
  tipoLocalId: number;
  tipoLocalNome: string;
};

/**
 * DTO de resposta do backend (TipoLocalController.java)
 * Usado para alimentar <select>
 */
export type TipoLocalOption = {
  value: number; // ID do TipoLocal
  label: string; // Nome do TipoLocal
};

/**
 * Lista todos os locais de trabalho cadastrados (ex: "Obra Centro", "Escritório")
 */
export async function listarLocaisTrabalho(): Promise<LocalTrabalho[]> {
  const resposta = await clienteHttp.get<LocalTrabalho[]>('/api/locais-trabalho');
  return resposta.data;
}

/**
 * Lista os tipos de local (ex: "Obra", "Escritório")
 */
export async function listarTiposLocal(): Promise<TipoLocalOption[]> {
  const resposta = await clienteHttp.get<TipoLocalOption[]>('/api/tipos-local');
  return resposta.data;
}

export type TipoLocalAdmin = {
  id: number;
  nome: string;
};

export async function listarTiposLocalAdmin(): Promise<TipoLocalAdmin[]> {
  const resposta = await clienteHttp.get<TipoLocalAdmin[]>('/api/tipos-local/admin');
  return resposta.data;
}

export async function criarTipoLocal(nome: string): Promise<TipoLocalAdmin> {
  const resposta = await clienteHttp.post<TipoLocalAdmin>('/api/tipos-local', { nome });
  return resposta.data;
}

export async function removerTipoLocal(id: number): Promise<void> {
  await clienteHttp.delete(`/api/tipos-local/${id}`);
}

export type LocalTrabalhoRequest = {
  nome: string;
  tipoLocalId: number;
};

export async function criarLocalTrabalho(dados: LocalTrabalhoRequest): Promise<LocalTrabalho> {
  const resposta = await clienteHttp.post<LocalTrabalho>('/api/locais-trabalho', dados);
  return resposta.data;
}

export async function removerLocalTrabalho(id: number): Promise<void> {
  await clienteHttp.delete(`/api/locais-trabalho/${id}`);
}