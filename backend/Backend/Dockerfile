# Dockerfile
FROM openjdk:21-jdk

#directorio de trabajo
WORKDIR /app

# Aqui debemos asegurarnos del nombre de nuestro jar coincida
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

# Ubicacion y nombre del wallet descomprimido
COPY ./Wallet_partymap /app/Wallet_partymap

#PUERTO
EXPOSE 8085

CMD ["java", "-jar", "app.jar"]