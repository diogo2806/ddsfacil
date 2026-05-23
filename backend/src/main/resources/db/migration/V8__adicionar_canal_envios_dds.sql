-- Arquivo: backend/src/main/resources/db/migration/V8__adicionar_canal_envios_dds.sql

ALTER TABLE envios_dds
    ADD COLUMN canal VARCHAR(20) NOT NULL DEFAULT 'SMS';
