import { useMemo } from 'react';
import { useEnvios } from '../../../hooks/useEnvios';
import { useConteudos } from '../../../hooks/useConteudos';
import { useFuncionarios } from '../../../hooks/useFuncionarios';
import { StatusEnvio } from '../../../types/enums';

export default function AbaDashboard() {
  const { consultaEnvios } = useEnvios();
  const { consultaConteudos } = useConteudos();
  const { consultaFuncionarios } = useFuncionarios();

  const carregando = consultaEnvios.isLoading || consultaConteudos.isLoading || consultaFuncionarios.isLoading;
  const houveErro = consultaEnvios.isError || consultaConteudos.isError || consultaFuncionarios.isError;

  const { totalEnvios, totalConfirmados, totalPendentes } = useMemo(() => {
    const envios = consultaEnvios.data ?? [];
    const confirmados = envios.filter((envio) => envio.status === StatusEnvio.CONFIRMADO).length;
    const pendentes = envios.filter((envio) => envio.status !== StatusEnvio.CONFIRMADO).length;
    return {
      totalEnvios: envios.length,
      totalConfirmados: confirmados,
      totalPendentes: pendentes,
    };
  }, [consultaEnvios.data]);

  const enviosRecentes = useMemo(() => {
    const envios = consultaEnvios.data ?? [];
    return envios
      .slice()
      .sort((a, b) => b.dataEnvio.localeCompare(a.dataEnvio) || b.momentoEnvio.localeCompare(a.momentoEnvio))
      .slice(0, 5);
  }, [consultaEnvios.data]);

  if (carregando) {
    return (
      <div className="rounded-lg border border-gray-200 bg-white p-6 text-gray-600">Carregando indicadores...</div>
    );
  }

  if (houveErro) {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-red-700">
        Não foi possível carregar os dados do dashboard. Tente novamente em instantes.
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <section className="grid gap-4 md:grid-cols-3">
        <Indicador titulo="Envios realizados" valor={totalEnvios} descricao="Total de DDS enviados" />
        <Indicador titulo="Confirmações" valor={totalConfirmados} descricao="Leituras confirmadas" destaque />
        <Indicador titulo="Pendentes" valor={totalPendentes} descricao="Aguardando confirmação" aviso={totalPendentes > 0} />
      </section>

      <section className="rounded-lg border border-gray-200 bg-white">
        <header className="border-b border-gray-100 px-4 py-3">
          <h3 className="text-lg font-semibold text-gray-900">Últimos envios</h3>
        </header>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              <tr>
                <th className="px-4 py-3">Funcionário</th>
                <th className="px-4 py-3">Conteúdo</th>
                <th className="px-4 py-3">Data</th>
                <th className="px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-white text-sm text-gray-700">
              {enviosRecentes.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-4 py-6 text-center text-gray-500">
                    Nenhum envio registrado até o momento.
                  </td>
                </tr>
              ) : (
                enviosRecentes.map((envio) => (
                  <tr key={envio.id}>
                    <td className="px-4 py-3">{envio.nomeFuncionario}</td>
                    <td className="px-4 py-3">{envio.tituloConteudo}</td>
                    <td className="px-4 py-3">{new Date(`${envio.dataEnvio}T${envio.momentoEnvio}`).toLocaleString('pt-BR')}</td>
                    <td className="px-4 py-3">
                      <StatusBadge status={envio.status} />
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

type IndicadorProps = {
  titulo: string;
  valor: number;
  descricao: string;
  destaque?: boolean;
  aviso?: boolean;
};

function Indicador({ titulo, valor, descricao, destaque, aviso }: IndicadorProps) {
  const classe = destaque
    ? 'bg-blue-600 text-white'
    : aviso
      ? 'bg-yellow-50 text-yellow-800 border border-yellow-200'
      : 'bg-white text-gray-900 border border-gray-200';

  return (
    <div className={`rounded-lg p-5 shadow-sm transition ${classe}`}>
      <p className="text-sm font-medium">{titulo}</p>
      <p className="mt-2 text-3xl font-bold">{valor}</p>
      <p className="mt-1 text-xs uppercase tracking-wide opacity-70">{descricao}</p>
    </div>
  );
}

type StatusBadgeProps = {
  status: StatusEnvio;
};

function StatusBadge({ status }: StatusBadgeProps) {
  const cores = {
    [StatusEnvio.CONFIRMADO]: 'bg-green-100 text-green-700 border border-green-200',
    [StatusEnvio.ENVIADO]: 'bg-blue-100 text-blue-700 border border-blue-200',
    [StatusEnvio.PENDENTE]: 'bg-yellow-100 text-yellow-800 border border-yellow-200',
  }[status] ?? 'bg-gray-100 text-gray-600 border border-gray-200';

  return <span className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${cores}`}>{status}</span>;
}
