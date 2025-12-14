package com.example.LAB5.concurrent;

import com.example.LAB5.functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplyingTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MultiplyingTask.class);
    private final TabulatedFunction function;

    public MultiplyingTask(TabulatedFunction function) {
        logger.debug("Создание MultiplyingTask для функции: {}", function);
        this.function = function;
        logger.info("MultiplyingTask инициализирован для функции с {} точками",
                function != null ? function.getCount() : "null");
    }

    @Override
    public void run() {
        logger.info("Поток {} начал выполнение MultiplyingTask", Thread.currentThread().getName());

        if (function == null) {
            logger.error("Функция не инициализирована (null) в потоке {}", Thread.currentThread().getName());
            return;
        }

        int pointCount = function.getCount();
        logger.debug("Обработка функции с {} точками в потоке {}", pointCount, Thread.currentThread().getName());

        if (pointCount == 0) {
            logger.warn("Функция не содержит точек для обработки в потоке {}", Thread.currentThread().getName());
            return;
        }

        try {
            for (int i = 0; i < pointCount; i++) {
                synchronized (function) {
                    logger.trace("Поток {} заблокировал функцию для точки {}",
                            Thread.currentThread().getName(), i);

                    double currentY = function.getY(i);
                    logger.debug("Точка {}: текущее Y = {}", i, currentY);

                    double newY = currentY * 2;
                    function.setY(i, newY);

                    logger.debug("Точка {}: Y изменено с {} на {}", i, currentY, newY);
                    logger.trace("Поток {} разблокировал функцию после точки {}",
                            Thread.currentThread().getName(), i);
                }

                // Небольшая пауза для демонстрации многопоточности
                if (logger.isTraceEnabled()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        logger.warn("Поток {} был прерван во время обработки",
                                Thread.currentThread().getName());
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            logger.info("Поток {} успешно обработал все {} точек функции",
                    Thread.currentThread().getName(), pointCount);

        } catch (Exception e) {
            logger.error("Критическая ошибка в потоке {} при обработке функции: {}",
                    Thread.currentThread().getName(), e.getMessage(), e);
        } finally {
            logger.info("Поток {} завершил выполнение MultiplyingTask", Thread.currentThread().getName());
        }
    }
}