FROM eclipse-temurin:22-jdk AS buildstage 
 
RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY pom.xml .
COPY src /app/src


RUN mvn clean package

FROM eclipse-temurin:22-jdk 

COPY --from=buildstage /app/target/microservicio-0.0.1-SNAPSHOT.jar /app/microservicio.jar

EXPOSE 8081

ENTRYPOINT [ "java", "-jar","/app/microservicio.jar" ]


