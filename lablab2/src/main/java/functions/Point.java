package functions;

/**
 * Класс для представления точки с координатами (x, y).
 * Используется для предоставления значений из табулированных функций
 * независимо от способа хранения данных.
 */
public class Point {

    /**
     * Координата x точки
     */
    public final double x;

    /**
     * Координата y точки
     */
    public final double y;

    /**
     * Конструктор точки с заданными координатами
     *
     * @param x координата x
     * @param y координата y
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает строковое представление точки в формате (x, y)
     *
     * @return строковое представление точки
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * Проверяет равенство точек с учетом погрешности вычислений
     *
     * @param obj объект для сравнения
     * @return true если точки равны с учетом погрешности
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Point point = (Point) obj;
        return Math.abs(x - point.x) < 1e-12 &&
                Math.abs(y - point.y) < 1e-12;
    }

    /**
     * Возвращает хэш-код точки
     *
     * @return хэш-код
     */
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}