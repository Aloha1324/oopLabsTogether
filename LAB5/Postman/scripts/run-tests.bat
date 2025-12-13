@echo off  
echo ================================  
echo    ‡€“‘Š API ’…‘’‚  
echo ================================  
echo.  
newman run "..\collections\api-tests.json" -e "..\environments\localhost.json" --reporters cli  
echo.  
pause 
