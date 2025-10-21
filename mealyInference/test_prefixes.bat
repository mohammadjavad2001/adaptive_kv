@echo off
echo Compiling prefix analysis tools...

cd /d "E:\learning\Projectpayan\software\Adaptive-Learning-master\mealyInference"

javac -cp "lib\*;target\classes" -d target\classes src\main\java\de\learnlib\ds\PrefixAnalyzer.java
javac -cp "lib\*;target\classes" -d target\classes src\main\java\de\learnlib\ds\PrefixDemo.java
javac -cp "lib\*;target\classes" -d target\classes src\main\java\de\learnlib\ds\TestPrefixUnderstanding.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Running prefix demonstration...
echo.

java -cp "lib\*;target\classes" de.learnlib.ds.PrefixDemo

echo.
echo Running prefix understanding test...
echo.

java -cp "lib\*;target\classes" de.learnlib.ds.TestPrefixUnderstanding

echo.
echo Test completed!
pause
