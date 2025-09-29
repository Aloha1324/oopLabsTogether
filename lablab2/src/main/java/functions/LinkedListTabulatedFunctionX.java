package functions;

/**
 * Реализация табулированной функции на основе двусвязного циклического списка
 */
public class LinkedListTabulatedFunctionX extends AbstractTabulatedFunctionX {

    private Node head; // Приватное поле - голова двусвязного циклического списка

    /**
     * Приватный метод добавления узла в конец списка
     */
    private void addNode(double x, double y) {
        Node newNode = new Node(); // Создаем новый узел
        newNode.x = x; // Устанавливаем значение x
        newNode.y = y; // Устанавливаем значение y

        if (head == null) {
            // Если список пустой - новый узел становится головой
            head = newNode;
            head.next = head; // Ссылка на следующий указывает на себя
            head.prev = head; // Ссылка на предыдущий указывает на себя
        } else {
            // Если список не пустой - добавляем в конец
            Node last = head.prev; // Последний узел - предыдущий от головы

            last.next = newNode; // Следующий от последнего - новый узел
            newNode.prev = last; // Предыдущий от нового - последний
            newNode.next = head; // Следующий от нового - голова
            head.prev = newNode; // Предыдущий от головы - новый узел
        }

        count++; // Увеличиваем счетчик узлов
    }

    /**
     * Конструктор из массивов значений
     */
    public LinkedListTabulatedFunctionX(double[] xValues, double[] yValues) {
        // Проверяем, что массивы не null
        if (xValues == null || yValues == null) {
            throw new IllegalArgumentException("Массивы не могут быть null");
        }
        // Проверяем, что длины массивов совпадают
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Длины массивов должны совпадать. xValues.length = " +
                    xValues.length + ", yValues.length = " + yValues.length);
        }
        // Проверяем, что массивы не пустые
        if (xValues.length == 0) {
            throw new IllegalArgumentException("Массивы не могут быть пустыми");
        }

        // Заполняем список значениями из массивов
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]); // Добавляем каждый узел
        }
    }

    /**
     * Конструктор дискретизации функции
     */
    public LinkedListTabulatedFunctionX(MathFunction source, double xFrom, double xTo, int count) {
        // Проверяем, что функция не null
        if (source == null) {
            throw new IllegalArgumentException("Функция не может быть null");
        }
        // Проверяем, что количество точек положительное
        if (count <= 0) {
            throw new IllegalArgumentException("Количество точек должно быть положительным");
        }

        // Если границы перепутаны - меняем местами
        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        // Если интервал вырожденный (одна точка)
        if (xFrom == xTo) {
            double yValue = source.apply(xFrom); // Вычисляем значение функции
            for (int i = 0; i < count; i++) {
                addNode(xFrom, yValue); // Добавляем узлы с одинаковыми значениями
            }
        } else {
            // Равномерная дискретизация функции
            double step = (xTo - xFrom) / (count - 1); // Вычисляем шаг
            for (int i = 0; i < count; i++) {
                double x = xFrom + i * step; // Вычисляем x
                double y = source.apply(x); // Вычисляем y
                addNode(x, y); // Добавляем узел
            }
        }
    }

    /**
     * Вспомогательный метод получения узла по индексу
     * Оптимизирован: для индексов в первой половине идет с головы,
     * для второй половины - с хвоста
     */
    private Node getNode(int index) {
        // Проверяем корректность индекса
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Индекс: " + index + ", Размер: " + count);
        }

        Node current;
        if (index < count / 2) {
            // Если индекс в первой половине - идем с головы
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next; // Переходим к следующему узлу
            }
        } else {
            // Если индекс во второй половине - идем с хвоста
            current = head.prev; // Начинаем с последнего узла
            for (int i = count - 1; i > index; i--) {
                current = current.prev; // Переходим к предыдущему узлу
            }
        }

        return current; // Возвращаем найденный узел
    }

    // Получение значения x по индексу
    @Override
    public double getX(int index) {
        return getNode(index).x; // Возвращаем x из узла
    }

    // Получение значения y по индексу
    @Override
    public double getY(int index) {
        return getNode(index).y; // Возвращаем y из узла
    }

    // Установка значения y по индексу
    @Override
    public void setY(int index, double value) {
        getNode(index).y = value; // Устанавливаем y в узле
    }

    // Поиск индекса x в таблице
    @Override
    public int indexOfX(double x) {
        if (head == null) return -1; // Если список пуст - возвращаем -1

        Node current = head;
        for (int i = 0; i < count; i++) {
            // Сравниваем с учетом погрешности вычислений
            if (Math.abs(current.x - x) < 1e-12) {
                return i; // Нашли совпадение - возвращаем индекс
            }
            current = current.next; // Переходим к следующему узлу
        }
        return -1; // Не нашли - возвращаем -1
    }

    // Поиск индекса y в таблице
    @Override
    public int indexOfY(double y) {
        if (head == null) return -1; // Если список пуст - возвращаем -1

        Node current = head;
        for (int i = 0; i < count; i++) {
            // Сравниваем с учетом погрешности вычислений
            if (Math.abs(current.y - y) < 1e-12) {
                return i; // Нашли совпадение - возвращаем индекс
            }
            current = current.next; // Переходим к следующему узлу
        }
        return -1; // Не нашли - возвращаем -1
    }

    // Получение левой границы (минимальный x)
    @Override
    public double leftBound() {
        if (head == null) {
            throw new IllegalStateException("Список пуст");
        }
        return head.x; // Голова списка содержит минимальный x
    }

    // Получение правой границы (максимальный x)
    @Override
    public double rightBound() {
        if (head == null) {
            throw new IllegalStateException("Список пуст");
        }
        return head.prev.x; // Предыдущий от головы содержит максимальный x
    }

    // Поиск индекса интервала для заданного x
    @Override
    protected int floorIndexOfX(double x) {
        if (head == null) {
            throw new IllegalStateException("Список пуст");
        }

        // Если x меньше всех значений - возвращаем 0
        if (x < head.x) {
            return 0;
        }

        // Ищем интервал, содержащий x
        Node current = head;
        for (int i = 0; i < count - 1; i++) {
            // Если x попадает в интервал [current.x, current.next.x)
            if (x >= current.x && x < current.next.x) {
                return i; // Возвращаем индекс левой границы интервала
            }
            current = current.next; // Переходим к следующему узлу
        }

        // Если x больше или равен последнему значению - возвращаем последний индекс
        return count - 1;
    }

    // Экстраполяция слева (для x < leftBound())
    @Override
    protected double extrapolateLeft(double x) {
        if (count == 1) {
            return head.y; // Если только одна точка - возвращаем её значение
        }
        // Используем линейную экстраполяцию на основе первых двух точек
        Node first = head;
        Node second = head.next;
        return interpolate(x, first.x, second.x, first.y, second.y);
    }

    // Экстраполяция справа (для x > rightBound())
    @Override
    protected double extrapolateRight(double x) {
        if (count == 1) {
            return head.y; // Если только одна точка - возвращаем её значение
        }
        // Используем линейную экстраполяцию на основе последних двух точек
        Node last = head.prev;
        Node prevLast = last.prev;
        return interpolate(x, prevLast.x, last.x, prevLast.y, last.y);
    }

    // Интерполяция внутри интервала по индексу
    @Override
    protected double interpolate(double x, int floorIndex) {
        if (count == 1) {
            return head.y; // Если только одна точка - возвращаем её значение
        }

        // Получаем узлы границ интервала
        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;
        // Выполняем линейную интерполяцию
        return interpolate(x, leftNode.x, rightNode.x, leftNode.y, rightNode.y);
    }
}