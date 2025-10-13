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

    // Конструктор без параметров — создает пустой список
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

    @Override
    public int indexOfX(double x) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (Double.compare(current.value.x, x) == 0)
                return i;
            current = current.next;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (Double.compare(current.value.y, y) == 0)
                return i;
            current = current.next;
        }
        return -1;
    }

    @Override
    public double leftBound() {
        if (count == 0) throw new IllegalStateException("Empty function");
        return head.value.x;
    }

    @Override
    public double rightBound() {
        if (count == 0) throw new IllegalStateException("Empty function");
        Node current = head;
        while (current.next != null)
            current = current.next;
        return current.value.x;
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
        Node current = head;
        while (current.next != null && current.next.value.x <= x) {
            current = current.next;
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
            throw new IllegalArgumentException("Invalid floor index: " + floorIndex);
        double x0 = getX(floorIndex);
        double x1 = getX(floorIndex + 1);
        if (x < x0 || x > x1)
            throw new InterpolationException("x is outside interpolation interval [" + x0 + "," + x1 + "]");
        double y0 = getY(floorIndex);
        double y1 = getY(floorIndex + 1);
        return interpolate(x, x0, x1, y0, y1);
    }

    protected double interpolate(double x, double x0, double x1, double y0, double y1) {
        if (x0 == x1) return (y0 + y1) / 2.0;
        return y0 + ( (x - x0) * (y1 - y0) ) / (x1 - x0);
    }

    @Override
    public LinkedListTabulatedFunction clone() {
        try {
            LinkedListTabulatedFunction cloned = (LinkedListTabulatedFunction) super.clone();
            // Deep copy nodes
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        LinkedListTabulatedFunction other = (LinkedListTabulatedFunction) obj;
        if (this.count != other.count) return false;
        Node curr1 = this.head;
        Node curr2 = other.head;
        while (curr1 != null) {
            if (Double.compare(curr1.value.x, curr2.value.x) != 0) return false;
            if (Double.compare(curr1.value.y, curr2.value.y) != 0) return false;
            curr1 = curr1.next;
            curr2 = curr2.next;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        Node current = head;
        while (current != null) {
            long temp = Double.doubleToLongBits(current.value.x);
            result = 31 * result + (int)(temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(current.value.y);
            result = 31 * result + (int)(temp ^ (temp >>> 32));
            current = current.next;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LinkedListTabulatedFunction{ count = " + count + ", points=[");
        Node current = head;
        while (current != null) {
            sb.append("(").append(current.value.x).append(", ").append(current.value.y).append(")");
            if (current.next != null) sb.append(", ");
            current = current.next;
        }
        sb.append("]}");
        return sb.toString();
    }

    // Итератор, который выбрасывает UnsupportedOperationException
    @Override
    public Iterator<Point> iterator() {
        throw new UnsupportedOperationException("Iterator not supported");
    }
}
