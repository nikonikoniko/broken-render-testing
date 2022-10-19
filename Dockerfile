FROM node:14.20.1

RUN apt-get update && apt-get -q -y install \
      openjdk-11-jdk \
      curl \
    && curl -s https://download.clojure.org/install/linux-install-1.10.1.492.sh | bash \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /tmp/build

COPY package.json package-lock.json ./
RUN npm ci
COPY . ./

RUN npx shadow-cljs release :frontend --debug
RUN npx shadow-cljs release :backend --debug

EXPOSE 3000
ENV PORT=3000

ENTRYPOINT ["./docker-entrypoint.sh"]
