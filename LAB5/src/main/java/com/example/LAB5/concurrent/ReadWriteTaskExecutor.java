package com.example.LAB5.concurrent;

import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.functions.LinkedListTabulatedFunction;
import com.example.LAB5.functions.ConstantFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadWriteTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ReadWriteTaskExecutor.class);

    public static void main(String[] args) {
        logger.info("Запуск ReadWriteTaskExecutor");

        // константная функцию с значением -1
        ConstantFunction constantFunction = new ConstantFunction(-1);
        logger.debug("Создана ConstantFunction со значением: -1");

        TabulatedFunction function = new LinkedListTabulatedFunction(
                constantFunction, 1, 1000, 1000
        );
        logger.info("Создана TabulatedFunction: {} точек на интервале [1, 1000]", function.getCount());

        ReadTask readTask = new ReadTask(function);
        WriteTask writeTask = new WriteTask(function, 0.5);
        logger.debug("Задачи ReadTask и WriteTask созданы");

        Thread readThread = new Thread(readTask);
        Thread writeThread = new Thread(writeTask);
        logger.info("Потоки инициализированы: читающий и пишущий");

        readThread.start();
        writeThread.start();
        logger.info("Потоки запущены");

        try {
            logger.debug("Ожидание завершения потоков...");
            readThread.join();
            writeThread.join();
            logger.info("Оба потока успешно завершились");
        } catch (InterruptedException e) {
            logger.error("Главный поток был прерван во время ожидания", e);
            Thread.currentThread().interrupt();
        }

        System.out.println("\nВсе потоки завершили выполнение.");
        logger.info("ReadWriteTaskExecutor завершил работу");
    }
}