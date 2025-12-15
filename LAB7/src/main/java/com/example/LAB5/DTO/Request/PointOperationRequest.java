package com.example.LAB5.DTO.Request;

import java.util.List;

public class PointOperationRequest {
    private Long functionId;
    private String operation = "ADD";
    private List<PointData> points;
    private List<Long> pointIds;
    private Double minX;
    private Double maxX;
    private Double minY;
    private Double maxY;

    public Long getFunctionId() { return functionId; }
    public String getOperation() { return operation; }
    public List<PointData> getPoints() { return points; }
    public List<Long> getPointIds() { return pointIds; }
    public Double getMinX() { return minX; }
    public Double getMaxX() { return maxX; }
    public Double getMinY() { return minY; }
    public Double getMaxY() { return maxY; }

    public void setFunctionId(Long functionId) { this.functionId = functionId; }
    public void setOperation(String operation) { this.operation = operation; }
    public void setPoints(List<PointData> points) { this.points = points; }
    public void setPointIds(List<Long> pointIds) { this.pointIds = pointIds; }
    public void setMinX(Double minX) { this.minX = minX; }
    public void setMaxX(Double maxX) { this.maxX = maxX; }
    public void setMinY(Double minY) { this.minY = minY; }
    public void setMaxY(Double maxY) { this.maxY = maxY; }

    public static class PointData {
        private Double x;
        private Double y;
        private Integer orderIndex;
        private Long id;

        public Double getX() { return x; }
        public Double getY() { return y; }
        public Integer getOrderIndex() { return orderIndex; }
        public Long getId() { return id; }

        public void setX(Double x) { this.x = x; }
        public void setY(Double y) { this.y = y; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
        public void setId(Long id) { this.id = id; }
    }
}