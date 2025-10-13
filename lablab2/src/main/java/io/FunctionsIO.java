package io;

import functions.TabulatedFunction;
import functions.Point;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Класс для операций ввода-вывода функций.
 * Не может иметь наследников и экземпляров.
 */
public final class FunctionsIO {

    /**
     * Исключение, указывающее, что операция не поддерживается.
     * Создано внутри класса FunctionsIO как вложенный класс.
     */
    public static class UnsupportedOperationException extends RuntimeException {

        public UnsupportedOperationException() {
            super();
        }

        public UnsupportedOperationException(String message) {
            super(message);
        }

        public UnsupportedOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Приватный конструктор, который предотвращает создание экземпляров класса.
     * Бросает UnsupportedOperationException при попытке вызова.
     *
     * @throws UnsupportedOperationException всегда, при попытке создания экземпляра
     */
    private FunctionsIO() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Создание экземпляров класса FunctionsIO запрещено");
    }


    /**
     * Записывает табулированную функцию в буферизованный байтовый поток.
     *
     * @param outputStream буферизованный выходной поток
     * @param function табулированная функция для записи
     * @throws IOException если происходит ошибка ввода-вывода
     */
    public static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        // Записываем количество точек
        dataOutputStream.writeInt(function.getCount());

        // Записываем все точки (x, y) с помощью цикла for-each
        for (Point point : function) {
            dataOutputStream.writeDouble(point.x);
            dataOutputStream.writeDouble(point.y);
        }

        // Сбрасываем буфер, но не закрываем поток
        outputStream.flush();
    }

    // Существующие статические методы остаются без изменений
    /**
     * Пример статического метода для работы с функциями.
     *
     * @param input входные данные функции
     * @return результат вычисления
     */
    public static double processFunction(double input) {
        // Реализация обработки функции
        return input * input; // пример реализации
    }

    /**
     * Другой пример статического метода.
     *
     * @param functionData данные функции
     * @return обработанные данные
     */
    public static String formatFunction(String functionData) {
        return "Function: " + functionData;
    }
}