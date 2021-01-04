FROM adoptopenjdk/openjdk14:debian-jre

RUN apt-get update && apt-get install -y gradle build-essential libcurl4-openssl-dev libncurses5
RUN useradd -m user
USER user

WORKDIR /compile
