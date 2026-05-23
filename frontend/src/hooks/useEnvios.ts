import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { CadastroEnvio, EnvioDds, criarEnvios, listarEnviosPorData } from '../servicos/enviosServico';

type UseEnviosOptions = {
  enabled?: boolean;
  onSuccessSave?: () => void;
  onErrorSave?: (mensagem: string) => void;
};

function extrairMensagem(erro: unknown, padrao: string): string {
  const dados = (erro as { response?: { data?: { mensagem?: string } } })?.response?.data;
  return dados?.mensagem ?? padrao;
}

export function useEnvios(options: UseEnviosOptions = {}) {
  const clienteConsulta = useQueryClient();

  const consultaEnvios = useQuery<EnvioDds[]>({
    queryKey: ['envios'],
    queryFn: () => listarEnviosPorData(undefined),
    enabled: options.enabled ?? true,
  });

  const mutacaoCriar = useMutation({
    mutationFn: (dados: CadastroEnvio) => criarEnvios(dados),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['envios'] });
      clienteConsulta.invalidateQueries({ queryKey: ['saldo-sms'] }); // <--- LINHA ADICIONADA
      options.onSuccessSave?.();
    },
    onError: (erro) =>
      options.onErrorSave?.(extrairMensagem(erro, 'Não foi possível criar o envio. Tente novamente.')),
  });

  return {
    consultaEnvios,
    mutacaoCriar,
  };
}
