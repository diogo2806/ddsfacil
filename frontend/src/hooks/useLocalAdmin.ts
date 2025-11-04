import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { listarTiposLocalAdmin, criarTipoLocal, removerTipoLocal, listarLocaisTrabalho, criarLocalTrabalho, removerLocalTrabalho, TipoLocalAdmin, LocalTrabalho } from '../servicos/localTrabalhoServico';

export function useLocalAdmin() {
  const clienteConsulta = useQueryClient();

  const consultaTipos = useQuery<TipoLocalAdmin[]>({
    queryKey: ['tiposLocalAdmin'],
    queryFn: listarTiposLocalAdmin,
  });

  const consultaLocais = useQuery<LocalTrabalho[]>({
    queryKey: ['locais-trabalho'],
    queryFn: listarLocaisTrabalho,
  });

  const mutacaoCriarTipo = useMutation({
    mutationFn: (nome: string) => criarTipoLocal(nome),
    onSuccess: () => clienteConsulta.invalidateQueries({ queryKey: ['tiposLocalAdmin'] }),
  });

  const mutacaoRemoverTipo = useMutation({
    mutationFn: (id: number) => removerTipoLocal(id),
    onSuccess: () => clienteConsulta.invalidateQueries({ queryKey: ['tiposLocalAdmin'] }),
  });

  const mutacaoCriarLocal = useMutation({
    mutationFn: (dados: { nome: string; tipoLocalId: number }) => criarLocalTrabalho(dados),
    onSuccess: () => clienteConsulta.invalidateQueries({ queryKey: ['locais-trabalho'] }),
  });

  const mutacaoRemoverLocal = useMutation({
    mutationFn: (id: number) => removerLocalTrabalho(id),
    onSuccess: () => clienteConsulta.invalidateQueries({ queryKey: ['locais-trabalho'] }),
  });

  return {
    consultaTipos,
    consultaLocais,
    mutacaoCriarTipo,
    mutacaoRemoverTipo,
    mutacaoCriarLocal,
    mutacaoRemoverLocal,
  };
}
