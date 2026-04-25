import { carregarSessaoUsuario } from './sessaoUsuario';

const chaveArmazenamento = 'ddsfacil_empresa_id_ativo';

function normalizarEmpresaId(valor: string | number | null | undefined): number | null {
  if (valor === null || valor === undefined) {
    return null;
  }
  if (typeof valor === 'number') {
    if (Number.isFinite(valor) && Number.isInteger(valor) && valor > 0) {
      return valor;
    }
    return null;
  }

  const textoLimpo = valor.replace(/[^0-9]/g, '').trim();
  if (!textoLimpo) {
    return null;
  }

  const numero = Number.parseInt(textoLimpo, 10);
  if (!Number.isFinite(numero) || numero <= 0) {
    return null;
  }

  return numero;
}

// Mantemos a leitura do env para casos de fallback (login inicial), mas com menor prioridade
function lerEmpresaPadraoConfigurada(): number {
  const textoConfigurado = typeof import.meta.env.VITE_EMPRESA_ID_PADRAO === 'string'
    ? import.meta.env.VITE_EMPRESA_ID_PADRAO.trim()
    : '';

  // Não lançamos erro aqui para não quebrar a build se a env não existir,
  // retornamos 1 como fallback seguro para desenvolvimento.
  if (!textoConfigurado) {
    return 1; 
  }

  const empresaPadrao = normalizarEmpresaId(textoConfigurado);
  return empresaPadrao || 1;
}

const empresaPadraoConfigurada = lerEmpresaPadraoConfigurada();
let empresaIdEmMemoria: number = empresaPadraoConfigurada;

function lerEmpresaPersistida(): number | null {
  if (typeof window === 'undefined') {
    return null;
  }

  try {
    const armazenado = window.localStorage.getItem(chaveArmazenamento);
    return normalizarEmpresaId(armazenado);
  } catch (erro) {
    console.warn('Não foi possível recuperar o identificador da empresa armazenado localmente.', erro);
    return null;
  }
}

const empresaPersistidaInicial = lerEmpresaPersistida();
if (empresaPersistidaInicial) {
  empresaIdEmMemoria = empresaPersistidaInicial;
}

function persistirEmpresaId(empresaId: number): void {
  if (typeof window === 'undefined') {
    return;
  }

  try {
    window.localStorage.setItem(chaveArmazenamento, String(empresaId));
  } catch (erro) {
    console.warn('Não foi possível salvar o identificador da empresa localmente.', erro);
  }
}

export function definirEmpresaIdAtual(empresaId: number): void {
  const empresaNormalizada = normalizarEmpresaId(empresaId);
  if (!empresaNormalizada) {
    throw new Error('O identificador da empresa deve ser um número inteiro maior que zero.');
  }

  empresaIdEmMemoria = empresaNormalizada;
  persistirEmpresaId(empresaNormalizada);
}

// --- MUDANÇA CRÍTICA AQUI ---
export function obterEmpresaIdAtualOpcional(): number | null {
  // 1. Prioridade Total: Sessão do Usuário Autenticado
  const sessao = carregarSessaoUsuario();
  if (sessao && sessao.empresaId) {
    return sessao.empresaId;
  }

  // 2. Fallback: Memória local (para tela de login/divulgação)
  return empresaIdEmMemoria ?? null;
}

export function obterEmpresaIdAtualObrigatorio(): number {
  const empresaAtual = obterEmpresaIdAtualOpcional();
  if (!empresaAtual) {
    // Se não achou na sessão nem na memória, usa o padrão do ENV para evitar crash
    return empresaPadraoConfigurada;
  }
  return empresaAtual;
}

export function sincronizarEmpresaIdDaUrl(): void {
  if (typeof window === 'undefined') {
    return;
  }

  const parametros = new URLSearchParams(window.location.search);
  const empresaDaUrl = normalizarEmpresaId(parametros.get('empresaId') ?? parametros.get('empresa'));

  if (!empresaDaUrl) {
    return;
  }

  definirEmpresaIdAtual(empresaDaUrl);
  parametros.delete('empresaId');
  parametros.delete('empresa');
  const novaBusca = parametros.toString();
  const novaUrl = `${window.location.pathname}${novaBusca ? `?${novaBusca}` : ''}${window.location.hash}`;
  window.history.replaceState({}, document.title, novaUrl);
}