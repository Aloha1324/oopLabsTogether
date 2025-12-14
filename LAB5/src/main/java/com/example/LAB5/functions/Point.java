package com.example.LAB5.functions;

import java.io.Serializable;

public class Point implements Serializable {

    private static final long serialVersionUID = 1L;

    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point point = (Point) obj;
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
    }

    public int hashCode() {
        return Double.hashCode(x) * 31 + Double.hashCode(y);
    }
}