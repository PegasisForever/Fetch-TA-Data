FROM adoptopenjdk/openjdk14:debian-jre
WORKDIR /server
COPY build/libs/fetch_ta_data.jar /server/fetch_ta_data.jar

HEALTHCHECK CMD java -jar /server/fetch_ta_data.jar ctl health

ENTRYPOINT ["java", "-XX:-OmitStackTraceInFastThrow", "-jar", "/server/fetch_ta_data.jar", "server"]
