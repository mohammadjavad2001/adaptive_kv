@echo off
REM Batch file to compile and run CompareAndVisualizeProducts.java
REM Usage: run_visualize_comparison.bat [product1_path] [product2_path] [name1] [name2]

echo ========================================
echo   Product Comparison Visualizer
echo ========================================
echo.

REM Compile the Java file
echo Compiling CompareAndVisualizeProducts.java...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.
echo Running visualization tool...
echo ========================================
echo.

REM Run with default arguments (Product 00001 vs 00002) or custom arguments
if "%~1"=="" (
    echo Using default products: 00001 vs 00002
    call mvn exec:java -Dexec.mainClass="CompareAndVisualizeProducts"
) else if "%~2"=="" (
    echo ERROR: Please provide both product paths
    pause
    exit /b 1
) else if "%~3"=="" (
    REM Only paths provided
    call mvn exec:java -Dexec.mainClass="CompareAndVisualizeProducts" -Dexec.args="%~1 %~2"
) else (
    REM All arguments provided
    call mvn exec:java -Dexec.mainClass="CompareAndVisualizeProducts" -Dexec.args="%~1 %~2 \"%~3\" \"%~4\""
)

echo.
echo ========================================
echo   Visualization Complete!
echo ========================================
pause





