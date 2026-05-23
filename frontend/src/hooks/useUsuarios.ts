import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  AtualizacaoUsuario,
  CadastroUsuario,
  Usuario,
  atualizarUsuario,
  criarUsuario,
  listarUsuarios,
  redefinirSenhaUsuario,
} from '../servicos/usuariosServico';

type UseUsuariosOptions = {
  enabled?: boolean;
  onSuccessSave?: () => void;
  onErrorSave?: (mensagem: string) => void;
  onSuccessUpdate?: () => void;
  onErrorUpdate?: (mensagem: string) => void;
  onSuccessSenha?: () => void;
  onErrorSenha?: (mensagem: string) => void;
};

function extrairMensagem(erro: unknown, padrao: string): string {
  const dados = (erro as { response?: { data?: { mensagem?: string } } })?.response?.data;
  return dados?.mensagem ?? padrao;
}

export function useUsuarios(options: UseUsuariosOptions = {}) {
  const clienteConsulta = useQueryClient();

  const consultaUsuarios = useQuery<Usuario[]>({
    queryKey: ['usuarios'],
    queryFn: listarUsuarios,
    enabled: options.enabled ?? true,
  });

  const invalidar = () => clienteConsulta.invalidateQueries({ queryKey: ['usuarios'] });

  const mutacaoCriar = useMutation({
    mutationFn: (dados: CadastroUsuario) => criarUsuario(dados),
    onSuccess: () => {
      invalidar();
      options.onSuccessSave?.();
    },
    onError: (erro) => options.onErrorSave?.(extrairMensagem(erro, 'Erro ao cadastrar usuário.')),
  });

  const mutacaoAtualizar = useMutation({
    mutationFn: ({ id, dados }: { id: number; dados: AtualizacaoUsuario }) => atualizarUsuario(id, dados),
    onSuccess: () => {
      invalidar();
      options.onSuccessUpdate?.();
    },
    onError: (erro) => options.onErrorUpdate?.(extrairMensagem(erro, 'Erro ao atualizar usuário.')),
  });

  const mutacaoSenha = useMutation({
    mutationFn: ({ id, novaSenha }: { id: number; novaSenha: string }) => redefinirSenhaUsuario(id, novaSenha),
    onSuccess: () => options.onSuccessSenha?.(),
    onError: (erro) => options.onErrorSenha?.(extrairMensagem(erro, 'Erro ao redefinir senha.')),
  });

  return {
    consultaUsuarios,
    mutacaoCriar,
    mutacaoAtualizar,
    mutacaoSenha,
  };
}
