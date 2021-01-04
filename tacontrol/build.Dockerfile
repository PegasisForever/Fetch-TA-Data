FROM adoptopenjdk/openjdk14:debian-jre

RUN apt-get update && apt-get install -y gradle build-essential libcurl4-openssl-dev libncurses5
RUN useradd -m user && mkdir /home/user/.gradle && chown user:user /home/user/.gradle
USER user

WORKDIR /compile
