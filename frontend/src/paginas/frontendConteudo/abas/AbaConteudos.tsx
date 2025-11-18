import { FormEvent, useMemo, useState } from 'react';
import DOMPurify from 'dompurify';
import { useConteudos } from '../../../hooks/useConteudos';
import { TipoConteudo, TipoNotificacao } from '../../../types/enums';

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

export default function AbaConteudos({ exibirNotificacao }: Props) {
  const [titulo, definirTitulo] = useState('');
  const [descricao, definirDescricao] = useState('');
  const [tipo, definirTipo] = useState<TipoConteudo>(TipoConteudo.TEXTO);
  const [url, definirUrl] = useState('');
  const [arquivo, definirArquivo] = useState<File | null>(null);

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
      mutacaoCriarComArquivo.mutate({ dados: { titulo: tituloLimpo, descricao: descricaoLimpa, tipo }, arquivo });
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
              onChange={(evento) => definirTipo(evento.target.value as TipoConteudo)}
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
                accept=".pdf,.doc,.docx,.png,.jpg,.jpeg"
                onChange={(evento) => definirArquivo(evento.target.files?.[0] ?? null)}
                className="w-full text-sm"
              />
            </div>
          )}

          <button
            type="submit"
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
            disabled={mutacaoCriar.isPending || mutacaoCriarComArquivo.isPending}
          >
            {mutacaoCriar.isPending || mutacaoCriarComArquivo.isPending ? 'Salvando...' : 'Salvar conteúdo'}
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
