# Node.js 22 LTS runtime
FROM node:22-alpine

WORKDIR /app

# Copy only minimal files first to leverage Docker layer cache
COPY package*.json ./
RUN npm ci --omit=dev || npm i --omit=dev

# Copy the rest
COPY . .

ENV NODE_ENV=production
EXPOSE 3000
CMD ["node", "server.js"]
