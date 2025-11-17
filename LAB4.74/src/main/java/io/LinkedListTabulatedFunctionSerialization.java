package io;

import functions.TabulatedFunction;
import functions.LinkedListTabulatedFunction;
import operations.TabulatedDifferentialOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class LinkedListTabulatedFunctionSerialization {

    private static final Logger logger = LoggerFactory.getLogger(LinkedListTabulatedFunctionSerialization.class);

    public static void main(String[] args) {
        logger.info("Запуск LinkedListTabulatedFunctionSerialization");

        serializeFunctions();
        deserializeFunctions();

        logger.info("LinkedListTabulatedFunctionSerialization завершен");
    }

    private static void serializeFunctions() {
        logger.info("=== НАЧАЛО СЕРИАЛИЗАЦИИ ФУНКЦИЙ ===");

        File outputDir = new File("output");
        if (!outputDir.exists()) {
            logger.debug("Попытка создать директорию output");
            if (outputDir.mkdir()) {
                logger.info("Директория output создана");
            } else {
                logger.error("Не удалось создать директорию output");
                return;
            }
        }

        try (FileOutputStream fos = new FileOutputStream("output/serialized linked list functions.bin");
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            logger.debug("Создание исходной функции");
            double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
            double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0};
            TabulatedFunction originalFunction = new LinkedListTabulatedFunction(xValues, yValues);
            logger.info("Создана исходная функция с {} точками", originalFunction.getCount());

            logger.debug("Вычисление производных");
            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();

            logger.info("Вычисление первой производной");
            TabulatedFunction firstDerivative = differentialOperator.derive(originalFunction);
            logger.debug("Первая производная вычислена, количество точек: {}", firstDerivative.getCount());

            logger.info("Вычисление второй производной");
            TabulatedFunction secondDerivative = differentialOperator.derive(firstDerivative);
            logger.debug("Вторая производная вычислена, количество точек: {}", secondDerivative.getCount());

            logger.debug("Информация о функциях перед сериализацией:");
            logger.debug("Исходная функция: {}", originalFunction.toString());
            logger.debug("Первая производная: {}", firstDerivative.toString());
            logger.debug("Вторая производная: {}", secondDerivative.toString());

            logger.info("Начало сериализации функций");

            logger.debug("Сериализация исходной функции");
            FunctionsIO.serialize(bos, originalFunction);
            logger.debug("Исходная функция сериализована");

            logger.debug("Сериализация первой производной");
            FunctionsIO.serialize(bos, firstDerivative);
            logger.debug("Первая производная сериализована");

            logger.debug("Сериализация второй производной");
            FunctionsIO.serialize(bos, secondDerivative);
            logger.debug("Вторая производная сериализована");

            File file = new File("output/serialized linked list functions.bin");
            logger.info("Функции успешно сериализованы в файл: {}, размер: {} байт",
                    file.getAbsolutePath(), file.length());

        } catch (IOException e) {
            logger.error("Ошибка при сериализации функций", e);
        }
    }

    private static void deserializeFunctions() {
        logger.info("=== НАЧАЛО ДЕСЕРИАЛИЗАЦИИ ФУНКЦИЙ ===");

        File file = new File("output/serialized linked list functions.bin");
        if (!file.exists()) {
            logger.error("Файл не найден: {}", file.getAbsolutePath());
            return;
        }

        try (FileInputStream fis = new FileInputStream("output/serialized linked list functions.bin");
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            logger.info("Десериализация функций из файла");

            logger.debug("Десериализация исходной функции");
            TabulatedFunction deserializedOriginal = FunctionsIO.deserialize(bis);
            logger.debug("Исходная функция десериализована, тип: {}, точки: {}",
                    deserializedOriginal.getClass().getSimpleName(), deserializedOriginal.getCount());

            logger.debug("Десериализация первой производной");
            TabulatedFunction deserializedFirstDerivative = FunctionsIO.deserialize(bis);
            logger.debug("Первая производная десериализована, тип: {}, точки: {}",
                    deserializedFirstDerivative.getClass().getSimpleName(), deserializedFirstDerivative.getCount());

            logger.debug("Десериализация второй производной");
            TabulatedFunction deserializedSecondDerivative = FunctionsIO.deserialize(bis);
            logger.debug("Вторая производная десериализована, тип: {}, точки: {}",
                    deserializedSecondDerivative.getClass().getSimpleName(), deserializedSecondDerivative.getCount());

            logger.info("Все функции успешно десериализованы");

            logger.debug("Вывод информации о десериализованных функциях:");
            logger.debug("Исходная функция: {}", deserializedOriginal.toString());
            logger.debug("Первая производная: {}", deserializedFirstDerivative.toString());
            logger.debug("Вторая производная: {}", deserializedSecondDerivative.toString());

            logger.debug("Детальная информация о точках второй производной:");
            for (int i = 0; i < deserializedSecondDerivative.getCount(); i++) {
                double x = deserializedSecondDerivative.getX(i);
                double y = deserializedSecondDerivative.getY(i);
                logger.debug("Точка {}: x = {:.2f}, y = {:.2f}", i, x, y);
            }

        } catch (IOException e) {
            logger.error("Ошибка ввода-вывода при десериализации", e);
        } catch (ClassNotFoundException e) {
            logger.error("Класс не найден при десериализации", e);
        }
    }
}