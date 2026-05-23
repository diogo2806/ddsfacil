import { FormEvent, useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  AtualizacaoLicenca,
  Licenca,
  StatusPagamento,
  atualizarLicenca,
  detalharLicenca,
  iniciarRecargaOnline,
  recarregarCreditos,
} from '../../../servicos/licencaServico';
import { TipoNotificacao } from '../../../types/enums';

const STATUS: StatusPagamento[] = ['EM_DIA', 'INADIMPLENTE', 'SUSPENSO'];
const ROTULO_STATUS: Record<StatusPagamento, string> = {
  EM_DIA: 'Em dia',
  INADIMPLENTE: 'Inadimplente',
  SUSPENSO: 'Suspenso',
};

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

function extrairMensagem(erro: unknown, padrao: string): string {
  const dados = (erro as { response?: { data?: { mensagem?: string } } })?.response?.data;
  return dados?.mensagem ?? padrao;
}

export default function AbaLicenca({ exibirNotificacao }: Props) {
  const clienteConsulta = useQueryClient();
  const [quantidade, definirQuantidade] = useState<string>('100');
  const [tipoPlano, definirTipoPlano] = useState<string>('');
  const [statusPagamento, definirStatusPagamento] = useState<StatusPagamento>('EM_DIA');
  const [dataRenovacao, definirDataRenovacao] = useState<string>('');

  const consultaLicenca = useQuery<Licenca>({
    queryKey: ['licenca'],
    queryFn: detalharLicenca,
  });

  useEffect(() => {
    if (consultaLicenca.data) {
      definirTipoPlano(consultaLicenca.data.tipoPlano ?? '');
      definirStatusPagamento(consultaLicenca.data.statusPagamento ?? 'EM_DIA');
      definirDataRenovacao(consultaLicenca.data.dataRenovacao ?? '');
    }
  }, [consultaLicenca.data]);

  function invalidar() {
    clienteConsulta.invalidateQueries({ queryKey: ['licenca'] });
    clienteConsulta.invalidateQueries({ queryKey: ['saldo-sms'] });
  }

  const mutacaoRecarga = useMutation({
    mutationFn: (qtd: number) => recarregarCreditos(qtd),
    onSuccess: () => {
      invalidar();
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Créditos adicionados com sucesso.' });
    },
    onError: (erro) =>
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: extrairMensagem(erro, 'Erro ao recarregar créditos.') }),
  });

  const mutacaoAtualizar = useMutation({
    mutationFn: (dados: AtualizacaoLicenca) => atualizarLicenca(dados),
    onSuccess: () => {
      invalidar();
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Licença atualizada.' });
    },
    onError: (erro) =>
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: extrairMensagem(erro, 'Erro ao atualizar licença.') }),
  });

  const mutacaoOnline = useMutation({
    mutationFn: (qtd: number) => iniciarRecargaOnline(qtd),
    onSuccess: (resultado) => {
      if (resultado.urlPagamento) {
        window.open(resultado.urlPagamento, '_blank', 'noopener');
      }
      exibirNotificacao({
        tipo: resultado.status === 'INDISPONIVEL' ? TipoNotificacao.ERRO : TipoNotificacao.SUCESSO,
        mensagem: resultado.mensagem,
      });
    },
    onError: (erro) =>
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: extrairMensagem(erro, 'Erro ao iniciar recarga online.') }),
  });

  function aoRecarregar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const qtd = Number.parseInt(quantidade, 10);
    if (Number.isNaN(qtd) || qtd < 1) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Informe uma quantidade válida (mínimo 1).' });
      return;
    }
    mutacaoRecarga.mutate(qtd);
  }

  function aoAtualizar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    mutacaoAtualizar.mutate({
      tipoPlano: tipoPlano.trim(),
      statusPagamento,
      dataRenovacao: dataRenovacao || null,
    });
  }

  if (consultaLicenca.isLoading) {
    return <div className="rounded-lg border border-gray-200 bg-white p-6 text-gray-600">Carregando licença...</div>;
  }

  if (consultaLicenca.isError) {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-red-700">
        Não foi possível carregar os dados da licença.
      </div>
    );
  }

  const licenca = consultaLicenca.data;

  return (
    <div className="grid gap-6 md:grid-cols-2">
      <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm md:col-span-2">
        <h3 className="text-lg font-semibold text-gray-900">Resumo da licença</h3>
        <div className="grid gap-4 sm:grid-cols-4">
          <Indicador titulo="Plano" valor={licenca?.tipoPlano ?? '-'} />
          <Indicador titulo="Saldo de SMS" valor={String(licenca?.saldoSms ?? 0)} destaque />
          <Indicador
            titulo="Status de pagamento"
            valor={licenca ? ROTULO_STATUS[licenca.statusPagamento] : '-'}
            aviso={licenca?.statusPagamento !== 'EM_DIA'}
          />
          <Indicador titulo="Renovação" valor={formatarData(licenca?.dataRenovacao ?? null)} />
        </div>
      </section>

      <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900">Recarregar créditos (manual)</h3>
        <form className="space-y-4" onSubmit={aoRecarregar}>
          <div className="space-y-2">
            <label htmlFor="recarga-qtd" className="text-sm font-medium text-gray-700">
              Quantidade de créditos
            </label>
            <input
              id="recarga-qtd"
              type="number"
              min={1}
              value={quantidade}
              onChange={(evento) => definirQuantidade(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>
          <button
            type="submit"
            disabled={mutacaoRecarga.isPending}
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
          >
            {mutacaoRecarga.isPending ? 'Adicionando...' : 'Adicionar créditos'}
          </button>
          <button
            type="button"
            disabled={mutacaoOnline.isPending}
            onClick={() => {
              const qtd = Number.parseInt(quantidade, 10);
              if (!Number.isNaN(qtd) && qtd >= 1) {
                mutacaoOnline.mutate(qtd);
              }
            }}
            className="w-full rounded-lg border border-blue-300 px-4 py-2 text-sm font-semibold text-blue-700 transition hover:bg-blue-50 disabled:cursor-not-allowed disabled:opacity-60"
          >
            Pagar online (gateway)
          </button>
          <p className="text-xs text-gray-500">
            A recarga manual credita imediatamente. O pagamento online depende de um gateway ainda não configurado.
          </p>
        </form>
      </section>

      <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900">Plano e cobrança</h3>
        <form className="space-y-4" onSubmit={aoAtualizar}>
          <div className="space-y-2">
            <label htmlFor="plano" className="text-sm font-medium text-gray-700">
              Plano
            </label>
            <input
              id="plano"
              type="text"
              value={tipoPlano}
              onChange={(evento) => definirTipoPlano(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>
          <div className="space-y-2">
            <label htmlFor="status" className="text-sm font-medium text-gray-700">
              Status de pagamento
            </label>
            <select
              id="status"
              value={statusPagamento}
              onChange={(evento) => definirStatusPagamento(evento.target.value as StatusPagamento)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            >
              {STATUS.map((item) => (
                <option key={item} value={item}>
                  {ROTULO_STATUS[item]}
                </option>
              ))}
            </select>
            <p className="text-xs text-gray-500">Envios ficam bloqueados quando o status não é "Em dia".</p>
          </div>
          <div className="space-y-2">
            <label htmlFor="renovacao" className="text-sm font-medium text-gray-700">
              Data de renovação
            </label>
            <input
              id="renovacao"
              type="date"
              value={dataRenovacao}
              onChange={(evento) => definirDataRenovacao(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            />
          </div>
          <button
            type="submit"
            disabled={mutacaoAtualizar.isPending}
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
          >
            {mutacaoAtualizar.isPending ? 'Salvando...' : 'Salvar plano e cobrança'}
          </button>
        </form>
      </section>
    </div>
  );
}

function formatarData(data: string | null): string {
  if (!data) {
    return '-';
  }
  const partes = data.split('-');
  if (partes.length === 3) {
    return `${partes[2]}/${partes[1]}/${partes[0]}`;
  }
  return data;
}

type IndicadorProps = {
  titulo: string;
  valor: string;
  destaque?: boolean;
  aviso?: boolean;
};

function Indicador({ titulo, valor, destaque, aviso }: IndicadorProps) {
  const classe = aviso
    ? 'bg-yellow-50 text-yellow-800 border border-yellow-200'
    : destaque
      ? 'bg-blue-600 text-white'
      : 'bg-white text-gray-900 border border-gray-200';
  return (
    <div className={`rounded-lg p-4 shadow-sm ${classe}`}>
      <p className="text-xs font-medium uppercase tracking-wide opacity-80">{titulo}</p>
      <p className="mt-1 text-xl font-bold">{valor}</p>
    </div>
  );
}
