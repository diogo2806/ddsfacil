-- Adiciona a coluna para armazenar os dados binários de arquivos associados aos conteúdos DDS
ALTER TABLE conteudos_dds
    ADD COLUMN IF NOT EXISTS arquivo_dados BYTEA;
