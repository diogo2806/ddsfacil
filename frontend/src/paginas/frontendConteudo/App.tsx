// Arquivo: frontend/src/paginas/frontendConteudo/App.tsx
import { useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
// react-query usage moved into hooks
import { useLocation, useNavigate } from 'react-router-dom';
import { useConteudos } from '../../hooks/useConteudos';
import { useFuncionarios } from '../../hooks/useFuncionarios';
import { useLocais } from '../../hooks/useLocais';
import { useEnvios } from '../../hooks/useEnvios';
import { useLocalAdmin } from '../../hooks/useLocalAdmin';
import TelaDivulgacao from './TelaDivulgacao';
import AdminLocais from './components/AdminLocais';

import { sanitizarTexto, sanitizarTextoMultilinha, sanitizarCelular } from '../../utils/validador';

type AbaPainel = 'dashboard' | 'enviar' | 'funcionarios' | 'conteudo';

type Notificacao = {
  tipo: 'sucesso' | 'erro';
  mensagem: string;
};

export default function App() {
  const localizacao = useLocation();
  const navegador = useNavigate();
  // clienteConsulta moved to hooks; App não precisa direto do queryClient
  const [telaAtual, definirTelaAtual] = useState<'divulgacao' | 'painel'>(() =>
    localizacao.pathname === '/painel' ? 'painel' : 'divulgacao',
  );
  const [abaAtiva, definirAbaAtiva] = useState<AbaPainel>('dashboard');
  const [dataDashboard, definirDataDashboard] = useState<string>(
    () => new Date().toISOString().split('T')[0],
  );
  // [REATORADO] Estado para localId (string 'todos' ou '1', '2', etc.)
  const [localIdSelecionado, definirLocalIdSelecionado] = useState<string>('todos');

  const [tituloConteudo, definirTituloConteudo] = useState('');
  const [descricaoConteudo, definirDescricaoConteudo] = useState('');
  const [tipoConteudo, definirTipoConteudo] = useState<'TEXTO' | 'LINK' | 'ARQUIVO'>('TEXTO');
  const [urlConteudo, definirUrlConteudo] = useState('');
  const [arquivoConteudo, definirArquivoConteudo] = useState<File | null>(null);

  const [nomeFuncionario, definirNomeFuncionario] = useState('');
  const [celularFuncionario, definirCelularFuncionario] = useState('');
  // [REATORADO] Estado para o ID do local no formulário (string vazia ou '1', '2', etc.)
  const [localIdFuncionario, definirLocalIdFuncionario] = useState<string>('');
  // estados para criar tipoLocal e local
  const [novoTipoLocal, definirNovoTipoLocal] = useState('');
  const [novoLocalNome, definirNovoLocalNome] = useState('');
  const [novoLocalTipoId, definirNovoLocalTipoId] = useState<number | ''>('');
  // estado para aba da administração (tipos | locais) e estados de edição
  const [abaAdmin, definirAbaAdmin] = useState<'tipos' | 'locais'>('tipos');
  const [editandoTipoId, definirEditandoTipoId] = useState<number | null>(null);
  const [editandoTipoNome, definirEditandoTipoNome] = useState<string>('');
  const [editandoLocalId, definirEditandoLocalId] = useState<number | null>(null);
  const [editandoLocalNome, definirEditandoLocalNome] = useState<string>('');
  const [editandoLocalTipoId, definirEditandoLocalTipoId] = useState<number | ''>('');
  const [notificacao, definirNotificacao] = useState<Notificacao | null>(null);
  const referenciaNotificacao = useRef<number>();

  useEffect(() => {
    definirTelaAtual(localizacao.pathname === '/painel' ? 'painel' : 'divulgacao');
  }, [localizacao.pathname]);

  function tratarSolicitacaoLogin() {
    definirTelaAtual('painel');
    navegador('/painel');
  }

  // Conteúdos
  const {
    consultaConteudos,
    mutacaoCriar: mutacaoCriarConteudo,
    mutacaoCriarComArquivo,
    mutacaoRemover: mutacaoRemocaoConteudo,
  } = useConteudos({ enabled: telaAtual === 'painel' });
  const { consultaFuncionarios, mutacaoCriar: mutacaoFuncionario, mutacaoRemover: mutacaoRemocaoFuncionario } =
    useFuncionarios({ enabled: telaAtual === 'painel' });

  // [REATORADO] Substitui consultaObras por consultaLocaisTrabalho
  const { consultaLocaisTrabalho } = useLocais({ enabled: telaAtual === 'painel' });

  const { consultaEnvios, mutacaoCriar: mutacaoEnvio } = useEnvios({ enabled: telaAtual === 'painel' });
  const {
    consultaTipos,
    consultaLocais: consultaLocaisAdmin,
    mutacaoCriarTipo,
    mutacaoRemoverTipo,
    mutacaoCriarLocal,
    mutacaoRemoverLocal,
    mutacaoAtualizarTipo,
    mutacaoAtualizarLocal,
  } = useLocalAdmin();

  // mutações de funcionários e envios são providas pelos hooks (useFuncionarios e useEnvios)

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
    if (!titulo) {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Preencha o título para salvar.' });
      return;
    }

    if (tipoConteudo === 'TEXTO') {
      if (!descricao) {
        exibirNotificacao({ tipo: 'erro', mensagem: 'Preencha a descrição para conteúdo de texto.' });
        return;
      }
      mutacaoCriarConteudo.mutate({ titulo, descricao, tipo: 'TEXTO' });
      return;
    }

    if (tipoConteudo === 'LINK') {
      const url = urlConteudo.trim();
      if (!url || !/^https?:\/\//i.test(url)) {
        exibirNotificacao({ tipo: 'erro', mensagem: 'Informe uma URL válida iniciando com http:// ou https://.' });
        return;
      }
      mutacaoCriarConteudo.mutate({ titulo, descricao: descricao ?? '', tipo: 'LINK', url });
      return;
    }

    // ARQUIVO
    if (tipoConteudo === 'ARQUIVO') {
      if (!arquivoConteudo) {
        exibirNotificacao({ tipo: 'erro', mensagem: 'Selecione um arquivo para upload.' });
        return;
      }
      // usar mutacao customizada via serviço
      // chamar criando diretamente pela função que faz FormData
      (async () => {
        try {
          await mutacaoCriarComArquivo.mutateAsync({ dados: { titulo, descricao: descricao ?? '', tipo: 'ARQUIVO' }, arquivo: arquivoConteudo! });
          // hook já invalida consultas; resetar campos locais
          definirTituloConteudo('');
          definirDescricaoConteudo('');
          definirArquivoConteudo(null);
          definirUrlConteudo('');
          definirTipoConteudo('TEXTO');
          exibirNotificacao({ tipo: 'sucesso', mensagem: 'Conteúdo com arquivo salvo com sucesso.' });
        } catch {
          exibirNotificacao({ tipo: 'erro', mensagem: 'Não foi possível salvar o conteúdo com arquivo.' });
        }
      })();
      return;
    }
  }

  // [REATORADO]
  function aoSalvarFuncionario(evento: React.FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const nome = sanitizarTexto(nomeFuncionario);
    const celular = sanitizarCelular(celularFuncionario);
    // [REATORADO] Validação e envio usando localIdFuncionario
    if (!nome || !celular || !localIdFuncionario) {
      exibirNotificacao({ tipo: 'erro', mensagem: 'Preencha todas as informações do funcionário, incluindo o Local de Trabalho.' });
      return;
    }
    mutacaoFuncionario.mutate({ nome, celular, localTrabalhoId: Number(localIdFuncionario) });
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
      exibirNotificacao({ tipo: 'erro', mensagem: 'Selecione um local com funcionários cadastrados.' }); // [REATORADO]
      return;
    }

    const dataEnvioHoje = new Date().toISOString().split('T')[0];
    mutacaoEnvio.mutate({
      conteudoId: Number(conteudoSelecionado),
      funcionariosIds: listaDestinatarios.map((destinatario) => destinatario.id),
      dataEnvio: dataEnvioHoje,
    });
  }

  // [REATORADO] Filtra por localTrabalhoId (número)
  const destinatariosSelecionados = useMemo(() => {
    const funcionarios = consultaFuncionarios.data ?? [];
    if (localIdSelecionado === 'todos') {
      return funcionarios;
    }
    const idNum = Number(localIdSelecionado);
    return funcionarios.filter((funcionario) => funcionario.localTrabalhoId === idNum);
  }, [consultaFuncionarios.data, localIdSelecionado]);

  if (telaAtual === 'divulgacao') {
    return <TelaDivulgacao aoSolicitarLogin={tratarSolicitacaoLogin} />;
  }

  const totalEnvios = consultaEnvios.data?.length ?? 0;
  // A verificação de status "Confirmado" [cite: 494] está correta e alinhada com a Regra 15 (usando a descrição)
  const totalConfirmados =
    consultaEnvios.data?.filter((envio) => envio.status === 'Confirmado').length ??
    0;
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
            {/* Admin quick-tabs (Tipos / Locais) shown in the main header as requested */}
            <BotaoAba ativo={abaAdmin === 'tipos'} onClick={() => definirAbaAdmin('tipos')}>
              Tipos de Local
            </BotaoAba>
            <BotaoAba ativo={abaAdmin === 'locais'} onClick={() => definirAbaAdmin('locais')}>
              Locais
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
                    // A checagem 'Confirmado' [cite: 509] está correta
                    const confirmado = envio.status === 'Confirmado';
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

                {/* [REATORADO] Select de Local de Trabalho */}
                <div>
                  <label htmlFor="local-trabalho-envio" className="mb-2 block text-sm font-medium text-gray-700">
                    2. Selecione os Destinatários (Local)
                  </label>
                  <select
                    id="local-trabalho-envio"
                    name="local-trabalho-envio"
                    value={localIdSelecionado}
                    onChange={(evento) => definirLocalIdSelecionado(evento.target.value)}
                    className="w-full rounded-lg border border-gray-300 bg-white p-3 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                    disabled={consultaLocaisTrabalho.isLoading || consultaLocaisTrabalho.isError}
                  >
                    <option value="todos">Todos os Locais</option>
                    {consultaLocaisTrabalho.data?.map((local) => (
                      <option key={local.id} value={local.id}>
                        {local.nome} ({local.tipoLocalNome})
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
              <p className="text-sm text-gray-500">A lista abaixo é filtrada com base na seleção.</p>
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

                  {/* [REATORADO] Substituído Input de Obra por Select de Local de Trabalho */}
                  <div>
                    <label htmlFor="local-funcionario" className="block text-sm font-medium text-gray-700">
                      Local de Trabalho
                    </label>
                    <select
                      id="local-funcionario"
                      name="local"
                      value={localIdFuncionario}
                      onChange={(evento) => definirLocalIdFuncionario(evento.target.value)}
                      className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                      disabled={consultaLocaisTrabalho.isLoading || consultaLocaisTrabalho.isError}
                    >
                      <option value="">Selecione...</option>
                      {consultaLocaisTrabalho.data?.map((local) => (
                        <option key={local.id} value={local.id}>
                          {local.nome}
                        </option>
                      ))}
                    </select>
                  </div>

                  <button
                    type="submit"
                    className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
                    disabled={mutacaoFuncionario.isPending || consultaLocaisTrabalho.isLoading}
                  >
                    {mutacaoFuncionario.isPending ? 'Salvando...' : 'Salvar Funcionário'}
                  </button>
                </form>

                <AdminLocais
                  aba={abaAdmin}
                  definirAba={definirAbaAdmin}
                  consultaTipos={consultaTipos}
                  consultaLocais={consultaLocaisAdmin}
                  mutacaoCriarTipo={mutacaoCriarTipo}
                  mutacaoRemoverTipo={mutacaoRemoverTipo}
                  mutacaoAtualizarTipo={mutacaoAtualizarTipo}
                  mutacaoCriarLocal={mutacaoCriarLocal}
                  mutacaoRemoverLocal={mutacaoRemoverLocal}
                  mutacaoAtualizarLocal={mutacaoAtualizarLocal}
                  editandoTipoId={editandoTipoId}
                  definirEditandoTipoId={definirEditandoTipoId}
                  editandoTipoNome={editandoTipoNome}
                  definirEditandoTipoNome={definirEditandoTipoNome}
                  novoTipoLocal={novoTipoLocal}
                  definirNovoTipoLocal={definirNovoTipoLocal}
                  editandoLocalId={editandoLocalId}
                  definirEditandoLocalId={definirEditandoLocalId}
                  editandoLocalNome={editandoLocalNome}
                  definirEditandoLocalNome={definirEditandoLocalNome}
                  editandoLocalTipoId={editandoLocalTipoId}
                  definirEditandoLocalTipoId={definirEditandoLocalTipoId}
                  novoLocalNome={novoLocalNome}
                  definirNovoLocalNome={definirNovoLocalNome}
                  novoLocalTipoId={novoLocalTipoId}
                  definirNovoLocalTipoId={definirNovoLocalTipoId}
                  exibirNotificacao={exibirNotificacao}
                />
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
                        {/* [REATORADO] */}
                        <CabecalhoTabela texto="Local de Trabalho" />
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
                          {/* [REATORADO] Exibe o nome do local */}
                          <CelulaTabela texto={funcionario.localTrabalhoNome} />
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
                    <label className="block text-sm font-medium text-gray-700">Tipo de conteúdo</label>
                    <div className="mt-2 flex gap-4">
                      <label className="inline-flex items-center gap-2">
                        <input
                          type="radio"
                          name="tipo"
                          value="TEXTO"
                          checked={tipoConteudo === 'TEXTO'}
                          onChange={() => definirTipoConteudo('TEXTO')}
                        />
                        <span className="text-sm">Texto</span>
                      </label>
                      <label className="inline-flex items-center gap-2">
                        <input
                          type="radio"
                          name="tipo"
                          value="LINK"
                          checked={tipoConteudo === 'LINK'}
                          onChange={() => definirTipoConteudo('LINK')}
                        />
                        <span className="text-sm">Link</span>
                      </label>
                      <label className="inline-flex items-center gap-2">
                        <input
                          type="radio"
                          name="tipo"
                          value="ARQUIVO"
                          checked={tipoConteudo === 'ARQUIVO'}
                          onChange={() => definirTipoConteudo('ARQUIVO')}
                        />
                        <span className="text-sm">Arquivo</span>
                      </label>
                    </div>
                  </div>

                  {tipoConteudo === 'TEXTO' && (
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
                  )}

                  {tipoConteudo === 'LINK' && (
                    <div>
                      <label htmlFor="url" className="block text-sm font-medium text-gray-700">
                        URL
                      </label>
                      <input
                        id="url"
                        name="url"
                        value={urlConteudo}
                        onChange={(evento) => definirUrlConteudo(evento.target.value)}
                        placeholder="https://..."
                        className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-gray-700 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
                      />
                    </div>
                  )}

                  {tipoConteudo === 'ARQUIVO' && (
                    <div>
                      <label htmlFor="arquivo" className="block text-sm font-medium text-gray-700">
                        Arquivo
                      </label>
                      <input
                        id="arquivo"
                        name="arquivo"
                        type="file"
                        onChange={(e) => definirArquivoConteudo(e.target.files ? e.target.files[0] : null)}
                        className="mt-1 w-full text-sm text-gray-700"
                      />
                    </div>
                  )}

                  <button
                    type="submit"
                    className="w-full rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
                    disabled={mutacaoCriarConteudo.isPending || mutacaoCriarComArquivo.isPending}
                  >
                    {mutacaoCriarConteudo.isPending || mutacaoCriarComArquivo.isPending ? 'Salvando...' : 'Salvar Conteúdo'}
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
                          {conteudo.tipo === 'LINK' && conteudo.url ? (
                            <p className="mt-1 text-sm text-blue-600">
                              <a href={conteudo.url} target="_blank" rel="noreferrer" className="underline">
                                {conteudo.url}
                              </a>
                            </p>
                          ) : conteudo.tipo === 'ARQUIVO' && conteudo.arquivoPath ? (
                            <p className="mt-1 text-sm text-blue-600">
                              <a href={conteudo.arquivoPath} target="_blank" rel="noreferrer" className="underline">
                                {conteudo.arquivoNome ?? 'Arquivo'}
                              </a>
                            </p>
                          ) : (
                            <p className="mt-1 text-sm text-gray-600">{conteudo.descricao}</p>
                          )}
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
function LinhaTabelaMensagem({ mensagem, colunas = 4 }: LinhaTabelaMensagemProps) {
  return (
    <tr>
      <td colSpan={colunas} className="px-6 py-8 text-center text-sm text-gray-500">
        {mensagem}
      </td>
    </tr>
  );
}