# Dockerfile para aplicación Angular - Solo Desarrollo
# Usar Node.js 20 como imagen base
FROM node:20-alpine

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de dependencias
COPY package*.json ./

# Instalar dependencias
RUN npm ci

# Copiar todo el código fuente
COPY . .

# Exponer puerto 4200 (puerto por defecto de Angular)
EXPOSE 4200

# Comando para ejecutar en desarrollo
CMD ["npm", "start", "--", "--host", "0.0.0.0", "--port", "4200"] 