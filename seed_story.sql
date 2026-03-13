-- ══════════════════════════════════════════════════════════════
-- HISTÓRIA: A Queda de Sauron — Motor Narrativo
-- ══════════════════════════════════════════════════════════════

-- Story
INSERT INTO stories (id, title, description, created_at, updated_at) VALUES
('aa000000-0000-0000-0000-000000000001',
 'A Queda de Sauron — Motor Narrativo',
 'Motor narrativo da Guerra do Anel. Cada cena transforma o estado do mundo atraves de requisitos e efeitos encadeados. Explore os caminhos possiveis: de Valfenda ao Monte da Perdicao, cada escolha importa.',
 NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ══ SCENES ══════════════════════════════════════════════════

INSERT INTO scenes (id, story_id, title, description, repeatable, display_order, priority, tags, involved_characters) VALUES

-- 1. Início — sem requisitos
('bb000001-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'O Conselho de Valfenda',
 'Elrond convoca representantes de todos os povos livres. O Um Anel e revelado como o inimigo central. Nove companheiros sao escolhidos — a Sociedade do Anel e formada.',
 false, 0, 90, 'abertura,politica,alianca', 'gandalf,frodo,aragorn,legolas,gimli,boromir'),

-- 2. Requer: missao.iniciada
('bb000002-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'A Travessia das Montanhas Nubladas',
 'A Sociedade tenta cruzar Caradhras. Saruman envia uma tempestade de neve. A passagem esta bloqueada — a decisao recai sobre Gandalf: entrar nas Minas de Moria.',
 false, 1, 70, 'jornada,clima,decisao', 'gandalf,frodo,aragorn'),

-- 3. Requer: montanhas.tentadas
('bb000003-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Gandalf Cai na Ponte de Khazad-dum',
 'Um Balrog de Morgoth emerge das profundezas. Gandalf enfrenta a criatura na Ponte de Khazad-dum: "Voce nao passara!" Ambos caem no abismo. A Sociedade foge devastada.',
 false, 2, 95, 'batalha,sacrificio,perda', 'gandalf,balrog'),

-- 4. Requer: moria.cruzada + boromir vivo (FACT_ABSENT boromir.morto)
('bb000004-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Boromir e Corrompido pelo Anel',
 'Em Amon Hen, o Anel sussurra para Boromir. Ele tenta toma-lo de Frodo a forca. Frodo foge sozinho. Boromir, arrependido, morre defendendo Merry e Pippin dos Uruk-hai — sua redenção final.',
 false, 3, 75, 'corrupcao,morte,redencao', 'boromir,frodo'),

-- 5. Requer: frodo.sozinho
('bb000005-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Frodo Aceita Gollum como Guia',
 'Frodo ve em Gollum o que ele mesmo pode se tornar. Contra o conselho de Sam, aceita a criatura como guia unico ate Mordor — um ato de compaixao que mudara o destino do mundo.',
 false, 4, 65, 'compaixao,risco,jornada', 'frodo,gollum,sam'),

-- 6. Requer: gandalf.morto
('bb000006-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Gandalf Retorna como o Branco',
 'Gandalf derrota o Balrog nas profundezas de Moria apos dez dias de combate. Os Valar o enviam de volta com maior poder. Ele encontra Aragorn, Legolas e Gimli em Fangorn como Gandalf, o Branco.',
 false, 5, 85, 'renascimento,poder,virada', 'gandalf'),

-- 7. Requer: boromir.morto (companhia dissolvida)
('bb000007-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Batalha do Abismo de Helm',
 'Dez mil Uruk-hai cercam a fortaleza. Aragorn lidera a defesa com punhado de guerreiros e velhos. No raiar do dia, Gandalf chega com os Rohirrim de Erkenbrand. Vitoria improvavel.',
 false, 6, 80, 'batalha,lideranca,esperanca', 'aragorn,gandalf,theoden'),

-- 8. Requer: helm.venceu
('bb000008-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Aragorn Convoca o Exercito dos Mortos',
 'Pelos Caminhos dos Mortos, Aragorn convoca as almas perjuras que devem obediencia a linhagem de Isildur desde a Terceira Era. O Rei que Retorna finalmente se revela.',
 false, 7, 85, 'destino,lideranca,sobrenatural', 'aragorn'),

-- 9. Requer: exercito.mortos
('bb000009-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Batalha dos Campos de Pelennor',
 'Os mortos-vivos tombam sobre as forcas de Mordor. Eowyn mata o Rei-Bruxo. Aragorn liberta os mortos de seu juramento. Gondor e salva — mas o verdadeiro objetivo e Frodo.',
 false, 8, 95, 'batalha,climax,vitoria', 'aragorn,eowyn,theoden'),

-- 10. Requer: gollum.guia + ABSENT gollum.morto
('bb000010-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Gollum Trai Frodo em Cirith Ungol',
 'Gollum manipula Frodo contra Sam com mentiras calculadas. Frodo manda Sam embora — seu maior erro. Shelob embosca Frodo. Gollum entrega o Portador do Anel a aranha.',
 false, 9, 60, 'traicao,manipulacao,queda', 'gollum,frodo,sam'),

-- 11. Requer: frodo.capturado
('bb000011-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Sam Resgata Frodo na Torre de Cirith Ungol',
 'Sam descobre que Frodo esta vivo e preso pelos Orcs. Invade a torre sozinho. Resgata Frodo e devolve o Anel. A amizade ordinaria de um hobbit simples que salva o mundo.',
 false, 10, 90, 'amizade,coragem,resgate', 'sam,frodo'),

-- 12. Requer: gollum.guia + sam.heroi (climax final)
('bb000012-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'A Crise no Monte da Perdicao',
 'No umbral das Chamas da Perdição, Frodo reivindica o Anel para si. Gollum morde fora o dedo de Frodo, danca com o Anel e cai nas chamas. O Um Anel e destruido. Sauron cai.',
 false, 11, 100, 'climax,destruicao,vitoria', 'frodo,gollum,sam')

ON CONFLICT DO NOTHING;

-- ══ REQUIREMENTS ════════════════════════════════════════════

INSERT INTO scene_requirements (id, scene_id, fact_key, requirement_type, expected_value) VALUES

-- Cena 2: requer missao.iniciada
('cc000001-0000-0000-0000-000000000001', 'bb000002-0000-0000-0000-000000000001', 'missao.iniciada', 'FACT_EXISTS', NULL),

-- Cena 3: requer montanhas.tentadas
('cc000002-0000-0000-0000-000000000001', 'bb000003-0000-0000-0000-000000000001', 'montanhas.tentadas', 'FACT_EXISTS', NULL),

-- Cena 4: requer moria.cruzada E boromir ainda vivo
('cc000003-0000-0000-0000-000000000001', 'bb000004-0000-0000-0000-000000000001', 'moria.cruzada', 'FACT_EXISTS', NULL),
('cc000004-0000-0000-0000-000000000001', 'bb000004-0000-0000-0000-000000000001', 'boromir.morto', 'FACT_ABSENT', NULL),

-- Cena 5: requer frodo.sozinho
('cc000005-0000-0000-0000-000000000001', 'bb000005-0000-0000-0000-000000000001', 'frodo.sozinho', 'FACT_EXISTS', NULL),

-- Cena 6: requer gandalf.morto
('cc000006-0000-0000-0000-000000000001', 'bb000006-0000-0000-0000-000000000001', 'gandalf.morto', 'FACT_EXISTS', NULL),

-- Cena 7: requer boromir.morto (companhia ja dissolvida)
('cc000007-0000-0000-0000-000000000001', 'bb000007-0000-0000-0000-000000000001', 'boromir.morto', 'FACT_EXISTS', NULL),

-- Cena 8: requer helm.venceu
('cc000008-0000-0000-0000-000000000001', 'bb000008-0000-0000-0000-000000000001', 'helm.venceu', 'FACT_EXISTS', NULL),

-- Cena 9: requer exercito.mortos
('cc000009-0000-0000-0000-000000000001', 'bb000009-0000-0000-0000-000000000001', 'exercito.mortos', 'FACT_EXISTS', NULL),

-- Cena 10: requer gollum.guia + gollum ainda vivo
('cc000010-0000-0000-0000-000000000001', 'bb000010-0000-0000-0000-000000000001', 'gollum.guia', 'FACT_EXISTS', NULL),
('cc000011-0000-0000-0000-000000000001', 'bb000010-0000-0000-0000-000000000001', 'gollum.morto', 'FACT_ABSENT', NULL),

-- Cena 11: requer frodo.capturado
('cc000012-0000-0000-0000-000000000001', 'bb000011-0000-0000-0000-000000000001', 'frodo.capturado', 'FACT_EXISTS', NULL),

-- Cena 12: requer gollum.guia + sam.heroi
('cc000013-0000-0000-0000-000000000001', 'bb000012-0000-0000-0000-000000000001', 'gollum.guia', 'FACT_EXISTS', NULL),
('cc000014-0000-0000-0000-000000000001', 'bb000012-0000-0000-0000-000000000001', 'sam.heroi', 'FACT_EXISTS', NULL)

ON CONFLICT DO NOTHING;

-- ══ EFFECTS ═════════════════════════════════════════════════

INSERT INTO scene_effects (id, scene_id, effect_type, fact_key, fact_value) VALUES

-- Cena 1: O Conselho
('dd000001-0000-0000-0000-000000000001', 'bb000001-0000-0000-0000-000000000001', 'SET_FACT', 'missao.iniciada', 'true'),
('dd000002-0000-0000-0000-000000000001', 'bb000001-0000-0000-0000-000000000001', 'SET_FACT', 'sociedade.formada', 'true'),
('dd000003-0000-0000-0000-000000000001', 'bb000001-0000-0000-0000-000000000001', 'SET_FACT', 'gandalf.ativo', 'true'),

-- Cena 2: Montanhas
('dd000004-0000-0000-0000-000000000001', 'bb000002-0000-0000-0000-000000000001', 'SET_FACT', 'montanhas.tentadas', 'true'),

-- Cena 3: Moria / Gandalf cai
('dd000005-0000-0000-0000-000000000001', 'bb000003-0000-0000-0000-000000000001', 'REMOVE_FACT', 'gandalf.ativo', NULL),
('dd000006-0000-0000-0000-000000000001', 'bb000003-0000-0000-0000-000000000001', 'SET_FACT', 'gandalf.morto', 'true'),
('dd000007-0000-0000-0000-000000000001', 'bb000003-0000-0000-0000-000000000001', 'SET_FACT', 'moria.cruzada', 'true'),

-- Cena 4: Boromir corrompido
('dd000008-0000-0000-0000-000000000001', 'bb000004-0000-0000-0000-000000000001', 'SET_FACT', 'boromir.morto', 'true'),
('dd000009-0000-0000-0000-000000000001', 'bb000004-0000-0000-0000-000000000001', 'SET_FACT', 'frodo.sozinho', 'true'),
('dd000010-0000-0000-0000-000000000001', 'bb000004-0000-0000-0000-000000000001', 'REMOVE_FACT', 'sociedade.formada', NULL),

-- Cena 5: Gollum como guia
('dd000011-0000-0000-0000-000000000001', 'bb000005-0000-0000-0000-000000000001', 'SET_FACT', 'gollum.guia', 'true'),
('dd000012-0000-0000-0000-000000000001', 'bb000005-0000-0000-0000-000000000001', 'SET_FACT', 'frodo.mordor', 'true'),

-- Cena 6: Gandalf o Branco
('dd000013-0000-0000-0000-000000000001', 'bb000006-0000-0000-0000-000000000001', 'REMOVE_FACT', 'gandalf.morto', NULL),
('dd000014-0000-0000-0000-000000000001', 'bb000006-0000-0000-0000-000000000001', 'SET_FACT', 'gandalf.ativo', 'true'),
('dd000015-0000-0000-0000-000000000001', 'bb000006-0000-0000-0000-000000000001', 'SET_FACT', 'gandalf.branco', 'true'),

-- Cena 7: Abismo de Helm
('dd000016-0000-0000-0000-000000000001', 'bb000007-0000-0000-0000-000000000001', 'SET_FACT', 'helm.venceu', 'true'),
('dd000017-0000-0000-0000-000000000001', 'bb000007-0000-0000-0000-000000000001', 'SET_FACT', 'aragorn.lidera', 'true'),

-- Cena 8: Exercito dos Mortos
('dd000018-0000-0000-0000-000000000001', 'bb000008-0000-0000-0000-000000000001', 'SET_FACT', 'exercito.mortos', 'true'),
('dd000019-0000-0000-0000-000000000001', 'bb000008-0000-0000-0000-000000000001', 'SET_FACT', 'aragorn.rei', 'true'),

-- Cena 9: Pelennor
('dd000020-0000-0000-0000-000000000001', 'bb000009-0000-0000-0000-000000000001', 'SET_FACT', 'gondor.salva', 'true'),
('dd000021-0000-0000-0000-000000000001', 'bb000009-0000-0000-0000-000000000001', 'SET_FACT', 'rei.bruxo.morto', 'true'),
('dd000022-0000-0000-0000-000000000001', 'bb000009-0000-0000-0000-000000000001', 'REMOVE_FACT', 'exercito.mortos', NULL),

-- Cena 10: Gollum trai
('dd000023-0000-0000-0000-000000000001', 'bb000010-0000-0000-0000-000000000001', 'SET_FACT', 'frodo.capturado', 'true'),
('dd000024-0000-0000-0000-000000000001', 'bb000010-0000-0000-0000-000000000001', 'SET_FACT', 'gollum.traiu', 'true'),

-- Cena 11: Sam resgata
('dd000025-0000-0000-0000-000000000001', 'bb000011-0000-0000-0000-000000000001', 'REMOVE_FACT', 'frodo.capturado', NULL),
('dd000026-0000-0000-0000-000000000001', 'bb000011-0000-0000-0000-000000000001', 'SET_FACT', 'sam.heroi', 'true'),

-- Cena 12: Monte da Perdicao
('dd000027-0000-0000-0000-000000000001', 'bb000012-0000-0000-0000-000000000001', 'SET_FACT', 'anel.destruido', 'true'),
('dd000028-0000-0000-0000-000000000001', 'bb000012-0000-0000-0000-000000000001', 'SET_FACT', 'gollum.morto', 'true'),
('dd000029-0000-0000-0000-000000000001', 'bb000012-0000-0000-0000-000000000001', 'SET_FACT', 'sauron.derrotado', 'true'),
('dd000030-0000-0000-0000-000000000001', 'bb000012-0000-0000-0000-000000000001', 'SET_FACT', 'missao.completa', 'true')

ON CONFLICT DO NOTHING;

-- ══ SESSION: "Caminho Canônico" (6 cenas aplicadas) ═════════

INSERT INTO story_sessions (id, story_id, name, created_at, updated_at) VALUES
('ee000001-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Caminho Canonico — Em Andamento', NOW() - INTERVAL '2 hours', NOW())
ON CONFLICT DO NOTHING;

-- World state atual da sessao (cenas 1-5 aplicadas)
INSERT INTO world_state_facts (id, session_id, fact_key, fact_value) VALUES
('ff000001-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'missao.iniciada', 'true'),
('ff000002-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'montanhas.tentadas', 'true'),
('ff000003-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'gandalf.morto', 'true'),
('ff000004-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'moria.cruzada', 'true'),
('ff000005-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'boromir.morto', 'true'),
('ff000006-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'frodo.sozinho', 'true'),
('ff000007-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'gollum.guia', 'true'),
('ff000008-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'frodo.mordor', 'true')
ON CONFLICT DO NOTHING;

-- Cenas aplicadas na sessao (5 cenas no historico)
INSERT INTO session_applied_scenes (id, session_id, scene_id, applied_at) VALUES
('a1000001-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'bb000001-0000-0000-0000-000000000001', NOW() - INTERVAL '110 minutes'),
('a1000002-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'bb000002-0000-0000-0000-000000000001', NOW() - INTERVAL '90 minutes'),
('a1000003-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'bb000003-0000-0000-0000-000000000001', NOW() - INTERVAL '70 minutes'),
('a1000004-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'bb000004-0000-0000-0000-000000000001', NOW() - INTERVAL '50 minutes'),
('a1000005-0000-0000-0000-000000000001', 'ee000001-0000-0000-0000-000000000001', 'bb000005-0000-0000-0000-000000000001', NOW() - INTERVAL '30 minutes')
ON CONFLICT DO NOTHING;

-- ══ SESSION 2: "Rota Alternativa" (comecou diferente) ══════

INSERT INTO story_sessions (id, story_id, name, created_at, updated_at) VALUES
('ee000002-0000-0000-0000-000000000001', 'aa000000-0000-0000-0000-000000000001',
 'Rota Alternativa — Aragorn em Foco', NOW() - INTERVAL '1 hour', NOW())
ON CONFLICT DO NOTHING;

INSERT INTO world_state_facts (id, session_id, fact_key, fact_value) VALUES
('ff000010-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'missao.iniciada', 'true'),
('ff000011-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'montanhas.tentadas', 'true'),
('ff000012-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'gandalf.morto', 'true'),
('ff000013-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'moria.cruzada', 'true'),
('ff000014-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'boromir.morto', 'true'),
('ff000015-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'frodo.sozinho', 'true'),
('ff000016-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'gollum.guia', 'true'),
('ff000017-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'frodo.mordor', 'true'),
('ff000018-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'gandalf.ativo', 'true'),
('ff000019-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'gandalf.branco', 'true'),
('ff000020-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'helm.venceu', 'true'),
('ff000021-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'aragorn.lidera', 'true'),
('ff000022-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'exercito.mortos', 'true'),
('ff000023-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'aragorn.rei', 'true')
ON CONFLICT DO NOTHING;

INSERT INTO session_applied_scenes (id, session_id, scene_id, applied_at) VALUES
('a2000010-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'bb000001-0000-0000-0000-000000000001', NOW() - INTERVAL '60 minutes'),
('a2000011-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'bb000002-0000-0000-0000-000000000001', NOW() - INTERVAL '55 minutes'),
('a2000012-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'bb000003-0000-0000-0000-000000000001', NOW() - INTERVAL '50 minutes'),
('a2000013-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'bb000004-0000-0000-0000-000000000001', NOW() - INTERVAL '45 minutes'),
('a2000014-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'bb000005-0000-0000-0000-000000000001', NOW() - INTERVAL '40 minutes'),
('a2000015-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'bb000006-0000-0000-0000-000000000001', NOW() - INTERVAL '35 minutes'),
('a2000016-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'bb000007-0000-0000-0000-000000000001', NOW() - INTERVAL '30 minutes'),
('a2000017-0000-0000-0000-000000000001', 'ee000002-0000-0000-0000-000000000001', 'bb000008-0000-0000-0000-000000000001', NOW() - INTERVAL '20 minutes')
ON CONFLICT DO NOTHING;
