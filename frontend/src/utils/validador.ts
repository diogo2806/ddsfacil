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
