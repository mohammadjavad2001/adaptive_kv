@echo off
REM Batch file to run Normal Learning on all Minepump_SPL products
REM This script learns all products independently from scratch (no tree reuse)

echo ========================================
echo   Normal Learning - All Products
echo ========================================
echo.
echo This will learn all Minepump_SPL products independently (no tree reuse)
echo Results will be saved to NormalLearning_Results_[timestamp].xlsx
echo.

REM Compile if needed
echo Compiling Java files...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo Starting normal learning...
echo.

REM Run the normal learning program
mvn exec:java -Dexec.mainClass="LearnAllProductsNormal"

echo.
echo ========================================
echo   Normal Learning Complete!
echo ========================================
echo Check the generated Excel file for results.
echo.
pause

