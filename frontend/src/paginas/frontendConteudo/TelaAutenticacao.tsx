import axios from 'axios';
import { FormEvent, useState } from 'react';
import { autenticarUsuario } from '../../servicos/autenticacaoServico';
import type { RespostaAutenticacao } from '../../types/autenticacao';
import { sanitizarEmailLogin, sanitizarSenhaLogin } from '../../utils/validador';

type PropriedadesTelaAutenticacao = {
  aoAutenticacaoBemSucedida: (dados: RespostaAutenticacao) => void;
  aoCancelar: () => void;
};

export default function TelaAutenticacao({ aoAutenticacaoBemSucedida, aoCancelar }: PropriedadesTelaAutenticacao) {
  const [email, definirEmail] = useState('');
  const [senha, definirSenha] = useState('');
  const [carregando, definirCarregando] = useState(false);
  const [mensagemErro, definirMensagemErro] = useState<string | null>(null);

  async function lidarEnvioFormulario(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const emailSanitizado = sanitizarEmailLogin(email);
    const senhaSanitizada = sanitizarSenhaLogin(senha);

    if (!emailSanitizado || !senhaSanitizada) {
      definirMensagemErro('Informe um e-mail corporativo e a senha fornecida pela DDS Facil.');
      return;
    }

    definirCarregando(true);
    try {
      const resposta = await autenticarUsuario(emailSanitizado, senhaSanitizada);
      definirMensagemErro(null);
      aoAutenticacaoBemSucedida(resposta);
    } catch (erro) {
      const mensagem = obterMensagemDeErro(erro);
      definirMensagemErro(mensagem);
    } finally {
      definirCarregando(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100 px-4 py-12">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="text-2xl font-semibold text-gray-900">Acessar o painel DDS Facil</h1>
        <p className="mt-2 text-sm text-gray-600">
          Entre com as credenciais enviadas pela equipe da DDS Facil para sua empresa.
        </p>

        <form onSubmit={lidarEnvioFormulario} className="mt-6 space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">
              E-mail corporativo
            </label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              inputMode="email"
              value={email}
              onChange={(evento) => definirEmail(evento.target.value)}
              className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
              placeholder="usuario@empresa.com.br"
              required
            />
          </div>

          <div>
            <label htmlFor="senha" className="block text-sm font-medium text-gray-700">
              Senha
            </label>
            <input
              id="senha"
              name="senha"
              type="password"
              autoComplete="current-password"
              value={senha}
              onChange={(evento) => definirSenha(evento.target.value)}
              className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
              placeholder="Digite sua senha"
              required
            />
          </div>

          {mensagemErro && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700" role="alert">
              {mensagemErro}
            </div>
          )}

          <button
            type="submit"
            disabled={carregando}
            className="flex w-full items-center justify-center rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white shadow-lg transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70"
          >
            {carregando ? 'Validando dados...' : 'Entrar no painel'}
          </button>

          <button
            type="button"
            onClick={aoCancelar}
            className="w-full rounded-lg border border-gray-300 px-4 py-2 text-center font-medium text-gray-700 transition hover:bg-gray-50"
          >
            Voltar para o site
          </button>
        </form>
      </div>
    </div>
  );
}

function obterMensagemDeErro(erro: unknown): string {
  if (axios.isAxiosError(erro)) {
    const resposta = erro.response?.data as { mensagem?: string } | undefined;
    if (resposta?.mensagem) {
      return resposta.mensagem;
    }
    return 'Credenciais inválidas. Verifique o e-mail e a senha informados.';
  }
  if (erro instanceof Error) {
    return erro.message;
  }
  return 'Não foi possível validar as credenciais. Tente novamente em instantes.';
}
