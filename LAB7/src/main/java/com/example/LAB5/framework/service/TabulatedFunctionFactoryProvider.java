package com.example.LAB5.framework.service;

import com.example.LAB5.functions.factory.ArrayTabulatedFunctionFactory;
import com.example.LAB5.functions.factory.LinkedListTabulatedFunctionFactory;
import com.example.LAB5.functions.factory.TabulatedFunctionFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TabulatedFunctionFactoryProvider {

    private final Map<String, TabulatedFunctionFactory> factoryTypes;
    private final Map<String, String> userFactoryMap = new ConcurrentHashMap<>();
    private static final String DEFAULT_FACTORY = "array";

    public TabulatedFunctionFactoryProvider(
            ArrayTabulatedFunctionFactory arrayFactory,
            LinkedListTabulatedFunctionFactory linkedListFactory) {
        this.factoryTypes = Map.of(
                "array", arrayFactory,
                "linked-list", linkedListFactory
        );
    }

    // Получить фабрику для конкретного пользователя
    public TabulatedFunctionFactory getFactoryForUser(String username) {
        String type = userFactoryMap.getOrDefault(username, DEFAULT_FACTORY);
        TabulatedFunctionFactory factory = factoryTypes.get(type);
        if (factory == null) {
            throw new IllegalStateException("Неизвестный тип фабрики: " + type);
        }
        return factory;
    }

    // Установить фабрику для пользователя
    public void setFactoryForUser(String username, String type) {
        if (!factoryTypes.containsKey(type)) {
            throw new IllegalArgumentException("Неизвестный тип фабрики: " + type +
                    ". Допустимые значения: " + String.join(", ", factoryTypes.keySet()));
        }
        userFactoryMap.put(username, type);
    }

    // Получить текущий тип фабрики пользователя (для API)
    public String getCurrentTypeForUser(String username) {
        return userFactoryMap.getOrDefault(username, DEFAULT_FACTORY);
    }
}