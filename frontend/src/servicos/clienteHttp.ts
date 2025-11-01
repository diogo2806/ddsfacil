import axios from 'axios';
import { URL_BASE_API } from '../configuracao/api';

export const clienteHttp = axios.create({
  baseURL: URL_BASE_API,
  headers: {
    'Content-Type': 'application/json',
  },
});
