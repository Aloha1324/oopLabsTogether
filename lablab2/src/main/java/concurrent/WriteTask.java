package concurrent;
import functions.TabulatedFunction;

public class WriteTask implements Runnable
{
    private TabulatedFunction function;
    private double value;
    private final Object lock;

    public WriteTask(TabulatedFunction function, double value, Object lock) {
        this.function = function;
        this.value = value;
        this.lock = lock;
    }

    @Override
    public void run() {
        for (int i = 0; i < function.getCount(); i++) {
            synchronized (lock) {
                function.setY(i, value);
                System.out.printf("Writing for index %d complete%n", i);
            }
        }
    }
}