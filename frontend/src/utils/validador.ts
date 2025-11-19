// utilitário de validação/sanitização reutilizável
export function sanitizarTexto(texto: string): string {
  return texto.replace(/[<>&"'`]/g, '').replace(/\s+/g, ' ').trim();
}

export function sanitizarTextoMultilinha(texto: string): string {
  return texto
    .split('\n')
    .map((linha) => sanitizarTexto(linha))
    .join('\n')
    .trim();
}

export function sanitizarCelular(texto: string): string {
  return texto.replace(/[^0-9()+\-\s]/g, '').trim();
}

export function sanitizarEmailLogin(email: string): string {
  return email.replace(/[^a-zA-Z0-9@._+-]/g, '').trim().toLowerCase();
}

export function sanitizarSenhaLogin(senha: string): string {
  return senha.replace(/[\u0000-\u001F\u007F]/g, '').trim();
}

export function sanitizarTokenJwt(token: string): string {
  return token.replace(/[^A-Za-z0-9._-]/g, '').trim();
}
