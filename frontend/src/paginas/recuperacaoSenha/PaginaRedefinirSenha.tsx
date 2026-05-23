import axios from 'axios';
import { FormEvent, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { redefinirSenhaComToken } from '../../servicos/autenticacaoServico';
import { sanitizarSenhaLogin } from '../../utils/validador';

export default function PaginaRedefinirSenha() {
  const [parametros] = useSearchParams();
  const navegador = useNavigate();
  const token = useMemo(() => (parametros.get('token') ?? '').trim(), [parametros]);

  const [novaSenha, definirNovaSenha] = useState('');
  const [confirmacao, definirConfirmacao] = useState('');
  const [enviando, definirEnviando] = useState(false);
  const [erro, definirErro] = useState<string | null>(null);
  const [concluido, definirConcluido] = useState(false);

  async function aoSubmeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    definirErro(null);

    if (!token) {
      definirErro('Link inválido. Solicite uma nova redefinição de senha.');
      return;
    }
    const senhaSanitizada = sanitizarSenhaLogin(novaSenha);
    if (senhaSanitizada.length < 8) {
      definirErro('A nova senha deve ter ao menos 8 caracteres.');
      return;
    }
    if (senhaSanitizada !== sanitizarSenhaLogin(confirmacao)) {
      definirErro('A confirmação não corresponde à nova senha.');
      return;
    }

    definirEnviando(true);
    try {
      await redefinirSenhaComToken(token, senhaSanitizada);
      definirConcluido(true);
    } catch (erroRequisicao) {
      definirErro(obterMensagemDeErro(erroRequisicao));
    } finally {
      definirEnviando(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100 px-4 py-12">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="text-2xl font-semibold text-gray-900">Redefinir senha</h1>

        {concluido ? (
          <div className="mt-6 space-y-4">
            <div className="rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">
              Senha redefinida com sucesso. Você já pode acessar o painel com a nova senha.
            </div>
            <button
              type="button"
              onClick={() => navegador('/painel')}
              className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white shadow-lg transition hover:bg-blue-700"
            >
              Ir para o login
            </button>
          </div>
        ) : (
          <>
            <p className="mt-2 text-sm text-gray-600">Crie uma nova senha para sua conta.</p>
            <form onSubmit={aoSubmeter} className="mt-6 space-y-4">
              <div>
                <label htmlFor="nova-senha" className="block text-sm font-medium text-gray-700">
                  Nova senha (mín. 8 caracteres)
                </label>
                <input
                  id="nova-senha"
                  type="password"
                  autoComplete="new-password"
                  value={novaSenha}
                  onChange={(evento) => definirNovaSenha(evento.target.value)}
                  className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                  required
                />
              </div>

              <div>
                <label htmlFor="confirmar-senha" className="block text-sm font-medium text-gray-700">
                  Confirmar nova senha
                </label>
                <input
                  id="confirmar-senha"
                  type="password"
                  autoComplete="new-password"
                  value={confirmacao}
                  onChange={(evento) => definirConfirmacao(evento.target.value)}
                  className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-900 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                  required
                />
              </div>

              {erro && (
                <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700" role="alert">
                  {erro}
                </div>
              )}

              <button
                type="submit"
                disabled={enviando}
                className="flex w-full items-center justify-center rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white shadow-lg transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {enviando ? 'Salvando...' : 'Redefinir senha'}
              </button>
            </form>
          </>
        )}
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
  }
  return 'Não foi possível redefinir a senha. O link pode ter expirado.';
}
