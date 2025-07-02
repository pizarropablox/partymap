# Frontend Angular - Sistema de Gestión de Eventos

## 📋 Descripción del Proyecto

Este es el frontend de una aplicación Angular para la gestión de eventos y reservas. La aplicación permite a usuarios autenticados (clientes, productores y administradores) gestionar eventos, realizar reservas y acceder a reportes según sus roles.

## 🏗️ Arquitectura del Proyecto

### Estructura de Carpetas

```
frontend/
├── src/
│   ├── app/
│   │   ├── components/          # Componentes reutilizables
│   │   │   ├── navbar/         # Barra de navegación principal
│   │   │   └── footer/         # Pie de página
│   │   ├── pages/              # Páginas principales de la aplicación
│   │   │   ├── home-map/       # Página principal con mapa
│   │   │   ├── events/         # Gestión de eventos
│   │   │   ├── reservas/       # Gestión de reservas
│   │   │   ├── reportes/       # Reportes y estadísticas
│   │   │   ├── productor/      # Gestión de productores
│   │   │   ├── about/          # Página de información
│   │   │   └── contact/        # Página de contacto
│   │   ├── services/           # Servicios para comunicación con API
│   │   │   ├── endpoints.service.ts    # Servicio centralizado de endpoints
│   │   │   ├── evento.service.ts       # Servicio de eventos
│   │   │   ├── reserva.service.ts      # Servicio de reservas
│   │   │   ├── ubicacion.service.ts    # Servicio de ubicaciones
│   │   │   └── ...             # Otros servicios
│   │   ├── shared/             # Componentes y servicios compartidos
│   │   ├── config/             # Configuraciones (endpoints, etc.)
│   │   ├── auth.guard.ts       # Guard de autenticación
│   │   ├── role.guard.ts       # Guard de roles
│   │   └── app.ts              # Componente principal
│   ├── environments/           # Configuraciones de entorno
│   └── styles.css              # Estilos globales
├── Dockerfile                  # Configuración de Docker para desarrollo
├── docker-compose.yml          # Orquestación de servicios Docker
└── package.json                # Dependencias del proyecto
```

## 🔐 Sistema de Autenticación

### Azure AD B2C
- **Proveedor**: Microsoft Azure AD B2C
- **Configuración**: MSAL (Microsoft Authentication Library)
- **Flujo**: OAuth 2.0 con OpenID Connect
- **Tokens**: JWT (JSON Web Tokens)

### Roles de Usuario
- **Cliente**: Puede realizar reservas y ver eventos
- **Productor**: Puede crear y gestionar eventos
- **Administrador**: Acceso completo al sistema

## 🗺️ Funcionalidades Principales

### 1. Página Principal (Home Map)
- Mapa interactivo con Google Maps
- Búsqueda de ubicaciones con autocompletado
- Visualización de eventos en el mapa
- Navegación y geocodificación

### 2. Gestión de Eventos
- Crear, editar y eliminar eventos
- Subir imágenes de eventos
- Configurar ubicaciones y fechas
- Gestión de capacidad y precios

### 3. Sistema de Reservas
- Realizar reservas de eventos
- Cancelar reservas
- Ver historial de reservas
- Estadísticas de reservas

### 4. Reportes y Estadísticas
- Reportes de eventos
- Estadísticas de reservas
- Análisis de usuarios
- Métricas de rendimiento

### 5. Gestión de Usuarios
- Crear y gestionar productores
- Administrar roles de usuario
- Perfiles de usuario

## 🛠️ Tecnologías Utilizadas

### Frontend
- **Angular 20**: Framework principal
- **TypeScript**: Lenguaje de programación
- **RxJS**: Programación reactiva
- **Google Maps API**: Mapas interactivos
- **MSAL**: Autenticación con Azure AD

### Desarrollo
- **Docker**: Contenedorización
- **Docker Compose**: Orquestación
- **Node.js**: Entorno de desarrollo

### Estilo y UI
- **CSS3**: Estilos personalizados
- **Responsive Design**: Diseño adaptable
- **Material Design**: Principios de diseño

## 🚀 Instalación y Ejecución

### Prerrequisitos
- Node.js 20+
- Docker y Docker Compose
- Git

### Desarrollo Local
```bash
# Clonar el repositorio
git clone <repository-url>
cd frontend

# Instalar dependencias
npm install

# Ejecutar en modo desarrollo
npm start
```

### Con Docker
```bash
# Ejecutar con Docker Compose
docker-compose up

# Acceder a la aplicación
# http://localhost:4200
```

## 🔧 Configuración

### Variables de Entorno
- `API_BASE_URL`: URL base de la API backend
- `AZURE_CLIENT_ID`: ID del cliente de Azure AD B2C
- `GOOGLE_MAPS_API_KEY`: Clave de API de Google Maps

### Configuración de Azure AD B2C
- Tenant: `DuocDesarrolloCloudNative.onmicrosoft.com`
- Policy: `B2C_1_DuocDesarrolloCloudNative_Login`
- Redirect URI: `http://localhost:4200`

## 📱 Características Responsivas

- **Desktop**: Interfaz completa con todas las funcionalidades
- **Tablet**: Adaptación de menús y controles
- **Mobile**: Menú hamburguesa y navegación optimizada

## 🔒 Seguridad

### Autenticación
- Tokens JWT con expiración
- Refresh tokens automáticos
- Validación de roles en frontend y backend

### Autorización
- Guards de ruta para protección
- Verificación de roles por endpoint
- Control de acceso basado en roles (RBAC)

### Datos
- Encriptación de tokens en localStorage
- Validación de entrada de datos
- Sanitización de contenido

## 📊 Monitoreo y Logs

### Logs de Desarrollo
- Console logs para debugging
- Errores de autenticación
- Errores de API

### Métricas
- Tiempo de carga de páginas
- Errores de usuario
- Uso de funcionalidades

## 🧪 Testing

### Tipos de Pruebas
- **Unit Tests**: Pruebas de componentes y servicios
- **Integration Tests**: Pruebas de integración con API
- **E2E Tests**: Pruebas end-to-end

### Ejecutar Pruebas
```bash
# Pruebas unitarias
npm test

# Pruebas e2e
npm run e2e
```

## 📈 Optimizaciones

### Rendimiento
- Lazy loading de módulos
- Debounce en búsquedas
- Compresión de imágenes
- Cache de datos

### SEO
- Meta tags dinámicos
- URLs amigables
- Sitemap generado

## 🤝 Contribución

### Flujo de Trabajo
1. Crear rama feature
2. Desarrollar funcionalidad
3. Ejecutar pruebas
4. Crear Pull Request
5. Code Review
6. Merge a main

### Estándares de Código
- ESLint para linting
- Prettier para formateo
- Conventional Commits
- TypeScript strict mode

## 📝 Notas de Desarrollo

### Estructura de Commits
```
feat: nueva funcionalidad
fix: corrección de bug
docs: documentación
style: cambios de estilo
refactor: refactorización
test: pruebas
chore: tareas de mantenimiento
```

### Convenciones de Nomenclatura
- **Componentes**: PascalCase (ej: `HomeMapComponent`)
- **Servicios**: PascalCase con sufijo Service (ej: `EventoService`)
- **Interfaces**: PascalCase con prefijo I (ej: `IEvento`)
- **Variables**: camelCase (ej: `userName`)
- **Constantes**: UPPER_SNAKE_CASE (ej: `API_BASE_URL`)

## 🆘 Soporte

### Problemas Comunes
1. **Error de autenticación**: Verificar tokens y configuración de Azure AD
2. **Error de API**: Verificar conectividad y endpoints
3. **Error de mapa**: Verificar API key de Google Maps

### Contacto
- **Desarrollador**: Equipo de desarrollo
- **Documentación**: README y comentarios en código
- **Issues**: Sistema de tickets del proyecto

## 📄 Licencia

Este proyecto está bajo la licencia [LICENCIA]. Ver el archivo LICENSE para más detalles. 