package com.example.LAB5.functions;

import com.example.LAB5.exceptions.DifferentLengthOfArraysException;
import com.example.LAB5.exceptions.InterpolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.Serializable;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Iterable<Point>, Cloneable, Serializable{
    private static final Logger logger = LoggerFactory.getLogger(LinkedListTabulatedFunction.class);
    private static final long serialVersionUID = 123456789L;

    private class Node implements Serializable {
        private static final long serialVersionUID = 987654321L;
        Point value;
        Node next;

        Node(Point value) {
            this.value = value;
        }
    }

    private Node head;
    private int count;

    public LinkedListTabulatedFunction() {
        logger.debug("Создание пустого LinkedListTabulatedFunction");
        this.head = null;
        this.count = 0;
    }

    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        logger.debug("Создание LinkedListTabulatedFunction из массивов: xValues.length={}, yValues.length={}",
                xValues != null ? xValues.length : "null", yValues != null ? yValues.length : "null");

        if (xValues == null || yValues == null) {
            logger.error("Один из массивов равен null");
            throw new NullPointerException("Arrays must not be null");
        }
        if (xValues.length < 2) {
            logger.error("Недостаточно точек: {}", xValues.length);
            throw new IllegalArgumentException("At least 2 points required");
        }
        if (xValues.length != yValues.length) {
            logger.error("Разная длина массивов: xValues={}, yValues={}", xValues.length, yValues.length);
            throw new DifferentLengthOfArraysException("Arrays length mismatch");
        }

        checkSorted(xValues);
        this.count = xValues.length;
        head = new Node(new Point(xValues[0], yValues[0]));
        Node current = head;

        for (int i = 1; i < count; i++) {
            Node node = new Node(new Point(xValues[i], yValues[i]));
            current.next = node;
            current = node;
        }

        logger.info("Создан LinkedListTabulatedFunction с {} точками", count);
    }

    public LinkedListTabulatedFunction(MathFunction func, double xFrom, double xTo, int count) {
        logger.debug("Создание LinkedListTabulatedFunction из функции: xFrom={}, xTo={}, count={}", xFrom, xTo, count);

        if (func == null) {
            logger.error("Функция равна null");
            throw new NullPointerException("Function must not be null");
        }
        if (count < 2) {
            logger.error("Недостаточно точек: {}", count);
            throw new IllegalArgumentException("At least 2 points required");
        }
        if (xFrom >= xTo) {
            logger.error("Некорректный диапазон: xFrom={} >= xTo={}", xFrom, xTo);
            throw new IllegalArgumentException("xFrom must be less than xTo");
        }

        this.count = count;
        double step = (xTo - xFrom) / (count - 1);
        head = new Node(new Point(xFrom, func.apply(xFrom)));
        Node current = head;

        for (int i = 1; i < count; i++) {
            double x = xFrom + i * step;
            double y = func.apply(x);
            Node node = new Node(new Point(x, y));
            current.next = node;
            current = node;
        }

        logger.info("Создан LinkedListTabulatedFunction из функции с {} точками", count);
    }

    public int getCount() {
        logger.trace("Получение количества точек: {}", count);
        return count;
    }

    public double getX(int index) {
        logger.trace("Получение X по индексу: {}", index);
        checkIndex(index);
        double result = getNode(index).value.x;
        logger.trace("X[{}] = {}", index, result);
        return result;
    }

    public double getY(int index) {
        logger.trace("Получение Y по индексу: {}", index);
        checkIndex(index);
        double result = getNode(index).value.y;
        logger.trace("Y[{}] = {}", index, result);
        return result;
    }

    public void setY(int index, double value) {
        logger.debug("Установка Y[{}] = {}", index, value);
        checkIndex(index);
        double oldValue = getNode(index).value.y;
        getNode(index).value.y = value;
        logger.debug("Y[{}] изменен с {} на {}", index, oldValue, value);
    }

    private Node getNode(int index) {
        logger.trace("Получение узла по индексу: {}", index);
        Node node = head;
        for (int i = 0; i < index; i++) {
            node = node.next;
        }
        return node;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= getCount()) {
            logger.error("Индекс вне диапазона: {} (допустимо: 0..{})", index, getCount() - 1);
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
    }

    public void insert(double x, double y) {
        logger.debug("Вставка точки: x={}, y={}", x, y);

        if (head == null) {
            head = new Node(new Point(x, y));
            count = 1;
            logger.debug("Вставлена первая точка, count={}", count);
            return;
        }

        if (Double.compare(x, head.value.x) < 0) {
            Node newHead = new Node(new Point(x, y));
            newHead.next = head;
            head = newHead;
            count++;
            logger.debug("Вставлена точка в начало, count={}", count);
            return;
        }

        Node current = head;
        Node prev = null;
        while (current != null && Double.compare(current.value.x, x) < 0) {
            prev = current;
            current = current.next;
        }

        if (current != null && Double.compare(current.value.x, x) == 0) {
            logger.debug("Точка с x={} существует, обновление Y с {} на {}", x, current.value.y, y);
            current.value.y = y;
        } else {
            Node newNode = new Node(new Point(x, y));
            prev.next = newNode;
            newNode.next = current;
            count++;
            logger.debug("Вставлена новая точка, count={}", count);
        }
    }

    public int indexOfX(double x) {
        logger.trace("Поиск индекса по X: {}", x);
        int index = 0;
        Node node = head;
        while (node != null) {
            if (Double.compare(node.value.x, x) == 0) {
                logger.trace("Найден индекс {} для X={}", index, x);
                return index;
            }
            node = node.next;
            index++;
        }
        logger.trace("X={} не найден", x);
        return -1;
    }

    public int indexOfY(double y) {
        logger.trace("Поиск индекса по Y: {}", y);
        int index = 0;
        Node node = head;
        while (node != null) {
            if (Double.compare(node.value.y, y) == 0) {
                logger.trace("Найден индекс {} для Y={}", index, y);
                return index;
            }
            node = node.next;
            index++;
        }
        logger.trace("Y={} не найден", y);
        return -1;
    }

    public double leftBound() {
        if (head == null) {
            logger.error("Попытка получить левую границу пустой функции");
            throw new IllegalStateException("Empty function");
        }
        double result = head.value.x;
        logger.trace("Левая граница: {}", result);
        return result;
    }

    public double rightBound() {
        if (head == null) {
            logger.error("Попытка получить правую границу пустой функции");
            throw new IllegalStateException("Empty function");
        }
        Node node = head;
        while (node.next != null) {
            node = node.next;
        }
        double result = node.value.x;
        logger.trace("Правая граница: {}", result);
        return result;
    }

    public int floorIndexOfX(double x) {
        logger.trace("Поиск floorIndexOfX для x={}", x);

        if (count < 2) {
            logger.error("Недостаточно данных для поиска floorIndexOfX: count={}", count);
            throw new IllegalStateException("Insufficient data");
        }

        if (x < leftBound()) {
            logger.trace("x={} меньше левой границы, возвращаем 0", x);
            return 0;
        }
        if (x >= rightBound()) {
            logger.trace("x={} больше правой границы, возвращаем {}", x, count - 2);
            return count - 2;
        }

        int index = 0;
        Node node = head;
        while (node.next != null && node.next.value.x <= x) {
            node = node.next;
            index++;
        }

        logger.trace("Найден floorIndexOfX: {} для x={}", index, x);
        return index;
    }

    public double extrapolateLeft(double x) {
        logger.debug("Экстраполяция слева для x={}", x);
        double x0 = getX(0);
        double x1 = getX(1);
        double y0 = getY(0);
        double y1 = getY(1);
        double result = interpolate(x, x0, x1, y0, y1);
        logger.debug("Результат экстраполяции слева: {}", result);
        return result;
    }

    public double extrapolateRight(double x) {
        logger.debug("Экстраполяция справа для x={}", x);
        int last = getCount() - 1;
        double x0 = getX(last - 1);
        double x1 = getX(last);
        double y0 = getY(last - 1);
        double y1 = getY(last);
        double result = interpolate(x, x0, x1, y0, y1);
        logger.debug("Результат экстраполяции справа: {}", result);
        return result;
    }

    public double interpolate(double x, int floorIndex) {
        logger.debug("Интерполяция для x={} в интервале {}", x, floorIndex);

        if (floorIndex < 0 || floorIndex >= getCount() - 1) {
            logger.error("Некорректный floorIndex: {} (допустимо: 0..{})", floorIndex, getCount() - 2);
            throw new IllegalArgumentException("Invalid floor index");
        }

        double x0 = getX(floorIndex);
        double x1 = getX(floorIndex + 1);

        if (x < x0 || x > x1) {
            logger.error("x={} вне интервала интерполяции [{}, {}]", x, x0, x1);
            throw new InterpolationException("x is outside interpolation interval");
        }

        double y0 = getY(floorIndex);
        double y1 = getY(floorIndex + 1);
        double result = interpolate(x, x0, x1, y0, y1);
        logger.debug("Результат интерполяции: {}", result);
        return result;
    }

    protected double interpolate(double x, double x0, double x1, double y0, double y1) {
        logger.trace("Линейная интерполяция: x={}, x0={}, x1={}, y0={}, y1={}", x, x0, x1, y0, y1);

        if (Double.compare(x0, x1) == 0) {
            logger.debug("Граничные точки совпадают, возвращаем среднее значение");
            return (y0 + y1) / 2.0;
        }

        double result = y0 + (y1 - y0) * (x - x0) / (x1 - x0);
        logger.trace("Результат линейной интерполяции: {}", result);
        return result;
    }

    public void remove(int index) {
        logger.debug("Удаление точки по индексу: {}", index);

        if (index < 0 || index >= getCount()) {
            logger.error("Индекс вне диапазона: {} (допустимо: 0..{})", index, getCount() - 1);
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }

        if (getCount() <= 2) {
            logger.error("Нельзя удалить точку - требуется минимум 2 точки, текущее количество: {}", getCount());
            throw new IllegalStateException("Cannot remove point - at least 2 points are required");
        }

        if (index == 0) {
            head = head.next;
        } else {
            Node prev = getNode(index - 1);
            prev.next = prev.next.next;
        }
        count--;

        logger.debug("Точка удалена, новый count={}", getCount());
    }

    public LinkedListTabulatedFunction clone() {
        logger.debug("Клонирование LinkedListTabulatedFunction");

        try {
            LinkedListTabulatedFunction cloned = (LinkedListTabulatedFunction) super.clone();

            if (head == null) {
                cloned.head = null;
                cloned.count = 0;
                logger.debug("Клонирована пустая функция");
            } else {
                cloned.head = new Node(new Point(head.value.x, head.value.y));
                Node currentOrig = head.next;
                Node currentClone = cloned.head;

                while (currentOrig != null) {
                    currentClone.next = new Node(new Point(currentOrig.value.x, currentOrig.value.y));
                    currentClone = currentClone.next;
                    currentOrig = currentOrig.next;
                }
                cloned.count = this.count;
                logger.debug("Клонирована функция с {} точками", count);
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            logger.error("Ошибка клонирования", e);
            throw new AssertionError("Clone not supported");
        }
    }

    public Iterator<Point> iterator() {
        logger.trace("Создание итератора");
        return new Iterator<>() {
            private Node currentNode = head;
            private int currentIndex = 0;

            public boolean hasNext() {
                boolean hasNext = currentIndex < count;
                logger.trace("Проверка наличия следующей точки: {}", hasNext);
                return hasNext;
            }

            public Point next() {
                if (!hasNext()) {
                    logger.error("Попытка получить следующую точку при отсутствии элементов");
                    throw new NoSuchElementException("No more elements");
                }
                Point point = new Point(currentNode.value.x, currentNode.value.y);
                logger.trace("Итератор вернул точку {}: [{}, {}]", currentIndex, point.x, point.y);
                currentNode = currentNode.next;
                currentIndex++;
                return point;
            }

            public void remove() {
                logger.error("Попытка вызвать remove() у итератора");
                throw new UnsupportedOperationException();
            }
        };
    }
}