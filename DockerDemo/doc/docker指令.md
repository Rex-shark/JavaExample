# DockerDemo 專案 Docker 指令教學

##  前置作業
```
- maven 把包jar
- 切換目錄至專案DockerDemo
```

##  1. 建立 Docker 映像檔 全小寫
```
docker build -t dockerdemo-i .
``` 

##  2. 啟動服務（背景模式） 不會重新 build
``` 
docker-compose up -d
```

##  3. 重新建置映像檔並啟動
``` 
docker-compose up --build
```
##  4. 停止所有容器
```
docker-compose stop
```

##  5. 關閉刪除容器
``` 
docker-compose down
```

