FROM adoptopenjdk/openjdk11:debian-jre
WORKDIR /server
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,address=10000,server=y,suspend=n
COPY target/fetch_ta_data.jar /server/fetch_ta_data.jar

CMD ["java", "-XX:-OmitStackTraceInFastThrow", "-jar", "/server/fetch_ta_data.jar", "server"]
