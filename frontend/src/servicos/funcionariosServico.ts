// Arquivo: frontend/src/servicos/funcionariosServico.ts
import { clienteHttp } from './clienteHttp';

/**
 * [REATORADO] Alinhado com FuncionarioResponse.java
 */
export type Funcionario = {
  id: number;
  nome: string;
  celular: string;
  localTrabalhoId: number;
  localTrabalhoNome: string;
  tipoLocalNome: string;
};

/**
 * [REATORADO] Alinhado com FuncionarioRequest.java
 */
export type CadastroFuncionario = {
  nome: string;
  celular: string;
  localTrabalhoId: number;
};

/**
 * [REATORADO] O backend filtra por 'localId'
 */
export async function listarFuncionarios(localId?: number): Promise<Funcionario[]> {
  const resposta = await clienteHttp.get<Funcionario[]>('/api/funcionarios', {
    params: localId ? { localId } : undefined,
  });
  return resposta.data;
}

// [REMOVIDO] O método listarObras() foi removido pois o endpoint /api/funcionarios/obras
// não existe mais no backend. Use localTrabalhoServico.ts

export async function criarFuncionario(dados: CadastroFuncionario): Promise<Funcionario> {
  const resposta = await clienteHttp.post<Funcionario>('/api/funcionarios', dados);
  return resposta.data;
}

export async function removerFuncionario(id: number): Promise<void> {
  await clienteHttp.delete(`/api/funcionarios/${id}`);
}