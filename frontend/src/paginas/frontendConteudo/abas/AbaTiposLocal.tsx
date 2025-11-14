import { FormEvent, useMemo, useState } from 'react';
import DOMPurify from 'dompurify';
import { useLocalAdmin } from '../../../hooks/useLocalAdmin';
import { TipoNotificacao } from '../../../types/enums';

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

export default function AbaTiposLocal({ exibirNotificacao }: Props) {
  const [novoNome, definirNovoNome] = useState('');
  const [editandoId, definirEditandoId] = useState<number | null>(null);
  const [nomeEdicao, definirNomeEdicao] = useState('');

  const { consultaTipos, mutacaoCriarTipo, mutacaoRemoverTipo, mutacaoAtualizarTipo } = useLocalAdmin();

  const tiposOrdenados = useMemo(
    () =>
      (consultaTipos.data ?? [])
        .slice()
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' })),
    [consultaTipos.data],
  );

  function aoCadastrar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const nomeSanitizado = DOMPurify.sanitize(novoNome, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
    if (!nomeSanitizado) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Informe um nome válido para o tipo de local.' });
      return;
    }

    mutacaoCriarTipo.mutate(nomeSanitizado, {
      onSuccess: () => {
        exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Tipo de local cadastrado.' });
        definirNovoNome('');
      },
      onError: () => {
        exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Erro ao cadastrar tipo de local.' });
      },
    });
  }

  function iniciarEdicao(id: number, nomeAtual: string) {
    definirEditandoId(id);
    definirNomeEdicao(nomeAtual);
  }

  function salvarEdicao(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (editandoId === null) {
      return;
    }
    const nomeSanitizado = DOMPurify.sanitize(nomeEdicao, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
    if (!nomeSanitizado) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Informe um nome válido para atualizar.' });
      return;
    }

    mutacaoAtualizarTipo?.mutate(
      { id: editandoId, nome: nomeSanitizado },
      {
        onSuccess: () => {
          exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Tipo de local atualizado.' });
          definirEditandoId(null);
          definirNomeEdicao('');
        },
        onError: () => {
          exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Erro ao atualizar tipo de local.' });
        },
      },
    );
  }

  function remover(id: number) {
    mutacaoRemoverTipo.mutate(id, {
      onSuccess: () => {
        exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Tipo de local removido.' });
      },
      onError: () => {
        exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Erro ao remover tipo de local.' });
      },
    });
  }

  return (
    <div className="grid gap-6 md:grid-cols-[1fr,1fr]">
      <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900">Cadastrar tipo de local</h3>
        <form className="space-y-4" onSubmit={aoCadastrar}>
          <div className="space-y-2">
            <label htmlFor="novo-tipo" className="text-sm font-medium text-gray-700">
              Nome do tipo
            </label>
            <input
              id="novo-tipo"
              type="text"
              value={novoNome}
              onChange={(evento) => definirNovoNome(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>
          <button
            type="submit"
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
            disabled={mutacaoCriarTipo.isPending}
          >
            {mutacaoCriarTipo.isPending ? 'Salvando...' : 'Salvar tipo'}
          </button>
        </form>
      </section>

      <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900">Tipos cadastrados</h3>
        {consultaTipos.isLoading ? (
          <p className="mt-3 text-sm text-gray-500">Carregando tipos de local...</p>
        ) : consultaTipos.isError ? (
          <p className="mt-3 text-sm text-red-600">Não foi possível carregar os tipos de local.</p>
        ) : tiposOrdenados.length === 0 ? (
          <p className="mt-3 text-sm text-gray-500">Nenhum tipo cadastrado até o momento.</p>
        ) : (
          <ul className="mt-4 space-y-3">
            {tiposOrdenados.map((tipoLocal) => (
              <li key={tipoLocal.id} className="rounded-lg border border-gray-100 p-4 shadow-sm">
                {editandoId === tipoLocal.id ? (
                  <form className="flex flex-col gap-3" onSubmit={salvarEdicao}>
                    <input
                      type="text"
                      value={nomeEdicao}
                      onChange={(evento) => definirNomeEdicao(evento.target.value)}
                      className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                      required
                    />
                    <div className="flex gap-2">
                      <button
                        type="submit"
                        className="rounded-lg bg-blue-600 px-3 py-2 text-sm font-semibold text-white hover:bg-blue-700"
                        disabled={mutacaoAtualizarTipo?.isPending}
                      >
                        Salvar
                      </button>
                      <button
                        type="button"
                        className="rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                        onClick={() => {
                          definirEditandoId(null);
                          definirNomeEdicao('');
                        }}
                      >
                        Cancelar
                      </button>
                    </div>
                  </form>
                ) : (
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium text-gray-900">{tipoLocal.nome}</p>
                    </div>
                    <div className="flex gap-3 text-sm font-semibold">
                      <button
                        type="button"
                        className="text-blue-600 hover:text-blue-700"
                        onClick={() => iniciarEdicao(tipoLocal.id, tipoLocal.nome)}
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        className="text-red-600 hover:text-red-700"
                        onClick={() => remover(tipoLocal.id)}
                        disabled={mutacaoRemoverTipo.isPending}
                      >
                        Remover
                      </button>
                    </div>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
