
FROM openjdk:8-jdk-alpine
MAINTAINER Krzysztof Karski <krzysztof.karski@emergenttech.com>

ENV REPO_DEFS classpath:repo-defaults.yml
ENV PORT 9191

COPY ./target/configrd-service-*-jar-with-dependencies.jar /apps/
WORKDIR /apps
RUN mv ./configrd-service-*-jar-with-dependencies.jar ./configrd-service.jar

EXPOSE $PORT
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Dconfigrd.config.location=$REPO_DEFS -jar ./configrd-service.jar $PORT