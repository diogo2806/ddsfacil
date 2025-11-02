import type { FC } from 'react';

type PropriedadesTelaDivulgacao = {
  aoSolicitarLogin: () => void;
};

const TelaDivulgacao: FC<PropriedadesTelaDivulgacao> = ({ aoSolicitarLogin }) => {
  return (
    <div className="min-h-screen bg-gray-50 text-gray-800 antialiased flex flex-col">
      <header className="bg-white/80 backdrop-blur-sm shadow-sm sticky top-0 z-50">
        <nav className="container mx-auto px-6 py-4 flex justify-between items-center">
          <div className="text-3xl font-bold text-blue-600">DDS Facil</div>
          <div className="flex items-center gap-4">
            <button
              type="button"
              className="hidden sm:inline-block bg-blue-600 text-white font-medium px-5 py-2 rounded-lg shadow-lg hover:bg-blue-700 transition duration-300"
            >
              Agendar Demonstração
            </button>
            <button
              type="button"
              onClick={aoSolicitarLogin}
              className="bg-white text-blue-600 border border-blue-200 font-medium px-5 py-2 rounded-lg shadow-lg hover:bg-blue-50 transition duration-300"
            >
              Entrar no Sistema
            </button>
          </div>
        </nav>
      </header>

      <main className="flex-1">
        <section className="bg-white pt-20 pb-24">
          <div className="container mx-auto px-6 text-center max-w-4xl">
            <h1 className="text-4xl md:text-5xl font-extrabold text-gray-900 leading-tight">
              Chega de papel. O DDS agora é digital e a confirmação é na hora.
            </h1>
            <p className="mt-6 text-lg md:text-xl text-gray-600 max-w-3xl mx-auto">
              Substitua o DDS de papel. Envie o treinamento por SMS, receba a confirmação na hora e garanta o compliance das suas equipes em campo sem logística.
            </p>
            <div className="mt-10 flex flex-col sm:flex-row justify-center gap-4">
              <a
                href="#solucao"
                className="bg-blue-600 text-white font-bold px-8 py-3 rounded-lg text-lg shadow-xl hover:bg-blue-700 transition duration-300"
              >
                Ver como funciona
              </a>
              <a
                href="#beneficios"
                className="bg-white text-blue-600 border border-blue-200 font-bold px-8 py-3 rounded-lg text-lg shadow-sm hover:bg-blue-50 transition duration-300"
              >
                Ver Benefícios
              </a>
            </div>
          </div>
        </section>

        <section className="py-20 bg-gray-50" id="problema">
          <div className="container mx-auto px-6">
            <div className="text-center max-w-3xl mx-auto">
              <h2 className="text-base font-semibold text-blue-600 uppercase tracking-wide">
                A Dor da Operação Atual
              </h2>
              <h3 className="mt-2 text-3xl md:text-4xl font-extrabold text-gray-900">
                Sua logística de DDS está custando caro e gerando risco
              </h3>
            </div>
            <div className="mt-16 grid md:grid-cols-3 gap-8">
              <div className="bg-white p-8 rounded-2xl shadow-xl border border-gray-100">
                <div className="bg-red-100 text-red-600 rounded-full h-12 w-12 flex items-center justify-center">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M6 9V2h12v7" />
                    <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2" />
                    <path d="M6 14h12v8H6z" />
                  </svg>
                </div>
                <h4 className="mt-5 text-xl font-bold text-gray-900">Custo Logístico</h4>
                <p className="mt-2 text-gray-600">
                  Gastos com impressão de papel, malotes para obras distantes e o envio de "dinheiro" para coordenar a operação.
                </p>
              </div>
              <div className="bg-white p-8 rounded-2xl shadow-xl border border-gray-100">
                <div className="bg-red-100 text-red-600 rounded-full h-12 w-12 flex items-center justify-center">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <circle cx="12" cy="12" r="10" />
                    <path d="m15 9-6 6" />
                    <path d="M9 9l6 6" />
                  </svg>
                </div>
                <h4 className="mt-5 text-xl font-bold text-gray-900">Risco de Compliance</h4>
                <p className="mt-2 text-gray-600">
                  "Muitas das vezes não acontece". Sem a confirmação, é impossível auditar quem participou, gerando risco jurídico.
                </p>
              </div>
              <div className="bg-white p-8 rounded-2xl shadow-xl border border-gray-100">
                <div className="bg-red-100 text-red-600 rounded-full h-12 w-12 flex items-center justify-center">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <circle cx="12" cy="12" r="10" />
                    <polyline points="12 6 12 12 16 14" />
                  </svg>
                </div>
                <h4 className="mt-5 text-xl font-bold text-gray-900">Falta de Controle</h4>
                <p className="mt-2 text-gray-600">
                  Atraso na coleta das listas de presença em papel. Você só descobre quem faltou semanas depois, quando já é tarde demais.
                </p>
              </div>
            </div>
          </div>
        </section>

        <section id="solucao" className="py-24 bg-blue-600 text-white">
          <div className="container mx-auto px-6">
            <div className="text-center max-w-3xl mx-auto">
              <h2 className="text-base font-semibold text-blue-200 uppercase tracking-wide">
                A Solução
              </h2>
              <h3 className="mt-2 text-3xl md:text-4xl font-extrabold text-white">Simples como 1, 2, 3</h3>
              <p className="mt-4 text-lg text-blue-100">
                O DDS Facil transforma um processo complexo em três passos simples.
              </p>
            </div>
            <div className="mt-16 grid md:grid-cols-3 gap-8 text-center text-gray-800">
              <div className="bg-white p-8 rounded-2xl shadow-xl">
                <div className="bg-blue-100 text-blue-600 rounded-full h-16 w-16 flex items-center justify-center text-3xl font-bold mx-auto">
                  1
                </div>
                <h4 className="mt-5 text-xl font-bold text-gray-900">Cadastre o DDS</h4>
                <p className="mt-2 text-gray-600">
                  Crie o conteúdo do dia (texto, imagem ou vídeo) na plataforma em 2 minutos.
                </p>
              </div>
              <div className="bg-white p-8 rounded-2xl shadow-xl">
                <div className="bg-blue-100 text-blue-600 rounded-full h-16 w-16 flex items-center justify-center text-3xl font-bold mx-auto">
                  2
                </div>
                <h4 className="mt-5 text-xl font-bold text-gray-900">Envie por SMS</h4>
                <p className="mt-2 text-gray-600">
                  Dispare o link único para todos os trabalhadores da obra com um único clique.
                </p>
              </div>
              <div className="bg-white p-8 rounded-2xl shadow-xl">
                <div className="bg-blue-100 text-blue-600 rounded-full h-16 w-16 flex items-center justify-center text-3xl font-bold mx-auto">
                  3
                </div>
                <h4 className="mt-5 text-xl font-bold text-gray-900">Receba a Confirmação</h4>
                <p className="mt-2 text-gray-600">
                  O trabalhador lê e confirma. Você vê o status em tempo real no dashboard.
                </p>
              </div>
            </div>
          </div>
        </section>

        <section id="beneficios" className="py-24 bg-white">
          <div className="container mx-auto px-6">
            <div className="text-center max-w-3xl mx-auto">
              <h2 className="text-base font-semibold text-blue-600 uppercase tracking-wide">
                Benefícios para a Gerência
              </h2>
              <h3 className="mt-2 text-3xl md:text-4xl font-extrabold text-gray-900">
                Controle total da operação na palma da sua mão
              </h3>
            </div>
            <div className="mt-16 max-w-5xl mx-auto grid md:grid-cols-2 gap-x-12 gap-y-10">
              <div className="flex items-start gap-5">
                <div className="flex-shrink-0 bg-green-100 text-green-600 rounded-full h-12 w-12 flex items-center justify-center">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                    <path d="m9 12 2 2 4-4" />
                  </svg>
                </div>
                <div>
                  <h4 className="text-xl font-bold text-gray-900">Compliance Garantido</h4>
                  <p className="mt-1 text-gray-600">
                    Registro digital, individual e com data/hora de quem participou. 100% auditável em caso de fiscalização.
                  </p>
                </div>
              </div>
              <div className="flex items-start gap-5">
                <div className="flex-shrink-0 bg-green-100 text-green-600 rounded-full h-12 w-12 flex items-center justify-center">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <line x1="12" y1="1" x2="12" y2="23" />
                    <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
                    <path d="m19 9-4-4-4 4" />
                  </svg>
                </div>
                <div>
                  <h4 className="text-xl font-bold text-gray-900">Custo Logístico Zero</h4>
                  <p className="mt-1 text-gray-600">
                    Elimine 100% dos custos de impressão, malotes e envio de "dinheiro" para coordenar o DDS em papel.
                  </p>
                </div>
              </div>
              <div className="flex items-start gap-5">
                <div className="flex-shrink-0 bg-green-100 text-green-600 rounded-full h-12 w-12 flex items-center justify-center">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M3 3v18h18" />
                    <path d="m18 9-5 5-4-4-3 3" />
                  </svg>
                </div>
                <div>
                  <h4 className="text-xl font-bold text-gray-900">Gestão em Tempo Real</h4>
                  <p className="mt-1 text-gray-600">
                    Acesse um dashboard e veja na hora a taxa de adesão por equipe ou obra. Aja proativamente sobre quem não participou.
                  </p>
                </div>
              </div>
              <div className="flex items-start gap-5">
                <div className="flex-shrink-0 bg-green-100 text-green-600 rounded-full h-12 w-12 flex items-center justify-center">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <rect x="5" y="2" width="14" height="20" rx="2" ry="2" />
                    <path d="M12 18h.01" />
                  </svg>
                </div>
                <div>
                  <h4 className="text-xl font-bold text-gray-900">Alcance Total</h4>
                  <p className="mt-1 text-gray-600">
                    Garanta que o DDS chegue até o trabalhador na obra mais distante, bastando ter um celular com sinal de SMS.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="py-24 bg-blue-50">
          <div className="container mx-auto px-6 text-center max-w-3xl">
            <h2 className="text-3xl md:text-4xl font-extrabold text-gray-900">
              Pronto para eliminar o papel e ganhar controle total da operação?
            </h2>
            <p className="mt-5 text-lg text-gray-600">
              Vamos conversar sobre como o DDS Facil pode simplificar sua operação, reduzir custos e garantir 100% de compliance.
            </p>
            <button
              type="button"
              onClick={aoSolicitarLogin}
              className="mt-8 inline-block bg-blue-600 text-white font-bold px-10 py-4 rounded-lg text-lg shadow-xl hover:bg-blue-700 transition duration-300"
            >
              Quero acessar o sistema agora
            </button>
          </div>
        </section>
      </main>

      <footer className="py-12 bg-gray-800 text-gray-400">
        <div className="container mx-auto px-6 text-center">
          <p className="text-2xl font-bold text-white mb-2">DDS Facil</p>
          <p>&copy; 2025 DDS Facil - Simplificando a Segurança do Trabalho.</p>
        </div>
      </footer>
    </div>
  );
};

export default TelaDivulgacao;
