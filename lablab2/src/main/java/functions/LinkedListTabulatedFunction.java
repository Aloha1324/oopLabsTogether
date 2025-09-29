package functions;

public class LinkedListTabulatedFunction implements Insertable {
    private Node head;
    private Node tail;
    private int count;

    public LinkedListTabulatedFunction() {
        this.head = null;
        this.tail = null;
        this.count = 0;
    }

    // Конструктор для удобства тестирования
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Arrays must have same length");
        }
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    // Реализация метода insert() из интерфейса Insertable
    @Override
    public void insert(double x, double y) {
        // Если список пустой, просто добавляем узел
        if (count == 0) {
            addNode(x, y);
            return;
        }

        // Поиск узла с таким же x
        Node current = head;
        while (current != null) {
            if (current.x == x) {
                // Если нашли узел с таким же x, заменяем y
                current.y = y;
                return;
            }
            current = current.next;
        }

        // Если не нашли узел с таким x, ищем место для вставки
        current = head;
        Node previous = null;

        while (current != null && current.x < x) {
            previous = current;
            current = current.next;
        }

        // Создаем новый узел
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;

        if (previous == null) {
            // Вставка в начало списка
            newNode.next = head;
            newNode.prev = null;
            if (head != null) {
                head.prev = newNode;
            }
            head = newNode;
        } else if (current == null) {
            // Вставка в конец списка
            newNode.prev = tail;
            newNode.next = null;
            tail.next = newNode;
            tail = newNode;
        } else {
            // Вставка в середину списка
            newNode.prev = previous;
            newNode.next = current;
            previous.next = newNode;
            current.prev = newNode;
        }

        count++;
    }

    // Вспомогательный метод для добавления узла в конец
    private void addNode(double x, double y) {
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;
        newNode.next = null;

        if (head == null) {
            // Если список пустой
            newNode.prev = null;
            head = newNode;
            tail = newNode;
        } else {
            // Если список не пустой
            newNode.prev = tail;
            tail.next = newNode;
            tail = newNode;
        }

        count++;
    }

    // Вспомогательные методы для тестирования
    public int getCount() {
        return count;
    }

    public double getX(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.x;
    }

    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.y;
    }

    // Внутренний класс Node
    private static class Node {
        double x;
        double y;
        Node next;
        Node prev;
    }
}