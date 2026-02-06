# 啟動容器

* docker run -d --name n8n -p 5678:5678 -e GENERIC_TIMEZONE="Asia/Taipei" -e TZ="Asia/Taipei" -v "D:\docker\n8n:/home/node/.n8n" docker.n8n.io/n8nio/n8n