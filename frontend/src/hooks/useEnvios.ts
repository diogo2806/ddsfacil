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
    onSuccess: (_, variaveis: CadastroEnvio) => {
      const dataEnvio = variaveis?.dataEnvio ?? new Date().toISOString().split('T')[0];
      clienteConsulta.invalidateQueries({ queryKey: ['envios', dataEnvio] });
      options.onSuccessSave?.();
    },
    onError: () => options.onErrorSave?.(),
  });

  return {
    consultaEnvios,
    mutacaoCriar,
  };
}
