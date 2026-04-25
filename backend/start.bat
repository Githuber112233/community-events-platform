@echo off
REM 后端启动脚本 - 自动终止旧Java进程后启动新服务
echo [INFO] 正在检查8080端口占用情况...

netstat -ano | findstr :8080 > temp_port.txt
setlocal enabledelayedexpansion
set PORT_FOUND=0
for /f "tokens=5" %%a in (temp_port.txt) do (
    set PID=%%a
    REM 获取进程命令确认是Java进程
    tasklist /FI "PID eq !PID!" | findstr "java" > nul
    if !errorlevel!==0 (
        echo [WARN] 发现旧Java进程 PID=!PID! 占用8080端口，正在终止...
        taskkill /F /PID !PID!
        set PORT_FOUND=1
    )
)
del temp_port.txt 2>nul

if !PORT_FOUND!==1 (
    echo [INFO] 等待端口释放...
    timeout /t 2 /nobreak > nul
)

echo [INFO] 启动Spring Boot服务...
cd /d %~dp0
mvn spring-boot:run