package concurrent;

import functions.TabulatedFunction;
import org.Logger;
import org.LoggerFactory;

public class MultiplyingTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MultiplyingTask.class);
    private final TabulatedFunction function;

    public MultiplyingTask(TabulatedFunction function) {
        if (function == null) {
            logger.error("Attempt to create MultiplyingTask with null function");
            throw new IllegalArgumentException("Function cannot be null");
        }
        this.function = function;
    }

    @Override
    public void run() {
        try {
            logger.info("Starting multiplication task for {} points", function.getCount());

            for (int i = 0; i < function.getCount(); i++) {
                synchronized (function) {
                    double currentY = function.getY(i);
                    function.setY(i, currentY * 2);
                }
            }

            logger.info("Multiplication task completed successfully");
        } catch (Exception e) {
            logger.error("Error during multiplication task execution", e);
            throw e;
        }
    }
}