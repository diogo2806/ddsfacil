import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { CadastroEnvio, EnvioDds, criarEnvios, listarEnviosPorData } from '../servicos/enviosServico';

type UseEnviosOptions = {
  enabled?: boolean;
  onSuccessSave?: () => void;
  onErrorSave?: () => void;
};

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
      options.onSuccessSave?.();
    },
    onError: () => options.onErrorSave?.(),
  });

  return {
    consultaEnvios,
    mutacaoCriar,
  };
}
