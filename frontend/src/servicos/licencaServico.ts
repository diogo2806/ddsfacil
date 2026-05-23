import { clienteHttp } from './clienteHttp';

export type CanalMensagem = 'SMS' | 'WHATSAPP';

export type SaldoResponse = {
  saldoSms: number;
  plano: string;
};

export type StatusPagamento = 'EM_DIA' | 'INADIMPLENTE' | 'SUSPENSO';

export type Licenca = {
  tipoPlano: string;
  saldoSms: number;
  saldoWhatsapp: number;
  dataRenovacao: string | null;
  statusPagamento: StatusPagamento;
  atualizadoEm: string;
};

export type AtualizacaoLicenca = {
  tipoPlano: string;
  statusPagamento: StatusPagamento;
  dataRenovacao?: string | null;
};

export type RecargaOnline = {
  status: string;
  mensagem: string;
  urlPagamento: string | null;
};

export async function consultarSaldo(): Promise<SaldoResponse> {
  const resposta = await clienteHttp.get<SaldoResponse>('/api/licencas/meu-saldo');
  return resposta.data;
}

export async function detalharLicenca(): Promise<Licenca> {
  const resposta = await clienteHttp.get<Licenca>('/api/licencas');
  return resposta.data;
}

export async function recarregarCreditos(quantidade: number, canal: CanalMensagem = 'SMS'): Promise<Licenca> {
  const resposta = await clienteHttp.post<Licenca>('/api/licencas/recarga', { quantidade, canal });
  return resposta.data;
}

export async function atualizarLicenca(dados: AtualizacaoLicenca): Promise<Licenca> {
  const resposta = await clienteHttp.put<Licenca>('/api/licencas', dados);
  return resposta.data;
}

export async function iniciarRecargaOnline(quantidade: number): Promise<RecargaOnline> {
  const resposta = await clienteHttp.post<RecargaOnline>('/api/licencas/recarga-online', { quantidade });
  return resposta.data;
}