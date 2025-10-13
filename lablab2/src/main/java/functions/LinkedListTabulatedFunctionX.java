package functions;

/**
 * Реализация табулированной функции на основе двусвязного циклического списка
 */
public class LinkedListTabulatedFunctionX extends AbstractTabulatedFunctionX implements Removable {

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
        if (xValues.length == 0) {
            throw new IllegalArgumentException("Массивы не могут быть пустыми");
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
        if (count <= 0) {
            throw new IllegalArgumentException("Количество точек должно быть положительным");
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
            throw new IndexOutOfBoundsException("Индекс: " + index + ", Размер: " + count);
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
    @Override
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

    @Override
    public double getX(int index) {
        return getNode(index).x;
    }

    @Override
    public double getY(int index) {
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value) {
        getNode(index).y = value;
    }

    @Override
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

    @Override
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

    @Override
    public double leftBound() {
        if (head == null) {
            throw new IllegalStateException("Список пуст");
        }
        return head.x;
    }

    @Override
    public double rightBound() {
        if (head == null) {
            throw new IllegalStateException("Список пуст");
        }
        return head.prev.x;
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (head == null) {
            throw new IllegalStateException("Список пуст");
        }

        if (x < head.x) {
            return 0;
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

    @Override
    protected double extrapolateLeft(double x) {
        if (count == 1) {
            return head.y;
        }
        Node first = head;
        Node second = head.next;
        return interpolate(x, first.x, second.x, first.y, second.y);
    }

    @Override
    protected double extrapolateRight(double x) {
        if (count == 1) {
            return head.y;
        }
        Node last = head.prev;
        Node prevLast = last.prev;
        return interpolate(x, prevLast.x, last.x, prevLast.y, last.y);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (count == 1) {
            return head.y;
        }

        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;
        return interpolate(x, leftNode.x, rightNode.x, leftNode.y, rightNode.y);
    }
}