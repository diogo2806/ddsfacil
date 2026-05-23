-- Arquivo: backend/src/main/resources/db/migration/V7__adicionar_agendamento_envios_dds.sql

ALTER TABLE envios_dds
    ADD COLUMN momento_agendado TIMESTAMP;
