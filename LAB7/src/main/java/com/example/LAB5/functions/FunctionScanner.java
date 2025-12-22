package com.example.LAB5.functions;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.ResourceBundle;

@Component
public class FunctionScanner {

    private static final String BUNDLE_NAME = "messages";
    private static final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);

    public List<MathFunctionInfo> scanFunctions() {
        List<MathFunctionInfo> functions = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages("com.example.LAB5.functions")
                .scan()) {

            for (io.github.classgraph.ClassInfo classInfo : scanResult.getClassesWithAnnotation(SimpleFunction.class.getName())) {
                // Используем Class<? extends MathFunction> вместо Class<?>
                Class<? extends MathFunction> clazz = classInfo.loadClass(MathFunction.class);

                if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }

                SimpleFunction annot = clazz.getAnnotation(SimpleFunction.class);
                String key = annot.name();
                String displayName = bundle.containsKey(key) ? bundle.getString(key) : key;
                int priority = annot.priority();

                try {
                    MathFunction instance = clazz.getDeclaredConstructor().newInstance();
                    functions.add(new MathFunctionInfo(displayName, priority, instance, clazz));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        functions.sort(Comparator
                .comparing(MathFunctionInfo::priority)
                .thenComparing(MathFunctionInfo::displayName));

        return functions;
    }

    public static class MathFunctionInfo {
        private final String displayName;
        private final int priority;
        private final MathFunction instance;
        private final Class<? extends MathFunction> clazz;

        public MathFunctionInfo(String displayName, int priority, MathFunction instance, Class<? extends MathFunction> clazz) {
            this.displayName = displayName;
            this.priority = priority;
            this.instance = instance;
            this.clazz = clazz;
        }

        public String displayName() { return displayName; }
        public int priority() { return priority; }
        public MathFunction instance() { return instance; }
        public Class<? extends MathFunction> clazz() { return clazz; }

        @Override
        public String toString() {
            return displayName;
        }
    }
}