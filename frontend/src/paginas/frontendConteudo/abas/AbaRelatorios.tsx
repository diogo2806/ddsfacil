import { FormEvent, useMemo, useState } from 'react';
import { useConteudos } from '../../../hooks/useConteudos';
import { useLocais } from '../../../hooks/useLocais';
import { baixarRelatorioCompliance } from '../../../servicos/relatorioServico';
import { TipoNotificacao } from '../../../types/enums';

const dataAtual = () => new Date().toISOString().split('T')[0];

type Props = {
  exibirNotificacao: (notificacao: { tipo: TipoNotificacao; mensagem: string }) => void;
};

async function extrairMensagemErro(erro: unknown): Promise<string> {
  const resposta = (erro as { response?: { data?: unknown } })?.response?.data;
  if (resposta instanceof Blob) {
    try {
      const texto = await resposta.text();
      const objeto = JSON.parse(texto) as { mensagem?: string };
      if (objeto?.mensagem) {
        return objeto.mensagem;
      }
    } catch {
      // ignora corpo não-JSON
    }
  }
  return 'Não foi possível gerar o relatório. Verifique os filtros e tente novamente.';
}

export default function AbaRelatorios({ exibirNotificacao }: Props) {
  const [conteudoId, definirConteudoId] = useState<string>('');
  const [data, definirData] = useState<string>(dataAtual());
  const [localId, definirLocalId] = useState<string>('');
  const [gerando, definirGerando] = useState<boolean>(false);

  const { consultaConteudos } = useConteudos();
  const { consultaLocaisTrabalho } = useLocais();

  const conteudos = useMemo(() => consultaConteudos.data ?? [], [consultaConteudos.data]);
  const locais = useMemo(
    () =>
      (consultaLocaisTrabalho.data ?? [])
        .slice()
        .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' })),
    [consultaLocaisTrabalho.data],
  );

  async function aoGerar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();

    const conteudoIdNumero = Number.parseInt(conteudoId, 10);
    if (Number.isNaN(conteudoIdNumero)) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Selecione um conteúdo para gerar a evidência.' });
      return;
    }
    if (!data) {
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem: 'Informe a data de referência.' });
      return;
    }

    const localIdNumero = Number.parseInt(localId, 10);

    definirGerando(true);
    try {
      const { arquivo, nomeArquivo } = await baixarRelatorioCompliance({
        conteudoId: conteudoIdNumero,
        data,
        localTrabalhoId: Number.isNaN(localIdNumero) ? undefined : localIdNumero,
      });

      const url = window.URL.createObjectURL(arquivo);
      const link = document.createElement('a');
      link.href = url;
      link.download = nomeArquivo;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      exibirNotificacao({ tipo: TipoNotificacao.SUCESSO, mensagem: 'Relatório de evidência gerado com sucesso.' });
    } catch (erro) {
      const mensagem = await extrairMensagemErro(erro);
      exibirNotificacao({ tipo: TipoNotificacao.ERRO, mensagem });
    } finally {
      definirGerando(false);
    }
  }

  return (
    <div className="grid gap-6 md:grid-cols-[2fr,3fr]">
      <section className="space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900">Evidência de compliance (PDF)</h3>
        <p className="text-sm text-gray-600">
          Gere o documento oficial com a lista de presença das confirmações de um DDS em uma data específica.
        </p>

        <form className="space-y-4" onSubmit={aoGerar}>
          <div className="space-y-2">
            <label htmlFor="relatorio-conteudo" className="text-sm font-medium text-gray-700">
              Conteúdo do DDS
            </label>
            <select
              id="relatorio-conteudo"
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              value={conteudoId}
              onChange={(evento) => definirConteudoId(evento.target.value)}
              disabled={consultaConteudos.isLoading}
              required
            >
              <option value="">Selecione um conteúdo</option>
              {conteudos.map((conteudo) => (
                <option key={conteudo.id} value={conteudo.id.toString()}>
                  {conteudo.titulo}
                </option>
              ))}
            </select>
            {consultaConteudos.isError && (
              <p className="text-sm text-red-600">Não foi possível carregar os conteúdos.</p>
            )}
          </div>

          <div className="space-y-2">
            <label htmlFor="relatorio-data" className="text-sm font-medium text-gray-700">
              Data de referência
            </label>
            <input
              id="relatorio-data"
              type="date"
              value={data}
              onChange={(evento) => definirData(evento.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              required
            />
          </div>

          <div className="space-y-2">
            <label htmlFor="relatorio-local" className="text-sm font-medium text-gray-700">
              Local de trabalho (opcional)
            </label>
            <select
              id="relatorio-local"
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
              value={localId}
              onChange={(evento) => definirLocalId(evento.target.value)}
              disabled={consultaLocaisTrabalho.isLoading}
            >
              <option value="">Todos os locais</option>
              {locais.map((local) => (
                <option key={local.id} value={local.id.toString()}>
                  {local.nome} ({local.tipoLocalNome})
                </option>
              ))}
            </select>
          </div>

          <button
            type="submit"
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
            disabled={gerando}
          >
            {gerando ? 'Gerando PDF...' : 'Baixar evidência em PDF'}
          </button>
        </form>
      </section>

      <section className="rounded-lg border border-blue-100 bg-blue-50 p-6 text-sm text-blue-900">
        <h4 className="text-base font-semibold">O que contém o documento</h4>
        <ul className="mt-3 list-disc space-y-2 pl-5">
          <li>Identificação do conteúdo ministrado (título e descrição).</li>
          <li>Lista de presença com os colaboradores que confirmaram a leitura.</li>
          <li>Horário de envio e de confirmação de cada participante.</li>
          <li>Código de autenticação digital para auditoria.</li>
        </ul>
        <p className="mt-4 text-xs text-blue-800">
          Apenas confirmações são listadas. Use o filtro de local para emitir uma evidência por obra/setor.
        </p>
      </section>
    </div>
  );
}
