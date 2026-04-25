import { clienteHttp } from './clienteHttp';

export type SaldoResponse = {
  saldoSms: number;
  plano: string;
};

export async function consultarSaldo(): Promise<SaldoResponse> {
  const resposta = await clienteHttp.get<SaldoResponse>('/api/licencas/meu-saldo');
  return resposta.data;
}