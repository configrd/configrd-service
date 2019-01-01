FROM openjdk:8u181-jre-alpine3.8
MAINTAINER Krzysztof Karski <kkkarski@gmail.com>

ENV CONFIG_URI file:/srv/configrd/repo-defaults.yml
ENV PORT 9191
ENV STREAMSOURCE file
ENV AWS_ACCESS_KEY_ID ""
ENV AWS_SECRET_ACCESS_KEY ""
ENV LOG_LEVEL INFO

COPY ./target/configrd-service-2.0.0.jar /apps/
WORKDIR /apps
RUN mkdir -p /srv/configrd

EXPOSE $PORT
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom \
				-Daws.accessKeyId=$AWS_ACCESS_KEY_ID \
				-Daws.secretKey=$AWS_SECRET_ACCESS_KEY \
				-Dconfigrd.log.level=$LOG_LEVEL \
				-jar ./configrd-service-2.0.0.jar ConfigrdServer \
				-u $CONFIG_URI \
				-p $PORT \
				-s $STREAMSOURCE