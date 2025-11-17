package functions;
import java.util.Iterator;

/**
 * Интерфейс для табулированных функций, добавляя методы для работы с таблицей значений
 */
public interface TabulatedFunction extends MathFunction, Iterable<Point> {

    /**
     * Возвращает количество табулированных значений
     * @return количество точек табуляции
     */
    int getCount();

    /**
     * Получает значение аргумента x по номеру индекса
     * @param index индекс значения (от 0 до getCount()-1)
     * @return значение x в указанной точке
     */
    double getX(int index);

    /**
     * Получает значение функции y по номеру индекса
     * @param index индекс значения (от 0 до getCount()-1)
     * @return значение y в указанной точке
     */
    double getY(int index);

    /**
     * Задает значение функции y по номеру индекса
     * @param index индекс значения (от 0 до getCount()-1)
     * @param value новое значение y
     */
    void setY(int index, double value);

    /**
     * Возвращает индекс аргумента x в таблице
     * Предполагается, что все x различны
     * @param x значение аргумента для поиска
     * @return индекс x или -1 если не найден
     */
    int indexOfX(double x);

    /**
     * Возвращает индекс первого вхождения значения y
     * @param y значение функции для поиска
     * @return индекс y или -1 если не найден
     */
    int indexOfY(double y);

    /**
     * Возвращает самый левый x (минимальное значение аргумента)
     * @return минимальное значение x
     */
    double leftBound();

    /**
     * Возвращает самый правый x (максимальное значение аргумента)
     * @return максимальное значение x
     */
    double rightBound();
}