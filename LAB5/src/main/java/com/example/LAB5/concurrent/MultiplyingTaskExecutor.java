package com.example.LAB5.concurrent;

import com.example.LAB5.functions.LinkedListTabulatedFunction;
import com.example.LAB5.functions.UnitFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class MultiplyingTaskExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MultiplyingTaskExecutor.class);

    public static void main(String[] args) {
        logger.info("Запуск MultiplyingTaskExecutor");

        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(new UnitFunction(), 1, 1000, 1000);
        logger.info("Создана функция: {} точек на интервале [1, 1000]", function.getCount());

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            MultiplyingTask task = new MultiplyingTask(function);
            Thread thread = new Thread(task);
            threads.add(thread);
            logger.debug("Создан поток {} для MultiplyingTask", i + 1);
        }
        logger.info("Создано {} потоков для умножения функции", threads.size());

        for (Thread thread : threads) {
            thread.start();
        }
        logger.info("Все потоки запущены");

        try {
            logger.debug("Ожидание выполнения потоков (2 секунды)...");
            Thread.sleep(2000);
            logger.debug("Ожидание завершено");
        } catch (InterruptedException e) {
            logger.error("Главный поток был прерван во время ожидания", e);
            Thread.currentThread().interrupt();
        }

        System.out.println("Табулированная функция после выполнения потоков:");
        logger.info("Вывод первых 10 точек функции после обработки");

        for (int i = 0; i < Math.min(10, function.getCount()); i++) {
            System.out.printf("x = %.1f, y = %.1f%n", function.getX(i), function.getY(i));
        }

        logger.info("MultiplyingTaskExecutor завершил работу");
    }
}