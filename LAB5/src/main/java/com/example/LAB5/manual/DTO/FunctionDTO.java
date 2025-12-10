package com.example.LAB5.manual.DTO;

import java.util.Objects;

public class FunctionDTO {

    private Long id;
    private Long userId;
    private String name;
    private String signature;

    public FunctionDTO() {
    }

    public FunctionDTO(Long userId, String name, String signature) {
        this.userId = userId;
        this.name = name;
        this.signature = signature;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FunctionDTO)) {
            return false;
        }
        FunctionDTO that = (FunctionDTO) other;
        return Objects.equals(id, that.id)
                && Objects.equals(userId, that.userId)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, name);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FunctionDTO{");
        sb.append("id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", signature='").append(signature).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
