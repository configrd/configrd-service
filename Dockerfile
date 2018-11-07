
FROM openjdk:8-jdk-alpine
MAINTAINER Krzysztof Karski <krzysztof.karski@emergenttech.com>

ENV configrd.config.uri classpath:repo-defaults.yml
ENV configrd.server.port 9191

COPY ./target/configrd-service-*-jar-with-dependencies.jar /apps/
WORKDIR /apps
RUN mv ./configrd-service-*-jar-with-dependencies.jar ./configrd-service.jar

EXPOSE $configrd.server.port
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom & \
				-Dconfigrd.config.location=$configrd.config.uri & \
				-jar ./configrd-service.jar $configrd.server.port