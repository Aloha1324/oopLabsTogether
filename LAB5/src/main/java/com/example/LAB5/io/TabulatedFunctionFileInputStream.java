package com.example.LAB5.io;

import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.functions.factory.TabulatedFunctionFactory;
import com.example.LAB5.functions.ArrayTabulatedFunction;
import com.example.LAB5.functions.LinkedListTabulatedFunction;
import com.example.LAB5.operations.TabulatedDifferentialOperator;
import java.io.*;

/**
 * Класс для чтения табулированных функций из файлов и консоли
 */
public class TabulatedFunctionFileInputStream {

    public static void main(String[] args) {
        // Часть 1: Чтение функции из бинарного файла
        readFunctionFromFile();

        // Часть 2: Чтение функции из консоли и вычисление производной
        readFunctionFromConsole();
    }

    private static void readFunctionFromFile() {
        // Создаем папку input если её нет
        File inputDir = new File("input");
        if (!inputDir.exists()) {
            if (inputDir.mkdir()) {
                System.out.println("Создана папка input");
            } else {
                System.err.println("Не удалось создать папку input");
                return;
            }
        }

        File file = new File("input/binary function.bin");
        if (!file.exists()) {
            System.err.println("Файл не найден: " + file.getAbsolutePath());
            System.err.println("Сначала создайте файл через CreateInputFile");
            return;
        }

        System.out.println("Чтение функции из файла: " + file.getAbsolutePath());

        // Используем try-with-resources для файлового потока
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            // Создаем фабрику для ArrayTabulatedFunction
            TabulatedFunctionFactory arrayFactory = new TabulatedFunctionFactory() {
                @Override
                public TabulatedFunction create(double[] xValues, double[] yValues) {
                    return new ArrayTabulatedFunction(xValues, yValues);
                }
            };

            // Читаем функцию из файла с помощью метода для BufferedInputStream
            TabulatedFunction function = FunctionsIO.readTabulatedFunction(bis, arrayFactory);

            // Выводим функцию
            System.out.println("Функция из файла:");
            System.out.println(function.toString());

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла:");
            e.printStackTrace();
        }
    }

    private static void readFunctionFromConsole() {
        System.out.println("\nВведите размер и значения функции");
        System.out.println("Формат ввода:");
        System.out.println("1. Первая строка - количество точек");
        System.out.println("2. Затем пары чисел x и y через пробел (по одной паре на строку)");
        System.out.println("Пример:");
        System.out.println("3");
        System.out.println("0.0 0.0");
        System.out.println("1.0 2.0");
        System.out.println("2.0 4.0");

        // Для работы с BufferedInputStream из консоли нужно использовать PipedInputStream
        // Но это сложно, поэтому используем временный файл как промежуточное решение

        File tempFile = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            // Создаем временный файл для бинарных данных
            tempFile = File.createTempFile("console_input", ".bin");
            tempFile.deleteOnExit(); // Удалим при завершении программы

            // Создаем потоки для чтения текста из консоли
            inputStreamReader = new InputStreamReader(System.in);
            bufferedReader = new BufferedReader(inputStreamReader);

            // Читаем количество точек из консоли
            String countLine = bufferedReader.readLine();
            int count = Integer.parseInt(countLine.trim());

            // Создаем массивы для данных
            double[] xValues = new double[count];
            double[] yValues = new double[count];

            // Читаем пары x, y из консоли
            for (int i = 0; i < count; i++) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    throw new IOException("Недостаточно данных. Ожидалось " + count + " точек");
                }

                String[] parts = line.trim().split("\\s+");
                if (parts.length < 2) {
                    throw new IOException("Неверный формат. Ожидались два числа через пробел: " + line);
                }

                xValues[i] = Double.parseDouble(parts[0]);
                yValues[i] = Double.parseDouble(parts[1]);
            }

            // Записываем данные во временный файл в бинарном формате
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos);
                 DataOutputStream dos = new DataOutputStream(bos)) {

                dos.writeInt(count);
                for (int i = 0; i < count; i++) {
                    dos.writeDouble(xValues[i]);
                    dos.writeDouble(yValues[i]);
                }
            }

            // Теперь читаем из временного файла через BufferedInputStream
            try (FileInputStream fis = new FileInputStream(tempFile);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                // Создаем фабрику для LinkedListTabulatedFunction
                TabulatedFunctionFactory linkedListFactory = new TabulatedFunctionFactory() {
                    @Override
                    public TabulatedFunction create(double[] xValues, double[] yValues) {
                        return new LinkedListTabulatedFunction(xValues, yValues);
                    }
                };

                // Используем метод readTabulatedFunction для BufferedInputStream
                TabulatedFunction function = FunctionsIO.readTabulatedFunction(bis, linkedListFactory);

                // Вычисляем производную с помощью TabulatedDifferentialOperator
                TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
                TabulatedFunction derivative = differentialOperator.derive(function);

                // Выводим производную
                System.out.println("Производная функции:");
                System.out.println(derivative.toString());
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении из консоли:");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Ошибка формата числа:");
            e.printStackTrace();
        } finally {
            // Удаляем временный файл
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }


        }
    }
}