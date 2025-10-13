package functions;

//Абстрактный класс для табулированных функций
public abstract class AbstractTabulatedFunctionX implements TabulatedFunction {

    protected int count; // Количество узлов в таблице

    //Находит индекс максимального x, который меньше заданного
    //x значение аргумента
    //индекс, где xValues[index] ≤ x < xValues[index+1]
    protected abstract int floorIndexOfX(double x);

    //Экстраполяция слева (для x < leftBound())
    // x значение аргумента
    //экстраполированное значение функции
    protected abstract double extrapolateLeft(double x);

    //Экстраполяция справа (для x > rightBound())
    //x значение аргумента
    //экстраполированное значение функции
    protected abstract double extrapolateRight(double x);

    //Интерполяция внутри интервала
    //x значение аргумента
    //floorIndex индекс левой границы интервала
    //интерполированное значение функции
    protected abstract double interpolate(double x, int floorIndex);

    //Вспомогательный метод для линейной интерполяции
    //x значение аргумента для интерполяции
    //leftX x левой границы интервала
    //rightX x правой границы интервала
    //leftY y левой границы интервала
    //rightY y правой границы интервала
    // интерполированное значение
    protected double interpolate(double x, double leftX, double rightX,
                                 double leftY, double rightY) {
        // Формула линейной интерполяции: y = leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX)
        return leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX);
    }

    //Реализация метода apply из MathFunction
    //Использует интерполяцию/экстраполяцию для вычисления значения в любой точке
    @Override
    public double apply(double x) {
        // Если x меньше левой границы - экстраполяция слева
        if (x < leftBound()) {
            return extrapolateLeft(x);
        }

        // Если x больше правой границы - экстраполяция справа
        if (x > rightBound()) {
            return extrapolateRight(x);
        }

        // Если x точно совпадает с одним из значений в таблице
        int exactIndex = indexOfX(x);
        if (exactIndex != -1) {
            return getY(exactIndex);
        }

        // Иначе - интерполяция внутри интервала
        int floorIndex = floorIndexOfX(x);
        return interpolate(x, floorIndex);
    }

    // количество точек в таблице
    @Override
    public int getCount() {
        return count;
    }
}