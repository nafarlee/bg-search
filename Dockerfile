FROM node@sha256:48022836f3fbf7d8cd398114b5091cbe3c4b6cd5a4f37f0e5b2aece7fd6d2fc4 AS build
WORKDIR /root
RUN apk add openjdk11-jdk
COPY package.json .
COPY package-lock.json .
RUN npm ci
COPY shadow-cljs.edn .
RUN npm run deps
COPY src/ ./src
RUN npm run release

FROM node@sha256:48022836f3fbf7d8cd398114b5091cbe3c4b6cd5a4f37f0e5b2aece7fd6d2fc4
USER node
WORKDIR /home/node
ENV NODE_ENV=production
COPY --from=build /root/package.json .
COPY --from=build /root/package-lock.json .
RUN npm ci
COPY --from=build /root/app.js .
COPY public/ ./public

LABEL org.opencontainers.image.source https://github.com/nafarlee/bg-search
ENTRYPOINT ["node", "app.js"]
