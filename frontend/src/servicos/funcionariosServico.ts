import { clienteHttp } from './clienteHttp';

export type Funcionario = {
  id: number;
  nome: string;
  celular: string;
  obra: string;
};

export type CadastroFuncionario = {
  nome: string;
  celular: string;
  obra: string;
};

export async function listarFuncionarios(obra?: string): Promise<Funcionario[]> {
  const resposta = await clienteHttp.get<Funcionario[]>('/funcionarios', {
    params: obra ? { obra } : undefined,
  });
  return resposta.data;
}

export async function listarObras(): Promise<string[]> {
  const resposta = await clienteHttp.get<string[]>('/funcionarios/obras');
  return resposta.data;
}

export async function criarFuncionario(dados: CadastroFuncionario): Promise<Funcionario> {
  const resposta = await clienteHttp.post<Funcionario>('/funcionarios', dados);
  return resposta.data;
}

export async function removerFuncionario(id: number): Promise<void> {
  await clienteHttp.delete(`/funcionarios/${id}`);
}
