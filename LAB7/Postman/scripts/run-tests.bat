@echo off
chcp 65001 > nul  # UTF-8 кодировку
echo ================================
echo    ТЕСТИРОВАНИЕ API
echo ================================
echo.

echo Запуск тестов...
echo.

newman run "..\collections\api-tests.json" ^
  -e "..\environments\localhost.json" ^
  --verbose ^
  --reporters cli,html ^
  --reporter-html-export "performance-report.html"

echo.
echo Отчет сохранен в performance-report.html
echo.
pause
