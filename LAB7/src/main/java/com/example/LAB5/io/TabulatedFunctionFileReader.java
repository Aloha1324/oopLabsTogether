package com.example.LAB5.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.example.LAB5.functions.ArrayTabulatedFunction;
import com.example.LAB5.functions.LinkedListTabulatedFunction;
import com.example.LAB5.functions.MathFunction;
import com.example.LAB5.functions.TabulatedFunction;
import com.example.LAB5.functions.factory.TabulatedFunctionFactory;

public class TabulatedFunctionFileReader {

    public static void main(String[] args) {
        try (FileReader fileReader1 = new FileReader("input/function.txt");
             BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
             FileReader fileReader2 = new FileReader("input/function.txt");
             BufferedReader bufferedReader2 = new BufferedReader(fileReader2)) {

            // Фабрика для ArrayTabulatedFunction
            TabulatedFunctionFactory arrayFactory = new TabulatedFunctionFactory() {
                @Override
                public TabulatedFunction create(double[] xValues, double[] yValues) {
                    return new ArrayTabulatedFunction(xValues, yValues);
                }

                @Override
                public TabulatedFunction create(MathFunction function, double xFrom, double xTo, int count) {
                    throw new UnsupportedOperationException("File reading does not support MathFunction-based creation");
                }
            };

            // Фабрика для LinkedListTabulatedFunction
            TabulatedFunctionFactory linkedListFactory = new TabulatedFunctionFactory() {
                @Override
                public TabulatedFunction create(double[] xValues, double[] yValues) {
                    return new LinkedListTabulatedFunction(xValues, yValues);
                }

                @Override
                public TabulatedFunction create(MathFunction function, double xFrom, double xTo, int count) {
                    throw new UnsupportedOperationException("File reading does not support MathFunction-based creation");
                }
            };

            TabulatedFunction arrayFunction = FunctionsIO.readTabulatedFunction(bufferedReader1, arrayFactory);
            System.out.println("ArrayTabulatedFunction:");
            System.out.println(arrayFunction.toString());
            System.out.println();

            TabulatedFunction linkedListFunction = FunctionsIO.readTabulatedFunction(bufferedReader2, linkedListFactory);
            System.out.println("LinkedListTabulatedFunction:");
            System.out.println(linkedListFunction.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}