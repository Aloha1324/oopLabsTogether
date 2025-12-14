CREATE TABLE points (
    id SERIAL PRIMARY KEY,
    function_id INTEGER NOT NULL,
    x_value DOUBLE PRECISION NOT NULL,
    y_value DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Внешний ключ на таблицу functions
    CONSTRAINT fk_points_function
        FOREIGN KEY (function_id)
        REFERENCES functions(id)
        ON DELETE CASCADE
);

-- Создание индекса для ускорения поиска точек по функции
CREATE INDEX idx_points_function_id ON points(function_id);

-- Составной индекс для поиска точек по координатам
CREATE INDEX idx_points_coordinates ON points(x_value, y_value);

COMMENT ON TABLE points IS 'Таблица для хранения точек графиков математических функций';
COMMENT ON COLUMN points.id IS 'Уникальный идентификатор точки (первичный ключ)';
COMMENT ON COLUMN points.function_id IS 'Идентификатор функции, к которой относится точка (внешний ключ)';
COMMENT ON COLUMN points.x_value IS 'Значение координаты X точки';
COMMENT ON COLUMN points.y_value IS 'Значение координаты Y точки (результат вычисления функции)';
COMMENT ON COLUMN points.created_at IS 'Дата и время вычисления точки';