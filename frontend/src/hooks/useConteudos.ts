import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  CadastroConteudo,
  ConteudoDds,
  criarConteudo,
  criarConteudoComArquivo,
  listarConteudos,
  removerConteudo,
} from '../servicos/conteudosServico';

type UseConteudosOptions = {
  enabled?: boolean;
  onSuccessSave?: () => void;
  onErrorSave?: () => void;
  onSuccessRemove?: () => void;
  onErrorRemove?: () => void;
};

export function useConteudos(options: UseConteudosOptions = {}) {
  const clienteConsulta = useQueryClient();

  const consultaConteudos = useQuery<ConteudoDds[]>({
    queryKey: ['conteudos'],
    queryFn: listarConteudos,
    enabled: options.enabled ?? true,
  });

  const mutacaoCriar = useMutation({
    mutationFn: (dados: CadastroConteudo) => criarConteudo(dados),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['conteudos'] });
      options.onSuccessSave?.();
    },
    onError: () => options.onErrorSave?.(),
  });

  const mutacaoCriarComArquivo = useMutation({
    mutationFn: (form: { dados: CadastroConteudo; arquivo: File }) =>
      criarConteudoComArquivo({ ...form.dados, arquivo: form.arquivo }),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['conteudos'] });
      options.onSuccessSave?.();
    },
    onError: () => options.onErrorSave?.(),
  });

  const mutacaoRemover = useMutation({
    mutationFn: (id: number) => removerConteudo(id),
    onSuccess: () => {
      clienteConsulta.invalidateQueries({ queryKey: ['conteudos'] });
      options.onSuccessRemove?.();
    },
    onError: () => options.onErrorRemove?.(),
  });

  return {
    consultaConteudos,
    mutacaoCriar,
    mutacaoCriarComArquivo,
    mutacaoRemover,
  };
}
