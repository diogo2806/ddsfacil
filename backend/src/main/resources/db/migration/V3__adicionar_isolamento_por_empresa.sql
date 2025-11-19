ALTER TABLE locais_trabalho ADD COLUMN empresa_id BIGINT;
UPDATE locais_trabalho SET empresa_id = COALESCE(empresa_id, 1);
ALTER TABLE locais_trabalho ALTER COLUMN empresa_id SET NOT NULL;

ALTER TABLE funcionarios ADD COLUMN empresa_id BIGINT;
UPDATE funcionarios f
SET empresa_id = COALESCE(f.empresa_id, l.empresa_id)
FROM locais_trabalho l
WHERE f.local_trabalho_id = l.id;
UPDATE funcionarios SET empresa_id = COALESCE(empresa_id, 1);
ALTER TABLE funcionarios ALTER COLUMN empresa_id SET NOT NULL;

ALTER TABLE conteudos_dds ADD COLUMN empresa_id BIGINT;
UPDATE conteudos_dds SET empresa_id = COALESCE(empresa_id, 1);
ALTER TABLE conteudos_dds ALTER COLUMN empresa_id SET NOT NULL;

ALTER TABLE envios_dds ADD COLUMN empresa_id BIGINT;
UPDATE envios_dds e
SET empresa_id = COALESCE(e.empresa_id, f.empresa_id)
FROM funcionarios f
WHERE e.funcionario_id = f.id;
UPDATE envios_dds SET empresa_id = COALESCE(empresa_id, 1);
ALTER TABLE envios_dds ALTER COLUMN empresa_id SET NOT NULL;

CREATE INDEX idx_locais_trabalho_empresa_id ON locais_trabalho (empresa_id);
CREATE INDEX idx_funcionarios_empresa_id ON funcionarios (empresa_id);
CREATE INDEX idx_conteudos_dds_empresa_id ON conteudos_dds (empresa_id);
CREATE INDEX idx_envios_dds_empresa_id ON envios_dds (empresa_id);
