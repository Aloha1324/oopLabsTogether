package functions;

import java.util.Iterator;
import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction {
    private static class Node {
        Point point;
        Node next;
        Node prev;

        Node(double x, double y) {
            this.point = new Point(x, y);
        }
    }

    private Node head;
    private Node tail;
    private int count;

    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length < 2 || yValues.length < 2) {
            throw new IllegalArgumentException("Minimum 2 points required");
        }
        if (xValues.length != yValues.length) {
            throw new DifferentLengthOfArraysException("Arrays must have the same length");
        }
        if (!isSorted(xValues)) {
            throw new ArrayIsNotSortedException("X values must be sorted");
        }

        this.count = xValues.length;
        for (int i = 0; i < count; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("Minimum 2 points required");
        }
        if (xFrom >= xTo) {
            throw new IllegalArgumentException("xFrom must be less than xTo");
        }
        if (source == null) {
            throw new NullPointerException("Source function cannot be null");
        }

        this.count = count;
        double step = (xTo - xFrom) / (count - 1);
        for (int i = 0; i < count; i++) {
            double x = xFrom + i * step;
            double y = source.apply(x);
            addNode(x, y);
        }
    }

    private boolean isSorted(double[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i] <= array[i - 1]) {
                return false;
            }
        }
        return true;
    }

    private void addNode(double x, double y) {
        Node newNode = new Node(x, y);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    private Node getNode(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }

        Node current;
        if (index < count / 2) {
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            current = tail;
            for (int i = count - 1; i > index; i--) {
                current = current.prev;
            }
        }
        return current;
    }

    private Node findNodeX(double x) {
        Node current = head;
        while (current != null) {
            if (current.point.x == x) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        return getNode(index).point.x;
    }

    @Override
    public double getY(int index) {
        return getNode(index).point.y;
    }

    @Override
    public void setY(int index, double value) {
        getNode(index).point.y = value;
    }

    @Override
    public double leftBound() {
        return head.point.x;
    }

    @Override
    public double rightBound() {
        return tail.point.x;
    }

    @Override
    public int indexOfX(double x) {
        Node current = head;
        int index = 0;
        while (current != null) {
            if (current.point.x == x) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        Node current = head;
        int index = 0;
        while (current != null) {
            if (current.point.y == y) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    @Override
    public int floorIndexOfX(double x) {
        if (x < head.point.x) {
            return 0;
        }
        if (x > tail.point.x) {
            return count - 2;
        }

        Node current = head;
        int index = 0;
        while (current.next != null) {
            if (x >= current.point.x && x < current.next.point.x) {
                return index;
            }
            current = current.next;
            index++;
        }
        return count - 2;
    }

    @Override
    public double extrapolateLeft(double x) {
        return interpolate(x, 0);
    }

    @Override
    public double extrapolateRight(double x) {
        return interpolate(x, count - 2);
    }

    @Override
    public double interpolate(double x, int floorIndex) {
        if (floorIndex < 0 || floorIndex >= count - 1) {
            throw new IllegalArgumentException("Invalid floor index: " + floorIndex);
        }

        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;

        double leftX = leftNode.point.x;
        double rightX = rightNode.point.x;
        double leftY = leftNode.point.y;
        double rightY = rightNode.point.y;

        return interpolate(x, leftX, rightX, leftY, rightY);
    }

    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        if (leftX == rightX) {
            return (leftY + rightY) / 2;
        }
        return leftY + (rightY - leftY) * (x - leftX) / (rightX - leftX);
    }

    @Override
    public double apply(double x) {
        if (x < head.point.x) {
            return extrapolateLeft(x);
        }
        if (x > tail.point.x) {
            return extrapolateRight(x);
        }

        int index = indexOfX(x);
        if (index != -1) {
            return getY(index);
        }

        int floorIndex = floorIndexOfX(x);
        return interpolate(x, floorIndex);
    }

    public void insert(double x, double y) {
        Node existingNode = findNodeX(x);
        if (existingNode != null) {
            existingNode.point.y = y;
            return;
        }

        Node newNode = new Node(x, y);

        if (head == null) {
            head = newNode;
            tail = newNode;
        } else if (x < head.point.x) {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        } else if (x > tail.point.x) {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        } else {
            Node current = head;
            while (current != null && current.point.x < x) {
                current = current.next;
            }
            newNode.next = current;
            newNode.prev = current.prev;
            current.prev.next = newNode;
            current.prev = newNode;
        }
        count++;
    }

    public void remove(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }

        Node nodeToRemove = getNode(index);

        if (nodeToRemove.prev != null) {
            nodeToRemove.prev.next = nodeToRemove.next;
        } else {
            head = nodeToRemove.next;
        }

        if (nodeToRemove.next != null) {
            nodeToRemove.next.prev = nodeToRemove.prev;
        } else {
            tail = nodeToRemove.prev;
        }

        count--;
    }

    @Override
    public Iterator<Point> iterator() {
        throw new UnsupportedOperationException("Iterator not supported for LinkedListTabulatedFunction");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LinkedListTabulatedFunction{");
        sb.append("size=").append(count);
        sb.append(", points=[");
        Node current = head;
        while (current != null) {
            sb.append("(").append(current.point.x).append(", ").append(current.point.y).append(")");
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        LinkedListTabulatedFunction that = (LinkedListTabulatedFunction) obj;
        if (count != that.count) return false;

        Node current1 = head;
        Node current2 = that.head;
        while (current1 != null) {
            if (current1.point.x != current2.point.x || current1.point.y != current2.point.y) {
                return false;
            }
            current1 = current1.next;
            current2 = current2.next;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        Node current = head;
        while (current != null) {
            result = 31 * result + Double.hashCode(current.point.x);
            result = 31 * result + Double.hashCode(current.point.y);
            current = current.next;
        }
        return result;
    }

    @Override
    public LinkedListTabulatedFunction clone() {
        double[] xValues = new double[count];
        double[] yValues = new double[count];

        Node current = head;
        int index = 0;
        while (current != null) {
            xValues[index] = current.point.x;
            yValues[index] = current.point.y;
            current = current.next;
            index++;
        }

        return new LinkedListTabulatedFunction(xValues, yValues);
    }
}