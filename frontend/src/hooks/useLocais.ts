import { useQuery } from '@tanstack/react-query';
import { listarLocaisTrabalho, LocalTrabalho } from '../servicos/localTrabalhoServico';

type UseLocaisOptions = {
  enabled?: boolean;
};

export function useLocais(options: UseLocaisOptions = {}) {
  const consultaLocaisTrabalho = useQuery<LocalTrabalho[]>({
    queryKey: ['locais-trabalho'],
    queryFn: listarLocaisTrabalho,
    enabled: options.enabled ?? true,
  });

  return {
    consultaLocaisTrabalho,
  };
}
