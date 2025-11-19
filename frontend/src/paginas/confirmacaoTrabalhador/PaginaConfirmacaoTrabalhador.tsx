import { useEffect, type ReactNode } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';
import DOMPurify from 'dompurify';
import {
  confirmarLeituraDds,
  consultarConteudoDds,
  DadosConfirmacaoTrabalhador,
} from '../../servicos/confirmacaoTrabalhadorServico';
import { TipoConteudo } from '../../types/enums';

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

  const { titulo, descricao, tipoConteudo, urlConteudo, nomeArquivo } = consultaConteudo.data;
  const tituloSeguro = sanitizarTexto(titulo);
  const descricaoSegura = sanitizarTexto(descricao);
  const urlSegura = sanitizarUrl(urlConteudo);
  const nomeArquivoSeguro = sanitizarNomeArquivo(nomeArquivo);
  const confirmacaoConcluida = mutacaoConfirmacao.isSuccess;
  const tipoArquivo = identificarTipoArquivo(nomeArquivoSeguro);

  return (
    <LayoutPagina>
      <header className="mb-6 text-center">
        <h1 className="text-2xl font-bold text-blue-700">DDS Fácil</h1>
        <p className="mt-2 text-sm text-gray-600">Confirmação de participação no Diálogo Diário de Segurança</p>
      </header>

      <article className="rounded-xl border border-gray-200 bg-white p-6 shadow">
        <h2 className="text-xl font-semibold text-gray-900">{tituloSeguro}</h2>
        {tipoConteudo === TipoConteudo.TEXTO && (
          <p className="mt-4 whitespace-pre-line text-base text-gray-700">{descricaoSegura}</p>
        )}
        {tipoConteudo === TipoConteudo.LINK && urlSegura && (
          <div className="mt-4 space-y-2 rounded-lg bg-blue-50 p-4 text-base text-gray-700">
            <p className="font-medium">Acesse o DDS pelo link seguro abaixo:</p>
            <a
              href={urlSegura}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-block rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700"
            >
              Abrir conteúdo
            </a>
          </div>
        )}
        {tipoConteudo === TipoConteudo.ARQUIVO && urlSegura && (
          <div className="mt-4 space-y-3 rounded-lg bg-blue-50 p-4 text-base text-gray-700">
            <p className="font-medium">
              Visualize o conteúdo do DDS abaixo ou realize o download para ler quando preferir.
            </p>
            <a
              href={urlSegura}
              target="_blank"
              rel="noopener noreferrer"
              download={nomeArquivoSeguro ?? undefined}
              className="inline-flex items-center justify-center rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700"
            >
              {nomeArquivoSeguro ? `Baixar ${nomeArquivoSeguro}` : 'Baixar arquivo'}
            </a>
            {renderizarVisualizacaoArquivo(urlSegura, tipoArquivo, nomeArquivoSeguro)}
          </div>
        )}
        {!urlSegura && tipoConteudo !== TipoConteudo.TEXTO && (
          <p className="mt-4 text-sm text-red-700">
            Não foi possível carregar o conteúdo associado. Solicite um novo link ao seu líder.
          </p>
        )}
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

function sanitizarTexto(valor: string): string {
  return DOMPurify.sanitize(valor, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
}

function sanitizarUrl(valor: string | null): string | null {
  const urlLimpa = DOMPurify.sanitize(valor ?? '', { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
  if (!urlLimpa) {
    return null;
  }
  const urlValida = /^https?:\/\/|^\//i;
  return urlValida.test(urlLimpa) ? urlLimpa : null;
}

function sanitizarNomeArquivo(valor: string | null): string | null {
  const textoLimpo = DOMPurify.sanitize(valor ?? '', { ALLOWED_TAGS: [], ALLOWED_ATTR: [] })
    .replace(/[^\w.\- ]/g, '')
    .trim();
  return textoLimpo.length > 0 ? textoLimpo : null;
}

type TipoArquivo = 'pdf' | 'imagem' | 'audio' | 'video' | 'desconhecido';

function identificarTipoArquivo(url: string | null): TipoArquivo {
  if (!url) {
    return 'desconhecido';
  }
  const caminhoLimpo = url.split('?')[0].toLowerCase();
  if (caminhoLimpo.endsWith('.pdf')) return 'pdf';
  if (/(\.jpg|\.jpeg|\.png|\.gif|\.webp)$/.test(caminhoLimpo)) return 'imagem';
  if (/(\.mp3|\.wav|\.ogg|\.m4a|\.aac)$/.test(caminhoLimpo)) return 'audio';
  if (/(\.mp4|\.webm|\.ogv|\.mov)$/i.test(caminhoLimpo)) return 'video';
  return 'desconhecido';
}

function renderizarVisualizacaoArquivo(
  urlSegura: string,
  tipoArquivo: TipoArquivo,
  nomeArquivoSeguro: string | null,
) {
  if (tipoArquivo === 'pdf') {
    return (
      <iframe
        src={urlSegura}
        title="Pré-visualização do PDF"
        className="h-96 w-full rounded-md border border-blue-200 bg-white"
      />
    );
  }

  if (tipoArquivo === 'imagem') {
    return (
      <img
        src={urlSegura}
        alt={nomeArquivoSeguro ?? 'Pré-visualização do arquivo'}
        className="max-h-[26rem] w-full rounded-md border border-blue-200 object-contain bg-white"
        loading="lazy"
      />
    );
  }

  if (tipoArquivo === 'audio') {
    return (
      <audio
        controls
        className="w-full rounded-md border border-blue-200 bg-white"
        src={urlSegura}
        controlsList="nodownload"
      >
        Seu navegador não suporta a reprodução de áudio.
      </audio>
    );
  }

  if (tipoArquivo === 'video') {
    return (
      <video
        controls
        className="w-full rounded-md border border-blue-200 bg-black"
        src={urlSegura}
        controlsList="nodownload"
      >
        Seu navegador não suporta a reprodução de vídeo.
      </video>
    );
  }

  return (
    <p className="text-sm text-gray-700">
      Pré-visualização indisponível para este formato. Realize o download para acessar o conteúdo.
    </p>
  );
}
