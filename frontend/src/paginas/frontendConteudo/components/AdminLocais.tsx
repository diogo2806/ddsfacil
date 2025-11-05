// Arquivo: frontend/src/paginas/frontendConteudo/components/AdminLocais.tsx
import React from 'react';
import { AbaAdmin } from '../../../types/enums';
import AdminTiposLocal from './AdminTiposLocal'; // [NOVO IMPORT]
import AdminLocaisTrabalho from './AdminLocaisTrabalho'; // [NOVO IMPORT]

// As props ainda são "gigantes" porque App.tsx está passando tudo para cá.
// Mas agora, este componente vai *distribuir* as props, não usá-las.
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
  exibirNotificacao: (n: { tipo: 'sucesso' | 'erro'; mensagem: string }) => void;
  aba: AbaAdmin;
  definirAba: (aba: AbaAdmin) => void;
};

// Este componente agora é um "pass-through"
export default function AdminLocais(props: Props) {
  const { aba, definirAba } = props;

  return (
    <div className="mt-6 border-t pt-6">
      <h4 className="text-lg font-semibold text-gray-900">Administrar Locais</h4>
      <p className="text-sm text-gray-500 mb-4">Cadastre tipos de local e locais de trabalho usados no sistema.</p>

      <div className="space-y-4">
        <div className="flex gap-2 mb-3">
          {/* Botão da Aba "Tipos" */}
          <button
            type="button"
            onClick={() => definirAba(AbaAdmin.TIPOS)}
            className={`px-3 py-1 rounded ${
              aba === AbaAdmin.TIPOS ? 'bg-blue-600 text-white' : 'bg-gray-100'
            }`}
          >
            Tipos de Local
          </button>
          
          {/* Botão da Aba "Locais" */}
          <button
            type="button"
            onClick={() => definirAba(AbaAdmin.LOCAIS)}
            className={`px-3 py-1 rounded ${
              aba === AbaAdmin.LOCAIS ? 'bg-blue-600 text-white' : 'bg-gray-100'
            }`}
          >
            Locais
          </button>
        </div>

        {/* Renderização condicional delegada.
          Passamos apenas as props que cada componente realmente precisa.
        */}
        {aba === AbaAdmin.TIPOS ? (
          <AdminTiposLocal
            consultaTipos={props.consultaTipos}
            mutacaoCriarTipo={props.mutacaoCriarTipo}
            mutacaoRemoverTipo={props.mutacaoRemoverTipo}
            mutacaoAtualizarTipo={props.mutacaoAtualizarTipo}
            editandoTipoId={props.editandoTipoId}
            definirEditandoTipoId={props.definirEditandoTipoId}
            editandoTipoNome={props.editandoTipoNome}
            definirEditandoTipoNome={props.definirEditandoTipoNome}
            novoTipoLocal={props.novoTipoLocal}
            definirNovoTipoLocal={props.definirNovoTipoLocal}
            exibirNotificacao={props.exibirNotificacao}
          />
        ) : (
          <AdminLocaisTrabalho
            consultaLocais={props.consultaLocais}
            consultaTipos={props.consultaTipos} // Necessário para o dropdown
            mutacaoCriarLocal={props.mutacaoCriarLocal}
            mutacaoRemoverLocal={props.mutacaoRemoverLocal}
            mutacaoAtualizarLocal={props.mutacaoAtualizarLocal}
            editandoLocalId={props.editandoLocalId}
            definirEditandoLocalId={props.definirEditandoLocalId}
            editandoLocalNome={props.editandoLocalNome}
            definirEditandoLocalNome={props.definirEditandoLocalNome}
            editandoLocalTipoId={props.editandoLocalTipoId}
            definirEditandoLocalTipoId={props.definirEditandoLocalTipoId}
            novoLocalNome={props.novoLocalNome}
            definirNovoLocalNome={props.definirNovoLocalNome}
            novoLocalTipoId={props.novoLocalTipoId}
            definirNovoLocalTipoId={props.definirNovoLocalTipoId}
            exibirNotificacao={props.exibirNotificacao}
          />
        )}
      </div>
    </div>
  );
}