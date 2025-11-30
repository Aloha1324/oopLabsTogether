package com.example.LAB5.manual.DTO;

import java.util.Objects;

public class FunctionDTO {
    private Long id;
    private Long userId;
    private String name;
    private String signature;

    public FunctionDTO() {}

    public FunctionDTO(Long userId, String name, String signature) {
        this.userId = userId;
        this.name = name;
        this.signature = signature;
    }

    public FunctionDTO(Long id, Long userId, String name, String signature) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.signature = signature;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionDTO that = (FunctionDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, name, signature);
    }

    @Override
    public String toString() {
        return "FunctionDTO{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}