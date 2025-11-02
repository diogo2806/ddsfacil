import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import App from './paginas/frontendConteudo/App';
import PaginaConfirmacaoTrabalhador from './paginas/confirmacaoTrabalhador/PaginaConfirmacaoTrabalhador';
import './styles.css';

const clienteConsulta = new QueryClient();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={clienteConsulta}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<App />} />
          <Route path="/c/:token" element={<PaginaConfirmacaoTrabalhador />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
);
