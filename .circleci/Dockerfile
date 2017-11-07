FROM circleci/openjdk:8-jdk-browsers
MAINTAINER Digital Studio <DigitalStudio@syscon.net>
RUN curl -sL https://deb.nodesource.com/setup_8.x | sudo -E bash - && \
    sudo apt-get install -y --no-install-recommends nodejs && \
    sudo apt-get install -y --no-install-recommends build-essential && \
    sudo npm i -g raml2html && \
    sudo npm i -g oas-raml-converter@1.0.13