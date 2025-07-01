# Configuración Centralizada de Endpoints

Este directorio contiene la configuración centralizada de endpoints para el proyecto Angular.

## Archivos

### `api-endpoints.ts`
Contiene todas las URLs de endpoints organizadas por módulo.

## Cómo usar

### 1. Importar la configuración
```typescript
import { ApiEndpoints } from '../config/api-endpoints';
```

### 2. Usar endpoints específicos
```typescript
// Endpoints de reservas
const estadisticasUrl = ApiEndpoints.RESERVA.ESTADISTICAS;
const reservasPorEvento = ApiEndpoints.RESERVA.POR_EVENTO(eventoId);

// Endpoints de usuarios
const usuarioActual = ApiEndpoints.USUARIO.CURRENT;
const todosUsuarios = ApiEndpoints.USUARIO.ALL;

// Endpoints de eventos
const todosEventos = ApiEndpoints.EVENTO.ALL;
const eventosPorUsuario = ApiEndpoints.EVENTO.POR_USUARIO(productorId);
```

### 3. Usar métodos utilitarios
```typescript
// Construir URL personalizada
const urlPersonalizada = ApiEndpoints.buildUrl('/mi-endpoint');

// Obtener URL base
const baseUrl = ApiEndpoints.getBaseUrl();
```

## Configuración del Environment

La URL base se configura en los archivos de environment:

- **Desarrollo**: `src/environments/environment.ts`
- **Producción**: `src/environments/environment.prod.ts`

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8085', // Cambiar aquí para modificar la URL base
  // ... otras configuraciones
};
```

## Ventajas

1. **Centralización**: Todos los endpoints en un solo lugar
2. **Mantenimiento**: Cambiar la URL base solo requiere modificar el environment
3. **Type Safety**: TypeScript proporciona autocompletado y validación
4. **Organización**: Endpoints agrupados por módulo
5. **Reutilización**: Fácil de usar en cualquier componente o servicio

## Estructura de Endpoints

### Reservas
- `ESTADISTICAS`: Estadísticas generales de reservas
- `ESTADISTICAS_BASICAS`: Estadísticas básicas
- `CANTIDAD_MINIMA`: Cantidad mínima de reservas
- `BUSCAR`: Buscar reservas con criterios
- `POR_EVENTO(eventoId)`: Reservas por evento específico
- `CANCELAR(reservaId)`: Cancelar reserva

### Usuarios
- `CURRENT`: Usuario actual
- `ALL`: Todos los usuarios
- `ESTADISTICAS`: Estadísticas de usuarios
- `PRODUCTOR(usuarioId)`: Productor por usuario
- `CREAR_PRODUCTOR`: Crear nuevo productor
- `ACTUALIZAR(usuarioId)`: Actualizar usuario
- `ELIMINAR(usuarioId)`: Eliminar usuario

### Eventos
- `ALL`: Todos los eventos
- `CREAR`: Crear nuevo evento
- `ACTUALIZAR(eventoId)`: Actualizar evento
- `ELIMINAR(eventoId)`: Eliminar evento
- `POR_USUARIO(productorId)`: Eventos por productor
- `MIS_ESTADISTICAS`: Estadísticas de eventos del usuario

### Ubicaciones
- `ALL`: Todas las ubicaciones
- `BASE`: URL base de ubicaciones

## Migración

Para migrar código existente:

1. Reemplazar URLs hardcodeadas con `ApiEndpoints`
2. Usar el servicio `EndpointsService` para operaciones HTTP
3. Actualizar imports en los componentes

### Ejemplo de migración

**Antes:**
```typescript
const response = await this.http.get('http://localhost:8085/usuario/all').toPromise();
```

**Después:**
```typescript
const response = await this.http.get(ApiEndpoints.USUARIO.ALL).toPromise();
```

O mejor aún, usar el servicio:
```typescript
const response = await this.endpointsService.getAllUsuarios().toPromise();
``` 