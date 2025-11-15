export interface TemaAparencia {
  identificador: string;
  nome: string;
  descricao?: string;
  corPrimaria: string;
  corSecundaria?: string;
  corTextoSobreposto: string;
}

const limparTexto = (valor: string): string =>
  valor
    .replace(/[<>]/g, '')
    .replace(/javascript:/gi, '')
    .trim();

const limparCor = (valor: string): string => valor.trim();

export const sanitizarTemaAparencia = (tema: TemaAparencia): TemaAparencia => ({
  identificador: limparTexto(tema.identificador),
  nome: limparTexto(tema.nome),
  descricao: tema.descricao ? limparTexto(tema.descricao) : undefined,
  corPrimaria: limparCor(tema.corPrimaria),
  corSecundaria: tema.corSecundaria ? limparCor(tema.corSecundaria) : undefined,
  corTextoSobreposto: limparCor(tema.corTextoSobreposto)
});

export const validarTemaAparencia = (tema: TemaAparencia): void => {
  if (!tema.identificador.trim()) {
    throw new Error('Identificador do tema não pode ser vazio.');
  }

  if (!/^#[0-9A-Fa-f]{6}$/u.test(tema.corPrimaria)) {
    throw new Error('Cor primária do tema está em formato inválido.');
  }

  if (tema.corSecundaria && !/^#[0-9A-Fa-f]{6}$/u.test(tema.corSecundaria)) {
    throw new Error('Cor secundária do tema está em formato inválido.');
  }

  if (!/^#[0-9A-Fa-f]{6}$/u.test(tema.corTextoSobreposto)) {
    throw new Error('Cor de texto sobreposto do tema está em formato inválido.');
  }
};
