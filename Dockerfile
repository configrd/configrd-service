FROM openjdk:8-jdk-alpine
MAINTAINER Krzysztof Karski <kkkarski@gmail.com>

ENV CONFIG_URI classpath:repo-defaults.yml
ENV PORT 9191
ENV STREAMSOURCE file
ENV AWS_ACCESS_KEY_ID
ENV AWS_SECRET_ACCESS_KEY

COPY ./target/configrd-service-*-jar-with-dependencies.jar /apps/
WORKDIR /apps
RUN mv ./configrd-service-*-jar-with-dependencies.jar ./configrd-service.jar

EXPOSE $PORT
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom \
				-Dconfigrd.config.uri=$CONFIG_URI \
				-Daws.accessKeyId=$AWS_ACCESS_KEY_ID \
				-Daws.secretKey=$AWS_SECRET_ACCESS_KEY \
				-jar ./configrd-service.jar $PORT
