package operations;

import functions.Point;
import functions.TabulatedFunction;

public class TabulatedFunctionOperationService {

    /**
     * Преобразует табулированную функцию в массив точек Point[]
     *
     * @param tabulatedFunction табулированная функция
     * @return массив точек
     */
    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        int size = 0;
        for (Point ignored : tabulatedFunction) {
            size++;
        }
        Point[] points = new Point[size];
        int i = 0;
        for (Point point : tabulatedFunction) {
            points[i++] = point;
        }
        return points;
    }
}
