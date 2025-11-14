import { FormEvent, useMemo, useState } from 'react';
import DOMPurify from 'dompurify';
import { useLocalAdmin } from '../../../hooks/useLocalAdmin';
import { TipoNotificacao } from '../../../types/enums';

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

export default function AbaLocais({ exibirNotificacao }: Props) {
  const [nome, definirNome] = useState('');
  const [tipoSelecionado, definirTipoSelecionado] = useState<string>('');
  const [editandoId, definirEditandoId] = useState<number | null>(null);
  const [nomeEdicao, definirNomeEdicao] = useState('');
  const [tipoEdicao, definirTipoEdicao] = useState<string>('');

  const {
    consultaLocais,
    consultaTipos,
    mutacaoCriarLocal,
    mutacaoRemoverLocal,
    mutacaoAtualizarLocal,
  } = useLocalAdmin();

  const tiposOrdenados = useMemo(
    () =>
      (consultaTipos.data ?? [])
        .slice()
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' })),
    [consultaTipos.data],
  );

  const locaisOrdenados = useMemo(
    () =>
      (consultaLocais.data ?? [])
        .slice()
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' })),
    [consultaLocais.data],
  );

  function aoCadastrar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const nomeSanitizado = DOMPurify.sanitize(nome, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
    const tipoId = Number.parseInt(tipoSelecionado, 10);

    if (!nomeSanitizado || Number.isNaN(tipoId)) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Informe nome e tipo para o local.' });
      return;
    }

    mutacaoCriarLocal.mutate(
      { nome: nomeSanitizado, tipoLocalId: tipoId },
      {
        onSuccess: () => {
          exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Local cadastrado com sucesso.' });
          definirNome('');
          definirTipoSelecionado('');
        },
        onError: () => {
          exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Erro ao cadastrar local.' });
        },
      },
    );
  }

  function iniciarEdicao(id: number, nomeAtual: string, tipoAtualId: number) {
    definirEditandoId(id);
    definirNomeEdicao(nomeAtual);
    definirTipoEdicao(tipoAtualId.toString());
  }

  function salvarEdicao(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (editandoId === null) {
      return;
    }
    const nomeSanitizado = DOMPurify.sanitize(nomeEdicao, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
    const tipoId = Number.parseInt(tipoEdicao, 10);

    if (!nomeSanitizado || Number.isNaN(tipoId)) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Informe nome e tipo válidos.' });
      return;
    }

    mutacaoAtualizarLocal?.mutate(
      { id: editandoId, dados: { nome: nomeSanitizado, tipoLocalId: tipoId } },
      {
        onSuccess: () => {
          exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Local atualizado com sucesso.' });
          definirEditandoId(null);
          definirNomeEdicao('');
          definirTipoEdicao('');
        },
        onError: () => {
          exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Erro ao atualizar local.' });
        },
      },
    );
  }

  function remover(id: number) {
    mutacaoRemoverLocal.mutate(id, {
      onSuccess: () => {
        exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Local removido.' });
      },
      onError: () => {
        exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Erro ao remover local.' });
      },
    });
  }

  return (
    <div className="grid gap-6 md:grid-cols-[1fr,1fr]">
      <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900">Cadastrar local</h3>
        <form className="space-y-4" onSubmit={aoCadastrar}>
          <div className="space-y-2">
            <label htmlFor="nome-local" className="text-sm font-medium text-gray-700">
              Nome
            </label>
            <input
              id="nome-local"
              type="text"
              value={nome}
              onChange={(evento) => definirNome(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>
          <div className="space-y-2">
            <label htmlFor="tipo-local" className="text-sm font-medium text-gray-700">
              Tipo de local
            </label>
            <select
              id="tipo-local"
              value={tipoSelecionado}
              onChange={(evento) => definirTipoSelecionado(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            >
              <option value="">Selecione um tipo</option>
              {tiposOrdenados.map((tipo) => (
                <option key={tipo.id} value={tipo.id.toString()}>
                  {tipo.nome}
                </option>
              ))}
            </select>
          </div>
          <button
            type="submit"
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
            disabled={mutacaoCriarLocal.isPending}
          >
            {mutacaoCriarLocal.isPending ? 'Salvando...' : 'Salvar local'}
          </button>
        </form>
      </section>

      <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900">Locais cadastrados</h3>
        {consultaLocais.isLoading ? (
          <p className="mt-3 text-sm text-gray-500">Carregando locais de trabalho...</p>
        ) : consultaLocais.isError ? (
          <p className="mt-3 text-sm text-red-600">Não foi possível carregar os locais de trabalho.</p>
        ) : locaisOrdenados.length === 0 ? (
          <p className="mt-3 text-sm text-gray-500">Nenhum local cadastrado até o momento.</p>
        ) : (
          <ul className="mt-4 space-y-3">
            {locaisOrdenados.map((local) => (
              <li key={local.id} className="rounded-lg border border-gray-100 p-4 shadow-sm">
                {editandoId === local.id ? (
                  <form className="flex flex-col gap-3" onSubmit={salvarEdicao}>
                    <input
                      type="text"
                      value={nomeEdicao}
                      onChange={(evento) => definirNomeEdicao(evento.target.value)}
                      className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                      required
                    />
                    <select
                      value={tipoEdicao}
                      onChange={(evento) => definirTipoEdicao(evento.target.value)}
                      className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                      required
                    >
                      <option value="">Selecione um tipo</option>
                      {tiposOrdenados.map((tipo) => (
                        <option key={tipo.id} value={tipo.id.toString()}>
                          {tipo.nome}
                        </option>
                      ))}
                    </select>
                    <div className="flex gap-2">
                      <button
                        type="submit"
                        className="rounded-lg bg-blue-600 px-3 py-2 text-sm font-semibold text-white hover:bg-blue-700"
                        disabled={mutacaoAtualizarLocal?.isPending}
                      >
                        Salvar
                      </button>
                      <button
                        type="button"
                        className="rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                        onClick={() => {
                          definirEditandoId(null);
                          definirNomeEdicao('');
                          definirTipoEdicao('');
                        }}
                      >
                        Cancelar
                      </button>
                    </div>
                  </form>
                ) : (
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium text-gray-900">{local.nome}</p>
                      <p className="text-xs text-gray-500">{local.tipoLocalNome}</p>
                    </div>
                    <div className="flex gap-3 text-sm font-semibold">
                      <button
                        type="button"
                        className="text-blue-600 hover:text-blue-700"
                        onClick={() => iniciarEdicao(local.id, local.nome, local.tipoLocalId)}
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        className="text-red-600 hover:text-red-700"
                        onClick={() => remover(local.id)}
                        disabled={mutacaoRemoverLocal.isPending}
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
