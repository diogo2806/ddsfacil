DELETE FROM envios_dds;
DELETE FROM conteudos_dds;
DELETE FROM funcionarios;

INSERT INTO conteudos_dds (id, titulo, descricao) VALUES
  (1, 'Procedimentos de Segurança em Altura', 'Reforce o uso de cintos de segurança, travas antiqueda e inspeções diárias antes de iniciar atividades acima de dois metros.'),
  (2, 'Checklist de Equipamentos de Proteção', 'Garanta que todos os colaboradores utilizem capacete, óculos, protetor auricular e luvas adequados antes de acessar o canteiro.');

INSERT INTO funcionarios (id, nome, celular, obra) VALUES
  (1, 'Ana Souza', '(11) 99999-1111', 'Obra Centro'),
  (2, 'Carlos Lima', '(11) 98888-2222', 'Obra Centro'),
  (3, 'Fernanda Alves', '(21) 97777-3333', 'Obra Aeroporto');

INSERT INTO envios_dds (id, funcionario_id, conteudo_id, data_envio, momento_envio, momento_confirmacao, status) VALUES
  (1, 1, 1, CURRENT_DATE, DATEADD('HOUR', -2, CURRENT_TIMESTAMP()), DATEADD('HOUR', -1, CURRENT_TIMESTAMP()), 'CONFIRMADO'),
  (2, 2, 1, CURRENT_DATE, DATEADD('HOUR', -2, CURRENT_TIMESTAMP()), NULL, 'ENVIADO'),
  (3, 3, 2, DATEADD('DAY', -1, CURRENT_DATE), DATEADD('HOUR', -26, CURRENT_TIMESTAMP()), NULL, 'ENVIADO');

ALTER TABLE conteudos_dds ALTER COLUMN id RESTART WITH 100;
ALTER TABLE funcionarios ALTER COLUMN id RESTART WITH 100;
ALTER TABLE envios_dds ALTER COLUMN id RESTART WITH 100;
