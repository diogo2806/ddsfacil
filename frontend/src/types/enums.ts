// Arquivo: frontend/src/types/enums.ts
// Enumeradores compartilhados entre componentes e serviços.

export enum TelaApp {
  DIVULGACAO = 'DIVULGACAO',
  AUTENTICACAO = 'AUTENTICACAO',
  PAINEL = 'PAINEL',
}

export enum AbaPainel {
  DASHBOARD = 'DASHBOARD',
  ENVIAR = 'ENVIAR',
  FUNCIONARIOS = 'FUNCIONARIOS',
  CONTEUDO = 'CONTEUDO',
  TIPOS_LOCAL = 'TIPOS_LOCAL',
  LOCAIS = 'LOCAIS',
  RELATORIOS = 'RELATORIOS',
  USUARIOS = 'USUARIOS',
}

export enum TipoNotificacao {
  SUCESSO = 'SUCESSO',
  ERRO = 'ERRO',
}

export enum StatusEnvio {
  PENDENTE = 'Pendente',
  ENVIADO = 'Enviado',
  CONFIRMADO = 'Confirmado',
  FALHA = 'Falha', 
}

export enum AbaAdmin {
  TIPOS = 'TIPOS',
  LOCAIS = 'LOCAIS',
}

export enum TipoConteudo {
  TEXTO = 'TEXTO',
  LINK = 'LINK',
  ARQUIVO = 'ARQUIVO',
}
