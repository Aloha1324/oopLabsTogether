package functions;

/**
 * Абстрактный класс для табулированных функций, реализующий общий функционал
 * для различных способов хранения данных (массивы, связные списки)
 */
public abstract class AbstractTabulatedFunction implements TabulatedFunction {

    /**
     * Метод поиска индекса максимального значения x, которое меньше заданного x
     * Для набора значений x [-3., 4., 6.] метод, применённый к 4.5, должен вернуть 1
     * Если все x больше заданного, метод должен вернуть 0
     * Если все x меньше заданного, метод должен вернуть count
     */
    protected abstract int floorIndexOfX(double x);

    /**
     * Метод экстраполяции для значений слева от минимального x
     */
    protected abstract double extrapolateLeft(double x);

    /**
     * Метод экстраполяции для значений справа от максимального x
     */
    protected abstract double extrapolateRight(double x);

    /**
     * Метод интерполяции с указанием индекса интервала
     */
    protected abstract double interpolate(double x, int floorIndex);

    /**
     * Защищенный метод с реализацией линейной интерполяции
     */
    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        // Проверка на совпадение граничных точек
        if (leftX == rightX) {
            return (leftY + rightY) / 2.0;
        }
        // Формула линейной интерполяции
        return leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX);
    }

    /**
     * Реализация метода apply для вычисления значения функции в любой точке x.
     * Использует интерполяцию и экстраполяцию на основе табличных значений
     */
    @Override
    public double apply(double x) {
        // Проверка на пустую таблицу
        if (getCount() == 0) {
            throw new IllegalStateException("Tabulated function is empty");
        }

        // Определение положения x относительно границ функции
        if (x < leftBound()) {
            return extrapolateLeft(x);
        } else if (x > rightBound()) {
            return extrapolateRight(x);
        } else {
            int exactIndex = indexOfX(x);
            if (exactIndex != -1) {
                return getY(exactIndex);
            } else {
                int floorIndex = floorIndexOfX(x);
                // Дополнительная проверка корректности индекса
                if (floorIndex < 0 || floorIndex >= getCount()) {
                    throw new IllegalStateException("Invalid floor index calculated: " + floorIndex);
                }
                return interpolate(x, floorIndex);
            }
        }
    }
}