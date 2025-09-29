package functions;

//Класс для создания сложных математических функций вида h(x) = g(f(x))
//Применяет сначала первую функцию, затем вторую к результату

public class CompositeFunction implements MathFunction {

    private final MathFunction firstFunction;
    private final MathFunction secondFunction;

    /**
     * Конструктор сложной функции
     * @param firstFunction первая функция (применяется первой)
     * @param secondFunction вторая функция (применяется к результату первой)
     */
    public CompositeFunction(MathFunction firstFunction, MathFunction secondFunction) {
        if (firstFunction == null || secondFunction == null) {
            throw new IllegalArgumentException("Функции не могут быть null");
        }
        this.firstFunction = firstFunction;
        this.secondFunction = secondFunction;
    }

    //Применяет сложную функцию к аргументу
    //x аргумент функции

    @Override
    public double apply(double x) {
        // Сначала применяем первую функцию
        double intermediateResult = firstFunction.apply(x);
        // Затем применяем вторую функцию к результату
        return secondFunction.apply(intermediateResult);
    }

    /**
     * @return первая функция
     */
    public MathFunction getFirstFunction() {
        return firstFunction;
    }

    /**
     * @return вторая функция
     */
    public MathFunction getSecondFunction() {
        return secondFunction;
    }
}