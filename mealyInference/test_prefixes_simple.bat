@echo off
echo Testing prefix analysis in Kearns-Vazirani algorithm...

cd /d "E:\learning\Projectpayan\software\Adaptive-Learning-master\mealyInference"

echo Compiling the modified code...
javac -cp "lib\*;target\classes" -d target\classes src\main\java\de\learnlib\algorithms\kv\KearnsVaziraniMealy.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Running a simple learning experiment to see prefixes...
echo.

java -cp "lib\*;target\classes" hi

echo.
echo Test completed! Check the output above to see what prefixes contain.
pause
