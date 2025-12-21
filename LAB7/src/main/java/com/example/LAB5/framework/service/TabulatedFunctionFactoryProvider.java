package com.example.LAB5.framework.service;

import com.example.LAB5.functions.factory.ArrayTabulatedFunctionFactory;
import com.example.LAB5.functions.factory.LinkedListTabulatedFunctionFactory;
import com.example.LAB5.functions.factory.TabulatedFunctionFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TabulatedFunctionFactoryProvider {

    private final Map<String, TabulatedFunctionFactory> factories;
    private volatile String currentType = "array"; // по умолчанию

    public TabulatedFunctionFactoryProvider(
            ArrayTabulatedFunctionFactory arrayFactory,
            LinkedListTabulatedFunctionFactory linkedListFactory) {
        this.factories = Map.of(
                "array", arrayFactory,
                "linked-list", linkedListFactory
        );
    }

    public TabulatedFunctionFactory getCurrentFactory() {
        return factories.get(currentType);
    }

    public String getCurrentType() {
        return currentType;
    }

    public void setFactoryType(String type) {
        if (!factories.containsKey(type)) {
            throw new IllegalArgumentException("Unknown factory type: " + type);
        }
        this.currentType = type;
    }
}