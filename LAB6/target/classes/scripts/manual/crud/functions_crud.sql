INSERT INTO functions (name, user_id, expression) VALUES ('quadratic', 1, 'x^2 + 2*x + 1');
INSERT INTO functions (name, user_id, expression) VALUES ('linear', 1, '2*x + 3');
INSERT INTO functions (name, user_id, expression) VALUES
('sine wave', 1, 'sin(x)'),
('cosine', 1, 'cos(x)'),
('exponential', 1, 'e^x');


SELECT * FROM functions;
SELECT * FROM functions WHERE id = 1;
SELECT * FROM functions WHERE user_id = 1;
SELECT * FROM functions WHERE name LIKE '%quad%';
SELECT * FROM functions WHERE expression LIKE '%x^2%';
SELECT f.*, u.username FROM functions f JOIN users u ON f.user_id = u.id;
SELECT user_id, COUNT(*) as function_count FROM functions GROUP BY user_id;
SELECT * FROM functions ORDER BY created_at DESC LIMIT 5;


UPDATE functions SET expression = 'x^2 + 3*x + 2' WHERE id = 1;
UPDATE functions SET name = 'updated_quadratic' WHERE id = 1;
UPDATE functions SET name = 'linear_function', expression = '3*x + 1' WHERE id = 2;


DELETE FROM functions WHERE id = 5;
DELETE FROM functions WHERE user_id = 1 AND name = 'exponential';
DELETE FROM functions WHERE created_at < '2024-01-01';