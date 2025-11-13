@echo off
REM Batch file to run Adaptive Learning on all Minepump_SPL products
REM This script learns all products with tree reuse (Product 1 is used as base for subsequent products)

echo ========================================
echo   Adaptive Learning - All Products
echo ========================================
echo.
echo This will learn all Minepump_SPL products with tree reuse
echo Results will be saved to AdaptiveLearning_Results_[timestamp].xlsx
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
echo Starting adaptive learning...
echo.

REM Run the adaptive learning program
mvn exec:java -Dexec.mainClass="LearnAllProductsAdaptive"

echo.
echo ========================================
echo   Adaptive Learning Complete!
echo ========================================
echo Check the generated Excel file for results.
echo.
pause

