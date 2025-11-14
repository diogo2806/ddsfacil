import { FormEvent, useMemo, useState } from 'react';
import DOMPurify from 'dompurify';
import { useConteudos } from '../../../hooks/useConteudos';
import { useFuncionarios } from '../../../hooks/useFuncionarios';
import { useEnvios } from '../../../hooks/useEnvios';
import { TipoNotificacao } from '../../../types/enums';

const dataAtual = () => new Date().toISOString().split('T')[0];

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

export default function AbaEnviarDds({ exibirNotificacao }: Props) {
  const [conteudoId, definirConteudoId] = useState<string>('');
  const [dataEnvio, definirDataEnvio] = useState<string>(dataAtual());
  const [funcionariosSelecionados, definirFuncionariosSelecionados] = useState<number[]>([]);

  const { consultaConteudos } = useConteudos();
  const { consultaFuncionarios } = useFuncionarios();
  const { mutacaoCriar } = useEnvios({
    onSuccessSave: () => {
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Envio criado com sucesso.' });
      definirFuncionariosSelecionados([]);
      definirConteudoId('');
      definirDataEnvio(dataAtual());
    },
    onErrorSave: () => {
      exibirNotificacao({
        tipo: TipoNotificacao.ERRO,
        mensagem: 'Não foi possível criar o envio. Tente novamente.',
      });
    },
  });

  const funcionariosOrdenados = useMemo(
    () =>
      (consultaFuncionarios.data ?? [])
        .slice()
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' })),
    [consultaFuncionarios.data],
  );

  const conteudos = useMemo(() => consultaConteudos.data ?? [], [consultaConteudos.data]);

  function alternarFuncionario(funcionarioId: number) {
    definirFuncionariosSelecionados((atual) =>
      atual.includes(funcionarioId)
        ? atual.filter((id) => id !== funcionarioId)
        : [...atual, funcionarioId],
    );
  }

  function aoSubmeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();

    if (!conteudoId) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Selecione um conteúdo antes de enviar.' });
      return;
    }
    if (funcionariosSelecionados.length === 0) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Selecione ao menos um funcionário.' });
      return;
    }

    const dataLimpa = DOMPurify.sanitize(dataEnvio, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).replace(/[^0-9-]/g, '');

    mutacaoCriar.mutate({
      conteudoId: Number.parseInt(conteudoId, 10),
      funcionariosIds: funcionariosSelecionados,
      dataEnvio: dataLimpa || undefined,
    });
  }

  return (
    <form onSubmit={aoSubmeter} className="grid gap-6 md:grid-cols-[2fr,3fr]">
      <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900">Configurar envio</h3>

        <div className="space-y-2">
          <label htmlFor="conteudo" className="text-sm font-medium text-gray-700">
            Conteúdo do DDS
          </label>
          <select
            id="conteudo"
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            value={conteudoId}
            onChange={(evento) => definirConteudoId(evento.target.value)}
            disabled={consultaConteudos.isLoading}
            required
          >
            <option value="">Selecione um conteúdo</option>
            {conteudos.map((conteudo) => (
              <option key={conteudo.id} value={conteudo.id.toString()}>
                {conteudo.titulo}
              </option>
            ))}
          </select>
          {consultaConteudos.isError && (
            <p className="text-sm text-red-600">Não foi possível carregar os conteúdos. Tente novamente.</p>
          )}
        </div>

        <div className="space-y-2">
          <label htmlFor="dataEnvio" className="text-sm font-medium text-gray-700">
            Data de envio
          </label>
          <input
            id="dataEnvio"
            type="date"
            value={dataEnvio}
            onChange={(evento) => definirDataEnvio(evento.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>

        <button
          type="submit"
          className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
          disabled={mutacaoCriar.isPending}
        >
          {mutacaoCriar.isPending ? 'Enviando...' : 'Enviar DDS'}
        </button>
      </section>

      <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900">Funcionários</h3>
          <span className="text-sm text-gray-500">
            {funcionariosSelecionados.length} selecionado{funcionariosSelecionados.length === 1 ? '' : 's'}
          </span>
        </div>

        {consultaFuncionarios.isLoading ? (
          <p className="text-sm text-gray-500">Carregando funcionários...</p>
        ) : consultaFuncionarios.isError ? (
          <p className="text-sm text-red-600">Não foi possível carregar os funcionários.</p>
        ) : funcionariosOrdenados.length === 0 ? (
          <p className="text-sm text-gray-500">Cadastre funcionários para realizar envios.</p>
        ) : (
          <ul className="max-h-80 space-y-2 overflow-y-auto pr-2 text-sm">
            {funcionariosOrdenados.map((funcionario) => (
              <li key={funcionario.id} className="flex items-start gap-3 rounded-lg border border-gray-100 p-3">
                <input
                  id={`func-${funcionario.id}`}
                  type="checkbox"
                  className="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  checked={funcionariosSelecionados.includes(funcionario.id)}
                  onChange={() => alternarFuncionario(funcionario.id)}
                />
                <label htmlFor={`func-${funcionario.id}`} className="flex-1 cursor-pointer">
                  <p className="font-medium text-gray-900">{funcionario.nome}</p>
                  <p className="text-xs text-gray-500">
                    {funcionario.tipoLocalNome} · {funcionario.localTrabalhoNome}
                  </p>
                  <p className="text-xs text-gray-500">{formatarCelular(funcionario.celular)}</p>
                </label>
              </li>
            ))}
          </ul>
        )}
      </section>
    </form>
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
