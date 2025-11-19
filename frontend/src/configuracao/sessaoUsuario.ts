import type { RespostaAutenticacao, SessaoUsuario } from '../types/autenticacao';
import { sanitizarTexto, sanitizarTokenJwt } from '../utils/validador';

const CHAVE_SESSAO = 'ddsfacil_sessao_usuario';

type SessaoBruta = {
  token?: string;
  empresaId?: number;
  nomeUsuario?: string;
  perfil?: string;
};

function normalizarPerfil(perfil: string | undefined): string {
  const texto = (perfil ?? '').toString().trim().toUpperCase();
  return texto || 'USUARIO';
}

function validarSessao(objeto: SessaoBruta | null): SessaoUsuario | null {
  if (!objeto) {
    return null;
  }
  const tokenSanitizado = sanitizarTokenJwt(String(objeto.token ?? ''));
  const empresaIdNumero = Number(objeto.empresaId);
  const nomeSanitizado = sanitizarTexto(String(objeto.nomeUsuario ?? ''));
  if (!tokenSanitizado || !Number.isFinite(empresaIdNumero) || empresaIdNumero <= 0 || !nomeSanitizado) {
    return null;
  }
  return {
    token: tokenSanitizado,
    empresaId: empresaIdNumero,
    nomeUsuario: nomeSanitizado,
    perfil: normalizarPerfil(objeto.perfil),
  };
}

export function converterRespostaParaSessao(resposta: RespostaAutenticacao): SessaoUsuario {
  const sessao = validarSessao(resposta);
  if (!sessao) {
    throw new Error('Resposta de autenticação inválida.');
  }
  return sessao;
}

export function salvarSessaoUsuario(sessao: SessaoUsuario): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao));
}

export function carregarSessaoUsuario(): SessaoUsuario | null {
  if (typeof window === 'undefined') {
    return null;
  }
  try {
    const registro = window.localStorage.getItem(CHAVE_SESSAO);
    if (!registro) {
      return null;
    }
    const objeto = JSON.parse(registro) as SessaoBruta;
    return validarSessao(objeto);
  } catch (erro) {
    console.warn('Sessão de usuário inválida ignorada.', erro);
    return null;
  }
}

export function removerSessaoUsuario(): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.removeItem(CHAVE_SESSAO);
}
