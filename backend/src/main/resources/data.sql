-- 初期データ（ボードが存在しない場合のみ挿入）
INSERT INTO boards (id, title)
SELECT 1, 'タスク管理ボード'
WHERE NOT EXISTS (SELECT 1 FROM boards WHERE id = 1);

-- シーケンスをリセット（id=1 を手動挿入したため）
SELECT setval('boards_id_seq', (SELECT MAX(id) FROM boards));

INSERT INTO lists (id, board_id, title, position)
SELECT 1, 1, 'やること', 0
WHERE NOT EXISTS (SELECT 1 FROM lists WHERE id = 1);

INSERT INTO lists (id, board_id, title, position)
SELECT 2, 1, '進行中', 1
WHERE NOT EXISTS (SELECT 1 FROM lists WHERE id = 2);

INSERT INTO lists (id, board_id, title, position)
SELECT 3, 1, '完了', 2
WHERE NOT EXISTS (SELECT 1 FROM lists WHERE id = 3);

SELECT setval('lists_id_seq', (SELECT MAX(id) FROM lists));

INSERT INTO cards (list_id, title, description, due_date, priority, position)
SELECT 1, '読書感想文を書く', '夏休みの課題、800字以上', CURRENT_DATE + INTERVAL '5 days', 'high', 0
WHERE NOT EXISTS (SELECT 1 FROM cards WHERE list_id = 1 AND title = '読書感想文を書く');

INSERT INTO cards (list_id, title, description, due_date, priority, position)
SELECT 1, '買い物リストを作る', '', CURRENT_DATE + INTERVAL '2 days', 'medium', 1
WHERE NOT EXISTS (SELECT 1 FROM cards WHERE list_id = 1 AND title = '買い物リストを作る');

INSERT INTO cards (list_id, title, description, due_date, priority, position)
SELECT 2, '自由研究まとめ', '理科の実験結果をまとめる', CURRENT_DATE - INTERVAL '1 day', 'high', 0
WHERE NOT EXISTS (SELECT 1 FROM cards WHERE list_id = 2 AND title = '自由研究まとめ');

INSERT INTO cards (list_id, title, description, due_date, priority, position)
SELECT 3, '日記を書く', '', NULL, 'low', 0
WHERE NOT EXISTS (SELECT 1 FROM cards WHERE list_id = 3 AND title = '日記を書く');
