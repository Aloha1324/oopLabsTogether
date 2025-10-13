package io;

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

    // Статические методы для работы с функциями

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