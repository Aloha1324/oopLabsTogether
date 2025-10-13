package io;

import java.io.*;
import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import operations.TabulatedDifferentialOperator;

public class ArrayTabulatedFunctionSerialization {

    public static void main(String[] args) {
        // Создаем папку output если не существует
        java.io.File outputDir = new java.io.File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Создаем исходную функцию
        double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0}; // y = x^2
        ArrayTabulatedFunction originalFunction = new ArrayTabulatedFunction(xValues, yValues);

        // Находим производные
        TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
        TabulatedFunction firstDerivative = differentialOperator.derive(originalFunction);
        TabulatedFunction secondDerivative = differentialOperator.derive(firstDerivative);

        // Сериализация функций
        try (FileOutputStream fileOutputStream = new FileOutputStream("output/serialized array functions.bin");
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

            // Сериализуем все три функции
            FunctionsIO.serialize(bufferedOutputStream, originalFunction);
            FunctionsIO.serialize(bufferedOutputStream, firstDerivative);
            FunctionsIO.serialize(bufferedOutputStream, secondDerivative);

            System.out.println("Функции успешно сериализованы в файл: output/serialized array functions.bin");

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Десериализация функций
        try (FileInputStream fileInputStream = new FileInputStream("output/serialized array functions.bin");
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            // Десериализуем все три функции
            TabulatedFunction deserializedOriginal = FunctionsIO.deserialize(bufferedInputStream);
            TabulatedFunction deserializedFirstDerivative = FunctionsIO.deserialize(bufferedInputStream);
            TabulatedFunction deserializedSecondDerivative = FunctionsIO.deserialize(bufferedInputStream);

            // Выводим значения функций
            System.out.println("\n=== Десериализованные функции ===");
            System.out.println("Исходная функция:");
            System.out.println(deserializedOriginal.toString());
            System.out.println("\nПервая производная:");
            System.out.println(deserializedFirstDerivative.toString());
            System.out.println("\nВторая производная:");
            System.out.println(deserializedSecondDerivative.toString());

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}