@echo off && setlocal enableextensions

set PROST_JAR=..\..\..\target\prost-1.0-SNAPSHOT.jar
set JAVA_CMD=java

if not exist %PROST_JAR% (
    echo Error: Could not locate prost.jar
    exit 1
)

set f_start=1
set f_max=10
set f_step=2

for /l %%x in (%f_start%, %f_step%, %f_max%) do (
  echo %%x
)

set x=0
:loop
echo %x%
if %x% gt 10

%JAVA_CMD% -jar ..\..\..\target\prost-1.0-SNAPSHOT.jar %*