package functions;

public interface MathFunction {

    double apply(double x);
    default MathFunction andThen(MathFunction after) {
        return x -> after.apply(this.apply(x));
    }
}