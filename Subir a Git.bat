@echo off
REM === Configuración ===
set /p mensaje=Escribe el mensaje del commit: 

REM === Ir a la carpeta del proyecto ===
cd /d "%~dp0"

REM === Agregar todos los cambios ===
git add .

REM === Crear commit ===
git commit -m "%mensaje%"

REM === Subir a GitHub ===
git push

echo.
echo Cambios subidos correctamente.
pause