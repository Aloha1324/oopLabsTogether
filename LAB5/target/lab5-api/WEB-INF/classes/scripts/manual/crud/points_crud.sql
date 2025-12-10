INSERT INTO points (function_id, x_value, y_value) VALUES (1, -2.0, 1.0);
INSERT INTO points (function_id, x_value, y_value) VALUES (1, 0.0, 1.0);
INSERT INTO points (function_id, x_value, y_value) VALUES
(1, -2.0, 1.0), (1, -1.0, 0.0), (1, 0.0, 1.0), (1, 1.0, 4.0), (1, 2.0, 9.0),
(2, 0.0, 3.0), (2, 1.0, 5.0), (2, 2.0, 7.0);


SELECT * FROM points;
SELECT * FROM points WHERE id = 1;
SELECT * FROM points WHERE function_id = 1;
SELECT * FROM points WHERE function_id = 1 AND x_value BETWEEN -1 AND 1;
SELECT * FROM points WHERE y_value > 5;
SELECT * FROM points WHERE x_value = 0.0;
SELECT p.*, f.name as function_name FROM points p JOIN functions f ON p.function_id = f.id;
SELECT function_id, COUNT(*) as point_count FROM points GROUP BY function_id;
SELECT * FROM points WHERE function_id = 1 ORDER BY x_value ASC;
SELECT MIN(x_value) as min_x, MAX(x_value) as max_x, AVG(y_value) as avg_y FROM points WHERE function_id = 1;


UPDATE points SET y_value = 25.0 WHERE id = 1;
UPDATE points SET x_value = 3.0, y_value = 12.0 WHERE id = 2;
UPDATE points SET y_value = y_value * 2 WHERE function_id = 1;


DELETE FROM points WHERE id = 8;
DELETE FROM points WHERE function_id = 2;
DELETE FROM points WHERE function_id = 1 AND x_value < 0;
DELETE FROM points WHERE y_value IS NULL;