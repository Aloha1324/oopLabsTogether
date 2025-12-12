@echo off 
echo ================================ 
echo    ЗАПУСК API ТЕСТОВ 
echo ================================ 
echo. 
echo Проверка API... 
curl -f http://localhost:8080/ >nul 2>&1 && echo Сервер доступен || echo Сервер не отвечает 
echo. 
newman run "..\collections\api-tests.json" -e "..\environments\localhost.json" --reporters cli 
echo. 
pause 
