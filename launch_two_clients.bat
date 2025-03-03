@echo off
chcp 65001 > nul
start /b java -Dfile.encoding=UTF-8 -jar .\build\libs\SecureClient-1.0-SNAPSHOT.jar --name %2 %1 %4
start /b java -Dfile.encoding=UTF-8 -jar .\build\libs\SecureClient-1.0-SNAPSHOT.jar --name %3 %1 %4

:: параметр 1 - тип (--dhi, например)
:: параметры 2, 3 - имена пользователей (CATHY, FRED)
:: параметр 4 - подтип (--alg, --el)