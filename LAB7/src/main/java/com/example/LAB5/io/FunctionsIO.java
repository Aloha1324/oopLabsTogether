package com.example.LAB5.io;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.functions.factory.TabulatedFunctionFactory;
import com.example.LAB5.functions.Point;

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
     * Сериализует функцию и записывает в буферизованный байтовый поток
     * @param stream буферизованный байтовый поток
     * @param function табулированная функция для сериализации
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
        objectOutputStream.writeObject(function);
        objectOutputStream.flush();
    }

    /**
     * Десериализует функцию из буферизованного байтового потока
     * @param stream буферизованный байтовый поток
     * @return десериализованная табулированная функция
     * @throws IOException если произошла ошибка ввода-вывода
     * @throws ClassNotFoundException если класс не найден при десериализации
     */
    public static TabulatedFunction deserialize(BufferedInputStream stream) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(stream);
        return (TabulatedFunction) objectInputStream.readObject();
    }

    /**
     * Записывает табулированную функцию в буферизованный байтовый поток.
     * outputStream буферизованный выходной поток
     *function табулированная функция для записи
     * IOException если происходит ошибка ввода-вывода
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

    /**
     * Читает табулированную функцию из буферизованного байтового потока.
     *
     * @param inputStream буферизованный входной поток
     * @param factory фабрика для создания функций
     * @return созданная табулированная функция
     * @throws IOException если происходит ошибка ввода-вывода
     */
    public static TabulatedFunction readTabulatedFunction(BufferedInputStream inputStream, TabulatedFunctionFactory factory) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // Считываем количество точек
        int count = dataInputStream.readInt();

        // Создаем массивы для x и y значений
        double[] xValues = new double[count];
        double[] yValues = new double[count];

        // Считываем все точки (x, y)
        for (int i = 0; i < count; i++) {
            xValues[i] = dataInputStream.readDouble();
            yValues[i] = dataInputStream.readDouble();
        }

        // Создаем функцию с помощью фабрики
        return factory.create(xValues, yValues);
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
        for (Point point : function) {
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