import { useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  CadastroConteudo,
  ConteudoDds,
  criarConteudo,
  listarConteudos,
  removerConteudo,
} from '../../servicos/conteudosServico';
import {
  CadastroFuncionario,
  Funcionario,
  criarFuncionario,
  listarFuncionarios,
  listarObras,
  removerFuncionario,
} from '../../servicos/funcionariosServico';
import {
  CadastroEnvio,
  EnvioDds,
  confirmarEnvio,
  criarEnvios,
  listarEnviosPorData,
} from '../../servicos/enviosServico';
import TelaDivulgacao from './TelaDivulgacao';

function sanitizarTexto(texto: string): string {
  return texto.replace(/[<>&"'`]/g, '').replace(/\s+/g, ' ').trim();
}

function sanitizarTextoMultilinha(texto: string): string {
  return texto
    .split('\n')
    .map((linha) => sanitizarTexto(linha))
    .join('\n')
    .trim();
}

function sanitizarCelular(texto: string): string {
  return texto.replace(/[^0-9()+\-\s]/g, '').trim();
}

type AbaPainel = 'dashboard' | 'enviar' | 'funcionarios' | 'conteudo';

type Notificacao = {
  tipo: 'sucesso' | 'erro';
  mensagem: string;
};

export default function App() {
  const clienteConsulta = useQueryClient();
  const [telaAtual, definirTelaAtual] = useState<'divulgacao' | 'painel'>('divulgacao');
  const [abaAtiva, definirAbaAtiva] = useState<AbaPainel>('dashboard');
  const [dataDashboard, definirDataDashboard] = useState<string>(
    () => new Date().toISOString().split('T')[0],
  );
  const [obraSelecionada, definirObraSelecionada] = useState<string>('todos');

  const [tituloConteudo, definirTituloConteudo] = useState('');
  const [descricaoConteudo, definirDescricaoConteudo] = useState('');

  const [nomeFuncionario, definirNomeFuncionario] = useState('');
  const [celularFuncionario, definirCelularFuncionario] = useState('');
  const [obraFuncionario, definirObraFuncionario] = useState('');

  const [notificacao, definirNotificacao] = useState<Notificacao | null>(null);
  const referenciaNotificacao = useRef<number>();

  if (telaAtual === 'divulgacao') {
    return <TelaDivulgacao aoSolicitarLogin={() => definirTelaAtual('painel')} />;
  }

  const consultaConteudos = useQuery<ConteudoDds[]>({
    queryKey: ['conteudos'],
    queryFn: listarConteudos,
  });

  const consultaFuncionarios = useQuery<Funcionario[]>({
    queryKey: ['funcionarios'],
    queryFn: () => listarFuncionarios(),
  });

  const consultaObras = useQuery<string[]>({
    queryKey: ['obras'],
    queryFn: listarObras,
  });

  const consultaEnvios = useQuery<EnvioDds[]>({
    queryKey: ['envios', dataDashboard],
    queryFn: () => listarEnviosPorData(dataDashboard || undefined),
  });

  const mutacaoConteudo = useMutation({
    mutationFn: (dados: CadastroConteudo) => criarConteudo(dados),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['conteudos'] });
      definirTituloConteudo('');
      definirDescricaoConteudo('');
      exibirNotificacao({ tipo: 'sucesso', mensagem: 'Conteúdo salvo com sucesso.' });
    },
    onError: () => {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Não foi possível salvar o conteúdo.' });
    },
  });

  const mutacaoRemocaoConteudo = useMutation({
    mutationFn: (id: number) => removerConteudo(id),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['conteudos'] });
      exibirNotificacao({ tipo: 'sucesso', mensagem: 'Conteúdo removido com sucesso.' });
    },
    onError: () => {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Não foi possível remover o conteúdo.' });
    },
  });

  const mutacaoFuncionario = useMutation({
    mutationFn: (dados: CadastroFuncionario) => criarFuncionario(dados),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['funcionarios'] });
      clienteConsulta.invalidateQueries({ queryKey: ['obras'] });
      definirNomeFuncionario('');
      definirCelularFuncionario('');
      definirObraFuncionario('');
      exibirNotificacao({ tipo: 'sucesso', mensagem: 'Funcionário cadastrado com sucesso.' });
    },
    onError: () => {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Não foi possível cadastrar o funcionário.' });
    },
  });

  const mutacaoRemocaoFuncionario = useMutation({
    mutationFn: (id: number) => removerFuncionario(id),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['funcionarios'] });
      clienteConsulta.invalidateQueries({ queryKey: ['obras'] });
      exibirNotificacao({ tipo: 'sucesso', mensagem: 'Funcionário removido com sucesso.' });
    },
    onError: () => {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Não foi possível remover o funcionário.' });
    },
  });

  const mutacaoEnvio = useMutation({
    mutationFn: (dados: CadastroEnvio) => criarEnvios(dados),
    onSuccess: (_, variaveis) => {
      const dataEnvio = variaveis.dataEnvio ?? new Date().toISOString().split('T')[0];
      clienteConsulta.invalidateQueries({ queryKey: ['envios', dataEnvio] });
      definirDataDashboard(dataEnvio);
      definirAbaAtiva('dashboard');
      exibirNotificacao({ tipo: 'sucesso', mensagem: 'Envio de DDS registrado com sucesso.' });
    },
    onError: (erro: unknown) => {
      if (erro && typeof erro === 'object' && 'response' in erro) {
        const resposta = erro as { response?: { data?: Record<string, string> | { mensagem?: string } } };
        const mensagem =
          resposta.response?.data && 'mensagem' in resposta.response.data
            ? resposta.response.data.mensagem
            : 'Não foi possível registrar o envio.';
        exibirNotificacao({ tipo: 'erro', mensagem });
      } else {
        exibirNotificacao({ tipo: 'erro', mensagem: 'Não foi possível registrar o envio.' });
      }
    },
  });

  const mutacaoConfirmacao = useMutation({
    mutationFn: (id: number) => confirmarEnvio(id),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['envios', dataDashboard] });
      exibirNotificacao({ tipo: 'sucesso', mensagem: 'Confirmação registrada.' });
    },
    onError: () => {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Não foi possível confirmar o DDS.' });
    },
  });

  useEffect(() => {
    return () => {
      if (referenciaNotificacao.current) {
        window.clearTimeout(referenciaNotificacao.current);
      }
    };
  }, []);

  function exibirNotificacao(novaNotificacao: Notificacao) {
    definirNotificacao(novaNotificacao);
    if (referenciaNotificacao.current) {
      window.clearTimeout(referenciaNotificacao.current);
    }
    referenciaNotificacao.current = window.setTimeout(() => {
      definirNotificacao(null);
    }, 3500);
  }

  function aoSalvarConteudo(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const titulo = sanitizarTexto(tituloConteudo);
    const descricao = sanitizarTextoMultilinha(descricaoConteudo);
    if (!titulo || !descricao) {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Preencha título e descrição para salvar.' });
      return;
    }
    mutacaoConteudo.mutate({ titulo, descricao });
  }

  function aoSalvarFuncionario(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const nome = sanitizarTexto(nomeFuncionario);
    const celular = sanitizarCelular(celularFuncionario);
    const obra = sanitizarTexto(obraFuncionario);
    if (!nome || !celular || !obra) {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Preencha todas as informações do funcionário.' });
      return;
    }
    mutacaoFuncionario.mutate({ nome, celular, obra });
  }

  function aoRemoverFuncionario(id: number) {
    if (confirm('Tem certeza de que deseja remover este funcionário?')) {
      mutacaoRemocaoFuncionario.mutate(id);
    }
  }

  function aoRemoverConteudo(id: number) {
    if (confirm('Tem certeza de que deseja remover este conteúdo?')) {
      mutacaoRemocaoConteudo.mutate(id);
    }
  }

  function aoEnviarDds(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    if (!consultaConteudos.data || consultaConteudos.data.length === 0) {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Cadastre um conteúdo antes de enviar o DDS.' });
      return;
    }

    const campoConteudo = evento.currentTarget.elements.namedItem('conteudo') as
      | HTMLSelectElement
      | null;
    const conteudoSelecionado = campoConteudo?.value ?? '';
    const listaDestinatarios = destinatariosSelecionados;

    if (!conteudoSelecionado) {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Selecione um conteúdo para o DDS.' });
      return;
    }

    if (listaDestinatarios.length === 0) {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Selecione uma obra com funcionários cadastrados.' });
      return;
    }

    const dataEnvioHoje = new Date().toISOString().split('T')[0];

    mutacaoEnvio.mutate({
      conteudoId: Number(conteudoSelecionado),
      funcionariosIds: listaDestinatarios.map((destinatario) => destinatario.id),
      dataEnvio: dataEnvioHoje,
    });
  }

  const destinatariosSelecionados = useMemo(() => {
    const funcionarios = consultaFuncionarios.data ?? [];
    if (obraSelecionada === 'todos') {
      return funcionarios;
    }
    return funcionarios.filter((funcionario) => funcionario.obra === obraSelecionada);
  }, [consultaFuncionarios.data, obraSelecionada]);

  const totalEnvios = consultaEnvios.data?.length ?? 0;
  const totalConfirmados =
    consultaEnvios.data?.filter((envio) => envio.status === 'CONFIRMADO').length ?? 0;
  const totalPendentes = totalEnvios - totalConfirmados;

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="sticky top-0 z-40 bg-white shadow-md">
        <nav className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
          <div className="text-2xl font-bold text-blue-600">
            DDS Facil
            <span className="ml-2 hidden text-sm font-normal text-gray-500 md:inline">Painel de Controle</span>
          </div>
          <div className="flex items-center gap-3">
            <span className="hidden text-gray-700 sm:inline">Olá, BSM</span>
            <img
              src="https://placehold.co/40x40/E2E8F0/4A5568?text=BSM"
              alt="Avatar BSM"
              className="h-10 w-10 rounded-full border-2 border-gray-200"
            />
          </div>
        </nav>
        <div className="border-b border-gray-200">
          <div className="mx-auto flex max-w-6xl gap-6 px-4">
            <BotaoAba ativo={abaAtiva === 'dashboard'} onClick={() => definirAbaAtiva('dashboard')}>
              Dashboard
            </BotaoAba>
            <BotaoAba ativo={abaAtiva === 'enviar'} onClick={() => definirAbaAtiva('enviar')}>
              Enviar DDS
            </BotaoAba>
            <BotaoAba
              ativo={abaAtiva === 'funcionarios'}
              onClick={() => definirAbaAtiva('funcionarios')}
            >
              Funcionários
            </BotaoAba>
            <BotaoAba ativo={abaAtiva === 'conteudo'} onClick={() => definirAbaAtiva('conteudo')}>
              Conteúdo DDS
            </BotaoAba>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8">
        <section className={abaAtiva === 'dashboard' ? 'block' : 'hidden'}>
          <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <h1 className="text-3xl font-bold text-gray-900">Dashboard: DDS do Dia</h1>
            <input
              type="date"
              value={dataDashboard}
              onChange={(evento) => definirDataDashboard(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200 md:w-auto"
            />
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <CartaoResumo titulo="Total de Envios" corTexto="text-blue-600" valor={totalEnvios} />
            <CartaoResumo titulo="Confirmações" corTexto="text-green-600" valor={totalConfirmados} />
            <CartaoResumo titulo="Pendentes" corTexto="text-red-600" valor={totalPendentes} />
          </div>

          <div className="mt-6 rounded-2xl border border-gray-100 bg-white shadow">
            <div className="border-b border-gray-200 px-6 py-4">
              <h2 className="text-xl font-semibold text-gray-900">Status por Funcionário</h2>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <CabecalhoTabela texto="Funcionário" />
                    <CabecalhoTabela texto="Obra" />
                    <CabecalhoTabela texto="Status" />
                    <CabecalhoTabela texto="Confirmado em" />
                    <CabecalhoTabela texto="Ações" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {consultaEnvios.isLoading && (
                    <LinhaTabelaMensagem mensagem="Carregando envios..." />
                  )}
                  {consultaEnvios.isError && (
                    <LinhaTabelaMensagem mensagem="Não foi possível carregar os envios." />
                  )}
                  {!consultaEnvios.isLoading &&
                    !consultaEnvios.isError &&
                    (consultaEnvios.data?.length ?? 0) === 0 && (
                      <LinhaTabelaMensagem mensagem="Nenhum DDS enviado na data selecionada." />
                    )}
                  {consultaEnvios.data?.map((envio) => {
                    const confirmado = envio.status === 'CONFIRMADO';
                    return (
                      <tr key={envio.id} className="hover:bg-gray-50">
                        <CelulaTabela texto={envio.nomeFuncionario} destaque />
                        <CelulaTabela texto={envio.obra} />
                        <td className="px-6 py-4 text-sm">
                          <span
                            className={`rounded-full px-3 py-1 text-xs font-semibold ${
                              confirmado
                                ? 'bg-green-200 text-green-800'
                                : 'bg-red-200 text-red-800'
                            }`}
                          >
                            {confirmado ? 'Confirmado' : 'Pendente'}
                          </span>
                        </td>
                        <CelulaTabela
                          texto={
                            envio.momentoConfirmacao
                              ? new Date(envio.momentoConfirmacao).toLocaleTimeString('pt-BR', {
                                  hour: '2-digit',
                                  minute: '2-digit',
                                })
                              : '---'
                          }
                        />
                        <td className="px-6 py-4 text-sm">
                          {confirmado ? (
                            <span className="text-gray-400">OK</span>
                          ) : (
                            <button
                              type="button"
                              className="font-medium text-blue-600 transition hover:text-blue-800"
                              onClick={() => mutacaoConfirmacao.mutate(envio.id)}
                              disabled={mutacaoConfirmacao.isPending}
                            >
                              {mutacaoConfirmacao.isPending ? 'Confirmando...' : 'Simular Confirmação'}
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </section>

        <section className={abaAtiva === 'enviar' ? 'block' : 'hidden'}>
          <h1 className="mb-6 text-3xl font-bold text-gray-900">Enviar DDS</h1>
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <div className="space-y-6 rounded-2xl border border-gray-100 bg-white p-8 shadow">
              <form className="space-y-6" onSubmit={aoEnviarDds}>
                <div>
                  <label htmlFor="conteudo" className="mb-2 block text-sm font-medium text-gray-700">
                    1. Selecione o Conteúdo do DDS
                  </label>
                  <select
                    id="conteudo"
                    name="conteudo"
                    className="w-full rounded-lg border border-gray-300 bg-white p-3 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                    disabled={consultaConteudos.isLoading || consultaConteudos.isError}
                  >
                    <option value="">Selecione...</option>
                    {consultaConteudos.data?.map((conteudo) => (
                      <option key={conteudo.id} value={conteudo.id}>
                        {conteudo.titulo}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label htmlFor="obra" className="mb-2 block text-sm font-medium text-gray-700">
                    2. Selecione os Destinatários
                  </label>
                  <select
                    id="obra"
                    name="obra"
                    value={obraSelecionada}
                    onChange={(evento) => definirObraSelecionada(evento.target.value)}
                    className="w-full rounded-lg border border-gray-300 bg-white p-3 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                    disabled={consultaObras.isLoading || consultaObras.isError}
                  >
                    <option value="todos">Todas as obras</option>
                    {consultaObras.data?.map((obra) => (
                      <option key={obra} value={obra}>
                        {obra}
                      </option>
                    ))}
                  </select>
                </div>

                <button
                  type="submit"
                  className="w-full rounded-lg bg-blue-600 px-8 py-3 text-lg font-semibold text-white shadow transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
                  disabled={mutacaoEnvio.isPending}
                >
                  {mutacaoEnvio.isPending ? 'Enviando...' : 'Enviar DDS por SMS'}
                </button>
              </form>

              <p className="text-center text-sm text-gray-500">
                O envio registrará os destinatários selecionados e atualizará o dashboard automaticamente.
              </p>
            </div>

            <div className="rounded-2xl border border-gray-100 bg-white p-8 shadow">
              <h3 className="text-xl font-semibold text-gray-900">Destinatários do Envio</h3>
              <p className="text-sm text-gray-500">A lista abaixo é carregada diretamente do backend.</p>
              <div className="mt-4 max-h-96 space-y-3 overflow-y-auto">
                {consultaFuncionarios.isLoading && (
                  <p className="text-gray-500">Carregando funcionários...</p>
                )}
                {consultaFuncionarios.isError && (
                  <p className="text-red-600">Não foi possível carregar os funcionários.</p>
                )}
                {!consultaFuncionarios.isLoading && !consultaFuncionarios.isError && destinatariosSelecionados.length === 0 && (
                  <p className="text-gray-500">Nenhum funcionário encontrado para a seleção atual.</p>
                )}
                {destinatariosSelecionados.map((funcionario) => (
                  <div
                    key={funcionario.id}
                    className="flex items-center justify-between rounded-lg bg-gray-50 p-3"
                  >
                    <span className="font-medium text-gray-800">{funcionario.nome}</span>
                    <span className="text-sm text-gray-500">{funcionario.celular}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        <section className={abaAtiva === 'funcionarios' ? 'block' : 'hidden'}>
          <h1 className="mb-6 text-3xl font-bold text-gray-900">Gerenciar Funcionários</h1>
          <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
            <div className="md:col-span-1">
              <div className="sticky top-28 rounded-2xl border border-gray-100 bg-white p-8 shadow">
                <h3 className="text-xl font-semibold text-gray-900">Novo Funcionário</h3>
                <p className="text-sm text-gray-500">Cadastre colaboradores para receberem o DDS.</p>
                <form className="mt-6 space-y-4" onSubmit={aoSalvarFuncionario}>
                  <div>
                    <label htmlFor="nome-funcionario" className="block text-sm font-medium text-gray-700">
                      Nome
                    </label>
                    <input
                      id="nome-funcionario"
                      name="nome"
                      value={nomeFuncionario}
                      onChange={(evento) => definirNomeFuncionario(evento.target.value)}
                      className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                      maxLength={120}
                    />
                  </div>
                  <div>
                    <label htmlFor="celular-funcionario" className="block text-sm font-medium text-gray-700">
                      Celular (com DDD)
                    </label>
                    <input
                      id="celular-funcionario"
                      name="celular"
                      value={celularFuncionario}
                      onChange={(evento) => definirCelularFuncionario(sanitizarCelular(evento.target.value))}
                      className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                      maxLength={20}
                      placeholder="(21) 99999-8888"
                    />
                  </div>
                  <div>
                    <label htmlFor="obra-funcionario" className="block text-sm font-medium text-gray-700">
                      Obra
                    </label>
                    <input
                      id="obra-funcionario"
                      name="obra"
                      value={obraFuncionario}
                      onChange={(evento) => definirObraFuncionario(evento.target.value)}
                      className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                      maxLength={120}
                    />
                  </div>
                  <button
                    type="submit"
                    className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
                    disabled={mutacaoFuncionario.isPending}
                  >
                    {mutacaoFuncionario.isPending ? 'Salvando...' : 'Salvar Funcionário'}
                  </button>
                </form>
              </div>
            </div>
            <div className="md:col-span-2">
              <div className="rounded-2xl border border-gray-100 bg-white shadow">
                <div className="border-b border-gray-200 px-6 py-4">
                  <h3 className="text-xl font-semibold text-gray-900">Funcionários Cadastrados</h3>
                  <p className="text-sm text-gray-500">Todos os dados são carregados direto do backend.</p>
                </div>
                <div className="overflow-x-auto">
                  <table className="min-w-full">
                    <thead className="bg-gray-50">
                      <tr>
                        <CabecalhoTabela texto="Nome" />
                        <CabecalhoTabela texto="Celular" />
                        <CabecalhoTabela texto="Obra" />
                        <CabecalhoTabela texto="Ações" />
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                      {consultaFuncionarios.isLoading && (
                        <LinhaTabelaMensagem mensagem="Carregando funcionários..." colunas={4} />
                      )}
                      {consultaFuncionarios.isError && (
                        <LinhaTabelaMensagem mensagem="Não foi possível carregar os funcionários." colunas={4} />
                      )}
                      {!consultaFuncionarios.isLoading &&
                        !consultaFuncionarios.isError &&
                        (consultaFuncionarios.data?.length ?? 0) === 0 && (
                          <LinhaTabelaMensagem
                            mensagem="Nenhum funcionário cadastrado até o momento."
                            colunas={4}
                          />
                        )}
                      {consultaFuncionarios.data?.map((funcionario) => (
                        <tr key={funcionario.id} className="hover:bg-gray-50">
                          <CelulaTabela texto={funcionario.nome} destaque />
                          <CelulaTabela texto={funcionario.celular} />
                          <CelulaTabela texto={funcionario.obra} />
                          <td className="px-6 py-4 text-sm">
                            <button
                              type="button"
                              className="font-medium text-red-600 transition hover:text-red-800"
                              onClick={() => aoRemoverFuncionario(funcionario.id)}
                              disabled={mutacaoRemocaoFuncionario.isPending}
                            >
                              {mutacaoRemocaoFuncionario.isPending ? 'Removendo...' : 'Remover'}
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className={abaAtiva === 'conteudo' ? 'block' : 'hidden'}>
          <h1 className="mb-6 text-3xl font-bold text-gray-900">Gerenciar Conteúdo DDS</h1>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <div className="lg:col-span-1">
              <div className="rounded-2xl border border-gray-100 bg-white p-8 shadow">
                <h2 className="text-xl font-semibold text-gray-900">Novo Conteúdo de DDS</h2>
                <p className="text-sm text-gray-500">Preencha as informações para cadastrar um novo DDS.</p>
                <form className="mt-6 space-y-4" onSubmit={aoSalvarConteudo}>
                  <div>
                    <label htmlFor="titulo" className="block text-sm font-medium text-gray-700">
                      Título
                    </label>
                    <input
                      id="titulo"
                      name="titulo"
                      value={tituloConteudo}
                      onChange={(evento) => definirTituloConteudo(evento.target.value)}
                      className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
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
                      value={descricaoConteudo}
                      onChange={(evento) => definirDescricaoConteudo(evento.target.value)}
                      className="mt-1 h-32 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                      maxLength={2000}
                    />
                  </div>
                  <button
                    type="submit"
                    className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
                    disabled={mutacaoConteudo.isPending}
                  >
                    {mutacaoConteudo.isPending ? 'Salvando...' : 'Salvar Conteúdo'}
                  </button>
                </form>
              </div>
            </div>
            <div className="lg:col-span-2">
              <div className="rounded-2xl border border-gray-100 bg-white p-6 shadow">
                <div className="flex items-center justify-between">
                  <div>
                    <h2 className="text-xl font-semibold text-gray-900">Conteúdos cadastrados</h2>
                    <p className="text-sm text-gray-500">Gerencie a biblioteca de DDS disponível.</p>
                  </div>
                  <span className="rounded-full bg-blue-50 px-3 py-1 text-sm font-medium text-blue-600">
                    {consultaConteudos.data?.length ?? 0} itens
                  </span>
                </div>
                <div className="mt-6 space-y-4">
                  {consultaConteudos.isLoading && <p className="text-gray-500">Carregando conteúdos...</p>}
                  {consultaConteudos.isError && (
                    <p className="text-red-600">Não foi possível carregar os conteúdos.</p>
                  )}
                  {!consultaConteudos.isLoading &&
                    !consultaConteudos.isError &&
                    (consultaConteudos.data?.length ?? 0) === 0 && (
                      <p className="text-gray-500">Nenhum conteúdo cadastrado até o momento.</p>
                    )}
                  {consultaConteudos.data?.map((conteudo) => (
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
                          onClick={() => aoRemoverConteudo(conteudo.id)}
                          disabled={mutacaoRemocaoConteudo.isPending}
                        >
                          {mutacaoRemocaoConteudo.isPending ? 'Removendo...' : 'Remover'}
                        </button>
                      </header>
                    </article>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>

      {notificacao && (
        <div
          className={`fixed bottom-6 right-6 rounded-lg px-5 py-3 text-white shadow-lg transition ${
            notificacao.tipo === 'sucesso' ? 'bg-green-600' : 'bg-red-600'
          }`}
        >
          {notificacao.mensagem}
        </div>
      )}
    </div>
  );
}

type BotaoAbaProps = {
  ativo: boolean;
  onClick: () => void;
  children: ReactNode;
};

function BotaoAba({ ativo, onClick, children }: BotaoAbaProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`-mb-px border-b-2 px-1 py-3 text-sm font-medium transition ${
        ativo
          ? 'border-blue-600 text-blue-600'
          : 'border-transparent text-gray-600 hover:text-blue-600'
      }`}
    >
      {children}
    </button>
  );
}

type CartaoResumoProps = {
  titulo: string;
  valor: number;
  corTexto: string;
};

function CartaoResumo({ titulo, valor, corTexto }: CartaoResumoProps) {
  return (
    <div className="rounded-2xl border border-gray-100 bg-white p-6 shadow">
      <h3 className="text-sm font-medium uppercase text-gray-500">{titulo}</h3>
      <p className={`mt-2 text-4xl font-extrabold ${corTexto}`}>{valor}</p>
    </div>
  );
}

type CabecalhoTabelaProps = {
  texto: string;
};

function CabecalhoTabela({ texto }: CabecalhoTabelaProps) {
  return (
    <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
      {texto}
    </th>
  );
}

type CelulaTabelaProps = {
  texto: string;
  destaque?: boolean;
};

function CelulaTabela({ texto, destaque = false }: CelulaTabelaProps) {
  return (
    <td className={`px-6 py-4 text-sm ${destaque ? 'font-medium text-gray-900' : 'text-gray-600'}`}>{texto}</td>
  );
}

type LinhaTabelaMensagemProps = {
  mensagem: string;
  colunas?: number;
};

function LinhaTabelaMensagem({ mensagem, colunas = 5 }: LinhaTabelaMensagemProps) {
  return (
    <tr>
      <td colSpan={colunas} className="px-6 py-8 text-center text-sm text-gray-500">
        {mensagem}
      </td>
    </tr>
  );
}
