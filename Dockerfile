ARG BUILD_ID

FROM node@sha256:48022836f3fbf7d8cd398114b5091cbe3c4b6cd5a4f37f0e5b2aece7fd6d2fc4 AS build
WORKDIR /root
RUN apk add openjdk21-jdk
COPY package.json .
COPY package-lock.json .
RUN npm ci
COPY shadow-cljs.edn .
RUN npm run deps
COPY src/ ./src
ARG BUILD_ID
RUN npm run "release:$BUILD_ID"

FROM node@sha256:48022836f3fbf7d8cd398114b5091cbe3c4b6cd5a4f37f0e5b2aece7fd6d2fc4
USER node
WORKDIR /home/node
ENV NODE_ENV=production
ARG BUILD_ID
COPY --from=build "/root/$BUILD_ID.dist.js" ./main.js
COPY public/ ./public

LABEL org.opencontainers.image.source=https://github.com/nafarlee/bg-search
ENTRYPOINT ["./main.js"]
