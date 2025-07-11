# Dockerfile para aplicación Angular - Desarrollo y Producción
FROM node:20-alpine

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de dependencias
COPY package*.json ./

# Instalar dependencias
RUN npm ci

# Copiar todo el código fuente
COPY . .

# Exponer puerto 4200
EXPOSE 4200

# Comando para ejecutar con configuración optimizada
CMD ["npm", "start", "--", "--host", "0.0.0.0", "--port", "4200"] 