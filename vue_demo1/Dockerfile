# File: Dockerfile
FROM node:22-alpine AS builder
WORKDIR /app

# 只複製相依檔以利用快取
COPY package*.json ./
RUN npm ci --silent

# 複製專案並建置
COPY . .
RUN npm run build

# 使用 nginx 提供靜態檔
FROM nginx:stable-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
# 使用自訂 nginx 設定以支援 SPA fallback
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

