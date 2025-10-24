package concurrent;

import functions.LinkedListTabulatedFunction;
import functions.UnitFunction;
import java.util.ArrayList;
import java.util.List;

public class MultiplyingTaskExecutor {
    public static void main(String[] args) {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(new UnitFunction(), 1, 1000, 1000);

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            MultiplyingTask task = new MultiplyingTask(function);
            Thread thread = new Thread(task);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Табулированная функция после выполнения потоков:");
        for (int i = 0; i < Math.min(10, function.getCount()); i++) {
            System.out.printf("x = %.1f, y = %.1f%n", function.getX(i), function.getY(i));
        }
    }
}