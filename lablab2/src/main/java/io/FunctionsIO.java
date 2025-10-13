package io;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;

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
     * Читает данные из буферизованного потока и создает функцию с помощью фабрики
     * @param reader буферизованный поток чтения
     * @param factory фабрика для создания функции
     * @return созданная табулированная функция
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static TabulatedFunction readTabulatedFunction(BufferedReader reader,
                                                          TabulatedFunctionFactory factory) throws IOException {
        try {
            // Читаем первую строку - количество точек
            String countLine = reader.readLine();
            int count = Integer.parseInt(countLine);

            // Создаем массивы для хранения значений
            double[] xValues = new double[count];
            double[] yValues = new double[count];

            // Создаем форматтер для чисел с запятой в качестве разделителя
            NumberFormat formatter = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

            // Читаем остальные строки с данными точек
            for (int i = 0; i < count; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Неожиданный конец файла: ожидалось " + count + " точек, но получено только " + i);
                }

                // Разбиваем строку на две части по пробелу
                String[] parts = line.split(" ");
                if (parts.length != 2) {
                    throw new IOException("Некорректный формат строки: " + line);
                }

                // Парсим значения x и y
                try {
                    Number xNumber = formatter.parse(parts[0]);
                    Number yNumber = formatter.parse(parts[1]);
                    xValues[i] = xNumber.doubleValue();
                    yValues[i] = yNumber.doubleValue();
                } catch (ParseException e) {
                    // Оборачиваем ParseException в IOException
                    throw new IOException("Ошибка парсинга чисел в строке: " + line, e);
                }
            }

            // Создаем и возвращаем функцию с помощью фабрики
            return factory.create(xValues, yValues);

        } catch (NumberFormatException e) {
            throw new IOException("Ошибка формата числа", e);
        }
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