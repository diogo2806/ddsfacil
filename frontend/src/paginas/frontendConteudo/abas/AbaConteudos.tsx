import { ChangeEvent, FormEvent, useMemo, useState } from 'react';
import DOMPurify from 'dompurify';
import { useConteudos } from '../../../hooks/useConteudos';
import { TipoConteudo, TipoNotificacao } from '../../../types/enums';

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

type DetalhesArquivo = {
  nome: string;
  tamanho: string;
  tipo: 'PDF' | 'IMAGEM';
};

export default function AbaConteudos({ exibirNotificacao }: Props) {
  const [titulo, definirTitulo] = useState('');
  const [descricao, definirDescricao] = useState('');
  const [tipo, definirTipo] = useState<TipoConteudo>(TipoConteudo.TEXTO);
  const [url, definirUrl] = useState('');
  const [arquivo, definirArquivo] = useState<File | null>(null);
  const [detalhesArquivo, definirDetalhesArquivo] = useState<DetalhesArquivo | null>(null);
  const [erroArquivo, definirErroArquivo] = useState<string | null>(null);
  const [progressoUpload, definirProgressoUpload] = useState(0);

  const { consultaConteudos, mutacaoCriar, mutacaoCriarComArquivo, mutacaoRemover } = useConteudos({
    onSuccessSave: () => {
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Conteúdo salvo com sucesso.' });
      redefinirFormulario();
    },
    onErrorSave: () => {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Erro ao salvar conteúdo.' });
    },
    onSuccessRemove: () => {
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Conteúdo removido.' });
    },
    onErrorRemove: () => {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Erro ao remover conteúdo.' });
    },
  });

  const conteudosOrdenados = useMemo(
    () =>
      (consultaConteudos.data ?? [])
        .slice()
        .sort((a, b) => a.titulo.localeCompare(b.titulo, 'pt-BR', { sensitivity: 'base' })),
    [consultaConteudos.data],
  );

  function redefinirFormulario() {
    definirTitulo('');
    definirDescricao('');
    definirTipo(TipoConteudo.TEXTO);
    definirUrl('');
    definirArquivo(null);
    definirDetalhesArquivo(null);
    definirErroArquivo(null);
    definirProgressoUpload(0);
  }

  function aoAlterarTipo(novoTipo: TipoConteudo) {
    definirTipo(novoTipo);
    if (novoTipo !== TipoConteudo.ARQUIVO) {
      definirArquivo(null);
      definirDetalhesArquivo(null);
      definirErroArquivo(null);
      definirProgressoUpload(0);
    }
  }

  function aoSelecionarArquivo(evento: ChangeEvent<HTMLInputElement>) {
    definirErroArquivo(null);
    definirProgressoUpload(0);

    const arquivoSelecionado = evento.target.files?.[0];
    if (!arquivoSelecionado) {
      definirArquivo(null);
      definirDetalhesArquivo(null);
      return;
    }

    const tipoMime = arquivoSelecionado.type.toLowerCase();
    const nomeSanitizado = DOMPurify.sanitize(arquivoSelecionado.name, {
      ALLOWED_TAGS: [],
      ALLOWED_ATTR: [],
    });
    const ehPdf = tipoMime === 'application/pdf' || nomeSanitizado.toLowerCase().endsWith('.pdf');
    const ehImagem = tipoMime.startsWith('image/');

    if (!ehPdf && !ehImagem) {
      definirErroArquivo('Formato inválido. Escolha apenas arquivos PDF ou imagens.');
      definirArquivo(null);
      definirDetalhesArquivo(null);
      evento.target.value = '';
      return;
    }

    definirArquivo(arquivoSelecionado);
    definirDetalhesArquivo({
      nome: nomeSanitizado,
      tamanho: formatarTamanhoArquivo(arquivoSelecionado.size),
      tipo: ehPdf ? 'PDF' : 'IMAGEM',
    });
  }

  function aoSubmeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();

    const tituloLimpo = DOMPurify.sanitize(titulo, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
    const descricaoLimpa = DOMPurify.sanitize(descricao, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
    const urlLimpa = DOMPurify.sanitize(url, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();

    if (!tituloLimpo) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Informe um título para o conteúdo.' });
      return;
    }

    if (tipo === TipoConteudo.ARQUIVO) {
      if (!arquivo) {
        exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Selecione um arquivo para enviar.' });
        return;
      }
      definirProgressoUpload(5);
      mutacaoCriarComArquivo.mutate(
        {
          dados: { titulo: tituloLimpo, descricao: descricaoLimpa, tipo },
          arquivo,
          aoProgredir: (percentual) => definirProgressoUpload(Math.min(100, Math.max(percentual, 5))),
        },
        {
          onSettled: () => {
            setTimeout(() => definirProgressoUpload(0), 400);
          },
        },
      );
      return;
    }

    if (tipo === TipoConteudo.LINK && urlLimpa && !/^https?:\/\//i.test(urlLimpa)) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Informe uma URL válida iniciando com http:// ou https://.' });
      return;
    }

    mutacaoCriar.mutate({
      titulo: tituloLimpo,
      descricao: descricaoLimpa || undefined,
      tipo,
      url: urlLimpa || undefined,
    });
  }

  function removerConteudo(id: number) {
    mutacaoRemover.mutate(id);
  }

  const estaSalvando = mutacaoCriar.isPending || mutacaoCriarComArquivo.isPending;
  const deveExibirProgresso = progressoUpload > 0 && tipo === TipoConteudo.ARQUIVO;

  return (
    <div className="grid gap-6 md:grid-cols-[1fr,2fr]">
      <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900">Cadastrar conteúdo</h3>
        <form className="space-y-4" onSubmit={aoSubmeter}>
          <div className="space-y-2">
            <label htmlFor="titulo" className="text-sm font-medium text-gray-700">
              Título
            </label>
            <input
              id="titulo"
              type="text"
              value={titulo}
              onChange={(evento) => definirTitulo(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="tipo" className="text-sm font-medium text-gray-700">
              Tipo de conteúdo
            </label>
            <select
              id="tipo"
              value={tipo}
              onChange={(evento) => aoAlterarTipo(evento.target.value as TipoConteudo)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            >
              <option value={TipoConteudo.TEXTO}>Texto</option>
              <option value={TipoConteudo.LINK}>Link</option>
              <option value={TipoConteudo.ARQUIVO}>Arquivo</option>
            </select>
          </div>

          {tipo === TipoConteudo.TEXTO && (
            <div className="space-y-2">
              <label htmlFor="descricao" className="text-sm font-medium text-gray-700">
                Descrição
              </label>
              <textarea
                id="descricao"
                value={descricao}
                onChange={(evento) => definirDescricao(evento.target.value)}
                className="h-32 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              />
            </div>
          )}

          {tipo === TipoConteudo.LINK && (
            <div className="space-y-2">
              <label htmlFor="url" className="text-sm font-medium text-gray-700">
                Endereço do conteúdo
              </label>
              <input
                id="url"
                type="url"
                value={url}
                onChange={(evento) => definirUrl(evento.target.value)}
                placeholder="https://exemplo.com"
                className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              />
            </div>
          )}

          {tipo === TipoConteudo.ARQUIVO && (
            <div className="space-y-2">
              <label htmlFor="arquivo" className="text-sm font-medium text-gray-700">
                Arquivo
              </label>
              <input
                id="arquivo"
                type="file"
                accept=".pdf,image/*"
                onChange={aoSelecionarArquivo}
                className={`w-full text-sm ${erroArquivo ? 'rounded-lg border border-red-500 px-3 py-2 text-red-700 focus:border-red-500 focus:outline-none' : 'rounded-lg border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none'}`}
              />
              <p className="text-xs text-gray-500">Apenas arquivos PDF ou imagens (PNG, JPG, JPEG, GIF, WEBP).</p>
              {detalhesArquivo && (
                <div
                  className="flex items-center justify-between rounded-lg border border-gray-100 bg-gray-50 px-3 py-2 text-sm text-gray-700"
                  aria-live="polite"
                >
                  <div className="flex items-center gap-3">
                    <span className="text-lg" aria-hidden="true">
                      {detalhesArquivo.tipo === 'PDF' ? '📄' : '🖼️'}
                    </span>
                    <div>
                      <p className="font-medium">{detalhesArquivo.nome}</p>
                      <p className="text-xs text-gray-500">{detalhesArquivo.tamanho}</p>
                    </div>
                  </div>
                  <span className="rounded-full bg-blue-100 px-2 py-0.5 text-xs font-semibold text-blue-700">
                    {detalhesArquivo.tipo}
                  </span>
                </div>
              )}
              {erroArquivo && (
                <p className="text-sm text-red-600" aria-live="assertive">
                  {erroArquivo}
                </p>
              )}
              {deveExibirProgresso && (
                <div className="space-y-1">
                  <div className="flex items-center justify-between text-xs text-gray-600">
                    <span>Enviando arquivo</span>
                    <span>{`${progressoUpload}%`}</span>
                  </div>
                  <div className="h-2 w-full rounded-full bg-gray-200">
                    <div
                      className="h-2 rounded-full bg-blue-600 transition-all"
                      style={{ width: `${progressoUpload}%` }}
                    />
                  </div>
                </div>
              )}
            </div>
          )}

          <button
            type="submit"
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
            disabled={estaSalvando}
          >
            {estaSalvando ? 'Salvando...' : 'Salvar conteúdo'}
          </button>
        </form>
      </section>

      <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <header className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900">Conteúdos cadastrados</h3>
          <span className="text-sm text-gray-500">{conteudosOrdenados.length} registro(s)</span>
        </header>

        {consultaConteudos.isLoading ? (
          <p className="text-sm text-gray-500">Carregando conteúdos...</p>
        ) : consultaConteudos.isError ? (
          <p className="text-sm text-red-600">Não foi possível carregar os conteúdos.</p>
        ) : conteudosOrdenados.length === 0 ? (
          <p className="text-sm text-gray-500">Nenhum conteúdo cadastrado até o momento.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 text-sm">
              <thead className="bg-gray-50 text-left font-semibold uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-4 py-3">Título</th>
                  <th className="px-4 py-3">Tipo</th>
                  <th className="px-4 py-3">Detalhes</th>
                  <th className="px-4 py-3 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 bg-white text-gray-700">
                {conteudosOrdenados.map((conteudo) => (
                  <tr key={conteudo.id}>
                    <td className="px-4 py-3 font-medium">{conteudo.titulo}</td>
                    <td className="px-4 py-3">{conteudo.tipo ?? TipoConteudo.TEXTO}</td>
                    <td className="px-4 py-3">
                      {conteudo.tipo === TipoConteudo.LINK && conteudo.url ? (
                        <a
                          href={conteudo.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-blue-600 underline"
                        >
                          Abrir link
                        </a>
                      ) : conteudo.tipo === TipoConteudo.ARQUIVO ? (
                        <a
                          href={`/api/conteudos/${conteudo.id}/arquivo`}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-blue-600 underline"
                        >
                          Baixar arquivo
                        </a>
                      ) : (
                        <span className="whitespace-pre-line text-xs text-gray-600">{conteudo.descricao}</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        type="button"
                        className="text-sm font-semibold text-red-600 hover:text-red-700"
                        onClick={() => removerConteudo(conteudo.id)}
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

function formatarTamanhoArquivo(bytes: number): string {
  if (!Number.isFinite(bytes) || bytes <= 0) {
    return '0 B';
  }

  const unidades = ['B', 'KB', 'MB', 'GB'];
  let tamanho = bytes;
  let indice = 0;

  while (tamanho >= 1024 && indice < unidades.length - 1) {
    tamanho /= 1024;
    indice += 1;
  }

  const casasDecimais = indice === 0 ? 0 : 1;
  return `${tamanho.toFixed(casasDecimais)} ${unidades[indice]}`;
}
