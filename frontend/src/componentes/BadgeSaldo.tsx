import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { consultarSaldo } from '../servicos/licencaServico';
import type { SessaoUsuario } from '../types/autenticacao';

type BadgeSaldoProps = {
  sessaoUsuario: SessaoUsuario | null;
};

export default function BadgeSaldo({ sessaoUsuario }: BadgeSaldoProps) {
  const empresaId = sessaoUsuario?.empresaId ?? null;
  const possuiTokenValido = Boolean(sessaoUsuario?.token);

  const deveConsultarSaldo = useMemo(() => Boolean(possuiTokenValido && empresaId && empresaId > 0), [empresaId, possuiTokenValido]);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['saldo-sms', empresaId],
    queryFn: consultarSaldo,
    refetchOnWindowFocus: false,
    enabled: deveConsultarSaldo,
    retry: 1,
  });

  if (!deveConsultarSaldo) {
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
    return (
      <div className="flex items-center gap-2 rounded-full border border-yellow-200 bg-yellow-50 px-3 py-1 text-xs font-medium text-yellow-800">
        Erro ao carregar saldo
      </div>
    );
  }

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