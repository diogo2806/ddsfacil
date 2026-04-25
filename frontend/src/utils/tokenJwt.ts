import { sanitizarTokenJwt } from './validador';

type DadosTokenJwt = {
  empresaId?: number | string | null;
};

function normalizarNumeroPositivo(valor: unknown): number | null {
  if (typeof valor === 'number' && Number.isFinite(valor) && valor > 0) {
    return Math.trunc(valor);
  }
  if (typeof valor === 'string') {
    const textoNumerico = valor.replace(/[^0-9]/g, '').trim();
    if (!textoNumerico) {
      return null;
    }
    const numero = Number.parseInt(textoNumerico, 10);
    if (Number.isFinite(numero) && numero > 0) {
      return numero;
    }
  }
  return null;
}

function decodificarSegmentoBase64Url(segmento: string): string {
  const base64Padrao = segmento.replace(/-/g, '+').replace(/_/g, '/');
  const padding = base64Padrao.length % 4;
  const base64Completo = base64Padrao + (padding ? '='.repeat(4 - padding) : '');

  if (typeof globalThis.atob === 'function') {
    return globalThis.atob(base64Completo);
  }

  const bufferGlobal = (globalThis as { Buffer?: { from: (input: string, tipo: string) => { toString: (formato: string) => string } } }).Buffer;
  if (bufferGlobal) {
    return bufferGlobal.from(base64Completo, 'base64').toString('utf-8');
  }

  throw new Error('Ambiente sem suporte para decodificação Base64.');
}

export function extrairEmpresaIdDoToken(token: string | null | undefined): number | null {
  if (!token) {
    return null;
  }

  const tokenSanitizado = sanitizarTokenJwt(token);
  if (!tokenSanitizado) {
    return null;
  }

  const partes = tokenSanitizado.split('.');
  if (partes.length < 2) {
    return null;
  }

  try {
    const corpoToken = decodificarSegmentoBase64Url(partes[1]);
    const dados = JSON.parse(corpoToken) as DadosTokenJwt;
    return normalizarNumeroPositivo(dados.empresaId ?? null);
  } catch (erro) {
    console.warn('Token JWT inválido ao extrair o identificador da empresa.', erro);
    return null;
  }
}
