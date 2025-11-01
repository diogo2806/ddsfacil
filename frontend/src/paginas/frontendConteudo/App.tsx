import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  CadastroConteudo,
  ConteudoDds,
  criarConteudo,
  listarConteudos,
  removerConteudo,
} from '../../servicos/conteudosServico';

function validarTextoLimpo(texto: string): string {
  return texto.replace(/[<>]/g, '');
}

export default function App() {
  const clienteConsulta = useQueryClient();
  const [titulo, definirTitulo] = useState('');
  const [descricao, definirDescricao] = useState('');
  const [mensagemErro, definirMensagemErro] = useState('');
  const [mensagemSucesso, definirMensagemSucesso] = useState('');

  const consultaConteudos = useQuery<ConteudoDds[]>({
    queryKey: ['conteudos'],
    queryFn: listarConteudos,
  });

  const mutacaoCriacao = useMutation({
    mutationFn: (dados: CadastroConteudo) => criarConteudo(dados),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['conteudos'] });
      definirTitulo('');
      definirDescricao('');
      definirMensagemErro('');
      definirMensagemSucesso('Conteúdo salvo com sucesso.');
    },
    onError: () => {
      definirMensagemSucesso('');
      definirMensagemErro('Não foi possível salvar o conteúdo. Verifique os dados informados.');
    },
  });

  const mutacaoRemocao = useMutation({
    mutationFn: (id: number) => removerConteudo(id),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['conteudos'] });
      definirMensagemSucesso('Conteúdo removido com sucesso.');
    },
    onError: () => {
      definirMensagemSucesso('');
      definirMensagemErro('Não foi possível remover o conteúdo. Tente novamente.');
    },
  });

  const possuiCarregamento = consultaConteudos.isLoading;
  const ocorreuErro = consultaConteudos.isError;
  const listaConteudos = consultaConteudos.data ?? [];

  function aoEnviar(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    definirMensagemSucesso('');
    definirMensagemErro('');

    const tituloLimpo = validarTextoLimpo(titulo.trim());
    const descricaoLimpa = validarTextoLimpo(descricao.trim());

    if (!tituloLimpo || !descricaoLimpa) {
      definirMensagemErro('Preencha título e descrição para salvar.');
      return;
    }

    mutacaoCriacao.mutate({ titulo: tituloLimpo, descricao: descricaoLimpa });
  }

  function aoRemover(id: number) {
    if (confirm('Tem certeza de que deseja remover este conteúdo?')) {
      mutacaoRemocao.mutate(id);
    }
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-white shadow">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-5">
          <div>
            <h1 className="text-2xl font-bold text-blue-600">DDS Facil</h1>
            <p className="text-sm text-gray-500">Administração de Conteúdos de DDS</p>
          </div>
        </div>
      </header>

      <main className="mx-auto flex max-w-6xl flex-col gap-8 px-4 py-8 lg:flex-row">
        <section className="w-full rounded-2xl bg-white p-6 shadow lg:max-w-sm">
          <h2 className="text-xl font-semibold text-gray-900">Novo Conteúdo de DDS</h2>
          <p className="mt-1 text-sm text-gray-500">Preencha as informações para cadastrar um novo conteúdo.</p>

          <form className="mt-6 space-y-4" onSubmit={aoEnviar}>
            <div>
              <label htmlFor="titulo" className="block text-sm font-medium text-gray-700">
                Título
              </label>
              <input
                id="titulo"
                name="titulo"
                value={titulo}
                onChange={(evento) => definirTitulo(evento.target.value)}
                className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                maxLength={120}
              />
            </div>

            <div>
              <label htmlFor="descricao" className="block text-sm font-medium text-gray-700">
                Descrição
              </label>
              <textarea
                id="descricao"
                name="descricao"
                value={descricao}
                onChange={(evento) => definirDescricao(evento.target.value)}
                className="mt-1 h-32 w-full rounded-lg border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                maxLength={2000}
              />
            </div>

            <button
              type="submit"
              className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
              disabled={mutacaoCriacao.isPending}
            >
              {mutacaoCriacao.isPending ? 'Salvando...' : 'Salvar Conteúdo'}
            </button>
          </form>

          {mensagemErro && <p className="mt-4 rounded-lg bg-red-100 px-4 py-2 text-sm text-red-700">{mensagemErro}</p>}
          {mensagemSucesso && (
            <p className="mt-4 rounded-lg bg-green-100 px-4 py-2 text-sm text-green-700">{mensagemSucesso}</p>
          )}
        </section>

        <section className="w-full rounded-2xl bg-white p-6 shadow">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-xl font-semibold text-gray-900">Conteúdos cadastrados</h2>
              <p className="text-sm text-gray-500">Consulte, organize e mantenha a biblioteca de DDS.</p>
            </div>
            <span className="rounded-full bg-blue-50 px-3 py-1 text-sm font-medium text-blue-600">
              {listaConteudos.length} itens
            </span>
          </div>

          <div className="mt-6 space-y-4">
            {possuiCarregamento && <p className="text-gray-500">Carregando conteúdos...</p>}
            {ocorreuErro && (
              <p className="text-red-600">Não foi possível carregar os conteúdos. Atualize a página e tente novamente.</p>
            )}
            {!possuiCarregamento && !ocorreuErro && listaConteudos.length === 0 && (
              <p className="text-gray-500">Nenhum conteúdo cadastrado até o momento.</p>
            )}
            {!possuiCarregamento &&
              !ocorreuErro &&
              listaConteudos.map((conteudo) => (
                <article
                  key={conteudo.id}
                  className="rounded-xl border border-gray-100 bg-gray-50 p-5 shadow-sm transition hover:border-blue-200 hover:bg-white"
                >
                  <header className="flex items-start justify-between gap-4">
                    <div>
                      <h3 className="text-lg font-semibold text-gray-900">{conteudo.titulo}</h3>
                      <p className="mt-1 text-sm text-gray-600">{conteudo.descricao}</p>
                    </div>
                    <button
                      type="button"
                      className="rounded-lg border border-red-200 px-3 py-1 text-sm font-medium text-red-600 transition hover:bg-red-50"
                      onClick={() => aoRemover(conteudo.id)}
                      disabled={mutacaoRemocao.isPending}
                    >
                      {mutacaoRemocao.isPending ? 'Removendo...' : 'Remover'}
                    </button>
                  </header>
                </article>
              ))}
          </div>
        </section>
      </main>
    </div>
  );
}
