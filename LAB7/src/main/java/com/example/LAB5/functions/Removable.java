package com.example.LAB5.functions;

/**
 * Интерфейс для удаления элементов по индексу
 */
public interface Removable {
    /**
     * Удаляет элемент по указанному индексу
     * @param index индекс удаляемого элемента
     * @throws IllegalArgumentException если индекс выходит за границы списка
     */
    void remove(int index);
}