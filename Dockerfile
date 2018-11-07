FROM openjdk:8-jdk-alpine
MAINTAINER Krzysztof Karski <krzysztof.karski@emergenttech.com>

ENV configrd.config.uri classpath:repo-defaults.yml
ENV configrd.server.port 9191
ENV configrd.config.streamSource file
ENV aws.accessKeyId
ENV aws.secretKeys

COPY ./target/configrd-service-*-jar-with-dependencies.jar /apps/
WORKDIR /apps
RUN mv ./configrd-service-*-jar-with-dependencies.jar ./configrd-service.jar

EXPOSE $configrd.server.port
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom \
				-Dconfigrd.config.uri=$configrd.config.uri \
				-Daws.accessKeyId=$aws.accessKeyId \
				-Daws.secretKey=$aws.secretKey \
				-jar ./configrd-service.jar $configrd.server.port