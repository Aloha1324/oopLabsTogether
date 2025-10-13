package operations;

import functions.*;
import functions.factory.TabulatedFunctionFactory;
import functions.factory.ArrayTabulatedFunctionFactory;

/**
 * Дифференциальный оператор для табулированных функций
 * Реализует численное дифференцирование для дискретных функций
 */
public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {

    private TabulatedFunctionFactory factory;

    /**
     * Конструктор с фабрикой по умолчанию
     */
    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    /**
     * Конструктор с пользовательской фабрикой
     *
     * @param factory фабрика для создания табулированных функций
     */
    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    /**
     * Возвращает текущую фабрику
     *
     * @return текущая фабрика табулированных функций
     */
    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    /**
     * Устанавливает новую фабрику
     *
     * @param factory новая фабрика табулированных функций
     */
    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    /**
     * Вычисляет производную табулированной функции
     * Использует численное дифференцирование:
     * - Для первых n-1 точек: правая разностная производная
     * - Для последней точки: левая разностная производная
     *
     * @param function исходная табулированная функция
     * @return производная табулированная функция
     */
    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        int pointCount = function.getCount();

        // Создаем массивы для новой функции
        double[] xValues = new double[pointCount];
        double[] yValues = new double[pointCount];

        // Копируем x-значения (они остаются теми же)
        for (int i = 0; i < pointCount; i++) {
            xValues[i] = function.getX(i);
        }

        // Вычисляем производные для первых n-1 точек (правая разностная производная)
        for (int i = 0; i < pointCount - 1; i++) {
            double x1 = function.getX(i);
            double x2 = function.getX(i + 1);
            double y1 = function.getY(i);
            double y2 = function.getY(i + 1);

            // Правая разностная производная: (y2 - y1) / (x2 - x1)
            yValues[i] = (y2 - y1) / (x2 - x1);
        }

        // Для последней точки используем левую разностную производную
        // (такая же как предпоследняя)
        if (pointCount > 1) {
            yValues[pointCount - 1] = yValues[pointCount - 2];
        } else {
            // Если всего одна точка, производная равна 0
            yValues[0] = 0.0;
        }

        // Создаем новую функцию через фабрику
        return factory.create(xValues, yValues);
    }

    /**
     * Альтернативная реализация без использования итератора
     *
     * @param function исходная табулированная функция
     * @return производная табулированная функция
     */
    public TabulatedFunction deriveUsingDirectAccess(TabulatedFunction function) {
        int pointCount = function.getCount();
        double[] xValues = new double[pointCount];
        double[] yValues = new double[pointCount];

        // Копируем x-значения
        for (int i = 0; i < pointCount; i++) {
            xValues[i] = function.getX(i);
        }

        // Вычисляем производные
        for (int i = 0; i < pointCount - 1; i++) {
            double deltaX = function.getX(i + 1) - function.getX(i);
            double deltaY = function.getY(i + 1) - function.getY(i);
            yValues[i] = deltaY / deltaX;
        }

        // Последняя точка
        if (pointCount > 1) {
            yValues[pointCount - 1] = yValues[pointCount - 2];
        } else {
            yValues[0] = 0.0;
        }

        return factory.create(xValues, yValues);
    }
}