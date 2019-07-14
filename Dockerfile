FROM openjdk:8u181-stretch
WORKDIR /
ADD target/ /
ENTRYPOINT java -jar `find . -name *.jar | tail -n1`
EXPOSE 8080
