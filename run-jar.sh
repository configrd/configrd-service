sudo chown $USER:$USER /srv -R
java -Djava.security.egd=file:/dev/./urandom -Dconfigrd.log.level=DEBUG -jar ./target/configrd-service-2.0.0.jar ConfigrdServer