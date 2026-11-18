FROM node:24-alpine AS build


WORKDIR /app


COPY package.json package-lock.json ./

RUN npm ci

ARG VITE_API_URL=https://localhost:3000/api
ARG VITE_API_ORIGIN=https://localhost:3000
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_API_ORIGIN=$VITE_API_ORIGIN

COPY . .

RUN npm run build

FROM nginxinc/nginx-unprivileged:alpine

USER root
RUN apk add --no-cache curl
USER 101

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 5173

   HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
     CMD curl -kfsS https://localhost:5173/ || exit 1