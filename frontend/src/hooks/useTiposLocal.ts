import { useQuery } from '@tanstack/react-query';
import { listarTiposLocal, TipoLocalOption } from '../servicos/localTrabalhoServico';

type UseTiposLocalOptions = {
  enabled?: boolean;
};

export function useTiposLocal(options: UseTiposLocalOptions = {}) {
  const consultaTiposLocal = useQuery<TipoLocalOption[]>({
    queryKey: ['tipos-local'],
    queryFn: listarTiposLocal,
    enabled: options.enabled ?? true,
  });

  return { consultaTiposLocal };
}
