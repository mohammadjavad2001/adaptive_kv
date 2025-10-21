@echo off
echo Compiling simple prefix test...

cd /d "E:\learning\Projectpayan\software\Adaptive-Learning-master\mealyInference"

javac -cp "lib\*;target\classes" -d target\classes src\main\java\de\learnlib\ds\PrefixAnalyzer.java
javac -cp "lib\*;target\classes" -d target\classes src\main\java\de\learnlib\ds\SimplePrefixTest.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Running simple prefix test...
echo.

java -cp "lib\*;target\classes" de.learnlib.ds.SimplePrefixTest

echo.
echo Test completed!
pause
