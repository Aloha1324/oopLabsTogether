package concurrent;



import functions.TabulatedFunction;

public class ReadTask implements Runnable
{
    private TabulatedFunction function;
    private final Object lock;

    public ReadTask(TabulatedFunction function, Object lock) {
        this.function = function;
        this.lock = lock;
    }

    @Override
    public void run() {
        for (int i = 0; i < function.getCount(); i++) {
            synchronized (lock) {
                double x = function.getX(i);
                double y = function.getY(i);
                System.out.printf("After read: i = %d, x = %f, y = %f%n", i, x, y);
            }
        }
    }
}