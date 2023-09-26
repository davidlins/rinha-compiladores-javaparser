FROM maven:3.9.4-amazoncorretto-20 as build

WORKDIR /app
COPY src /app/src
COPY pom.xml /app

RUN mvn clean package -DskipTests assembly:single

FROM amazoncorretto:20

COPY --from=build /app/target/*-jar-with-dependencies.jar rinha-compiladores.jar 

ENTRYPOINT [ "java","--enable-preview", "-jar", "rinha-compiladores.jar","/var/rinha/source.rinha.json"]

