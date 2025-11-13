@echo off
echo ============================================================
echo  Comprehensive Learning Comparison: Adaptive vs Normal
echo  Mine Pump Software Product Line
echo ============================================================
echo.

REM Set JAVA_HOME if not already set (adjust path as needed)
if "%JAVA_HOME%"=="" (
    echo Setting JAVA_HOME...
    set "JAVA_HOME=C:\Program Files\Java\jdk-11"
)

echo Compiling with Maven...
call mvn clean compile -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo ============================================================
echo  Running Comparison (This will take a while...)
echo  Learning all 15 products with both approaches
echo ============================================================
echo.

REM Run the comparison
call mvn exec:java -Dexec.mainClass="CompareAdaptiveVsNormal" -Dexec.cleanupDaemonThreads=false

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Execution failed!
    pause
    exit /b 1
)

echo.
echo ============================================================
echo  Comparison completed successfully!
echo  Check learning_comparison_results.xlsx for results
echo ============================================================
pause

