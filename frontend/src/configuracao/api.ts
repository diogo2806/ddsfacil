const urlConfigurada = typeof import.meta.env.VITE_URL_BASE_API === 'string'
  ? import.meta.env.VITE_URL_BASE_API.trim()
  : '';

if (!urlConfigurada) {
  throw new Error(
    'Configure a variável de ambiente VITE_URL_BASE_API com a URL base real do backend antes de iniciar o aplicativo.',
  );
}

if (!/^https?:\/\//i.test(urlConfigurada)) {
  throw new Error('A URL base da API deve iniciar com http:// ou https://');
}

export const URL_BASE_API = urlConfigurada.replace(/\/+$/, '');
