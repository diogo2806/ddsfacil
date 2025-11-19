// Arquivo: frontend/src/paginas/frontendConteudo/App.tsx
import { useEffect, useRef, useState, type ReactNode } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  AbaPainel,
  TelaApp,
  TipoNotificacao,
} from '../../types/enums';
import TelaDivulgacao from './TelaDivulgacao';
import TelaAutenticacao from './TelaAutenticacao';
import type { RespostaAutenticacao, SessaoUsuario } from '../../types/autenticacao';
import {
  carregarSessaoUsuario,
  converterRespostaParaSessao,
  removerSessaoUsuario,
  salvarSessaoUsuario,
} from '../../configuracao/sessaoUsuario';
import { configurarCabecalhoAutorizacaoJwt } from '../../servicos/clienteHttp';
import { definirEmpresaIdAtual } from '../../configuracao/empresa';

// Importe as "páginas" (abas)
import AbaDashboard from './abas/AbaDashboard';
import AbaEnviarDds from './abas/AbaEnviarDds';
import AbaFuncionarios from './abas/AbaFuncionarios';
import AbaConteudos from './abas/AbaConteudos';
// [NOVOS IMPORTS]
import AbaTiposLocal from './abas/AbaTiposLocal';
import AbaLocais from './abas/AbaLocais';
import BadgeSaldo from '../../componentes/BadgeSaldo'; // Importe o componente

type Notificacao = {
  tipo: TipoNotificacao;
  mensagem: string;
};

export default function App() {
  const localizacao = useLocation();
  const navegador = useNavigate();

  const [telaAtual, definirTelaAtual] = useState<TelaApp>(() =>
    localizacao.pathname === '/painel' ? TelaApp.AUTENTICACAO : TelaApp.DIVULGACAO,
  );
  const [sessaoUsuario, definirSessaoUsuario] = useState<SessaoUsuario | null>(() => carregarSessaoUsuario());

  const [abaAtiva, definirAbaAtiva] = useState<AbaPainel>(AbaPainel.DASHBOARD);

  const [notificacao, definirNotificacao] = useState<Notificacao | null>(null);
  const referenciaNotificacao = useRef<number>();

  const usuarioAutenticado = Boolean(sessaoUsuario);

  useEffect(() => {
    if (localizacao.pathname === '/painel') {
      definirTelaAtual(usuarioAutenticado ? TelaApp.PAINEL : TelaApp.AUTENTICACAO);
      return;
    }
    definirTelaAtual(TelaApp.DIVULGACAO);
  }, [localizacao.pathname, usuarioAutenticado]);

  useEffect(() => {
    if (sessaoUsuario) {
      configurarCabecalhoAutorizacaoJwt(sessaoUsuario.token);
      definirEmpresaIdAtual(sessaoUsuario.empresaId);
    } else {
      configurarCabecalhoAutorizacaoJwt(null);
    }
  }, [sessaoUsuario]);

  function tratarSolicitacaoLogin() {
    if (usuarioAutenticado) {
      definirTelaAtual(TelaApp.PAINEL);
      navegador('/painel');
      return;
    }
    definirTelaAtual(TelaApp.AUTENTICACAO);
    navegador('/painel');
  }

  function lidarAutenticacaoBemSucedida(resposta: RespostaAutenticacao) {
    const sessaoNormalizada = converterRespostaParaSessao(resposta);
    salvarSessaoUsuario(sessaoNormalizada);
    definirSessaoUsuario(sessaoNormalizada);
    definirTelaAtual(TelaApp.PAINEL);
    navegador('/painel');
  }

  function lidarCancelamentoAutenticacao() {
    definirTelaAtual(TelaApp.DIVULGACAO);
    navegador('/');
  }

  function encerrarSessao() {
    removerSessaoUsuario();
    definirSessaoUsuario(null);
    definirAbaAtiva(AbaPainel.DASHBOARD);
    definirTelaAtual(TelaApp.DIVULGACAO);
    navegador('/');
  }

  function exibirNotificacao(novaNotificacao: Notificacao) {
    definirNotificacao(novaNotificacao);
    if (referenciaNotificacao.current) {
      window.clearTimeout(referenciaNotificacao.current);
    }
    referenciaNotificacao.current = window.setTimeout(() => {
      definirNotificacao(null);
    }, 3500);
  }

  if (telaAtual === TelaApp.DIVULGACAO) {
    return <TelaDivulgacao aoSolicitarLogin={tratarSolicitacaoLogin} usuarioAutenticado={usuarioAutenticado} />;
  }

  if (telaAtual === TelaApp.AUTENTICACAO) {
    return (
      <TelaAutenticacao
        aoAutenticacaoBemSucedida={lidarAutenticacaoBemSucedida}
        aoCancelar={lidarCancelamentoAutenticacao}
      />
    );
  }

  const nomeUsuarioLogado = sessaoUsuario?.nomeUsuario ?? 'Usuário';

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="sticky top-0 z-40 bg-white shadow-md">
        <nav className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
          <div className="text-2xl font-bold text-blue-600">
            DDS Facil
            <span className="ml-2 hidden text-sm font-normal text-gray-500 md:inline">Painel de Controle</span>
          </div>
          <div className="flex items-center gap-3">
            <div className="hidden sm:block">
              <BadgeSaldo />
            </div>
            <span className="hidden text-gray-700 sm:inline">Olá, {nomeUsuarioLogado}</span>
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100 font-semibold text-blue-700">
              {obterIniciaisUsuario(nomeUsuarioLogado)}
            </div>
            <button
              type="button"
              onClick={encerrarSessao}
              className="rounded-lg border border-red-200 px-3 py-1 text-sm font-medium text-red-700 transition hover:bg-red-50"
            >
              Sair
            </button>
          </div>
        </nav>
        <div className="border-b border-gray-200">
          {/* O cabeçalho agora renderiza 6 ABAS REAIS */}
          <div className="mx-auto flex max-w-6xl gap-6 px-4">
            <BotaoAba ativo={abaAtiva === AbaPainel.DASHBOARD} onClick={() => definirAbaAtiva(AbaPainel.DASHBOARD)}>
              Dashboard
            </BotaoAba>
            <BotaoAba ativo={abaAtiva === AbaPainel.ENVIAR} onClick={() => definirAbaAtiva(AbaPainel.ENVIAR)}>
              Enviar DDS
            </BotaoAba>
            <BotaoAba
              ativo={abaAtiva === AbaPainel.FUNCIONARIOS}
              onClick={() => definirAbaAtiva(AbaPainel.FUNCIONARIOS)}
            >
              Funcionários
            </BotaoAba>
            <BotaoAba ativo={abaAtiva === AbaPainel.CONTEUDO} onClick={() => definirAbaAtiva(AbaPainel.CONTEUDO)}>
              Conteúdo DDS
            </BotaoAba>
            {/* [ABAS NOVAS] */}
            <BotaoAba ativo={abaAtiva === AbaPainel.TIPOS_LOCAL} onClick={() => definirAbaAtiva(AbaPainel.TIPOS_LOCAL)}>
              Tipos de Local
            </BotaoAba>
            <BotaoAba ativo={abaAtiva === AbaPainel.LOCAIS} onClick={() => definirAbaAtiva(AbaPainel.LOCAIS)}>
              Locais
            </BotaoAba>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8">
        {/* Renderização limpa baseada na aba ativa */}
        {abaAtiva === AbaPainel.DASHBOARD && <AbaDashboard />}
        
        {abaAtiva === AbaPainel.ENVIAR && (
          <AbaEnviarDds exibirNotificacao={exibirNotificacao} />
        )}
        
        {abaAtiva === AbaPainel.FUNCIONARIOS && (
          <AbaFuncionarios exibirNotificacao={exibirNotificacao} />
        )}
        
        {abaAtiva === AbaPainel.CONTEUDO && (
          <AbaConteudos exibirNotificacao={exibirNotificacao} />
        )}
        
        {/* [NOVAS PÁGINAS RENDERIZADAS] */}
        {abaAtiva === AbaPainel.TIPOS_LOCAL && (
          <AbaTiposLocal exibirNotificacao={exibirNotificacao} />
        )}

        {abaAtiva === AbaPainel.LOCAIS && (
          <AbaLocais exibirNotificacao={exibirNotificacao} />
        )}
      </main>

      {notificacao && (
        <div
          className={`fixed bottom-6 right-6 rounded-lg px-5 py-3 text-white shadow-lg transition ${
            notificacao.tipo === TipoNotificacao.SUCESSO ? 'bg-green-600' : 'bg-red-600'
          }`}
        >
          {notificacao.mensagem}
        </div>
      )}
    </div>
  );
}

// ... BotaoAba (componente auxiliar) ...
type BotaoAbaProps = {
  ativo: boolean;
  onClick: () => void;
  children: ReactNode;
};

function obterIniciaisUsuario(nome: string): string {
  return (
    nome
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((parte) => parte.charAt(0).toUpperCase())
      .join('') || 'US'
  );
}

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