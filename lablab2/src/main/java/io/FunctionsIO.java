package io;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import functions.TabulatedFunction;

/**
 * Класс для операций ввода-вывода функций.
 * Не может иметь наследников и экземпляров.
 */
public final class FunctionsIO {

    /**
     * Исключение, указывающее, что операция не поддерживается.
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
     */
    private FunctionsIO() {
        throw new UnsupportedOperationException("Создание экземпляров класса FunctionsIO запрещено");
    }

    /**
     * Записывает представление функции в буферизованный символьный поток
     * @param writer буферизованный поток записи
     * @param function табулированная функция для записи
     */
    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) {
        PrintWriter printWriter = new PrintWriter(writer);

        // Записываем количество точек
        printWriter.println(function.getCount());

        // Записываем все точки функции
        for (functions.Point point : function) {
            printWriter.printf("%f %f\n", point.x, point.y);
        }

        // Сбрасываем буфер, но не закрываем поток
        printWriter.flush();
    }

    /**
     * Пример статического метода для работы с функциями.
     */
    public static double processFunction(double input) {
        return input * input;
    }

    /**
     * Другой пример статического метода.
     */
    public static String formatFunction(String functionData) {
        return "Function: " + functionData;
    }
}