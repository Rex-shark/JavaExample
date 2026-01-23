#
# 1. 貼上標籤 (注意冒號後面是 frontend)
docker tag linebot-game-frontend rexliu427/tist-2025:frontend

# 2. 推送
docker push rexliu427/tist-2025:frontend


# 1. 貼上標籤 (注意冒號後面是 backend)
docker tag linebot-game-backend rexliu427/tist-2025:backend

# 2. 推送
docker push rexliu427/tist-2025:backend



給對方的檔案清單 (Checklist)
您必須給朋友以下 3 個檔案 放在同一個資料夾內：

docker-compose.yml (上面修改過的這個版本)

.env (包含 LINE Token 和資料庫密碼)

nginx-proxy.conf (因為 Nginx 服務有掛載它)