import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  CadastroFuncionario,
  Funcionario,
  criarFuncionario,
  listarFuncionarios,
  removerFuncionario,
} from '../servicos/funcionariosServico';

type UseFuncionariosOptions = {
  enabled?: boolean;
  onSuccessSave?: () => void;
  onErrorSave?: () => void;
  onSuccessRemove?: () => void;
  onErrorRemove?: () => void;
};

export function useFuncionarios(options: UseFuncionariosOptions = {}) {
  const clienteConsulta = useQueryClient();

  const consultaFuncionarios = useQuery<Funcionario[]>({
    queryKey: ['funcionarios'],
    queryFn: () => listarFuncionarios(),
    enabled: options.enabled ?? true,
  });

  const mutacaoCriar = useMutation({
    mutationFn: (dados: CadastroFuncionario) => criarFuncionario(dados),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['funcionarios'] });
      options.onSuccessSave?.();
    },
    onError: () => options.onErrorSave?.(),
  });

  const mutacaoRemover = useMutation({
    mutationFn: (id: number) => removerFuncionario(id),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['funcionarios'] });
      options.onSuccessRemove?.();
    },
    onError: () => options.onErrorRemove?.(),
  });

  return {
    consultaFuncionarios,
    mutacaoCriar,
    mutacaoRemover,
  };
}
