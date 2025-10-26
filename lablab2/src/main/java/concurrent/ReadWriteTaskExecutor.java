package concurrent;


import functions.TabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.ConstantFunction;

public class ReadWriteTaskExecutor {

    public static void main(String[] args) {

        // константная функцию с значением -1
        ConstantFunction constantFunction = new ConstantFunction(-1);

        TabulatedFunction function = new LinkedListTabulatedFunction(
                 constantFunction, 1, 1000, 1000
        );

        // общий объект для синхронизации
        Object lock = new Object();

        ReadTask readTask = new ReadTask(function, lock);
        WriteTask writeTask = new WriteTask(function, 0.5, lock);

        Thread readThread = new Thread(readTask);
        Thread writeThread = new Thread(writeTask);

        readThread.start();
        writeThread.start();


        try {
            readThread.join();
            writeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nВсе потоки завершили выполнение.");


    }
}
