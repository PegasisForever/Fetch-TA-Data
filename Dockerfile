FROM adoptopenjdk/openjdk14:debian-jre
WORKDIR /server
COPY target/fetch_ta_data.jar /server/fetch_ta_data.jar

ENTRYPOINT ["java", "-XX:-OmitStackTraceInFastThrow", "-jar", "/server/fetch_ta_data.jar", "server"]
