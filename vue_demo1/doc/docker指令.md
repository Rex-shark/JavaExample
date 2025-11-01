# vue_demo1專案 Docker 指令教學

##  
```
cd .\vue_demo1
docker build -t vue_demo1:latest .
docker run -d -p 8080:80 --name vue-demo1-c vue_demo1:latest
```



