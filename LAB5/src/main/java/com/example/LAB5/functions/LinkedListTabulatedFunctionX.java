package com.example.LAB5.functions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Реализация табулированной функции на основе двусвязного циклического списка
 */
public class LinkedListTabulatedFunctionX extends AbstractTabulatedFunctionX implements Removable, Iterable<Point> {

    static class Node {
        public Node next;
        public Node prev;
        public double x;
        public double y;
    }

    private Node head; // Голова двусвязного циклического списка

    /**
     * Приватный метод добавления узла в конец списка
     */
    private void addNode(double x, double y) {
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;

        if (head == null) {
            // Список пустой - новый узел становится головой
            head = newNode;
            head.next = head;
            head.prev = head;
        } else {
            // Список не пустой - добавляем в конец
            Node last = head.prev;

            last.next = newNode;
            newNode.prev = last;
            newNode.next = head;
            head.prev = newNode;
        }

        count++;
    }

    /**
     * Конструктор из массивов значений
     */
    public LinkedListTabulatedFunctionX(double[] xValues, double[] yValues) {
        if (xValues == null || yValues == null) {
            throw new IllegalArgumentException("Массивы не могут быть null");
        }
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Длины массивов должны совпадать");
        }
        if (xValues.length < 2) {
            throw new IllegalArgumentException("Длина таблицы должна быть не менее 2 точек");
        }

        // Заполняем список
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    /**
     * Конструктор дискретизации функции
     */
    public LinkedListTabulatedFunctionX(MathFunction source, double xFrom, double xTo, int count) {
        if (source == null) {
            throw new IllegalArgumentException("Функция не может быть null");
        }
        if (count < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }

        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        if (xFrom == xTo) {
            double yValue = source.apply(xFrom);
            for (int i = 0; i < count; i++) {
                addNode(xFrom, yValue);
            }
        } else {
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; i++) {
                double x = xFrom + i * step;
                double y = source.apply(x);
                addNode(x, y);
            }
        }
    }

    /**
     * Вспомогательный метод получения узла по индексу
     */
    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Индекс: " + index + " выходит за границы списка. Допустимый диапазон: 0-" + (count - 1));
        }

        Node current;
        if (index < count / 2) {
            // Если индекс в первой половине - идем с головы
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            // Если индекс во второй половине - идем с хвоста
            current = head.prev;
            for (int i = count - 1; i > index; i--) {
                current = current.prev;
            }
        }

        return current;
    }

    /**
     * Реализация метода remove из интерфейса Removable
     * Удаляет узел по указанному индексу
     */
    public void remove(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Индекс: " + index + " выходит за границы списка. Допустимый диапазон: 0-" + (count - 1));
        }

        if (count == 1) {
            // Если удаляем единственный узел
            head = null;
        } else {
            Node nodeToRemove = getNode(index);

            // Перенаправляем ссылки соседних узлов
            nodeToRemove.prev.next = nodeToRemove.next;
            nodeToRemove.next.prev = nodeToRemove.prev;

            // Если удаляем голову, обновляем голову
            if (index == 0) {
                head = nodeToRemove.next;
            }

            // Обнуляем ссылки удаляемого узла (не обязательно, но для чистоты)
            nodeToRemove.next = null;
            nodeToRemove.prev = null;
        }

        count--;
    }

    public double getX(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Индекс: " + index + " выходит за границы списка. Допустимый диапазон: 0-" + (count - 1));
        }
        return getNode(index).x;
    }

    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Индекс: " + index + " выходит за границы списка. Допустимый диапазон: 0-" + (count - 1));
        }
        return getNode(index).y;
    }

    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Индекс: " + index + " выходит за границы списка. Допустимый диапазон: 0-" + (count - 1));
        }
        getNode(index).y = value;
    }

    public int indexOfX(double x) {
        if (head == null) return -1;

        Node current = head;
        for (int i = 0; i < count; i++) {
            if (Math.abs(current.x - x) < 1e-12) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    public int indexOfY(double y) {
        if (head == null) return -1;

        Node current = head;
        for (int i = 0; i < count; i++) {
            if (Math.abs(current.y - y) < 1e-12) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    public double leftBound() {
        if (head == null) {
            throw new IllegalStateException("Список пуст");
        }
        return head.x;
    }

    public double rightBound() {
        if (head == null) {
            throw new IllegalStateException("Список пуст");
        }
        return head.prev.x;
    }

    protected int floorIndexOfX(double x) {
        if (head == null) {
            throw new IllegalStateException("Список пуст");
        }

        if (x < head.x) {
            throw new IllegalArgumentException("x = " + x + " меньше левой границы " + head.x);
        }

        Node current = head;
        for (int i = 0; i < count - 1; i++) {
            if (x >= current.x && x < current.next.x) {
                return i;
            }
            current = current.next;
        }

        return count - 1;
    }

    protected double extrapolateLeft(double x) {
        // Убрана проверка на count == 1, так как теперь гарантируется минимум 2 точки
        Node first = head;
        Node second = head.next;
        return interpolate(x, first.x, second.x, first.y, second.y);
    }

    protected double extrapolateRight(double x) {
        // Убрана проверка на count == 1, так как теперь гарантируется минимум 2 точки
        Node last = head.prev;
        Node prevLast = last.prev;
        return interpolate(x, prevLast.x, last.x, prevLast.y, last.y);
    }

    protected double interpolate(double x, int floorIndex) {
        // Убрана проверка на count == 1, так как теперь гарантируется минимум 2 точки
        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;
        return interpolate(x, leftNode.x, rightNode.x, leftNode.y, rightNode.y);
    }

    /**
     * Реализация метода iterator() из интерфейса Iterable
     */
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private Node node = head;  // Текущий узел, начинаем с головы
            private int returnedCount = 0;  // Количество уже возвращенных элементов

            public boolean hasNext() {
                // Элементы есть, если мы еще не вернули все точки
                return returnedCount < count;
            }

            public Point next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more elements in the list");
                }

                // Создаем точку из текущего узла
                Point point = new Point(node.x, node.y);

                // Переходим к следующему узлу
                node = node.next;
                returnedCount++;

                return point;
            }
        };
    }
}