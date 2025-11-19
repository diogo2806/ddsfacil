export type RespostaAutenticacao = {
  token: string;
  empresaId: number;
  nomeUsuario: string;
  perfil: string;
};

export type SessaoUsuario = RespostaAutenticacao;
