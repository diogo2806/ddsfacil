const urlConfigurada = typeof import.meta.env.VITE_URL_BASE_API === 'string'
  ? import.meta.env.VITE_URL_BASE_API.trim()
  : '';

const URL_API_ANTIGA = 'api-ddsfacil.valenstech.com.br';
const URL_API_ATUAL = 'api-ddsfacil.iforce.com.br';

if (!urlConfigurada) {
  throw new Error(
    'Configure a variável de ambiente VITE_URL_BASE_API com a URL base real do backend antes de iniciar o aplicativo.',
  );
}

if (!/^https?:\/\//i.test(urlConfigurada)) {
  throw new Error('A URL base da API deve iniciar com http:// ou https://');
}

const urlNormalizada = urlConfigurada.replace(/\/+$/, '');

export const URL_BASE_API = urlNormalizada.includes(URL_API_ANTIGA)
  ? urlNormalizada.replace(URL_API_ANTIGA, URL_API_ATUAL)
  : urlNormalizada;
