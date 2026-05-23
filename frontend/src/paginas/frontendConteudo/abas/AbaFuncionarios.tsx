import { ChangeEvent, FormEvent, useMemo, useRef, useState } from 'react';
import DOMPurify from 'dompurify';
import { useFuncionarios } from '../../../hooks/useFuncionarios';
import { useLocais } from '../../../hooks/useLocais';
import { Funcionario, ResultadoImportacao } from '../../../servicos/funcionariosServico';
import { TipoNotificacao } from '../../../types/enums';

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

export default function AbaFuncionarios({ exibirNotificacao }: Props) {
  const [nome, definirNome] = useState('');
  const [celular, definirCelular] = useState('');
  const [localId, definirLocalId] = useState<string>('');

  const [editandoId, definirEditandoId] = useState<number | null>(null);
  const [editNome, definirEditNome] = useState('');
  const [editCelular, definirEditCelular] = useState('');
  const [editLocalId, definirEditLocalId] = useState<string>('');

  const [resultadoImportacao, definirResultadoImportacao] = useState<ResultadoImportacao | null>(null);
  const referenciaArquivo = useRef<HTMLInputElement>(null);

  const { consultaFuncionarios, mutacaoCriar, mutacaoAtualizar, mutacaoRemover, mutacaoImportar } = useFuncionarios({
    onSuccessSave: () => {
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Funcionário cadastrado.' });
      definirNome('');
      definirCelular('');
      definirLocalId('');
    },
    onErrorSave: (mensagem) => exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem }),
    onSuccessUpdate: () => {
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Funcionário atualizado.' });
      definirEditandoId(null);
    },
    onErrorUpdate: (mensagem) => exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem }),
    onSuccessRemove: () => exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Funcionário removido.' }),
    onErrorRemove: (mensagem) => exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem }),
    onSuccessImport: (resultado) => {
      definirResultadoImportacao(resultado);
      exibirNotificacao({
        tipo: resultado.importados > 0 ? TipoNotificacao.SUCESSO : TipoNotificacao.ERRO,
        mensagem: `Importação concluída: ${resultado.importados} cadastrado(s), ${resultado.erros.length} erro(s).`,
      });
      if (referenciaArquivo.current) {
        referenciaArquivo.current.value = '';
      }
    },
    onErrorImport: (mensagem) => exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem }),
  });

  const { consultaLocaisTrabalho } = useLocais();

  const funcionariosOrdenados = useMemo(
    () =>
      (consultaFuncionarios.data ?? [])
        .slice()
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' })),
    [consultaFuncionarios.data],
  );

  const locaisDisponiveis = useMemo(
    () =>
      (consultaLocaisTrabalho.data ?? [])
        .slice()
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' })),
    [consultaLocaisTrabalho.data],
  );

  function aoCadastrar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();

    const nomeLimpo = DOMPurify.sanitize(nome, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
    const celularApenasDigitos = celular.replace(/\D/g, '');
    const localIdNumero = Number.parseInt(localId, 10);

    if (!nomeLimpo || celularApenasDigitos.length < 10 || Number.isNaN(localIdNumero)) {
      exibirNotificacao({
        tipo: TipoNotificacao.ERRO,
        mensagem: 'Preencha todos os campos corretamente antes de salvar.',
      });
      return;
    }

    mutacaoCriar.mutate({ nome: nomeLimpo, celular: celularApenasDigitos, localTrabalhoId: localIdNumero });
  }

  function iniciarEdicao(funcionario: Funcionario) {
    definirEditandoId(funcionario.id);
    definirEditNome(funcionario.nome);
    definirEditCelular(funcionario.celular);
    definirEditLocalId(funcionario.localTrabalhoId.toString());
    definirResultadoImportacao(null);
  }

  function salvarEdicao(id: number) {
    const nomeLimpo = DOMPurify.sanitize(editNome, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
    const celularApenasDigitos = editCelular.replace(/\D/g, '');
    const localIdNumero = Number.parseInt(editLocalId, 10);

    if (!nomeLimpo || celularApenasDigitos.length < 10 || Number.isNaN(localIdNumero)) {
      exibirNotificacao({
        tipo: TipoNotificacao.ERRO,
        mensagem: 'Preencha todos os campos corretamente antes de salvar.',
      });
      return;
    }

    mutacaoAtualizar.mutate({
      id,
      dados: { nome: nomeLimpo, celular: celularApenasDigitos, localTrabalhoId: localIdNumero },
    });
  }

  function aoSelecionarArquivo(evento: ChangeEvent<HTMLInputElement>) {
    const arquivo = evento.target.files?.[0];
    if (!arquivo) {
      return;
    }
    definirResultadoImportacao(null);
    mutacaoImportar.mutate(arquivo);
  }

  return (
    <div className="grid gap-6 md:grid-cols-[1fr,2fr]">
      <div className="space-y-6">
        <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900">Cadastrar funcionário</h3>
          <form className="space-y-4" onSubmit={aoCadastrar}>
            <div className="space-y-2">
              <label htmlFor="nome" className="text-sm font-medium text-gray-700">
                Nome completo
              </label>
              <input
                id="nome"
                type="text"
                value={nome}
                onChange={(evento) => definirNome(evento.target.value)}
                className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                required
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="celular" className="text-sm font-medium text-gray-700">
                Celular
              </label>
              <input
                id="celular"
                type="tel"
                value={celular}
                onChange={(evento) => definirCelular(evento.target.value)}
                placeholder="(11) 90000-0000"
                className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                required
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="local" className="text-sm font-medium text-gray-700">
                Local de trabalho
              </label>
              <select
                id="local"
                value={localId}
                onChange={(evento) => definirLocalId(evento.target.value)}
                className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                required
              >
                <option value="">Selecione um local</option>
                {locaisDisponiveis.map((local) => (
                  <option key={local.id} value={local.id.toString()}>
                    {local.nome} ({local.tipoLocalNome})
                  </option>
                ))}
              </select>
              {consultaLocaisTrabalho.isError && (
                <p className="text-sm text-red-600">Não foi possível carregar os locais de trabalho.</p>
              )}
            </div>

            <button
              type="submit"
              className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
              disabled={mutacaoCriar.isPending}
            >
              {mutacaoCriar.isPending ? 'Salvando...' : 'Salvar funcionário'}
            </button>
          </form>
        </section>

        <section className="space-y-3 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-gray-900">Importar via CSV</h3>
          <p className="text-xs text-gray-500">
            Arquivo com colunas <strong>nome;celular;local</strong> (uma linha por funcionário). O local deve
            corresponder a um local de trabalho já cadastrado.
          </p>
          <input
            ref={referenciaArquivo}
            type="file"
            accept=".csv,text/csv"
            onChange={aoSelecionarArquivo}
            disabled={mutacaoImportar.isPending}
            className="block w-full text-sm text-gray-600 file:mr-3 file:rounded-lg file:border-0 file:bg-blue-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-blue-700 hover:file:bg-blue-100"
          />
          {mutacaoImportar.isPending && <p className="text-sm text-gray-500">Importando...</p>}
          {resultadoImportacao && (
            <div className="rounded-lg border border-gray-100 bg-gray-50 p-3 text-xs text-gray-700">
              <p className="font-semibold text-gray-900">
                {resultadoImportacao.importados} de {resultadoImportacao.totalLinhas} linha(s) importada(s).
              </p>
              {resultadoImportacao.erros.length > 0 && (
                <ul className="mt-2 max-h-40 space-y-1 overflow-y-auto">
                  {resultadoImportacao.erros.map((erro) => (
                    <li key={erro.linha} className="text-red-600">
                      Linha {erro.linha}: {erro.motivo}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </section>
      </div>

      <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <header className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900">Funcionários cadastrados</h3>
          <span className="text-sm text-gray-500">{funcionariosOrdenados.length} registro(s)</span>
        </header>

        {consultaFuncionarios.isLoading ? (
          <p className="text-sm text-gray-500">Carregando funcionários...</p>
        ) : consultaFuncionarios.isError ? (
          <p className="text-sm text-red-600">Não foi possível carregar os funcionários.</p>
        ) : funcionariosOrdenados.length === 0 ? (
          <p className="text-sm text-gray-500">Nenhum funcionário cadastrado até o momento.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 text-sm">
              <thead className="bg-gray-50 text-left font-semibold uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-4 py-3">Nome</th>
                  <th className="px-4 py-3">Contato</th>
                  <th className="px-4 py-3">Local</th>
                  <th className="px-4 py-3 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 bg-white text-gray-700">
                {funcionariosOrdenados.map((funcionario) =>
                  editandoId === funcionario.id ? (
                    <tr key={funcionario.id} className="bg-blue-50/40">
                      <td className="px-4 py-3">
                        <input
                          type="text"
                          value={editNome}
                          onChange={(evento) => definirEditNome(evento.target.value)}
                          className="w-full rounded-lg border border-gray-300 px-2 py-1 text-sm focus:border-blue-500 focus:outline-none"
                        />
                      </td>
                      <td className="px-4 py-3">
                        <input
                          type="tel"
                          value={editCelular}
                          onChange={(evento) => definirEditCelular(evento.target.value)}
                          className="w-full rounded-lg border border-gray-300 px-2 py-1 text-sm focus:border-blue-500 focus:outline-none"
                        />
                      </td>
                      <td className="px-4 py-3">
                        <select
                          value={editLocalId}
                          onChange={(evento) => definirEditLocalId(evento.target.value)}
                          className="w-full rounded-lg border border-gray-300 px-2 py-1 text-sm focus:border-blue-500 focus:outline-none"
                        >
                          {locaisDisponiveis.map((local) => (
                            <option key={local.id} value={local.id.toString()}>
                              {local.nome} ({local.tipoLocalNome})
                            </option>
                          ))}
                        </select>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex items-center justify-end gap-3">
                          <button
                            type="button"
                            className="text-sm font-semibold text-blue-600 hover:text-blue-700"
                            onClick={() => salvarEdicao(funcionario.id)}
                            disabled={mutacaoAtualizar.isPending}
                          >
                            Salvar
                          </button>
                          <button
                            type="button"
                            className="text-sm text-gray-500 hover:text-gray-700"
                            onClick={() => definirEditandoId(null)}
                          >
                            Cancelar
                          </button>
                        </div>
                      </td>
                    </tr>
                  ) : (
                    <tr key={funcionario.id}>
                      <td className="px-4 py-3 font-medium">{funcionario.nome}</td>
                      <td className="px-4 py-3">{formatarCelular(funcionario.celular)}</td>
                      <td className="px-4 py-3">
                        <span className="block text-sm font-medium text-gray-900">{funcionario.localTrabalhoNome}</span>
                        <span className="text-xs text-gray-500">{funcionario.tipoLocalNome}</span>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex items-center justify-end gap-3">
                          <button
                            type="button"
                            className="text-sm font-semibold text-blue-600 hover:text-blue-700"
                            onClick={() => iniciarEdicao(funcionario)}
                          >
                            Editar
                          </button>
                          <button
                            type="button"
                            className="text-sm font-semibold text-red-600 hover:text-red-700"
                            onClick={() => mutacaoRemover.mutate(funcionario.id)}
                            disabled={mutacaoRemover.isPending}
                          >
                            Remover
                          </button>
                        </div>
                      </td>
                    </tr>
                  ),
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

function formatarCelular(valor: string) {
  const digitos = valor.replace(/\D/g, '');
  if (digitos.length === 11) {
    return digitos.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
  }
  if (digitos.length === 10) {
    return digitos.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
  }
  return digitos;
}
