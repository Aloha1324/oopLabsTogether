package functions;

public class ArrayTabulatedFunctionRemovable implements Removable {
    private double[] xValues;
    private double[] yValues;
    private int count;

    // Конструкторы
    public ArrayTabulatedFunctionRemovable(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Длины массивов X и Y должны совпадать");
        }
        if (xValues.length < 2) {
            throw new IllegalArgumentException("Минимальное количество точек - 2");
        }

        this.count = xValues.length;
        this.xValues = new double[count];
        this.yValues = new double[count];

        System.arraycopy(xValues, 0, this.xValues, 0, count);
        System.arraycopy(yValues, 0, this.yValues, 0, count);
    }

    public ArrayTabulatedFunctionRemovable(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Минимальное количество точек - 2");
        }
        if (xFrom >= xTo) {
            throw new IllegalArgumentException("xFrom должен быть меньше xTo");
        }

        this.count = count;
        this.xValues = new double[count];
        this.yValues = new double[count];

        double step = (xTo - xFrom) / (count - 1);
        for (int i = 0; i < count; i++) {
            xValues[i] = xFrom + i * step;
            yValues[i] = source.apply(xValues[i]);
        }
    }

    // Реализация метода remove() из интерфейса Removable
    public void remove(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Индекс выходит за границы массива: " + index +
                    ". Допустимый диапазон: [0, " + (count - 1) + "]");
        }

        // Проверка, что после удаления останется минимум 2 точки
        if (count <= 2) {
            throw new IllegalStateException("Невозможно удалить элемент. Минимальное количество точек - 2");
        }

        // Сдвигаем элементы влево, начиная с позиции index
        for (int i = index; i < count - 1; i++) {
            xValues[i] = xValues[i + 1];
            yValues[i] = yValues[i + 1];
        }

        // Уменьшаем счетчик
        count--;

        // Очищаем последний элемент
        xValues[count] = 0.0;
        yValues[count] = 0.0;
    }

    // Вспомогательные методы для тестирования
    public int getCount() {
        return count;
    }

    public double getX(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Индекс выходит за границы: " + index);
        }
        return xValues[index];
    }

    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Индекс выходит за границы: " + index);
        }
        return yValues[index];
    }

    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Индекс выходит за границы: " + index);
        }
        yValues[index] = value;
    }

    public double leftBound() {
        return xValues[0];
    }

    public double rightBound() {
        return xValues[count - 1];
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArrayTabulatedFunctionRemovable[");
        for (int i = 0; i < count; i++) {
            sb.append("(").append(xValues[i]).append(", ").append(yValues[i]).append(")");
            if (i < count - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}