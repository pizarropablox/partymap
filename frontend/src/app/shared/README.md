# Sistema de Mensajes Reutilizable

Este sistema proporciona una forma consistente y moderna de mostrar mensajes en toda la aplicación Angular.

## Componentes

### MensajeService
Servicio global para manejar mensajes que puede ser inyectado en cualquier componente.

### MensajeModalComponent
Componente modal que se muestra automáticamente cuando se envía un mensaje a través del servicio.

## Uso

### 1. Inyectar el servicio en tu componente

```typescript
import { MensajeService } from '../../shared/mensaje.service';

constructor(private mensajeService: MensajeService) {}
```

### 2. Mostrar mensajes

```typescript
// Mensaje de éxito (auto-cierra en 3 segundos)
this.mensajeService.mostrarExito('Operación completada exitosamente');

// Mensaje de error (auto-cierra en 5 segundos)
this.mensajeService.mostrarError('Ha ocurrido un error');

// Mensaje de advertencia (auto-cierra en 4 segundos)
this.mensajeService.mostrarAdvertencia('Por favor, completa todos los campos');

// Mensaje informativo (auto-cierra en 3 segundos)
this.mensajeService.mostrarInfo('Información importante');

// Mensaje personalizado
this.mensajeService.mostrarMensaje({
  texto: 'Mensaje personalizado',
  tipo: 'exito',
  titulo: 'Título personalizado',
  duracion: 0 // No auto-cerrar
});
```

### 3. Cerrar mensaje manualmente

```typescript
this.mensajeService.cerrarMensaje();
```

## Tipos de Mensaje

- `exito`: Verde con icono de check
- `error`: Rojo con icono de X
- `advertencia`: Amarillo con icono de triángulo
- `info`: Azul con icono de información

## Características

- **Responsive**: Se adapta a diferentes tamaños de pantalla
- **Animaciones**: Entrada y salida suaves
- **Auto-cierre**: Configurable por tipo de mensaje
- **Accesible**: Soporte para navegación por teclado
- **Consistente**: Diseño uniforme en toda la aplicación

## Integración

El componente `MensajeModalComponent` ya está incluido en el `app.component.html` y se mostrará automáticamente cuando se envíe un mensaje a través del servicio.

## Migración desde alert()

Reemplaza todas las llamadas a `alert()` por el método apropiado del servicio:

```typescript
// Antes
alert('Mensaje de éxito');

// Después
this.mensajeService.mostrarExito('Mensaje de éxito');
```

Esto proporciona una experiencia de usuario mucho mejor con mensajes estilizados y consistentes. 