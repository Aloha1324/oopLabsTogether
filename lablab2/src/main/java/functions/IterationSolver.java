package functions;

public class IterationSolver {

    public static double solve(MathFunction phi, double start, double eps, int maxSteps) {
        double x = start;

        for (int i = 0; i < maxSteps; i++) {
            double next = phi.apply(x);
            if (Math.abs(next - x) < eps) {
                return next;
            }
            x = next;
        }

        throw new RuntimeException("Не сошлось за " + maxSteps + " шагов");
    }

    public static double solve(MathFunction phi, double start) {
        return solve(phi, start, 1e-10, 1000);
    }
}


