import { FormEvent, useState } from 'react';
import { alterarMinhaSenha } from '../servicos/usuariosServico';
import { TipoNotificacao } from '../types/enums';

type Props = {
  aoFechar: () => void;
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

function extrairMensagem(erro: unknown, padrao: string): string {
  const dados = (erro as { response?: { data?: { mensagem?: string } } })?.response?.data;
  return dados?.mensagem ?? padrao;
}

export default function ModalAlterarSenha({ aoFechar, exibirNotificacao }: Props) {
  const [senhaAtual, definirSenhaAtual] = useState('');
  const [novaSenha, definirNovaSenha] = useState('');
  const [confirmacao, definirConfirmacao] = useState('');
  const [salvando, definirSalvando] = useState(false);

  async function aoSubmeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();

    if (novaSenha.length < 8) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'A nova senha deve ter ao menos 8 caracteres.' });
      return;
    }
    if (novaSenha !== confirmacao) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'A confirmação não corresponde à nova senha.' });
      return;
    }

    definirSalvando(true);
    try {
      await alterarMinhaSenha(senhaAtual, novaSenha);
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Senha alterada com sucesso.' });
      aoFechar();
    } catch (erro) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: extrairMensagem(erro, 'Não foi possível alterar a senha.') });
    } finally {
      definirSalvando(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={aoFechar}>
      <div
        className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl"
        onClick={(evento) => evento.stopPropagation()}
      >
        <h3 className="text-lg font-semibold text-gray-900">Alterar minha senha</h3>
        <form className="mt-4 space-y-4" onSubmit={aoSubmeter}>
          <div className="space-y-2">
            <label htmlFor="senha-atual" className="text-sm font-medium text-gray-700">
              Senha atual
            </label>
            <input
              id="senha-atual"
              type="password"
              value={senhaAtual}
              onChange={(evento) => definirSenhaAtual(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>
          <div className="space-y-2">
            <label htmlFor="nova-senha" className="text-sm font-medium text-gray-700">
              Nova senha (mín. 8 caracteres)
            </label>
            <input
              id="nova-senha"
              type="password"
              value={novaSenha}
              onChange={(evento) => definirNovaSenha(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>
          <div className="space-y-2">
            <label htmlFor="confirmar-senha" className="text-sm font-medium text-gray-700">
              Confirmar nova senha
            </label>
            <input
              id="confirmar-senha"
              type="password"
              value={confirmacao}
              onChange={(evento) => definirConfirmacao(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>
          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={aoFechar}
              className="rounded-lg px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-800"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={salvando}
              className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
            >
              {salvando ? 'Salvando...' : 'Alterar senha'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
