package io;

import functions.TabulatedFunction;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

/**
 * Класс для записи табулированных функций в файлы
 */
public class TabulatedFunctionFileOutputStream {

    public static void main(String[] args) {
        // Проверяем существование папки output
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            if (outputDir.mkdir()) {
                System.out.println("Создана папка output");
            } else {
                System.err.println("Не удалось создать папку output");
                return;
            }
        }

        System.out.println("Текущая рабочая директория: " + System.getProperty("user.dir"));

        // Создаем две табулированные функции

        // Функция на основе массива: y = x для простоты
        double[] xValuesArray = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValuesArray = {0.0, 1.0, 2.0, 3.0, 4.0};
        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValuesArray, yValuesArray);

        // Функция на основе связного списка: y = x^2
        double[] xValuesList = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValuesList = {0.0, 1.0, 4.0, 9.0, 16.0};
        TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValuesList, yValuesList);

        System.out.println("Начало записи файлов...");

        // Используем одну конструкцию try-with-resources для обоих потоков
        try (FileOutputStream fos1 = new FileOutputStream("output/array function.bin");
             FileOutputStream fos2 = new FileOutputStream("output/linked list function.bin");
             BufferedOutputStream bos1 = new BufferedOutputStream(fos1);
             BufferedOutputStream bos2 = new BufferedOutputStream(fos2)) {

            System.out.println("Потоки созданы успешно");

            // ЗАПИСЫВАЕМ ФУНКЦИИ С ПОМОЩЬЮ FunctionsIO.writeTabulatedFunction
            FunctionsIO.writeTabulatedFunction(bos1, arrayFunction);
            System.out.println("Array function записана");

            FunctionsIO.writeTabulatedFunction(bos2, linkedListFunction);
            System.out.println("Linked list function записана");

            System.out.println("Функции успешно записаны в файлы:");
            System.out.println("- output/array function.bin");
            System.out.println("- output/linked list function.bin");

            // Проверяем размеры файлов
            File file1 = new File("output/array function.bin");
            File file2 = new File("output/linked list function.bin");
            System.out.println("Размер array function.bin: " + file1.length() + " байт");
            System.out.println("Размер linked list function.bin: " + file2.length() + " байт");

        } catch (IOException e) {
            // Обрабатываем исключение - выводим стектрейс в поток ошибок
            System.err.println("Ошибка при записи файлов:");
            e.printStackTrace();
        }
    }
}