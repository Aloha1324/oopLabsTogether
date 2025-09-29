package functions;

//Интерфейс для табулированных функций, добавляя методы для работы с таблицей значений

public interface TabulatedFunction extends MathFunction {

    //Возвращает количество табулированных значений
    int getCount();

    //Получает значение аргумента x по номеру индекса
    //index индекс значения (от 0 до getCount()-1)
    double getX(int index);

    //Получает значение функции y по номеру индекса
    //index индекс значения (от 0 до getCount()-1)
    double getY(int index);

    //Задает значение функции y по номеру индекса
    //index индекс значения (от 0 до getCount()-1)
    //value новое значение y
    void setY(int index, double value);

    //Возвращает индекс аргумента x в таблице
    //Предполагается, что все x различны
    int indexOfX(double x);

    //Возвращает индекс первого вхождения значения y
    int indexOfY(double y);

    //Возвращает самый левый x (минимальное значение аргумента)
    double leftBound();

    //Возвращает самый правый x (максимальное значение аргумента)
    double rightBound();
}