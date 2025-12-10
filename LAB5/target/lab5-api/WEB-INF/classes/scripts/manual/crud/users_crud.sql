INSERT INTO users (username, password_hash) VALUES ('seryozha', 'hashed_password_123');
INSERT INTO users (username, password_hash) VALUES ('artem', 'hash_artem_789'), ('abob', 'hash_abob_000');


SELECT * FROM users;
SELECT * FROM users WHERE id = 1;
SELECT * FROM users WHERE username = 'seryozha';
SELECT * FROM users WHERE username LIKE '%seryozha%';
SELECT COUNT(*) as total_users FROM users;
SELECT * FROM users ORDER BY created_at DESC;
SELECT * FROM users ORDER BY id LIMIT 10 OFFSET 0;
SELECT id, username FROM users WHERE created_at > '2024-01-01';


UPDATE users SET password_hash = 'new_updated_hash' WHERE id = 1;
UPDATE users SET username = 'seryozha_updated' WHERE id = 1;
UPDATE users SET username = 'artem_updated', password_hash = 'new_artem_hash' WHERE id = 2;

-- DELETE OPERATIONS
DELETE FROM users WHERE id = 3;
DELETE FROM users WHERE username = 'abob';
DELETE FROM users WHERE id IN (SELECT id FROM users ORDER BY id DESC LIMIT 1);