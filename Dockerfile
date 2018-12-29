FROM openjdk:8-jdk-alpine
MAINTAINER Krzysztof Karski <kkkarski@gmail.com>

ENV CONFIG_URI file:/srv/configrd/repo-defaults.yml
ENV PORT 9191
ENV STREAMSOURCE file
ENV AWS_ACCESS_KEY_ID ""
ENV AWS_SECRET_ACCESS_KEY ""
ENV LOG_LEVEL INFO

COPY ./target/configrd-service-*-jar-with-dependencies.jar /apps/
WORKDIR /apps
RUN mv ./configrd-service-*-jar-with-dependencies.jar ./configrd-service.jar && \
	mkdir -p /srv/configrd

EXPOSE $PORT
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom \
				-Daws.accessKeyId=$AWS_ACCESS_KEY_ID \
				-Daws.secretKey=$AWS_SECRET_ACCESS_KEY \
				-Dconfigrd.log.level=$LOG_LEVEL \
				-jar ./configrd-service.jar ConfigrdServer \
				-u $CONFIG_URI \
				-p $PORT \
				-s $STREAMSOURCE