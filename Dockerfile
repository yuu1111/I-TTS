FROM ghcr.io/mg8853/yolks:amazon-corretto-java25

USER root

COPY selfhost/build/libs/itts-selfhost-NONE.jar /opt/itts/itts-selfhost.jar

USER container
ENV TZ=Asia/Tokyo
WORKDIR /home/container

CMD ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "/opt/itts/itts-selfhost.jar"]
