@echo off
set /p passwordtoencrypt=패스워드를 입력하세요:
C:/MYDATA_API/DOMA/jdk1.8.0_172/bin/java -jar SecureTomcatJDBC.jar %passwordtoencrypt% 
timeout /t 20