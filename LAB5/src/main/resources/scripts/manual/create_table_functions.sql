CREATE TABLE functions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id INTEGER NOT NULL,
    expression TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Внешний ключ на таблицу users
    CONSTRAINT fk_functions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Создание индекса для ускорения поиска функций по пользователю
CREATE INDEX idx_functions_user_id ON functions(user_id);

COMMENT ON TABLE functions IS 'Таблица для хранения математических функций пользователей';
COMMENT ON COLUMN functions.id IS 'Уникальный идентификатор функции (первичный ключ)';
COMMENT ON COLUMN functions.name IS 'Название функции';
COMMENT ON COLUMN functions.user_id IS 'Идентификатор пользователя, создавшего функцию (внешний ключ)';
COMMENT ON COLUMN functions.expression IS 'Математическое выражение функции';
COMMENT ON COLUMN functions.created_at IS 'Дата и время создания функции';