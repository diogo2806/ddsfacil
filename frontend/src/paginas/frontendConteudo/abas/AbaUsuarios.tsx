import { FormEvent, useMemo, useState } from 'react';
import DOMPurify from 'dompurify';
import { useUsuarios } from '../../../hooks/useUsuarios';
import { PerfilUsuario, Usuario } from '../../../servicos/usuariosServico';
import { TipoNotificacao } from '../../../types/enums';

const PERFIS: PerfilUsuario[] = ['ADMIN', 'GESTOR', 'OPERADOR'];

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

export default function AbaUsuarios({ exibirNotificacao }: Props) {
  const [nome, definirNome] = useState('');
  const [email, definirEmail] = useState('');
  const [senha, definirSenha] = useState('');
  const [perfil, definirPerfil] = useState<PerfilUsuario>('OPERADOR');

  const [linhaSenha, definirLinhaSenha] = useState<number | null>(null);
  const [novaSenha, definirNovaSenha] = useState('');

  const { consultaUsuarios, mutacaoCriar, mutacaoAtualizar, mutacaoSenha } = useUsuarios({
    onSuccessSave: () => {
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Usuário cadastrado.' });
      definirNome('');
      definirEmail('');
      definirSenha('');
      definirPerfil('OPERADOR');
    },
    onErrorSave: (mensagem) => exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem }),
    onSuccessUpdate: () => exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Usuário atualizado.' }),
    onErrorUpdate: (mensagem) => exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem }),
    onSuccessSenha: () => {
      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Senha redefinida.' });
      definirLinhaSenha(null);
      definirNovaSenha('');
    },
    onErrorSenha: (mensagem) => exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem }),
  });

  const usuarios = useMemo(
    () =>
      (consultaUsuarios.data ?? [])
        .slice()
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' })),
    [consultaUsuarios.data],
  );

  function aoCadastrar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const nomeLimpo = DOMPurify.sanitize(nome, { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
    const emailLimpo = email.trim().toLowerCase();

    if (!nomeLimpo || !emailLimpo || senha.length < 8) {
      exibirNotificacao({
        tipo: TipoNotificacao.ERRO,
        mensagem: 'Preencha nome, e-mail e uma senha de ao menos 8 caracteres.',
      });
      return;
    }

    mutacaoCriar.mutate({ nome: nomeLimpo, email: emailLimpo, senha, perfil });
  }

  function alterarPerfil(usuario: Usuario, novoPerfil: PerfilUsuario) {
    mutacaoAtualizar.mutate({ id: usuario.id, dados: { nome: usuario.nome, perfil: novoPerfil, ativo: usuario.ativo } });
  }

  function alternarStatus(usuario: Usuario) {
    mutacaoAtualizar.mutate({
      id: usuario.id,
      dados: { nome: usuario.nome, perfil: usuario.perfil, ativo: !usuario.ativo },
    });
  }

  function confirmarSenha(usuarioId: number) {
    if (novaSenha.length < 8) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'A nova senha deve ter ao menos 8 caracteres.' });
      return;
    }
    mutacaoSenha.mutate({ id: usuarioId, novaSenha });
  }

  return (
    <div className="grid gap-6 md:grid-cols-[1fr,2fr]">
      <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900">Novo usuário</h3>
        <form className="space-y-4" onSubmit={aoCadastrar}>
          <div className="space-y-2">
            <label htmlFor="usuario-nome" className="text-sm font-medium text-gray-700">
              Nome completo
            </label>
            <input
              id="usuario-nome"
              type="text"
              value={nome}
              onChange={(evento) => definirNome(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="usuario-email" className="text-sm font-medium text-gray-700">
              E-mail
            </label>
            <input
              id="usuario-email"
              type="email"
              value={email}
              onChange={(evento) => definirEmail(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="usuario-senha" className="text-sm font-medium text-gray-700">
              Senha (mín. 8 caracteres)
            </label>
            <input
              id="usuario-senha"
              type="password"
              value={senha}
              onChange={(evento) => definirSenha(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="usuario-perfil" className="text-sm font-medium text-gray-700">
              Perfil
            </label>
            <select
              id="usuario-perfil"
              value={perfil}
              onChange={(evento) => definirPerfil(evento.target.value as PerfilUsuario)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
            >
              {PERFIS.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </div>

          <button
            type="submit"
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
            disabled={mutacaoCriar.isPending}
          >
            {mutacaoCriar.isPending ? 'Salvando...' : 'Cadastrar usuário'}
          </button>
        </form>
      </section>

      <section className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <header className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900">Usuários do painel</h3>
          <span className="text-sm text-gray-500">{usuarios.length} registro(s)</span>
        </header>

        {consultaUsuarios.isLoading ? (
          <p className="text-sm text-gray-500">Carregando usuários...</p>
        ) : consultaUsuarios.isError ? (
          <p className="text-sm text-red-600">Não foi possível carregar os usuários.</p>
        ) : usuarios.length === 0 ? (
          <p className="text-sm text-gray-500">Nenhum usuário cadastrado.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 text-sm">
              <thead className="bg-gray-50 text-left font-semibold uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-3 py-3">Nome</th>
                  <th className="px-3 py-3">E-mail</th>
                  <th className="px-3 py-3">Perfil</th>
                  <th className="px-3 py-3">Status</th>
                  <th className="px-3 py-3 text-right">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 bg-white text-gray-700">
                {usuarios.map((usuario) => (
                  <tr key={usuario.id}>
                    <td className="px-3 py-3 font-medium">{usuario.nome}</td>
                    <td className="px-3 py-3 text-gray-500">{usuario.email}</td>
                    <td className="px-3 py-3">
                      <select
                        value={usuario.perfil}
                        onChange={(evento) => alterarPerfil(usuario, evento.target.value as PerfilUsuario)}
                        disabled={mutacaoAtualizar.isPending}
                        className="rounded-lg border border-gray-300 px-2 py-1 text-xs focus:border-blue-500 focus:outline-none"
                      >
                        {PERFIS.map((item) => (
                          <option key={item} value={item}>
                            {item}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td className="px-3 py-3">
                      <button
                        type="button"
                        onClick={() => alternarStatus(usuario)}
                        disabled={mutacaoAtualizar.isPending}
                        className={`rounded-full px-3 py-1 text-xs font-semibold ${
                          usuario.ativo
                            ? 'bg-green-100 text-green-700'
                            : 'bg-gray-200 text-gray-600'
                        }`}
                      >
                        {usuario.ativo ? 'Ativo' : 'Inativo'}
                      </button>
                    </td>
                    <td className="px-3 py-3 text-right">
                      {linhaSenha === usuario.id ? (
                        <div className="flex items-center justify-end gap-2">
                          <input
                            type="password"
                            value={novaSenha}
                            onChange={(evento) => definirNovaSenha(evento.target.value)}
                            placeholder="Nova senha"
                            className="w-32 rounded-lg border border-gray-300 px-2 py-1 text-xs focus:border-blue-500 focus:outline-none"
                          />
                          <button
                            type="button"
                            onClick={() => confirmarSenha(usuario.id)}
                            disabled={mutacaoSenha.isPending}
                            className="text-xs font-semibold text-blue-600 hover:text-blue-700"
                          >
                            Confirmar
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              definirLinhaSenha(null);
                              definirNovaSenha('');
                            }}
                            className="text-xs text-gray-500 hover:text-gray-700"
                          >
                            Cancelar
                          </button>
                        </div>
                      ) : (
                        <button
                          type="button"
                          onClick={() => {
                            definirLinhaSenha(usuario.id);
                            definirNovaSenha('');
                          }}
                          className="text-xs font-semibold text-blue-600 hover:text-blue-700"
                        >
                          Redefinir senha
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
