package com.example.LAB5.DTO.Request;

public class SearchRequest {
    private String name;
    private String type;
    private Long userId;
    private String username;
    private Double minX;
    private Double maxX;
    private Double minY;
    private Double maxY;
    private Integer minPoints;
    private Integer maxPoints;
    private String createdAfter;
    private String createdBefore;
    private String updatedAfter;
    private String updatedBefore;
    private String expressionContains;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    public String getName() { return name; }
    public String getType() { return type; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Double getMinX() { return minX; }
    public Double getMaxX() { return maxX; }
    public Double getMinY() { return minY; }
    public Double getMaxY() { return maxY; }
    public Integer getMinPoints() { return minPoints; }
    public Integer getMaxPoints() { return maxPoints; }
    public String getCreatedAfter() { return createdAfter; }
    public String getCreatedBefore() { return createdBefore; }
    public String getUpdatedAfter() { return updatedAfter; }
    public String getUpdatedBefore() { return updatedBefore; }
    public String getExpressionContains() { return expressionContains; }
    public Integer getPage() { return page; }
    public Integer getSize() { return size; }
    public String getSortBy() { return sortBy; }
    public String getSortDirection() { return sortDirection; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setMinX(Double minX) { this.minX = minX; }
    public void setMaxX(Double maxX) { this.maxX = maxX; }
    public void setMinY(Double minY) { this.minY = minY; }
    public void setMaxY(Double maxY) { this.maxY = maxY; }
    public void setMinPoints(Integer minPoints) { this.minPoints = minPoints; }
    public void setMaxPoints(Integer maxPoints) { this.maxPoints = maxPoints; }
    public void setCreatedAfter(String createdAfter) { this.createdAfter = createdAfter; }
    public void setCreatedBefore(String createdBefore) { this.createdBefore = createdBefore; }
    public void setUpdatedAfter(String updatedAfter) { this.updatedAfter = updatedAfter; }
    public void setUpdatedBefore(String updatedBefore) { this.updatedBefore = updatedBefore; }
    public void setExpressionContains(String expressionContains) { this.expressionContains = expressionContains; }
    public void setPage(Integer page) { this.page = page; }
    public void setSize(Integer size) { this.size = size; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}