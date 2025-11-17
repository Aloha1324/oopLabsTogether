package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.factory.TabulatedFunctionFactory;

public class TabulatedFunctionFileReader {

    public static void main(String[] args) {
        // Используем try-with-resources для двух потоков чтения из одного файла
        try (FileReader fileReader1 = new FileReader("input/function.txt");
             BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
             FileReader fileReader2 = new FileReader("input/function.txt");
             BufferedReader bufferedReader2 = new BufferedReader(fileReader2)) {

            // Создаем фабрику для ArrayTabulatedFunction
            TabulatedFunctionFactory arrayFactory = new TabulatedFunctionFactory() {
                @Override
                public TabulatedFunction create(double[] xValues, double[] yValues) {
                    return new ArrayTabulatedFunction(xValues, yValues);
                }
            };

            // Создаем фабрику для LinkedListTabulatedFunction
            TabulatedFunctionFactory linkedListFactory = new TabulatedFunctionFactory() {
                @Override
                public TabulatedFunction create(double[] xValues, double[] yValues) {
                    return new LinkedListTabulatedFunction(xValues, yValues);
                }
            };

            // Читаем функцию с реализацией в виде массива
            TabulatedFunction arrayFunction = FunctionsIO.readTabulatedFunction(bufferedReader1, arrayFactory);
            System.out.println("ArrayTabulatedFunction:");
            System.out.println(arrayFunction.toString());
            System.out.println();

            // Читаем функцию с реализацией в виде связного списка
            TabulatedFunction linkedListFunction = FunctionsIO.readTabulatedFunction(bufferedReader2, linkedListFactory);
            System.out.println("LinkedListTabulatedFunction:");
            System.out.println(linkedListFunction.toString());

        } catch (IOException e) {
            // Обрабатываем исключение - выводим стектрейс в поток ошибок
            e.printStackTrace();
        }
    }
}