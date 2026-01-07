@echo off
echo Loading environment variables from .env file...

for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
    set "%%a=%%b"
)

echo Starting Spring Boot application...
.\mvnw.cmd spring-boot:run
