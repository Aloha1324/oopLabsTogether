package com.example.LAB5.concurrent;

import com.example.LAB5.functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WriteTask.class);
    private final TabulatedFunction function;
    private final double value;

    public WriteTask(TabulatedFunction function, double value) {
        this.function = function;
        this.value = value;
        logger.info("WriteTask создан для установки значения {} на все точки функции", value);
    }

    @Override
    public void run() {
        logger.info("Поток {} начал запись значения {} в функцию",
                Thread.currentThread().getName(), value);

        if (function == null) {
            logger.error("Функция не инициализирована (null) в потоке {}",
                    Thread.currentThread().getName());
            return;
        }

        int pointCount = function.getCount();
        logger.debug("Запись в функцию с {} точками", pointCount);

        try {
            for (int i = 0; i < pointCount; i++) {
                synchronized (function) {
                    double oldValue = function.getY(i);
                    function.setY(i, value);
                    logger.debug("Точка {}: Y изменен с {} на {}", i, oldValue, value);

                    // Сохраняем оригинальный вывод
                    System.out.printf("Writing for index %d complete%n", i);
                }
            }

            logger.info("Поток {} завершил запись значения {} во все {} точек",
                    Thread.currentThread().getName(), value, pointCount);

        } catch (Exception e) {
            logger.error("Ошибка в потоке {} при записи значения {}",
                    Thread.currentThread().getName(), value, e);
        }
    }
}