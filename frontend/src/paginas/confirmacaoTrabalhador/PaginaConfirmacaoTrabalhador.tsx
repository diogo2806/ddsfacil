import { useEffect, type ReactNode } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';
import {
  confirmarLeituraDds,
  consultarConteudoDds,
  DadosConfirmacaoTrabalhador,
} from '../../servicos/confirmacaoTrabalhadorServico';

export default function PaginaConfirmacaoTrabalhador() {
  const parametros = useParams<{ token: string }>();
  const tokenAcesso = (parametros.token ?? '').trim();

  const consultaConteudo = useQuery<DadosConfirmacaoTrabalhador>({
    queryKey: ['confirmacao-dds', tokenAcesso],
    queryFn: () => consultarConteudoDds(tokenAcesso),
    enabled: tokenAcesso.length > 0,
    retry: false,
  });

  const mutacaoConfirmacao = useMutation({
    mutationFn: () => confirmarLeituraDds(tokenAcesso),
  });

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  if (!tokenAcesso) {
    return (
      <LayoutPagina>
        <MensagemEstado titulo="Link inválido" descricao="Token de acesso não informado." />
      </LayoutPagina>
    );
  }

  if (consultaConteudo.isLoading) {
    return (
      <LayoutPagina>
        <MensagemEstado titulo="Carregando" descricao="Buscando informações do DDS." />
      </LayoutPagina>
    );
  }

  if (consultaConteudo.isError || !consultaConteudo.data) {
    return (
      <LayoutPagina>
        <MensagemEstado
          titulo="Link não encontrado"
          descricao="Não localizamos um DDS associado a este link. Verifique com o seu líder."
        />
      </LayoutPagina>
    );
  }

  const { titulo, descricao } = consultaConteudo.data;
  const confirmacaoConcluida = mutacaoConfirmacao.isSuccess;

  return (
    <LayoutPagina>
      <header className="mb-6 text-center">
        <h1 className="text-2xl font-bold text-blue-700">DDS Fácil</h1>
        <p className="mt-2 text-sm text-gray-600">Confirmação de participação no Diálogo Diário de Segurança</p>
      </header>

      <article className="rounded-xl border border-gray-200 bg-white p-6 shadow">
        <h2 className="text-xl font-semibold text-gray-900">{titulo}</h2>
        <p className="mt-4 whitespace-pre-line text-base text-gray-700">{descricao}</p>
      </article>

      <div className="mt-6">
        {confirmacaoConcluida ? (
          <div className="rounded-lg bg-green-100 p-4 text-center text-green-800">
            Obrigado! Sua presença foi confirmada.
          </div>
        ) : (
          <button
            type="button"
            className="w-full rounded-lg bg-blue-600 px-6 py-3 text-lg font-semibold text-white shadow transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
            onClick={() => mutacaoConfirmacao.mutate()}
            disabled={mutacaoConfirmacao.isPending}
          >
            {mutacaoConfirmacao.isPending ? 'Enviando confirmação...' : 'Li e confirmo minha participação'}
          </button>
        )}
        {mutacaoConfirmacao.isError && (
          <p className="mt-3 text-center text-sm text-red-600">
            Não foi possível registrar sua confirmação. Tente novamente em instantes.
          </p>
        )}
      </div>
    </LayoutPagina>
  );
}

type LayoutPaginaProps = {
  children: ReactNode;
};

function LayoutPagina({ children }: LayoutPaginaProps) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-b from-blue-50 to-blue-100 px-4 py-12">
      <div className="w-full max-w-xl space-y-6">{children}</div>
    </div>
  );
}

type MensagemEstadoProps = {
  titulo: string;
  descricao: string;
};

function MensagemEstado({ titulo, descricao }: MensagemEstadoProps) {
  return (
    <div className="rounded-xl border border-blue-200 bg-white p-6 text-center shadow">
      <h2 className="text-xl font-semibold text-gray-900">{titulo}</h2>
      <p className="mt-3 text-sm text-gray-600">{descricao}</p>
    </div>
  );
}
