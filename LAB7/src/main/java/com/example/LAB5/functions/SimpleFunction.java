package com.example.LAB5.functions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для пометки простых математических функций,
 * которые могут использоваться для создания табулированных функций.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SimpleFunction {
    /**
     * Ключ локализованного названия (например, "function.sqr")
     */
    String name();

    /**
     * Приоритет отображения в списке (меньше — выше).
     * При равенстве приоритета — сортировка по локализованному имени.
     */
    int priority() default 0;
}