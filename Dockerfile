FROM adoptopenjdk/openjdk11-openj9:debianslim-jre
WORKDIR /server
COPY build/libs/fetch_ta_data.jar tacontrol/build/release/tacontrol /server/

HEALTHCHECK --timeout=5s --retries=1 CMD /server/tacontrol health

ENTRYPOINT ["java", "-Xtune:virtualized", "-XX:-OmitStackTraceInFastThrow", "-jar", "/server/fetch_ta_data.jar", "server"]
