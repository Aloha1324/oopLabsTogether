package com.example.LAB5.DTO.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * DTO для запроса на вычисление определённого интеграла
 * табулированной функции на всём её интервале.
 */
public class IntegrationRequest {

    @NotNull(message = "ID функции обязателен")
    private Long functionId;

    @Min(value = 1, message = "Минимум 1 поток")
    private Integer threadCount = 1; // по умолчанию — 1 поток

    // Геттеры и сеттеры

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    /**
     * Возвращает количество потоков для параллельного вычисления.
     * Значение автоматически ограничивается в диапазоне [1, 16].
     *
     * @return количество потоков (от 1 до 16)
     */
    public int getThreadCount() {
        if (threadCount == null) {
            return 1;
        }
        return Math.min(16, Math.max(1, threadCount));
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    // equals / hashCode / toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegrationRequest that = (IntegrationRequest) o;
        return Objects.equals(functionId, that.functionId) &&
                Objects.equals(threadCount, that.threadCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionId, threadCount);
    }

    @Override
    public String toString() {
        return "IntegrationRequest{" +
                "functionId=" + functionId +
                ", threadCount=" + getThreadCount() +
                '}';
    }
}