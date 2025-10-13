package io;

import functions.TabulatedFunction;
import functions.LinkedListTabulatedFunction;
import operations.TabulatedDifferentialOperator;
import java.io.*;

/**
 * Класс для сериализации и десериализации табулированных функций
 */
public class LinkedListTabulatedFunctionSerialization {

    public static void main(String[] args) {
        // Часть 1: Создание функций и их сериализация
        serializeFunctions();

        // Часть 2: Десериализация функций и вывод в консоль
        deserializeFunctions();
    }

    private static void serializeFunctions() {
        System.out.println("=== СЕРИАЛИЗАЦИЯ ФУНКЦИЙ ===");

        // Создаем папку output если её нет
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            if (outputDir.mkdir()) {
                System.out.println("Создана папка output");
            } else {
                System.err.println("Не удалось создать папку output");
                return;
            }
        }

        // Используем try-with-resources для файлового потока записи
        try (FileOutputStream fos = new FileOutputStream("output/serialized linked list functions.bin");
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            System.out.println("Создание исходной функции...");

            // Создаем исходную табулированную функцию типа LinkedListTabulatedFunction
            double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
            double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0}; // y = x^2
            TabulatedFunction originalFunction = new LinkedListTabulatedFunction(xValues, yValues);

            System.out.println("Вычисление производных...");

            // Находим первую и вторую производные
            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
            TabulatedFunction firstDerivative = differentialOperator.derive(originalFunction);
            TabulatedFunction secondDerivative = differentialOperator.derive(firstDerivative);

            // Выводим информацию о функциях перед сериализацией
            System.out.println("Исходная функция: " + originalFunction.toString());
            System.out.println("Первая производная: " + firstDerivative.toString());
            System.out.println("Вторая производная: " + secondDerivative.toString());

            System.out.println("Сериализация функций в файл...");

            // Сериализуем все три функции в поток
            FunctionsIO.serialize(bos, originalFunction);
            FunctionsIO.serialize(bos, firstDerivative);
            FunctionsIO.serialize(bos, secondDerivative);

            System.out.println("Функции успешно сериализованы в файл: output/serialized linked list functions.bin");
            System.out.println("Размер файла: " + new File("output/serialized linked list functions.bin").length() + " байт");

        } catch (IOException e) {
            System.err.println("Ошибка при сериализации функций:");
            e.printStackTrace();
        }
    }

    private static void deserializeFunctions() {
        System.out.println("\n=== ДЕСЕРИАЛИЗАЦИЯ ФУНКЦИЙ ===");

        File file = new File("output/serialized linked list functions.bin");
        if (!file.exists()) {
            System.err.println("Файл не найден: " + file.getAbsolutePath());
            System.err.println("Сначала выполните сериализацию функций");
            return;
        }

        // Используем try-with-resources для файлового потока чтения
        try (FileInputStream fis = new FileInputStream("output/serialized linked list functions.bin");
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            System.out.println("Десериализация функций из файла...");

            // Десериализуем все три функции из файла
            TabulatedFunction deserializedOriginal = FunctionsIO.deserialize(bis);
            TabulatedFunction deserializedFirstDerivative = FunctionsIO.deserialize(bis);
            TabulatedFunction deserializedSecondDerivative = FunctionsIO.deserialize(bis);

            // Выводим значения всех функций в консоль
            System.out.println("\n=== ДЕСЕРИАЛИЗОВАННЫЕ ФУНКЦИИ ===");

            System.out.println("1. Исходная функция:");
            System.out.println(deserializedOriginal.toString());
            System.out.println("Тип: " + deserializedOriginal.getClass().getSimpleName());
            System.out.println("Количество точек: " + deserializedOriginal.getCount());

            System.out.println("\n2. Первая производная:");
            System.out.println(deserializedFirstDerivative.toString());
            System.out.println("Тип: " + deserializedFirstDerivative.getClass().getSimpleName());
            System.out.println("Количество точек: " + deserializedFirstDerivative.getCount());

            System.out.println("\n3. Вторая производная:");
            System.out.println(deserializedSecondDerivative.toString());
            System.out.println("Тип: " + deserializedSecondDerivative.getClass().getSimpleName());
            System.out.println("Количество точек: " + deserializedSecondDerivative.getCount());

            // Дополнительная проверка: выводим точки второй производной
            System.out.println("\n=== ТОЧКИ ВТОРОЙ ПРОИЗВОДНОЙ ===");
            for (int i = 0; i < deserializedSecondDerivative.getCount(); i++) {
                double x = deserializedSecondDerivative.getX(i);
                double y = deserializedSecondDerivative.getY(i);
                System.out.printf("Точка %d: x = %.2f, y = %.2f%n", i, x, y);
            }

        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода при десериализации:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Ошибка: класс не найден при десериализации:");
            e.printStackTrace();
        }
    }
}