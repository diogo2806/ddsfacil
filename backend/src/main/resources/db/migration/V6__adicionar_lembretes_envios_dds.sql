-- Arquivo: backend/src/main/resources/db/migration/V6__adicionar_lembretes_envios_dds.sql

ALTER TABLE envios_dds
    ADD COLUMN quantidade_lembretes INTEGER NOT NULL DEFAULT 0;

ALTER TABLE envios_dds
    ADD COLUMN momento_ultimo_lembrete TIMESTAMP;
