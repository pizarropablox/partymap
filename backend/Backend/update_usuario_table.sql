-- Script para actualizar la tabla USUARIO para sincronización con Azure B2C
-- Ejecutar este script en tu base de datos Oracle

-- Agregar nuevas columnas para Azure B2C
ALTER TABLE USUARIO ADD (
    azure_b2c_id VARCHAR2(100),
    nombre_azure VARCHAR2(100),
    apellido_azure VARCHAR2(100),
    rol_azure VARCHAR2(50),
    es_usuario_azure NUMBER(1) DEFAULT 0
);

-- Hacer la contraseña opcional (nullable)
ALTER TABLE USUARIO MODIFY contraseña VARCHAR2(100) NULL;

-- Crear índice único para azure_b2c_id
CREATE UNIQUE INDEX idx_usuario_azure_b2c_id ON USUARIO(azure_b2c_id);

-- Crear índice para búsquedas por email (si no existe)
CREATE INDEX idx_usuario_email ON USUARIO(email);

-- Comentarios para documentar las nuevas columnas
COMMENT ON COLUMN USUARIO.azure_b2c_id IS 'ID único de Azure B2C (sub claim del JWT)';
COMMENT ON COLUMN USUARIO.nombre_azure IS 'Nombre del usuario desde Azure B2C (given_name)';
COMMENT ON COLUMN USUARIO.apellido_azure IS 'Apellido del usuario desde Azure B2C (family_name)';
COMMENT ON COLUMN USUARIO.rol_azure IS 'Rol desde Azure B2C (extension_Roles)';
COMMENT ON COLUMN USUARIO.es_usuario_azure IS 'Indica si el usuario fue creado desde Azure B2C (1=SI, 0=NO)';

-- Verificar que los cambios se aplicaron correctamente
SELECT column_name, data_type, nullable, data_default 
FROM user_tab_columns 
WHERE table_name = 'USUARIO' 
ORDER BY column_id; 