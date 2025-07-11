    // src/environments/environment.ts
    // Este archivo de entorno es para el modo de desarrollo (cuando ejecutas 'ng serve')
    export const environment = {
      production: false, // Indica que no estamos en producción
      // Aquí va tu clave API de Google Maps. ¡Reemplaza el placeholder!
      // Asegúrate de que esta API key tenga habilitados los servicios:
      // - Maps JavaScript API
      // - Geocoding API
      // - Places API (opcional)
      googleMapsApiKey: 'AIzaSyCRQWonRBlilzJNvnyyFKaXmgn54yCL5EY', 
      
      // URL del backend
      apiUrl: 'http://localhost:8085',
      //apiUrl: 'http://18.235.227.189:8085',
      
      // Configuración de Azure AD B2C - VALORES REALES
      msalConfig: {
        auth: {
          clientId: 'ad16d15c-7d6e-4f58-8146-4b5b3d7b7124', // Tu Application (client) ID real
          authority: 'https://duocdesarrollocloudnative.b2clogin.com/DuocDesarrolloCloudNative.onmicrosoft.com/B2C_1_DuocDesarrolloCloudNative_Login',
          redirectUri: 'http://localhost:4200',
          postLogoutRedirectUri: 'http://localhost:4200',
        },
      },
      apiConfig: {
        scopes: ['https://DuocDesarrolloCloudNative.onmicrosoft.com/api/user_impersonation'], // Scope basado en tu tenant
        uri: 'https://your-api-endpoint.com/api',
      },
    };