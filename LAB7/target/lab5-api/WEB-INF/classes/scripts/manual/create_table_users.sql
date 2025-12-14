CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'Таблица для хранения зарегистрированных пользователей';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор пользователя (первичный ключ)';
COMMENT ON COLUMN users.username IS 'Уникальное имя пользователя';
COMMENT ON COLUMN users.password_hash IS 'Хэш пароля пользователя';
COMMENT ON COLUMN users.created_at IS 'Дата и время создания записи пользователя';