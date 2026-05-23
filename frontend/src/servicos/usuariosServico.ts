import { clienteHttp } from './clienteHttp';

export type PerfilUsuario = 'ADMIN' | 'GESTOR' | 'OPERADOR';

export type Usuario = {
  id: number;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
  ativo: boolean;
  criadoEm: string;
};

export type CadastroUsuario = {
  nome: string;
  email: string;
  senha: string;
  perfil: PerfilUsuario;
};

export type AtualizacaoUsuario = {
  nome: string;
  perfil: PerfilUsuario;
  ativo: boolean;
};

export async function listarUsuarios(): Promise<Usuario[]> {
  const resposta = await clienteHttp.get<Usuario[]>('/api/usuarios');
  return resposta.data;
}

export async function criarUsuario(dados: CadastroUsuario): Promise<Usuario> {
  const resposta = await clienteHttp.post<Usuario>('/api/usuarios', dados);
  return resposta.data;
}

export async function atualizarUsuario(id: number, dados: AtualizacaoUsuario): Promise<Usuario> {
  const resposta = await clienteHttp.put<Usuario>(`/api/usuarios/${id}`, dados);
  return resposta.data;
}

export async function redefinirSenhaUsuario(id: number, novaSenha: string): Promise<void> {
  await clienteHttp.put(`/api/usuarios/${id}/senha`, { novaSenha });
}

export async function alterarMinhaSenha(senhaAtual: string, novaSenha: string): Promise<void> {
  await clienteHttp.put('/api/minha-conta/senha', { senhaAtual, novaSenha });
}
