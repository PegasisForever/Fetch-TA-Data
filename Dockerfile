FROM adoptopenjdk/openjdk14:debian-jre
WORKDIR /server
COPY build/libs/fetch_ta_data.jar tacontrol/build/release/tacontrol /server/

HEALTHCHECK --timeout=5s --retries=1 CMD /server/tacontrol health

ENTRYPOINT ["java", "-XX:-OmitStackTraceInFastThrow", "-jar", "/server/fetch_ta_data.jar", "server"]
