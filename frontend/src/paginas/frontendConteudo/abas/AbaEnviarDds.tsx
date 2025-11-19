import { ChangeEvent, FormEvent, useEffect, useMemo, useState } from 'react';
import DOMPurify from 'dompurify';
import { useConteudos } from '../../../hooks/useConteudos';
import { useFuncionarios } from '../../../hooks/useFuncionarios';
import { useEnvios } from '../../../hooks/useEnvios';
import { useLocais } from '../../../hooks/useLocais';
import { useTiposLocal } from '../../../hooks/useTiposLocal';
import { StatusEnvio, TipoNotificacao } from '../../../types/enums';

const dataAtual = () => new Date().toISOString().split('T')[0];

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

export default function AbaEnviarDds({ exibirNotificacao }: Props) {
  const [conteudoId, definirConteudoId] = useState<string>('');
  const [dataEnvio, definirDataEnvio] = useState<string>(dataAtual());
  const [funcionariosSelecionados, definirFuncionariosSelecionados] = useState<number[]>([]);
  const [opcaoObra, definirOpcaoObra] = useState<string>('');
  const [opcaoTipo, definirOpcaoTipo] = useState<string>('');
  const [mensagemSelecao, definirMensagemSelecao] = useState<string>('');

  const { consultaConteudos } = useConteudos();
  const { consultaFuncionarios } = useFuncionarios();
  const { consultaLocaisTrabalho } = useLocais();
  const { consultaTiposLocal } = useTiposLocal();
  const { consultaEnvios, mutacaoCriar } = useEnvios({
    onSuccessSave: () => {
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Envio criado com sucesso.' });
      definirFuncionariosSelecionados([]);
      definirConteudoId('');
      definirDataEnvio(dataAtual());
      definirMensagemSelecao('');
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
  const locaisTrabalho = useMemo(() => consultaLocaisTrabalho.data ?? [], [consultaLocaisTrabalho.data]);
  const tiposLocal = useMemo(() => consultaTiposLocal.data ?? [], [consultaTiposLocal.data]);
  const funcionariosPendentesNoDia = useMemo(() => {
    const envios = consultaEnvios.data ?? [];
    const pendentes = envios.filter(
      (envio) => envio.status === StatusEnvio.PENDENTE || envio.status === StatusEnvio.ENVIADO,
    );
    const conjunto = new Set<number>();
    pendentes.forEach((envio) => {
      const idFuncionario = Number(envio.funcionarioId);
      if (!Number.isNaN(idFuncionario)) {
        conjunto.add(idFuncionario);
      }
    });
    return conjunto;
  }, [consultaEnvios.data]);
  const quantidadePendentesDisponiveis = useMemo(
    () => funcionariosOrdenados.filter((funcionario) => funcionariosPendentesNoDia.has(funcionario.id)).length,
    [funcionariosOrdenados, funcionariosPendentesNoDia],
  );

  useEffect(() => {
    definirFuncionariosSelecionados((atual) => {
      const idsValidos = new Set(funcionariosOrdenados.map((funcionario) => funcionario.id));
      return atual.filter((id) => idsValidos.has(id));
    });
  }, [funcionariosOrdenados]);

  function alternarFuncionario(funcionarioId: number) {
    definirFuncionariosSelecionados((atual) =>
      atual.includes(funcionarioId)
        ? atual.filter((id) => id !== funcionarioId)
        : [...atual, funcionarioId],
    );
  }

  function atualizarSelecaoInteligente(ids: number[], descricao: string) {
    if (ids.length === 0) {
      definirFuncionariosSelecionados([]);
      definirMensagemSelecao('Nenhum funcionário disponível para o filtro escolhido.');
      return;
    }

    const conjuntoIds = new Set(ids);
    const idsOrdenados = funcionariosOrdenados
      .filter((funcionario) => conjuntoIds.has(funcionario.id))
      .map((funcionario) => funcionario.id);
    definirFuncionariosSelecionados(idsOrdenados);
    definirMensagemSelecao(descricao);
  }

  function aoSelecionarObra(evento: ChangeEvent<HTMLSelectElement>) {
    const valorLimpo = DOMPurify.sanitize(evento.target.value, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });
    definirOpcaoObra(valorLimpo);

    if (!valorLimpo) {
      return;
    }

    const localId = Number.parseInt(valorLimpo, 10);
    definirOpcaoObra('');
    if (Number.isNaN(localId)) {
      return;
    }

    const ids = funcionariosOrdenados
      .filter((funcionario) => funcionario.localTrabalhoId === localId)
      .map((funcionario) => funcionario.id);
    const localSelecionado = locaisTrabalho.find((local) => local.id === localId);
    const nomeLocalSeguro = DOMPurify.sanitize(localSelecionado?.nome ?? 'local selecionado', {
      ALLOWED_TAGS: [],
      ALLOWED_ATTR: [],
    });
    atualizarSelecaoInteligente(
      ids,
      `Selecionados ${ids.length} funcionário(s) da obra ${nomeLocalSeguro || 'selecionada'}.`,
    );
  }

  function aoSelecionarTipo(evento: ChangeEvent<HTMLSelectElement>) {
    const valorLimpo = DOMPurify.sanitize(evento.target.value, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });
    definirOpcaoTipo(valorLimpo);

    if (!valorLimpo) {
      return;
    }

    const tipoId = Number.parseInt(valorLimpo, 10);
    definirOpcaoTipo('');
    if (Number.isNaN(tipoId)) {
      return;
    }

    const tipoSelecionado = tiposLocal.find((tipo) => tipo.value === tipoId);
    const nomeTipo = tipoSelecionado?.label ?? '';
    const ids = nomeTipo
      ? funcionariosOrdenados
          .filter((funcionario) => funcionario.tipoLocalNome.localeCompare(nomeTipo, 'pt-BR', { sensitivity: 'base' }) === 0)
          .map((funcionario) => funcionario.id)
      : [];
    const nomeTipoSeguro = DOMPurify.sanitize(nomeTipo || 'tipo selecionado', { ALLOWED_TAGS: [], ALLOWED_ATTR: [] });
    atualizarSelecaoInteligente(
      ids,
      `Selecionados ${ids.length} funcionário(s) do tipo ${nomeTipoSeguro || 'informado'}.`,
    );
  }

  function selecionarPendentes() {
    if (consultaEnvios.isError) {
      definirMensagemSelecao('Não foi possível consultar os envios pendentes. Atualize a página.');
      return;
    }

    const ids = funcionariosOrdenados
      .filter((funcionario) => funcionariosPendentesNoDia.has(funcionario.id))
      .map((funcionario) => funcionario.id);
    atualizarSelecaoInteligente(
      ids,
      `Selecionados ${ids.length} funcionário(s) pendente(s) de confirmação no dia.`,
    );
  }

  function limparSelecao() {
    definirFuncionariosSelecionados([]);
    definirMensagemSelecao('Seleção limpa. Escolha um filtro ou marque manualmente.');
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

        <div className="space-y-3 rounded-lg border border-blue-100 bg-blue-50 p-4 text-sm text-blue-900">
          <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
            <p className="font-semibold">Seleção inteligente</p>
            <button
              type="button"
              className="text-xs font-semibold text-blue-700 underline-offset-2 transition hover:underline disabled:text-blue-300"
              onClick={limparSelecao}
              disabled={funcionariosSelecionados.length === 0}
            >
              Limpar seleção
            </button>
          </div>
          <div className="grid gap-3 md:grid-cols-3">
            <select
              className="w-full rounded-lg border border-blue-200 px-3 py-2 text-sm text-gray-900 focus:border-blue-500 focus:outline-none"
              value={opcaoObra}
              onChange={aoSelecionarObra}
              disabled={consultaLocaisTrabalho.isLoading || consultaLocaisTrabalho.isError}
            >
              <option value="">Selecionar todos de uma obra</option>
              {locaisTrabalho.map((local) => (
                <option key={local.id} value={local.id.toString()}>
                  {local.nome}
                </option>
              ))}
            </select>
            <select
              className="w-full rounded-lg border border-blue-200 px-3 py-2 text-sm text-gray-900 focus:border-blue-500 focus:outline-none"
              value={opcaoTipo}
              onChange={aoSelecionarTipo}
              disabled={consultaTiposLocal.isLoading || consultaTiposLocal.isError}
            >
              <option value="">Selecionar todos por tipo</option>
              {tiposLocal.map((tipo) => (
                <option key={tipo.value} value={tipo.value.toString()}>
                  {tipo.label}
                </option>
              ))}
            </select>
            <button
              type="button"
              className="rounded-lg border border-blue-300 bg-white px-3 py-2 text-sm font-semibold text-blue-700 transition hover:bg-blue-100 disabled:cursor-not-allowed disabled:border-blue-100 disabled:text-blue-300"
              onClick={selecionarPendentes}
              disabled={consultaEnvios.isLoading || quantidadePendentesDisponiveis === 0}
            >
              {consultaEnvios.isLoading
                ? 'Carregando pendências...'
                : quantidadePendentesDisponiveis > 0
                  ? `Selecionar pendentes (${quantidadePendentesDisponiveis})`
                  : 'Nenhum pendente disponível'}
            </button>
          </div>
          {consultaLocaisTrabalho.isError && (
            <p className="text-xs text-red-700">Não foi possível carregar as obras. Atualize a página para tentar novamente.</p>
          )}
          {consultaTiposLocal.isError && (
            <p className="text-xs text-red-700">Não foi possível carregar os tipos de local. Recarregue para continuar.</p>
          )}
          {consultaEnvios.isError && (
            <p className="text-xs text-red-700">Não foi possível consultar os envios pendentes.</p>
          )}
          <p className="text-xs text-blue-800">Aplicar um filtro substitui a seleção atual. Ajuste manualmente na lista abaixo.</p>
          {mensagemSelecao && <p className="text-xs font-medium text-blue-900">{mensagemSelecao}</p>}
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
