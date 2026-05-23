import { clienteHttp } from './clienteHttp';

export type FiltroRelatorioCompliance = {
  conteudoId: number;
  data: string;
  localTrabalhoId?: number;
};

export async function baixarRelatorioCompliance(
  filtro: FiltroRelatorioCompliance,
): Promise<{ arquivo: Blob; nomeArquivo: string }> {
  const resposta = await clienteHttp.get<Blob>('/api/relatorios/dds/compliance', {
    responseType: 'blob',
    params: {
      conteudoId: filtro.conteudoId,
      data: filtro.data,
      ...(filtro.localTrabalhoId ? { localTrabalhoId: filtro.localTrabalhoId } : {}),
    },
  });

  const contentDisposition = resposta.headers['content-disposition'];
  const nomeExtraido = typeof contentDisposition === 'string'
    ? contentDisposition.match(/filename="?([^";]+)"?/i)?.[1]
    : undefined;

  return {
    arquivo: resposta.data,
    nomeArquivo: nomeExtraido ?? `evidencia_dds_${filtro.data}.pdf`,
  };
}
