package com.example.LAB5.concurrent;

import com.example.LAB5.functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ReadTask.class);
    private final TabulatedFunction function;

    public ReadTask(TabulatedFunction function) {
        logger.debug("Создание ReadTask для функции: {}", function);
        this.function = function;
        logger.info("ReadTask инициализирован для функции с {} точками",
                function != null ? function.getCount() : "null");
    }

    @Override
    public void run() {
        logger.info("Поток {} начал выполнение ReadTask", Thread.currentThread().getName());

        if (function == null) {
            logger.error("Функция не инициализирована (null) в потоке {}", Thread.currentThread().getName());
            return;
        }

        int pointCount = function.getCount();
        logger.debug("Чтение функции с {} точками в потоке {}", pointCount, Thread.currentThread().getName());

        if (pointCount == 0) {
            logger.warn("Функция не содержит точек для чтения в потоке {}", Thread.currentThread().getName());
            return;
        }

        try {
            for (int i = 0; i < pointCount; i++) {
                synchronized (function) {
                    logger.trace("Поток {} заблокировал функцию для чтения точки {}",
                            Thread.currentThread().getName(), i);

                    double x = function.getX(i);
                    double y = function.getY(i);

                    logger.debug("Точка {}: x = {}, y = {}", i, x, y);

                    // Сохраняем оригинальный вывод для совместимости
                    System.out.printf("After read: i = %d, x = %f, y = %f%n", i, x, y);

                    logger.trace("Поток {} разблокировал функцию после чтения точки {}",
                            Thread.currentThread().getName(), i);
                }

                // Небольшая пауза для демонстрации многопоточности (только в trace режиме)
                if (logger.isTraceEnabled()) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        logger.warn("Поток {} был прерван во время чтения",
                                Thread.currentThread().getName());
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            logger.info("Поток {} успешно прочитал все {} точек функции",
                    Thread.currentThread().getName(), pointCount);

        } catch (IndexOutOfBoundsException e) {
            logger.error("Ошибка индексации при чтении функции в потоке {}: индекс вне границ",
                    Thread.currentThread().getName(), e);
        } catch (Exception e) {
            logger.error("Неожиданная ошибка в потоке {} при чтении функции: {}",
                    Thread.currentThread().getName(), e.getMessage(), e);
        } finally {
            logger.info("Поток {} завершил выполнение ReadTask", Thread.currentThread().getName());
        }
    }
}