package functions;

import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
import exceptions.InterpolationException;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Iterable<Point>, Cloneable {

    private class Node {
        Point value;
        Node next;

        Node(Point value) {
            this.value = value;
        }
    }

    private Node head;
    private int count;

    // Конструктор без параметров
    public LinkedListTabulatedFunction() {
        this.head = null;
        this.count = 0;
    }

    // Конструктор на основе массивов
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues == null || yValues == null)
            throw new NullPointerException("Arrays must not be null");
        if (xValues.length < 2)
            throw new IllegalArgumentException("At least 2 points required");
        checkLengthIsTheSame(xValues, yValues);
        checkSorted(xValues);

        this.count = xValues.length;
        head = new Node(new Point(xValues[0], yValues[0]));
        Node current = head;
        for (int i = 1; i < count; i++) {
            Node node = new Node(new Point(xValues[i], yValues[i]));
            current.next = node;
            current = node;
        }
    }

    // Конструктор на основе функции и интервала
    public LinkedListTabulatedFunction(MathFunction func, double xFrom, double xTo, int count) {
        if (func == null)
            throw new NullPointerException("Function must not be null");
        if (count < 2)
            throw new IllegalArgumentException("At least 2 points required");
        if (xFrom >= xTo)
            throw new IllegalArgumentException("xFrom must be less than xTo");

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
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        checkIndex(index);
        return getNode(index).value.x;
    }

    @Override
    public double getY(int index) {
        checkIndex(index);
        return getNode(index).value.y;
    }

    @Override
    public void setY(int index, double value) {
        checkIndex(index);
        getNode(index).value.y = value;
    }

    private Node getNode(int index) {
        Node node = head;
        for (int i = 0; i < index; i++)
            node = node.next;
        return node;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= getCount())
            throw new IllegalArgumentException("Index out of bounds: " + index);
    }

    public void insert(double x, double y) {
        if (head == null) {
            head = new Node(new Point(x, y));
            count = 1;
            return;
        }
        if (Double.compare(x, head.value.x) < 0) {
            Node newHead = new Node(new Point(x, y));
            newHead.next = head;
            head = newHead;
            count++;
            return;
        }
        Node current = head;
        Node prev = null;
        while (current != null && current.value.x < x) {
            prev = current;
            current = current.next;
        }
        if (current != null && Double.compare(current.value.x, x) == 0) {
            current.value.y = y;
        } else {
            Node newNode = new Node(new Point(x, y));
            prev.next = newNode;
            newNode.next = current;
            count++;
        }
    }

    @Override
    public int indexOfX(double x) {
        int index = 0;
        Node node = head;
        while (node != null) {
            if (Double.compare(node.value.x, x) == 0)
                return index;
            node = node.next;
            index++;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        int index = 0;
        Node node = head;
        while (node != null) {
            if (Double.compare(node.value.y, y) == 0)
                return index;
            node = node.next;
            index++;
        }
        return -1;
    }

    @Override
    public double leftBound() {
        if (head == null)
            throw new IllegalStateException("Empty function");
        return head.value.x;
    }

    @Override
    public double rightBound() {
        if (head == null)
            throw new IllegalStateException("Empty function");
        Node node = head;
        while (node.next != null)
            node = node.next;
        return node.value.x;
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (count < 2)
            throw new IllegalStateException("Insufficient data");
        if (x < leftBound())
            return 0;
        if (x > rightBound())
            return count - 2;
        int index = 0;
        Node node = head;
        while (node.next != null && node.next.value.x <= x) {
            node = node.next;
            index++;
        }
        return index;
    }

    @Override
    protected double extrapolateLeft(double x) {
        double x0 = getX(0);
        double x1 = getX(1);
        double y0 = getY(0);
        double y1 = getY(1);
        return interpolate(x, x0, x1, y0, y1);
    }

    @Override
    protected double extrapolateRight(double x) {
        int last = getCount() - 1;
        double x0 = getX(last - 1);
        double x1 = getX(last);
        double y0 = getY(last - 1);
        double y1 = getY(last);
        return interpolate(x, x0, x1, y0, y1);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (floorIndex < 0 || floorIndex >= getCount() - 1)
            throw new IllegalArgumentException("Invalid floor index");
        double x0 = getX(floorIndex);
        double x1 = getX(floorIndex + 1);
        if (x < x0 || x > x1)
            throw new InterpolationException("x is outside interpolation interval");
        double y0 = getY(floorIndex);
        double y1 = getY(floorIndex + 1);
        return interpolate(x, x0, x1, y0, y1);
    }

    protected double interpolate(double x, double x0, double x1, double y0, double y1) {
        if (Double.compare(x0, x1) == 0)
            return (y0 + y1) / 2.0;
        return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
    }

    @Override
    public LinkedListTabulatedFunction clone() {
        try {
            LinkedListTabulatedFunction cloned = (LinkedListTabulatedFunction) super.clone();

            if (head == null) {
                cloned.head = null;
                cloned.count = 0;
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
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported");
        }
    }

    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private Node current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Point next() {
                if (!hasNext())
                    throw new NoSuchElementException("No more points available");
                Point result = current.value;
                current = current.next;
                return result;
            }
        };
    }
}
