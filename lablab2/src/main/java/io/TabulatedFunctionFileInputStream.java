package io;

import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import operations.TabulatedDifferentialOperator;
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

        // Используем try-with-resources для файлового потока
        try (FileInputStream fis = new FileInputStream("input/binary function.bin");
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            System.out.println("Чтение функции из файла...");

            // Создаем фабрику для ArrayTabulatedFunction
            TabulatedFunctionFactory arrayFactory = new TabulatedFunctionFactory() {
                @Override
                public TabulatedFunction create(double[] xValues, double[] yValues) {
                    return new ArrayTabulatedFunction(xValues, yValues);
                }
            };

            // Читаем функцию из файла с помощью нового метода
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

        // Не используем try-with-resources, так как нельзя закрывать System.in
        BufferedReader reader = null;

        try {
            // Создаем BufferedReader для чтения из консоли
            reader = new BufferedReader(new InputStreamReader(System.in));

            // Создаем фабрику для LinkedListTabulatedFunction
            TabulatedFunctionFactory linkedListFactory = new TabulatedFunctionFactory() {
                @Override
                public TabulatedFunction create(double[] xValues, double[] yValues) {
                    return new LinkedListTabulatedFunction(xValues, yValues);
                }
            };

            // Используем существующий метод readTabulatedFunction для BufferedReader
            TabulatedFunction function = FunctionsIO.readTabulatedFunction(reader, linkedListFactory);

            // Вычисляем производную
            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
            TabulatedFunction derivative = differentialOperator.derive(function);

            // Выводим производную
            System.out.println("Производная функции:");
            System.out.println(derivative.toString());

        } catch (IOException e) {
            System.err.println("Ошибка при чтении из консоли:");
            e.printStackTrace();
        } finally {
            // Не закрываем reader, так как он оборачивает System.in
            // System.in закрывать нельзя!
        }
    }
}