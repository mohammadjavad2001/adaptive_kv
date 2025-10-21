@echo off
echo Compiling hi.java...
cd target\classes
del hi.class 2>nul
del hi$*.class 2>nul
cd ..\..

"C:\Program Files\Java\jdk-11\bin\javac.exe" -encoding UTF-8 -cp "target\classes;lib\*" -sourcepath src\main\java src\main\java\hi.java -d target\classes

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo Running hi...
    "C:\Program Files\Java\jdk-11\bin\java.exe" -cp "target\classes;lib\*" hi
) else (
    echo Compilation failed!
    exit /b 1
)


