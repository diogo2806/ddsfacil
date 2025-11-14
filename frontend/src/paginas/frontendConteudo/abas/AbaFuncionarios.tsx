import { FormEvent, useMemo, useState } from 'react';
import DOMPurify from 'dompurify';
import { useFuncionarios } from '../../../hooks/useFuncionarios';
import { useLocais } from '../../../hooks/useLocais';
import { TipoNotificacao } from '../../../types/enums';

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

export default function AbaFuncionarios({ exibirNotificacao }: Props) {
  const [nome, definirNome] = useState('');
  const [celular, definirCelular] = useState('');
  const [localId, definirLocalId] = useState<string>('');

  const { consultaFuncionarios, mutacaoCriar, mutacaoRemover } = useFuncionarios({
    onSuccessSave: () => {
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Funcionário cadastrado.' });
      definirNome('');
      definirCelular('');
      definirLocalId('');
    },
    onErrorSave: () => {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Erro ao cadastrar funcionário.' });
    },
    onSuccessRemove: () => {
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Funcionário removido.' });
    },
    onErrorRemove: () => {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Erro ao remover funcionário.' });
    },
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

  function removerFuncionario(id: number) {
    mutacaoRemover.mutate(id);
  }

  return (
    <div className="grid gap-6 md:grid-cols-[1fr,2fr]">
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
                {funcionariosOrdenados.map((funcionario) => (
                  <tr key={funcionario.id}>
                    <td className="px-4 py-3 font-medium">{funcionario.nome}</td>
                    <td className="px-4 py-3">{formatarCelular(funcionario.celular)}</td>
                    <td className="px-4 py-3">
                      <span className="block text-sm font-medium text-gray-900">{funcionario.localTrabalhoNome}</span>
                      <span className="text-xs text-gray-500">{funcionario.tipoLocalNome}</span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        type="button"
                        className="text-sm font-semibold text-red-600 hover:text-red-700"
                        onClick={() => removerFuncionario(funcionario.id)}
                        disabled={mutacaoRemover.isPending}
                      >
                        Remover
                      </button>
                    </td>
                  </tr>
                ))}
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
