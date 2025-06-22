# Dockerfile
FROM openjdk:17-jdk-slim

#directorio de trabajo
WORKDIR /app

# Aqui debemos asegurarnos del nombre de nuestro jar coincida
COPY target/nuevo-proyecto-1.0-SNAPSHOT.jar app.jar

# Ubicacion y nombre del wallet descomprimido
COPY ./Wallet_partymap /app/Wallet_partymap

#PUERTO
EXPOSE 8083

CMD ["java", "-jar", "app.jar"]