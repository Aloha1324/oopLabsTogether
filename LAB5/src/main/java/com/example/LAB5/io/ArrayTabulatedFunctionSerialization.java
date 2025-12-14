package com.example.LAB5.io;

import java.io.*;
import com.example.LAB5.functions.ArrayTabulatedFunction;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.operations.TabulatedDifferentialOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayTabulatedFunctionSerialization {

    private static final Logger logger = LoggerFactory.getLogger(ArrayTabulatedFunctionSerialization.class);

    public static void main(String[] args) {
        logger.info("Запуск ArrayTabulatedFunctionSerialization");

        java.io.File outputDir = new java.io.File("output");
        if (!outputDir.exists()) {
            logger.debug("Создание директории output");
            outputDir.mkdirs();
            logger.info("Директория output создана");
        }

        double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0};
        logger.debug("Создание исходной функции: xValues={}, yValues={}", xValues, yValues);

        ArrayTabulatedFunction originalFunction = new ArrayTabulatedFunction(xValues, yValues);
        logger.info("Создана исходная функция с {} точками", originalFunction.getCount());

        TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
        logger.debug("Создание TabulatedDifferentialOperator");

        logger.info("Вычисление первой производной");
        TabulatedFunction firstDerivative = differentialOperator.derive(originalFunction);
        logger.debug("Первая производная вычислена, количество точек: {}", firstDerivative.getCount());

        logger.info("Вычисление второй производной");
        TabulatedFunction secondDerivative = differentialOperator.derive(firstDerivative);
        logger.debug("Вторая производная вычислена, количество точек: {}", secondDerivative.getCount());

        logger.info("Начало сериализации функций");
        try (FileOutputStream fileOutputStream = new FileOutputStream("output/serialized array functions.bin");
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

            logger.debug("Сериализация исходной функции");
            FunctionsIO.serialize(bufferedOutputStream, originalFunction);
            logger.debug("Исходная функция сериализована");

            logger.debug("Сериализация первой производной");
            FunctionsIO.serialize(bufferedOutputStream, firstDerivative);
            logger.debug("Первая производная сериализована");

            logger.debug("Сериализация второй производной");
            FunctionsIO.serialize(bufferedOutputStream, secondDerivative);
            logger.debug("Вторая производная сериализована");

            logger.info("Все функции успешно сериализованы в файл: output/serialized array functions.bin");
            System.out.println("Функции успешно сериализованы в файл: output/serialized array functions.bin");

        } catch (IOException e) {
            logger.error("Ошибка при сериализации функций", e);
            e.printStackTrace();
        }

        logger.info("Начало десериализации функций");
        try (FileInputStream fileInputStream = new FileInputStream("output/serialized array functions.bin");
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            logger.debug("Десериализация исходной функции");
            TabulatedFunction deserializedOriginal = FunctionsIO.deserialize(bufferedInputStream);
            logger.debug("Исходная функция десериализована, количество точек: {}", deserializedOriginal.getCount());

            logger.debug("Десериализация первой производной");
            TabulatedFunction deserializedFirstDerivative = FunctionsIO.deserialize(bufferedInputStream);
            logger.debug("Первая производная десериализована, количество точек: {}", deserializedFirstDerivative.getCount());

            logger.debug("Десериализация второй производной");
            TabulatedFunction deserializedSecondDerivative = FunctionsIO.deserialize(bufferedInputStream);
            logger.debug("Вторая производная десериализована, количество точек: {}", deserializedSecondDerivative.getCount());

            logger.info("Все функции успешно десериализованы");

            System.out.println("\n=== Десериализованные функции ===");
            System.out.println("Исходная функция:");
            System.out.println(deserializedOriginal.toString());
            System.out.println("\nПервая производная:");
            System.out.println(deserializedFirstDerivative.toString());
            System.out.println("\nВторая производная:");
            System.out.println(deserializedSecondDerivative.toString());

            logger.debug("Вывод десериализованных функций завершен");

        } catch (IOException e) {
            logger.error("Ошибка ввода-вывода при десериализации", e);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            logger.error("Класс не найден при десериализации", e);
            e.printStackTrace();
        }

        logger.info("ArrayTabulatedFunctionSerialization завершен");
    }
}