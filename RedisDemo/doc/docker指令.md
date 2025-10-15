# RedisDemo 專案 Docker 指令教學

##  1. 建立 Docker 映像檔
```
docker build -t redisdemo-i .
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

