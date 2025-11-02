# game-docker1 專案 Docker 指令教學

1. 切到 game-docker1 專案目錄下，建立鏡像
```
docker build -t game-docker1:latest -f Dockerfile .
```
2. 前端：啟動容器並加入既有 network 與對外埠
```
docker run -d --name game-docker1-c --network docker-network -p 5173:80 game-docker1:latest

```
