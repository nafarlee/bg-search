FROM node:18-alpine3.21 AS build
WORKDIR /root
RUN apk add openjdk17-jdk
COPY package.json .
COPY package-lock.json .
RUN npm ci
COPY shadow-cljs.edn .
COPY src/ ./src
RUN npm run release

FROM node:18-alpine3.21
USER node
WORKDIR /home/node
ENV NODE_ENV=production
COPY --from=build /root/package.json .
COPY --from=build /root/package-lock.json .
RUN npm ci
COPY --from=build /root/app.js .
COPY public/ ./public
ENTRYPOINT ["node", "app.js"]
