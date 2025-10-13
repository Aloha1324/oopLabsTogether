package io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;

public class TabulatedFunctionFileWriter {

    public static void main(String[] args) {
        // Создаем две табулированные функции

        // Функция на основе массива
        double[] xValues1 = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValues1 = {0.0, 1.0, 4.0, 9.0, 16.0}; // y = x^2
        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues1, yValues1);

        // Функция на основе связного списка
        double[] xValues2 = {-2.0, -1.0, 0.0, 1.0, 2.0};
        double[] yValues2 = {4.0, 1.0, 0.0, 1.0, 4.0}; // y = x^2
        TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValues2, yValues2);

        // Используем try-with-resources для двух потоков
        try (FileWriter fileWriter1 = new FileWriter("output/array function.txt");
             BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
             FileWriter fileWriter2 = new FileWriter("output/linked list function.txt");
             BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2)) {

            // Записываем функции в соответствующие файлы
            FunctionsIO.writeTabulatedFunction(bufferedWriter1, arrayFunction);
            FunctionsIO.writeTabulatedFunction(bufferedWriter2, linkedListFunction);

            System.out.println("Функции успешно записаны в файлы!");

        } catch (IOException e) {
            // Обрабатываем исключение - выводим стектрейс в поток ошибок
            e.printStackTrace();
        }
    }
}