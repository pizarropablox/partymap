#Dockerfile
FROM eclipse-temurin:22-jdk AS buildstage 

RUN apt-get update && apt-get install -y maven

#directorio de trabajo
WORKDIR /app

COPY pom.xml .
COPY src /app/src

RUN mvn clean package

FROM eclipse-temurin:22-jdk 

#aqui debemos asegurarnos que el nombre dee nuestro jar coincida
COPY target/backend-0.0.1-SNAPSHOT.jar /app/backend.jar

#Ubicacion y nombre del Wallet descomprimido
COPY /Wallet_partymap /app/Wallet_partymap

#Puerto
EXPOSE 8085

CMD [ "java", "-jar", "/app/backend.jar" ]