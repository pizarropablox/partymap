// src/environments/environment.prod.ts
// Este archivo de entorno es para el modo de producción (cuando compilas para despliegue)
export const environment = {
  production: true, // Indica que estamos en producción
  // Aquí va tu clave API de Google Maps. ¡Debe ser la misma clave real!
  googleMapsApiKey: 'AIzaSyCRQWonRBlilzJNvnyyFKaXmgn54yCL5EY', 
  // URL del backend en producción
  apiUrl: 'http://localhost:8085',
};