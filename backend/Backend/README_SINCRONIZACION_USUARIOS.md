# Sincronización de Usuarios con Azure B2C

Este documento explica cómo funciona la sincronización automática de usuarios desde Azure B2C hacia la base de datos local.

## 📋 Resumen

Cuando un usuario se autentica con un token JWT de Azure B2C, el sistema automáticamente:

1. **Extrae la información** del JWT (email, nombre, apellido, rol, etc.)
2. **Busca el usuario** en la base de datos local
3. **Crea o actualiza** el usuario según corresponda
4. **Mantiene sincronizada** la información entre Azure B2C y la base de datos local

## 🗄️ Cambios en la Base de Datos

### Nuevas Columnas en la Tabla USUARIO

```sql
-- Ejecutar el script: update_usuario_table.sql

ALTER TABLE USUARIO ADD (
    azure_b2c_id VARCHAR2(100),        -- ID único de Azure B2C
    nombre_azure VARCHAR2(100),        -- Nombre desde Azure B2C
    apellido_azure VARCHAR2(100),      -- Apellido desde Azure B2C
    rol_azure VARCHAR2(50),            -- Rol desde Azure B2C
    es_usuario_azure NUMBER(1) DEFAULT 0  -- Indica si es usuario de Azure B2C
);

-- Hacer la contraseña opcional
ALTER TABLE USUARIO MODIFY contraseña VARCHAR2(100) NULL;
```

## 🔄 Proceso de Sincronización

### 1. Filtro Automático
- **`JwtUserSyncFilter`**: Se ejecuta después de cada autenticación JWT
- Extrae automáticamente la información del token
- Sincroniza el usuario en la base de datos

### 2. Lógica de Sincronización
```java
// Buscar por Azure B2C ID primero
Optional<Usuario> usuario = getUsuarioByAzureB2cId(azureB2cId);

if (usuario.exists()) {
    // Actualizar información existente
    usuario.actualizarDesdeAzureB2C(nombre, apellido, rol);
} else {
    // Buscar por email como respaldo
    usuario = getUsuarioByEmail(email);
    
    if (usuario.exists()) {
        // Vincular usuario existente con Azure B2C
        usuario.setAzureB2cId(azureB2cId);
        usuario.actualizarDesdeAzureB2C(nombre, apellido, rol);
    } else {
        // Crear nuevo usuario
        crearNuevoUsuario(email, nombre, apellido, azureB2cId, rol);
    }
}
```

### 3. Mapeo de Roles
```java
// Mapeo de roles de Azure B2C a TipoUsuario
"ADMINISTRADOR" → TipoUsuario.ADMINISTRADOR
"PRODUCTOR"     → TipoUsuario.PRODUCTOR
"CLIENTE"       → TipoUsuario.CLIENTE
```

## 🧪 Endpoints de Prueba

### 1. Verificar JWT
```bash
GET /test/ping
Authorization: Bearer <token>
```

### 2. Ver Información del JWT
```bash
GET /test/auth
Authorization: Bearer <token>
```

### 3. Probar Sincronización Manual
```bash
GET /test/sync-user
Authorization: Bearer <token>
```

### 4. Obtener Usuario Actual
```bash
GET /test/current-user
Authorization: Bearer <token>
```

## 📊 Información Extraída del JWT

| Campo JWT | Columna BD | Descripción |
|-----------|------------|-------------|
| `sub` | `azure_b2c_id` | ID único del usuario en Azure B2C |
| `emails[0]` | `email` | Email del usuario |
| `given_name` | `nombre_azure` | Nombre del usuario |
| `family_name` | `apellido_azure` | Apellido del usuario |
| `extension_Roles` | `rol_azure` | Rol asignado en Azure B2C |

## 🔧 Configuración

### 1. SecurityConfig
El filtro se agrega automáticamente después de la autenticación JWT:

```java
.addFilterAfter(jwtUserSyncFilter, BearerTokenAuthenticationFilter.class)
```

### 2. Aplicación Automática
- Se ejecuta en **todos los endpoints autenticados**
- No interrumpe el flujo normal de la aplicación
- Maneja errores de forma silenciosa (logging)

## 🚀 Flujo de Uso

### Primer Acceso
1. Usuario se autentica con Azure B2C
2. Sistema recibe JWT válido
3. `JwtUserSyncFilter` extrae información del token
4. Se crea nuevo usuario en la base de datos
5. Usuario puede acceder a endpoints protegidos

### Accesos Posteriores
1. Usuario se autentica con Azure B2C
2. Sistema recibe JWT válido
3. `JwtUserSyncFilter` actualiza información del usuario
4. Usuario accede con información actualizada

## ⚠️ Consideraciones

### 1. Contraseñas
- Los usuarios de Azure B2C **NO necesitan contraseña** en la BD local
- La columna `contraseña` es opcional (nullable)

### 2. Roles
- Los roles se mapean automáticamente desde `extension_Roles`
- Si no hay rol, se asigna `CLIENTE` por defecto

### 3. Identificación
- **Primera prioridad**: Azure B2C ID (`sub` claim)
- **Segunda prioridad**: Email (para usuarios existentes)

### 4. Actualizaciones
- La información se actualiza automáticamente en cada acceso
- Se mantiene sincronizada con Azure B2C

## 🔍 Monitoreo

### Logs
```java
// Usuario sincronizado exitosamente
logger.debug("Usuario sincronizado: {} ({})", usuario.getEmail(), usuario.getTipoUsuario());

// Error en sincronización
logger.error("Error sincronizando usuario desde JWT", e);
```

### Verificación
```bash
# Verificar que el usuario existe en la BD
SELECT * FROM USUARIO WHERE azure_b2c_id = 'tu-azure-b2c-id';

# Verificar usuarios de Azure B2C
SELECT * FROM USUARIO WHERE es_usuario_azure = 1;
```

## 🎯 Beneficios

1. **Sincronización Automática**: No requiere intervención manual
2. **Consistencia de Datos**: Mantiene BD local actualizada
3. **Flexibilidad**: Soporta usuarios locales y de Azure B2C
4. **Escalabilidad**: Funciona con cualquier cantidad de usuarios
5. **Seguridad**: Mantiene la seguridad de Azure B2C

## 🚨 Troubleshooting

### Usuario no se sincroniza
1. Verificar que el JWT contenga los claims necesarios
2. Revisar logs de error en la aplicación
3. Verificar conectividad con la base de datos

### Error de mapeo de roles
1. Verificar que `extension_Roles` esté configurado en Azure B2C
2. Revisar el mapeo en `mapearRolAzure()`

### Problemas de rendimiento
1. El filtro se ejecuta en cada request autenticado
2. Considerar cachear información del usuario si es necesario 