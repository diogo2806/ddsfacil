import React from 'react';
import type { TipoLocalAdmin, LocalTrabalho } from '../../../servicos/localTrabalhoServico';

type Notificacao = { tipo: 'sucesso' | 'erro'; mensagem: string };

type Props = {
  consultaTipos: any;
  consultaLocais: any;
  mutacaoCriarTipo: any;
  mutacaoRemoverTipo: any;
  mutacaoAtualizarTipo?: any;
  mutacaoCriarLocal: any;
  mutacaoRemoverLocal: any;
  mutacaoAtualizarLocal?: any;
  editandoTipoId: number | null;
  definirEditandoTipoId: (v: number | null) => void;
  editandoTipoNome: string;
  definirEditandoTipoNome: (v: string) => void;
  novoTipoLocal: string;
  definirNovoTipoLocal: (v: string) => void;
  editandoLocalId: number | null;
  definirEditandoLocalId: (v: number | null) => void;
  editandoLocalNome: string;
  definirEditandoLocalNome: (v: string) => void;
  editandoLocalTipoId: number | '';
  definirEditandoLocalTipoId: (v: number | '') => void;
  novoLocalNome: string;
  definirNovoLocalNome: (v: string) => void;
  novoLocalTipoId: number | '';
  definirNovoLocalTipoId: (v: number | '') => void;
  exibirNotificacao: (n: Notificacao) => void;
  aba: 'tipos' | 'locais';
  definirAba: (aba: 'tipos' | 'locais') => void;
};

export default function AdminLocais({
  consultaTipos,
  consultaLocais,
  mutacaoCriarTipo,
  mutacaoRemoverTipo,
  mutacaoAtualizarTipo,
  mutacaoCriarLocal,
  mutacaoRemoverLocal,
  mutacaoAtualizarLocal,
  editandoTipoId,
  definirEditandoTipoId,
  editandoTipoNome,
  definirEditandoTipoNome,
  novoTipoLocal,
  definirNovoTipoLocal,
  editandoLocalId,
  definirEditandoLocalId,
  editandoLocalNome,
  definirEditandoLocalNome,
  editandoLocalTipoId,
  definirEditandoLocalTipoId,
  novoLocalNome,
  definirNovoLocalNome,
  novoLocalTipoId,
  definirNovoLocalTipoId,
  exibirNotificacao,
  aba,
  definirAba,
}: Props) {
  return (
    <div className="mt-6 border-t pt-6">
      <h4 className="text-lg font-semibold text-gray-900">Administrar Locais</h4>
      <p className="text-sm text-gray-500 mb-4">Cadastre tipos de local e locais de trabalho usados no sistema.</p>

      <div className="space-y-4">
        <div className="flex gap-2 mb-3">
          <button
            type="button"
            onClick={() => definirAba('tipos')}
            className={`px-3 py-1 rounded ${aba === 'tipos' ? 'bg-blue-600 text-white' : 'bg-gray-100'}`}
          >
            Tipos de Local
          </button>
          <button
            type="button"
            onClick={() => definirAba('locais')}
            className={`px-3 py-1 rounded ${aba === 'locais' ? 'bg-blue-600 text-white' : 'bg-gray-100'}`}
          >
            Locais
          </button>
        </div>

        {aba === 'tipos' ? (
          <div>
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                const nome = (editandoTipoId ? editandoTipoNome : novoTipoLocal).trim();
                if (!nome) {
                  exibirNotificacao({ tipo: 'erro', mensagem: 'Informe o nome do tipo.' });
                  return;
                }
                try {
                  if (editandoTipoId) {
                    await mutacaoAtualizarTipo?.mutateAsync({ id: editandoTipoId, nome });
                    definirEditandoTipoId(null);
                    definirEditandoTipoNome('');
                    exibirNotificacao({ tipo: 'sucesso', mensagem: 'Tipo atualizado.' });
                  } else {
                    await mutacaoCriarTipo.mutateAsync(nome);
                    definirNovoTipoLocal('');
                    exibirNotificacao({ tipo: 'sucesso', mensagem: 'Tipo de local criado.' });
                  }
                } catch {
                  exibirNotificacao({ tipo: 'erro', mensagem: 'Não foi possível salvar o tipo.' });
                }
              }}
              className="flex gap-2"
            >
              <input
                className="flex-1 rounded-lg border border-gray-300 px-3 py-2"
                placeholder="Novo tipo (ex: Obra, Escritório)"
                value={editandoTipoId ? editandoTipoNome : novoTipoLocal}
                onChange={(ev) => (editandoTipoId ? definirEditandoTipoNome(ev.target.value) : definirNovoTipoLocal(ev.target.value))}
              />
              <div className="flex gap-2">
                {editandoTipoId && (
                  <button
                    type="button"
                    className="rounded-lg bg-gray-300 px-4 py-2"
                    onClick={() => { definirEditandoTipoId(null); definirEditandoTipoNome(''); }}
                  >
                    Cancelar
                  </button>
                )}
                <button
                  type="submit"
                  className="rounded-lg bg-green-600 px-4 py-2 text-white"
                  disabled={mutacaoCriarTipo.isPending || mutacaoAtualizarTipo?.isPending}
                >
                  {(editandoTipoId ? mutacaoAtualizarTipo?.isPending : mutacaoCriarTipo.isPending) ? 'Salvando...' : (editandoTipoId ? 'Salvar' : 'Criar Tipo')}
                </button>
              </div>
            </form>

            <div className="mt-4">
              <h5 className="text-sm font-medium text-gray-700">Tipos cadastrados</h5>
              <div className="mt-2 space-y-2">
                {consultaTipos.isLoading && <p className="text-gray-500">Carregando tipos...</p>}
                {consultaTipos.isError && <p className="text-red-600">Não foi possível carregar os tipos.</p>}
                {consultaTipos.data?.map((tipo: TipoLocalAdmin) => (
                  <div key={tipo.id} className="flex items-center justify-between rounded bg-gray-50 px-3 py-2">
                    <span>{tipo.nome}</span>
                    <div className="flex gap-2">
                      <button
                        className="text-sm px-2 py-1 bg-yellow-100 rounded"
                        onClick={() => { definirEditandoTipoId(tipo.id); definirEditandoTipoNome(tipo.nome); }}
                      >
                        Editar
                      </button>
                      <button
                        className="text-red-600 text-sm"
                        onClick={() => {
                          if (!confirm('Remover este tipo?')) return;
                          mutacaoRemoverTipo.mutate(tipo.id);
                        }}
                        disabled={mutacaoRemoverTipo.isPending}
                      >
                        {mutacaoRemoverTipo.isPending ? 'Removendo...' : 'Remover'}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ) : (
          <div>
            <form
              onSubmit={async (e) => {
                e.preventDefault();
                const nome = (editandoLocalId ? editandoLocalNome : novoLocalNome).trim();
                const tipoId = Number(editandoLocalId ? editandoLocalTipoId : novoLocalTipoId);
                if (!nome || !tipoId) {
                  exibirNotificacao({ tipo: 'erro', mensagem: 'Preencha nome e tipo do local.' });
                  return;
                }
                try {
                  if (editandoLocalId) {
                    await mutacaoAtualizarLocal?.mutateAsync({ id: editandoLocalId, dados: { nome, tipoLocalId: tipoId } });
                    definirEditandoLocalId(null);
                    definirEditandoLocalNome('');
                    definirEditandoLocalTipoId('');
                    exibirNotificacao({ tipo: 'sucesso', mensagem: 'Local atualizado.' });
                  } else {
                    await mutacaoCriarLocal.mutateAsync({ nome, tipoLocalId: tipoId });
                    definirNovoLocalNome('');
                    definirNovoLocalTipoId('');
                    exibirNotificacao({ tipo: 'sucesso', mensagem: 'Local criado.' });
                  }
                } catch {
                  exibirNotificacao({ tipo: 'erro', mensagem: 'Não foi possível salvar o local.' });
                }
              }}
              className="space-y-2"
            >
              <div className="flex gap-2">
                <input
                  className="flex-1 rounded-lg border border-gray-300 px-3 py-2"
                  placeholder="Nome do local"
                  value={editandoLocalId ? editandoLocalNome : novoLocalNome}
                  onChange={(ev) => (editandoLocalId ? definirEditandoLocalNome(ev.target.value) : definirNovoLocalNome(ev.target.value))}
                />
                <select
                  value={editandoLocalId ? editandoLocalTipoId : novoLocalTipoId}
                  onChange={(ev) => (editandoLocalId ? definirEditandoLocalTipoId(ev.target.value ? Number(ev.target.value) : '') : definirNovoLocalTipoId(ev.target.value ? Number(ev.target.value) : ''))}
                  className="rounded-lg border border-gray-300 px-3 py-2"
                >
                  <option value="">Tipo</option>
                  {consultaTipos.data?.map((t: TipoLocalAdmin) => (
                    <option key={t.id} value={t.id}>{t.nome}</option>
                  ))}
                </select>
              </div>
              <div className="flex gap-2">
                {editandoLocalId && (
                  <button
                    type="button"
                    className="rounded-lg bg-gray-300 px-4 py-2"
                    onClick={() => { definirEditandoLocalId(null); definirEditandoLocalNome(''); definirEditandoLocalTipoId(''); }}
                  >
                    Cancelar
                  </button>
                )}
                <button type="submit" className="rounded-lg bg-blue-600 px-4 py-2 text-white" disabled={mutacaoCriarLocal.isPending || mutacaoAtualizarLocal?.isPending}>
                  {(editandoLocalId ? mutacaoAtualizarLocal?.isPending : mutacaoCriarLocal.isPending) ? 'Salvando...' : (editandoLocalId ? 'Salvar' : 'Criar Local')}
                </button>
              </div>
            </form>

            <div className="mt-4">
              <h5 className="text-sm font-medium text-gray-700">Locais cadastrados</h5>
              <div className="mt-2 space-y-2">
                {consultaLocais.isLoading && <p className="text-gray-500">Carregando locais...</p>}
                {consultaLocais.isError && <p className="text-red-600">Não foi possível carregar os locais.</p>}
                {consultaLocais.data?.map((local: LocalTrabalho) => (
                  <div key={local.id} className="flex items-center justify-between rounded bg-gray-50 px-3 py-2">
                    <span>{local.nome} ({local.tipoLocalNome})</span>
                    <div className="flex gap-2">
                      <button
                        className="text-sm px-2 py-1 bg-yellow-100 rounded"
                        onClick={() => { definirEditandoLocalId(local.id); definirEditandoLocalNome(local.nome); definirEditandoLocalTipoId(local.tipoLocalId); }}
                      >
                        Editar
                      </button>
                      <button
                        className="text-red-600 text-sm"
                        onClick={() => {
                          if (!confirm('Remover este local?')) return;
                          mutacaoRemoverLocal.mutate(local.id);
                        }}
                        disabled={mutacaoRemoverLocal.isPending}
                      >
                        {mutacaoRemoverLocal.isPending ? 'Removendo...' : 'Remover'}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
