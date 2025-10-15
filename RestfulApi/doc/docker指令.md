``` 
    docker build -t restfulapi-i .
    docker run -p 8080:8080 --name restfulapi-c restfulapi-i
```

#  停止容器
`docker stop restfulapi-c`
#  刪除容器
`docker rm restfulapi-c`
#  重新啟動容器
`docker start restfulapi-c`
#  查看容器日誌
`docker logs restfulapi-c`
#  查看容器內部IP
`docker inspect -f "{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}" restfulapi-c`
#  進入容器內部
`docker exec -it restfulapi-c /bin/bash`
#  查看所有容器
`docker ps -a`
#  查看所有鏡像
`docker images`
