FROM node:24-alpine

ENV NODE_ENV=production

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci --omit=dev && npm cache clean --force

COPY . .

RUN mkdir -p logs && chown node:node logs

USER node

EXPOSE 3000

CMD ["node", "app.js"]