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

function lerEmpresaPadraoConfigurada(): number {
  const textoConfigurado = typeof import.meta.env.VITE_EMPRESA_ID_PADRAO === 'string'
    ? import.meta.env.VITE_EMPRESA_ID_PADRAO.trim()
    : '';

  if (!textoConfigurado) {
    throw new Error(
      'Configure a variável de ambiente VITE_EMPRESA_ID_PADRAO com o identificador numérico da empresa que utilizará o painel.',
    );
  }

  const empresaPadrao = normalizarEmpresaId(textoConfigurado);
  if (!empresaPadrao) {
    throw new Error('A variável VITE_EMPRESA_ID_PADRAO deve conter um número inteiro maior que zero.');
  }

  return empresaPadrao;
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

export function obterEmpresaIdAtualOpcional(): number | null {
  return empresaIdEmMemoria ?? null;
}

export function obterEmpresaIdAtualObrigatorio(): number {
  const empresaAtual = obterEmpresaIdAtualOpcional();
  if (!empresaAtual) {
    throw new Error('Nenhuma empresa foi definida para a sessão atual.');
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
