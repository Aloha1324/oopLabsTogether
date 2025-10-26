package functions;

//Класс для создания сложных математических функций вида h(x) = g(f(x))
//Применяет сначала первую функцию, затем вторую к результату

public class CompositeFunction implements MathFunction {

    private final MathFunction firstFunction;
    private final MathFunction secondFunction;

    //Конструктор сложной функции
     //firstFunction первая функция (применяется первой)
     //secondFunction вторая функция (применяется к результату первой)

    public CompositeFunction(MathFunction firstFunction, MathFunction secondFunction) {
        if (firstFunction == null || secondFunction == null) {
            throw new IllegalArgumentException("Функции не могут быть null");
        }
        this.firstFunction = firstFunction;
        this.secondFunction = secondFunction;
    }

    //Применяет сложную функцию к аргументу
    //x аргумент функции

    public double apply(double x) {
        // Сначала применяем первую функцию
        double intermediateResult = firstFunction.apply(x);
        // Затем применяем вторую функцию к результату
        return secondFunction.apply(intermediateResult);
    }

    public MathFunction getFirstFunction() {
        return firstFunction;
    }


    public MathFunction getSecondFunction() {
        return secondFunction;
    }
}