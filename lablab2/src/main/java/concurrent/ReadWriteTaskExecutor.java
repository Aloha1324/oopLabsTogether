package concurrent;

import functions.ConstantFunction;
import functions.TabulatedFunction;
import functions.LinkedListTabulatedFunction;

public class ReadWriteTaskExecutor {
    public static void main(String[] args) {
        // Создаем константную функцию с значением -1
        ConstantFunction constantFunction = new ConstantFunction(-1);

        // Создаем табулированную функцию на основе константной функции
        // в интервале от 1 до 1000 с большим количеством точек
        TabulatedFunction tabulatedFunction = new LinkedListTabulatedFunction(
                constantFunction, 1, 1000, 1000
        );

        // Создаем задачи для чтения и записи
        ReadTask readTask = new ReadTask(tabulatedFunction);
        WriteTask writeTask = new WriteTask(tabulatedFunction, 0.5);

        // Создаем потоки исполнения
        Thread readThread = new Thread(readTask);
        Thread writeThread = new Thread(writeTask);

        // Стартуем потоки
        readThread.start();
        writeThread.start();

        // Ожидаем завершения потоков (опционально)
        try {
            readThread.join();
            writeThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Потоки были прерваны: " + e.getMessage());
        }
    }
}