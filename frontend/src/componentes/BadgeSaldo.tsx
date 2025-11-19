import { useQuery } from '@tanstack/react-query';
import { consultarSaldo } from '../servicos/licencaServico';
import { obterEmpresaIdAtualOpcional } from '../configuracao/empresa'; 

export default function BadgeSaldo() {
  // 1. Recuperamos o ID da empresa atual da configuração local
  const empresaId = obterEmpresaIdAtualOpcional();

  const { data, isLoading, isError } = useQuery({
    queryKey: ['saldo-sms'],
    queryFn: consultarSaldo,
    refetchOnWindowFocus: false,
    // 2. A query só será executada se existir um ID de empresa definido.
    // Isso evita a "Race Condition" onde o componente monta antes da autenticação terminar.
    enabled: !!empresaId, 
    retry: 1, // Tenta apenas mais uma vez em caso de falha, para não inundar o log
  });

  // Se ainda não tem empresaId (login não finalizou), não exibe nada
  if (!empresaId) {
    return null;
  }

  if (isLoading) {
    return (
      <div className="flex items-center gap-2 rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-500">
        <div className="h-2 w-2 animate-pulse rounded-full bg-gray-400" />
        Carregando...
      </div>
    );
  }

  if (isError || !data) {
    // Em caso de erro (ex: 403 real), esconde o componente para não quebrar o layout
    return null;
  }

  // Lógica visual: Azul se tiver saldo confortável, Vermelho pulsante se estiver acabando (< 10)
  const corBadge = data.saldoSms > 10 
    ? 'bg-blue-50 text-blue-700 border-blue-200' 
    : 'bg-red-50 text-red-700 border-red-200 animate-pulse';

  return (
    <div
      className={`flex items-center gap-2 rounded-full border px-3 py-1 text-xs font-semibold transition-colors ${corBadge}`}
      title={`Plano atual: ${data.plano}`}
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        className="h-3 w-3"
      >
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
      </svg>
      {data.saldoSms} créditos
    </div>
  );
}