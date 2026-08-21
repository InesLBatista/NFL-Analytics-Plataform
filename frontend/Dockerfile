FROM node:22-alpine AS build
WORKDIR /app

#copy package files first to leverage Docker layer cache
#only re-installs if package-lock.json changes
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build


#nginx serves the static files and proxies /api/* to the backend
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
