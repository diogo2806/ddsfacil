import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  CadastroFuncionario,
  Funcionario,
  ResultadoImportacao,
  atualizarFuncionario,
  criarFuncionario,
  importarFuncionarios,
  listarFuncionarios,
  removerFuncionario,
} from '../servicos/funcionariosServico';

type UseFuncionariosOptions = {
  enabled?: boolean;
  onSuccessSave?: () => void;
  onErrorSave?: (mensagem: string) => void;
  onSuccessUpdate?: () => void;
  onErrorUpdate?: (mensagem: string) => void;
  onSuccessRemove?: () => void;
  onErrorRemove?: (mensagem: string) => void;
  onSuccessImport?: (resultado: ResultadoImportacao) => void;
  onErrorImport?: (mensagem: string) => void;
};

function extrairMensagem(erro: unknown, padrao: string): string {
  const dados = (erro as { response?: { data?: { mensagem?: string } } })?.response?.data;
  return dados?.mensagem ?? padrao;
}

export function useFuncionarios(options: UseFuncionariosOptions = {}) {
  const clienteConsulta = useQueryClient();

  const consultaFuncionarios = useQuery<Funcionario[]>({
    queryKey: ['funcionarios'],
    queryFn: () => listarFuncionarios(),
    enabled: options.enabled ?? true,
  });

  const invalidar = () => clienteConsulta.invalidateQueries({ queryKey: ['funcionarios'] });

  const mutacaoCriar = useMutation({
    mutationFn: (dados: CadastroFuncionario) => criarFuncionario(dados),
    onSuccess: () => {
      invalidar();
      options.onSuccessSave?.();
    },
    onError: (erro) => options.onErrorSave?.(extrairMensagem(erro, 'Erro ao cadastrar funcionário.')),
  });

  const mutacaoAtualizar = useMutation({
    mutationFn: ({ id, dados }: { id: number; dados: CadastroFuncionario }) => atualizarFuncionario(id, dados),
    onSuccess: () => {
      invalidar();
      options.onSuccessUpdate?.();
    },
    onError: (erro) => options.onErrorUpdate?.(extrairMensagem(erro, 'Erro ao atualizar funcionário.')),
  });

  const mutacaoRemover = useMutation({
    mutationFn: (id: number) => removerFuncionario(id),
    onSuccess: () => {
      invalidar();
      options.onSuccessRemove?.();
    },
    onError: (erro) => options.onErrorRemove?.(extrairMensagem(erro, 'Erro ao remover funcionário.')),
  });

  const mutacaoImportar = useMutation({
    mutationFn: (arquivo: File) => importarFuncionarios(arquivo),
    onSuccess: (resultado) => {
      invalidar();
      options.onSuccessImport?.(resultado);
    },
    onError: (erro) => options.onErrorImport?.(extrairMensagem(erro, 'Erro ao importar funcionários.')),
  });

  return {
    consultaFuncionarios,
    mutacaoCriar,
    mutacaoAtualizar,
    mutacaoRemover,
    mutacaoImportar,
  };
}
