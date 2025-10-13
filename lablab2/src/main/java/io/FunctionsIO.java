package io;

<<<<<<< HEAD
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;
=======
import functions.TabulatedFunction;
import functions.Point;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
>>>>>>> 9099a5cca8d733f33a7148691e6bc45c28783f70

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