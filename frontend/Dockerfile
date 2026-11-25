# Stage 1: Build the React application
FROM node:26-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Vite inlines these at build time, so they must be present before `npm run build`.
ARG VITE_API_BASE_URL=/api
ARG VITE_WS_BASE_URL=/ws
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_WS_BASE_URL=$VITE_WS_BASE_URL

# Build the application
RUN npm run build

# Stage 2: Serve with Nginx (root 가 아닌 nginx 사용자로 실행)
FROM nginxinc/nginx-unprivileged:alpine

# 베이스 이미지 이후에 나온 OS 보안 패치 반영. 설치에만 root 를 쓰고 nginx 사용자로 되돌림
USER root
RUN apk upgrade --no-cache
USER nginx

# Copy built files from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 비root 프로세스라 8080
EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
