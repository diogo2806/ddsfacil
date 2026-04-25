import { URL_BASE_API } from '../configuracao/api';

export function resolverUrlConteudo(valor: string | null | undefined): string | null {
  const bruto = typeof valor === 'string' ? valor.trim() : '';
  if (!bruto) {
    return null;
  }

  if (/^https?:\/\//i.test(bruto)) {
    return bruto;
  }

  if (bruto.startsWith('/')) {
    return `${URL_BASE_API}${bruto}`;
  }

  return null;
}
