import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MensajeService } from '../../shared/mensaje.service';
import { NavigationService } from '../../services/navigation.service';

interface Usuario {
  id: number;
  nombre: string;
  email: string;
  tipoUsuario: string;
  activo: number;
  fechaCreacion: string;
  rutProductor?: string; // Campo correcto del JSON
}

interface Productor {
  id: number;
  nombre: string;
  email: string;
  tipoUsuario: string;
  activo: number;
  fechaCreacion: string;
  rutProductor?: string; // Campo correcto del JSON
}

interface Evento {
  nombre: string;
  descripcion: string;
  fecha: string;
  hora: string;
  ubicacion: string;
  capacidad: number;
  precio: number;
  categoria: string;
}

@Component({
  selector: 'app-productor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './productor.component.html',
  styleUrls: ['./productor.component.css']
})
export class ProductorComponent implements OnInit, OnDestroy {
  // URL de Azure B2C para login
  private readonly AZURE_B2C_LOGIN_URL = 'https://duocdesarrollocloudnative.b2clogin.com/DuocDesarrolloCloudNative.onmicrosoft.com/oauth2/v2.0/authorize?p=B2C_1_DuocDesarrolloCloudNative_Login&client_id=ad16d15c-7d6e-4f58-8146-4b5b3d7b7124&nonce=defaultNonce&redirect_uri=http%3A%2F%2Flocalhost%3A4200&scope=openid&response_type=id_token&prompt=login';

  mostrarModalProductor = false;
  mostrarModalEvento = false;
  mostrarModalEdicion = false;
  mostrarModalConfirmacion = false;
  mostrarModalMensaje = false;
  
  // Formulario de productor
  nuevoProductor: Productor = {
    id: 0,
    nombre: '',
    email: '',
    tipoUsuario: '',
    activo: 1,
    fechaCreacion: new Date().toISOString()
  };

  // Formulario de evento
  nuevoEvento: Evento = {
    nombre: '',
    descripcion: '',
    fecha: '',
    hora: '',
    ubicacion: '',
    capacidad: 0,
    precio: 0,
    categoria: ''
  };

  // Productor en edición
  productorEnEdicion: Productor = {
    id: 0,
    nombre: '',
    email: '',
    tipoUsuario: '',
    activo: 1,
    fechaCreacion: '',
    rutProductor: ''
  };

  // Variables para el modal de confirmación
  productorAEliminar: Productor | null = null;
  mensajeConfirmacion = '';
  tipoMensaje: 'confirmacion' | 'exito' | 'error' | 'advertencia' = 'confirmacion';

  // Estados del formulario
  isSubmitting = false;
  formError = '';
  formSuccess = '';

  productores: Productor[] = [];
  productoresFiltrados: Productor[] = [];
  filtroProductorNombre = '';
  filtroProductorFecha = '';
  
  // Variables para paginación
  productoresPorPagina = 10;
  paginaActual = 1;
  totalPaginas = 1;
  productoresPaginados: Productor[] = [];
  
  usuarioActualId: number | null = null;
  userRole: string = '';
  cargando = false;
  error = '';
  
  // Timer para verificación automática del token
  private tokenCheckInterval: any;

  constructor(
    private router: Router,
    private http: HttpClient,
    private mensajeService: MensajeService,
    private navigation: NavigationService
  ) {}

  async ngOnInit(): Promise<void> {

    await this.obtenerUsuarioActualId();
    this.obtenerUserRole();
    this.cargarProductores(); // Carga inicial SÍ debe limpiar sesión si hay problemas
    this.iniciarVerificacionToken();
  }

  ngOnDestroy(): void {
    this.detenerVerificacionToken();
  }

  iniciarVerificacionToken(): void {
    // Verificar el token cada minuto (60000 ms)
    this.tokenCheckInterval = setInterval(() => {
      this.verificarTokenAutomaticamente();
    }, 60000);
    
    // También verificar inmediatamente al cargar
    this.verificarTokenAutomaticamente();
  }

  detenerVerificacionToken(): void {
    if (this.tokenCheckInterval) {
      clearInterval(this.tokenCheckInterval);
      this.tokenCheckInterval = null;
    }
  }

  verificarTokenAutomaticamente(): void {
    const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
    
    if (!token) {
      this.cerrarSesionAutomaticamente();
      return;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      
      if (payload.exp) {
        const exp = payload.exp * 1000;
        const ahora = Date.now();
        const tiempoRestante = exp - ahora;
        
        // Si el token expira en menos de 5 minutos, mostrar advertencia
        if (tiempoRestante < 5 * 60 * 1000 && tiempoRestante > 0) {
          this.mostrarAdvertenciaToken();
        }
        
        // Si el token ya expiró, cerrar sesión automáticamente
        if (tiempoRestante <= 0) {
          this.cerrarSesionAutomaticamente();
        }
      }
    } catch (error) {
      console.error('Error al verificar token automáticamente:', error);
      // Si hay error al decodificar, asumir que el token es inválido
      this.cerrarSesionAutomaticamente();
    }

  }

  mostrarAdvertenciaToken(): void {
    // Mostrar una alerta de que el token expira pronto
    if (confirm('Tu sesión expirará pronto. ¿Deseas renovar tu sesión?')) {
      // Redirigir al login para renovar
      this.irAlLogin();
    }
  }

  cerrarSesionAutomaticamente(): void {
    
    // Limpiar sesión
    this.limpiarSesion();
    
    // Mostrar mensaje al usuario
    this.mensajeService.mostrarAdvertencia('Tu sesión ha expirado. Serás redirigido al login.');
    
    // Redirigir al login después de un breve delay
    setTimeout(() => {
      this.irAlLogin();
    }, 1000);
  }

  async obtenerUsuarioActualId(): Promise<void> {
    
    try {
      // Verificar si hay token
      const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
      if (!token) {
        this.usuarioActualId = null;
        return;
      }

      // Configurar headers
      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      });

      // Obtener el usuario actual desde el endpoint
      const userResponse = await this.http.get<any>('http://18.235.227.189:8085/usuario/current', { headers }).toPromise();
      
      if (userResponse && userResponse.id) {
        this.usuarioActualId = userResponse.id;
      } else {
        console.error('No se pudo obtener el ID del usuario del endpoint');
        this.usuarioActualId = null;
      }
    } catch (error: any) {
      console.error('Error al obtener usuario actual:', error);
      
      // Fallback: intentar extraer del token
      const idToken = localStorage.getItem('idToken');
      let userId: number | null = null;
      
      if (idToken) {
        try {
          const payload = JSON.parse(atob(idToken.split('.')[1]));

          
          // Buscar el ID numérico del usuario en diferentes campos posibles
          const possibleId = payload.sub || payload.usuarioId || payload.userId || payload.id;
          
          if (possibleId && !isNaN(Number(possibleId))) {
            userId = Number(possibleId);

          }
        } catch (e) {
          console.error('Error al decodificar token:', e);
        }
      }
      
      // Si no se encuentra en el token, buscar en userInfo
      if (!userId) {
        const userInfo = localStorage.getItem('userInfo');
        if (userInfo) {
          try {
            const user = JSON.parse(userInfo);

            
            const possibleId = user.usuarioId || user.id || user.userId;
            if (possibleId && !isNaN(Number(possibleId))) {
              userId = Number(possibleId);

            }
          } catch (e) {
            console.error('Error al parsear userInfo:', e);
          }
        }
      }
      
      this.usuarioActualId = userId;

    }
  }

  obtenerUserRole(): void {
    // Obtener el rol del usuario desde el token o localStorage
    const idToken = localStorage.getItem('idToken');
    let role = '';
    if (idToken) {
      try {
        const payload = JSON.parse(atob(idToken.split('.')[1]));
        if (payload && payload.extension_Roles) {
          role = payload.extension_Roles;
        } else if (payload && payload.role) {
          role = payload.role;
        }
      } catch (e) {}
    }
    if (!role) {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo);
          if (user && user.role) {
            role = user.role;
          }
        } catch (e) {}
      }
    }
    this.userRole = role;
  }

  crearNuevoProductor(): void {
    this.nuevoProductor = {
      id: 0,
      nombre: '',
      email: '',
      tipoUsuario: '',
      activo: 1,
      fechaCreacion: new Date().toISOString()
    };
    this.mostrarModalProductor = true;
  }

  crearNuevoEvento(): void {
    this.nuevoEvento = {
      nombre: '',
      descripcion: '',
      fecha: '',
      hora: '',
      ubicacion: '',
      capacidad: 0,
      precio: 0,
      categoria: ''
    };
    this.mostrarModalEvento = true;
  }

  async guardarProductor(): Promise<void> {
    
    if (!this.nuevoProductor.nombre || !this.nuevoProductor.rutProductor) {
      this.mensajeService.mostrarAdvertencia('Por favor complete todos los campos obligatorios');
      return;
    }

    try {
      // Verificar autenticación de forma más flexible
      const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
      if (!token) {
        this.mensajeService.mostrarAdvertencia('No se encontró token de autenticación. Por favor, inicia sesión nuevamente.');
        this.limpiarSesion();
        return;
      }

      // Configurar headers primero
      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      });

      // Obtener el ID del usuario actual - ser más tolerante
      let usuarioId = this.usuarioActualId;
      
      if (!usuarioId) {
  
        try {
          // Obtener el usuario actual desde el endpoint
          const userResponse = await this.http.get<any>('http://18.235.227.189:8085/usuario/current', { headers }).toPromise();
          if (userResponse && userResponse.id) {
            usuarioId = userResponse.id;
            // Actualizar la variable de clase
            this.usuarioActualId = usuarioId;
          } else {
            console.error('No se pudo obtener el ID del usuario del endpoint');
            this.mensajeService.mostrarError('No se pudo obtener la información del usuario. Por favor, recarga la página e intenta nuevamente.');
            return;
          }
        } catch (userError: any) {
          console.error('Error al obtener usuario actual:', userError);
          
          // Si falla el endpoint, intentar extraer del token como fallback
          try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            
            // Buscar el ID numérico del usuario en diferentes campos posibles
            usuarioId = payload.sub || payload.usuarioId || payload.userId || payload.id;
            
            // Si no encontramos un ID numérico, intentar buscar en userInfo del localStorage
            if (!usuarioId || isNaN(Number(usuarioId))) {
              const userInfo = localStorage.getItem('userInfo');
              if (userInfo) {
                const user = JSON.parse(userInfo);
                usuarioId = user.id || user.usuarioId || user.userId;
              }
            }
            
            // Verificar que sea un número válido
            if (!usuarioId || isNaN(Number(usuarioId))) {
              console.error('No se pudo obtener un ID numérico válido del usuario');
              this.mensajeService.mostrarError('No se pudo obtener la información del usuario. Por favor, recarga la página e intenta nuevamente.');
              return;
            } else {
              // Convertir a número para asegurar que sea un Long
              usuarioId = Number(usuarioId);
              // Actualizar la variable de clase
              this.usuarioActualId = usuarioId;
            }
          } catch (e) {
            console.error('Error al extraer usuario ID del token:', e);
            this.mensajeService.mostrarError('Error al obtener información del usuario. Por favor, recarga la página e intenta nuevamente.');
            return;
          }
        }
      }

      const productorData = {
        nombre: this.nuevoProductor.nombre,
        email: this.nuevoProductor.email,
        tipoUsuario: 'productor', // Siempre será productor
        activo: 1,
        rutProductor: this.limpiarRut(this.nuevoProductor.rutProductor || ''), // Asegurar que el RUT esté limpio
        usuarioId: usuarioId
      };



      // Verificar si el usuario ya tiene un productor
      try {
        const productorExistente = await this.http.get<any>(`http://18.235.227.189:8085/usuario/productor/${usuarioId}`, { headers }).toPromise();
        if (productorExistente) {
          this.mensajeService.mostrarError('Ya existe un usuario para este ID. No se puede crear otro.');
          return;
        }
      } catch (verificacionError: any) {
        // Si el error es 404, significa que no existe un productor, lo cual está bien
        if (verificacionError.status !== 404) {
          console.error('Error al verificar productor existente:', verificacionError);
          // Continuar con la creación aunque falle la verificación
        }
      }

      // Realizar la petición POST al backend
              const response = await this.http.post<any>('http://18.235.227.189:8085/usuario/crear-productor', productorData, { headers }).toPromise();
      


      // Mostrar mensaje de éxito
      this.mensajeService.mostrarExito('Usuario creado exitosamente');
      this.cerrarModalProductor();
      
      // Actualizar la lista de productores
      try {
        await this.cargarProductores(false); // No limpiar sesión en caso de error
      } catch (error) {
        console.error('Error al recargar productores después de crear:', error);
        // No limpiar sesión aquí, solo mostrar error
        this.mensajeService.mostrarAdvertencia('Productor creado exitosamente, pero hubo un problema al actualizar la lista. Por favor, recarga la página.');
      }

    } catch (error: any) {
      console.error('Error al crear productor:', error);
      
      let mensajeError = 'Error al crear el productor. Por favor, inténtalo de nuevo.';
      
      if (error.status === 400) {
        // Error de validación del lado del servidor
        if (error.error && error.error.message) {
          mensajeError = error.error.message;
        } else if (error.error && typeof error.error === 'string') {
          mensajeError = error.error;
        } else {
          mensajeError = 'Los datos proporcionados no son válidos. Verifica que el RUT sea correcto y que no exista un productor para este usuario.';
        }
      } else if (error.status === 401) {
        mensajeError = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
        this.limpiarSesion();
      } else if (error.status === 403) {
        mensajeError = 'No tienes permisos para crear productores.';
      } else if (error.status === 409) {
        mensajeError = 'Ya existe un productor para este usuario.';
      } else if (error.status >= 500) {
        mensajeError = 'Error del servidor. Por favor, inténtalo más tarde.';
      } else if (error.error && error.error.message) {
        mensajeError = error.error.message;
      }
      
      this.mensajeService.mostrarError(mensajeError);
    }
  }

  async guardarEvento(): Promise<void> {
    if (!this.nuevoEvento.nombre || !this.nuevoEvento.descripcion || !this.nuevoEvento.fecha || 
          !this.nuevoEvento.hora || !this.nuevoEvento.ubicacion || !this.nuevoEvento.capacidad || 
          !this.nuevoEvento.precio || !this.nuevoEvento.categoria) {
      this.mensajeService.mostrarAdvertencia('Por favor complete todos los campos obligatorios');
      return;
    }

    try {
      const eventoData = {
        nombre: this.nuevoEvento.nombre,
        descripcion: this.nuevoEvento.descripcion,
        fecha: this.nuevoEvento.fecha,
        hora: this.nuevoEvento.hora,
        ubicacion: this.nuevoEvento.ubicacion,
        capacidad: this.nuevoEvento.capacidad,
        precio: this.nuevoEvento.precio,
        categoria: this.nuevoEvento.categoria
      };

      // Aquí iría la llamada real al backend
      // const response = await this.http.post('/api/eventos', eventoData).toPromise();
      
      this.mensajeService.mostrarExito('Evento creado exitosamente');
      this.cerrarModalEvento();
    } catch (error) {
      console.error('Error al crear evento:', error);
      this.mensajeService.mostrarError('Error al crear el evento');
    }
  }

  cerrarModalProductor(): void {
    this.mostrarModalProductor = false;
    this.nuevoProductor = {
      id: 0,
      nombre: '',
      email: '',
      tipoUsuario: '',
      activo: 1,
      fechaCreacion: new Date().toISOString()
    };
  }

  cerrarModalEvento(): void {
    this.mostrarModalEvento = false;
    this.nuevoEvento = {
      nombre: '',
      descripcion: '',
      fecha: '',
      hora: '',
      ubicacion: '',
      capacidad: 0,
      precio: 0,
      categoria: ''
    };
  }

  async cargarProductores(limpiarSesionEnError: boolean = true): Promise<void> {
    this.cargando = true;
    this.error = '';

    try {
      // Verificar si el usuario está autenticado
      if (!this.verificarAutenticacion()) {
        this.error = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
        if (limpiarSesionEnError) {
          this.limpiarSesion();
        }
        return;
      }

      // Obtener el token de autorización
      const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
      
      // Configurar headers
      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      });



      // Primero probar si el servidor está disponible
      try {
        const response = await this.http.get<any[]>('http://18.235.227.189:8085/usuario/all', { headers }).toPromise();
        

        
        if (response) {
          // Mapear todos los usuarios de la respuesta del backend
          this.productores = response.map(item => ({
            id: item.id || 0,
            nombre: item.nombre || '',
            email: item.email || '',
            tipoUsuario: item.tipoUsuario || '',
            activo: item.activo || 1,
            fechaCreacion: item.fechaCreacion || new Date().toISOString(),
            rutProductor: item.rutProductor || ''
          }));


          
          // Aplicar filtros
          this.aplicarFiltroProductores();
        } else {
          throw new Error('No se recibió respuesta del servidor');
        }
      } catch (httpError: any) {
        console.error('Error en primer intento:', httpError);
        
        // Si falla con autorización, intentar sin headers
        if (httpError.status === 401 || httpError.status === 403) {

          
          try {
            const responseWithoutAuth = await this.http.get<any[]>('http://18.235.227.189:8085/usuario/all').toPromise();
            
            if (responseWithoutAuth) {
              this.productores = responseWithoutAuth.map(item => ({
                id: item.id || 0,
                nombre: item.nombre || '',
                email: item.email || '',
                tipoUsuario: item.tipoUsuario || '',
                activo: item.activo || 1,
                fechaCreacion: item.fechaCreacion || new Date().toISOString(),
                rutProductor: item.rutProductor || ''
              }));
              
              this.aplicarFiltroProductores();
              return; // Salir si funcionó sin autorización
            }
          } catch (secondError) {
            console.error('Error en segundo intento:', secondError);
          }
        }
        
        throw httpError; // Re-lanzar el error original si no funcionó
      }
    } catch (error: any) {
      console.error('Error detallado al cargar usuarios:', error);
      
      // Determinar el tipo de error
      let mensajeError = 'Error al cargar los usuarios. Por favor, inténtalo de nuevo.';
      
      if (error.status === 0) {
        mensajeError = 'No se puede conectar con el servidor. Verifica que el backend esté ejecutándose en http://18.235.227.189:8085';
      } else if (error.status === 401) {
        mensajeError = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
        if (limpiarSesionEnError) {
          this.limpiarSesion();
        }
      } else if (error.status === 403) {
        mensajeError = 'Acceso denegado. No tienes permisos para ver estos usuarios.';
      } else if (error.status === 404) {
        mensajeError = 'El endpoint no fue encontrado. Verifica la URL del servidor.';
      } else if (error.status >= 500) {
        mensajeError = 'Error del servidor. Por favor, inténtalo más tarde.';
      } else if (error.message) {
        mensajeError = `Error: ${error.message}`;
      }
      
      this.error = mensajeError;
      
      // Solo limpiar sesión si se especifica y no es un error temporal
      if (limpiarSesionEnError && error.status !== 0 && error.status !== 500) {
        this.limpiarSesion();
      }
    } finally {
      this.cargando = false;
    }
  }

  verificarAutenticacion(): boolean {
    const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
    if (!token) {
      return false;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      
      // Si el token tiene campo de expiración, verificarlo
      if (payload.exp) {
        const exp = payload.exp * 1000;
        const ahora = Date.now();
        
        if (ahora >= exp) {
      return false;
    }
      }
      
      return true;
    } catch (error) {
      console.error('Error al verificar token:', error);
      return false;
    }
  }

  limpiarSesion(): void {

    
    // Limpiar todos los tokens del localStorage
    const tokensToRemove = [
      'jwt', 'idToken', 'userInfo', 'accessToken', 
      'msal.access_token.key', 'msal.id_token.key',
      'msal.client.info', 'msal.nonce.idtoken',
      'msal.state.login', 'msal.session.state',
      'msal.error', 'msal.error.description'
    ];
    
    tokensToRemove.forEach(token => {
      localStorage.removeItem(token);
      sessionStorage.removeItem(token);
    });
    
    // Limpiar también cualquier token en sessionStorage
    sessionStorage.clear();
    

  }

  aplicarFiltroProductores(): void {
    let filtrados = this.productores;

    // Filtrar por nombre de usuario o email
    if (this.filtroProductorNombre) {
      filtrados = filtrados.filter(usuario => 
        usuario.nombre.toLowerCase().includes(this.filtroProductorNombre.toLowerCase()) ||
        usuario.email.toLowerCase().includes(this.filtroProductorNombre.toLowerCase())
      );
    }

    this.productoresFiltrados = filtrados;
    this.paginaActual = 1; // Resetear a la primera página cuando se aplica un filtro
    this.aplicarPaginacion();
  }

  aplicarPaginacion(): void {
    // Calcular el total de páginas
    this.totalPaginas = Math.ceil(this.productoresFiltrados.length / this.productoresPorPagina);
    
    // Asegurar que la página actual esté dentro del rango válido
    if (this.paginaActual < 1) {
      this.paginaActual = 1;
    } else if (this.paginaActual > this.totalPaginas) {
      this.paginaActual = this.totalPaginas;
    }
    
    // Calcular el rango de usuarios a mostrar
    const inicio = (this.paginaActual - 1) * this.productoresPorPagina;
    const fin = inicio + this.productoresPorPagina;
    
    // Obtener los usuarios de la página actual
    this.productoresPaginados = this.productoresFiltrados.slice(inicio, fin);
  }

  irAPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas) {
      this.paginaActual = pagina;
      this.aplicarPaginacion();
    }
  }

  paginaAnterior(): void {
    if (this.paginaActual > 1) {
      this.paginaActual--;
      this.aplicarPaginacion();
    }
  }

  paginaSiguiente(): void {
    if (this.paginaActual < this.totalPaginas) {
      this.paginaActual++;
      this.aplicarPaginacion();
    }
  }

  obtenerRangoPagina(): string {
    const inicio = (this.paginaActual - 1) * this.productoresPorPagina + 1;
    const fin = Math.min(this.paginaActual * this.productoresPorPagina, this.productoresFiltrados.length);
    return `${inicio}-${fin} de ${this.productoresFiltrados.length} usuarios`;
  }

  limpiarFiltroProductores(): void {
    this.filtroProductorNombre = '';
    this.aplicarFiltroProductores();
  }

  formatearFechaInput(fecha: string): string {
    return new Date(fecha).toLocaleDateString('es-CL', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  }

  obtenerNombreCompletoUsuario(productor: Productor): string {
    return productor.nombre || 'Usuario no disponible';
  }

  obtenerEstadoProductor(productor: Productor): string {
    return productor.activo === 1 ? 'Activo' : 'Inactivo';
  }

  obtenerEstadoUsuario(productor: Productor): string {
    return productor.activo === 1 ? 'Activo' : 'Inactivo';
  }

  irAlLogin(): void {

    
    // Limpiar sesión primero
    this.limpiarSesion();
    
    // Redirigir directamente a Azure B2C
    this.navigation.goTo(this.AZURE_B2C_LOGIN_URL);
  }

  // Métodos para edición de productor
  editarProductor(productor: Productor): void {
    
    // Limpiar el RUT de puntos para el formato esperado por el backend
    const rutLimpio = this.limpiarRut(productor.rutProductor || '');
    
    // Copiar los datos del productor al formulario de edición
    this.productorEnEdicion = {
      id: productor.id,
      nombre: productor.nombre,
      email: productor.email,
      tipoUsuario: productor.tipoUsuario,
      activo: productor.activo,
      fechaCreacion: productor.fechaCreacion,
      rutProductor: rutLimpio
    };
    
    console.log('=== DATOS CARGADOS EN EL FORMULARIO ===');
    console.log('productorEnEdicion completo:', this.productorEnEdicion);
    console.log('RUT en el formulario:', this.productorEnEdicion.rutProductor);
    console.log('Nombre empresa en el formulario:', this.productorEnEdicion.nombre);
    
    // Limpiar mensajes previos
    this.formError = '';
    this.formSuccess = '';
    
    // Mostrar modal
    this.mostrarModalEdicion = true;
    console.log('Modal de edición abierto');
    console.log('=== FIN editarProductor ===');
  }

  // Método auxiliar para limpiar el RUT de puntos
  private limpiarRut(rut: string): string {
    return rut.replace(/\./g, '').replace(/-/g, '');
  }

  // Método para limpiar el RUT en tiempo real mientras el usuario escribe
  limpiarRutEnInput(event: any): void {
    const input = event.target;
    const valorOriginal = input.value;
    
    // Limpiar puntos y guiones
    const valorLimpio = this.limpiarRut(valorOriginal);
    
    // Solo actualizar si el valor cambió
    if (valorOriginal !== valorLimpio) {
  
      this.productorEnEdicion.rutProductor = valorLimpio;
      // Forzar la actualización del input
      setTimeout(() => {
        input.value = valorLimpio;
      }, 0);
    }
  }

  async verificarEventosProductor(productorId: number): Promise<boolean> {
    try {
      const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
      if (!token) {
        return false;
      }

      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      });

      // Hacer una llamada al endpoint para verificar si el productor tiene eventos
      const response = await this.http.get<any[]>(
        `http://18.235.227.189:8085/evento/usuario/${productorId}`,
        { headers }
      ).toPromise();

      return !!(response && response.length > 0);
    } catch (error) {
      console.error('Error al verificar eventos del productor:', error);
      return false; // En caso de error, asumimos que sí tiene eventos por seguridad
    }
  }

  async eliminarProductorConVerificacion(productor: Productor): Promise<void> {

    
    try {
      // Verificar si tiene eventos
      const tieneEventos = await this.verificarEventosProductor(productor.id);
      
      if (tieneEventos) {
        this.mensajeService.mostrarAdvertencia(
          `No se puede eliminar el usuario "${productor.nombre}" porque tiene eventos asociados.`
        );
        return;
      }
      
      // Si no tiene eventos, mostrar confirmación
      this.mostrarConfirmacionEliminacion(productor);
      
    } catch (error) {
      console.error('Error al verificar eventos del productor:', error);
      // Si falla la verificación, usar el método original
      this.eliminarProductor(productor);
    }
  }

  mostrarConfirmacionEliminacion(productor: Productor): void {
    this.productorAEliminar = productor;
    this.mensajeConfirmacion = `¿Estás seguro de que quieres eliminar el usuario "${productor.nombre}"?\n\nEsta acción no se puede deshacer.`;
    this.tipoMensaje = 'confirmacion';
    this.mostrarModalConfirmacion = true;
  }

  async confirmarEliminacion(): Promise<void> {
    if (!this.productorAEliminar) return;
    
    this.mostrarModalConfirmacion = false;
    await this.realizarEliminacion(this.productorAEliminar);
    this.productorAEliminar = null;
  }

  cancelarEliminacion(): void {
    this.mostrarModalConfirmacion = false;
    this.productorAEliminar = null;
  }

  cerrarModalMensaje(): void {
    this.mostrarModalMensaje = false;
  }

  eliminarProductor(productor: Productor): void {

    
    // Verificar si el productor tiene eventos asociados (esto es una verificación básica del frontend)
    // En un caso real, deberías hacer una llamada al backend para verificar esto
    const mensajeConfirmacion = `¿Estás seguro de que quieres eliminar el usuario "${productor.nombre}"?\n\n` +
      `⚠️ ADVERTENCIA: Si este usuario tiene eventos asociados, la eliminación fallará.\n` +
      `Primero debe eliminar todos los eventos del usuario antes de poder eliminarlo.\n\n` +
      `¿Deseas continuar?`;
    
    // Confirmar eliminación
    if (confirm(mensajeConfirmacion)) {
      this.realizarEliminacion(productor);
    }
  }

  async realizarEliminacion(productor: Productor): Promise<void> {
    try {
      // Verificar autenticación
      if (!this.verificarAutenticacion()) {
        this.mensajeService.mostrarError('Sesión expirada. Por favor, inicia sesión nuevamente.');
        this.limpiarSesion();
        return;
      }

      // Obtener token
      const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      });



      // Realizar la petición DELETE al endpoint correcto
      const response = await this.http.delete<any>(
        `http://18.235.227.189:8085/usuario/eliminar/${productor.id}`,
        { headers }
      ).toPromise();



      // Mostrar mensaje de éxito
      this.mensajeService.mostrarExito('Usuario eliminado exitosamente');
      
      // Actualizar la lista de productores de forma segura
      try {
        await this.cargarProductores(false); // No limpiar sesión automáticamente
      } catch (error) {
        console.error('Error al recargar productores después de eliminar:', error);
        this.mensajeService.mostrarAdvertencia('Productor eliminado exitosamente, pero hubo un problema al actualizar la lista. Por favor, recarga la página.');
      }

    } catch (error: any) {
      console.error('Error al eliminar productor:', error);
      
      let mensajeError = 'Error al eliminar el productor. Por favor, inténtalo de nuevo.';
      let tipoError: 'error' | 'advertencia' = 'error';
      
      if (error.status === 401) {
        mensajeError = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
        this.limpiarSesion();
      } else if (error.status === 403) {
        mensajeError = 'No tienes permisos para eliminar este productor.';
      } else if (error.status === 404) {
        mensajeError = 'Productor no encontrado.';
      } else if (error.status === 409) {
        mensajeError = 'No se puede eliminar el productor porque tiene eventos asociados. Primero debe eliminar todos los eventos del productor.';
        tipoError = 'advertencia';
      } else if (error.status >= 500) {
        // Verificar si el error del servidor es específico sobre eventos asociados
        if (error.error && error.error.message) {
          if (error.error.message.toLowerCase().includes('evento') || 
              error.error.message.toLowerCase().includes('eventos') ||
              error.error.message.toLowerCase().includes('asociado') ||
              error.error.message.toLowerCase().includes('constraint')) {
            mensajeError = 'No se puede eliminar el productor porque tiene eventos asociados. Primero debe eliminar todos los eventos del productor.';
            tipoError = 'advertencia';
          } else {
            mensajeError = error.error.message;
          }
        } else {
          mensajeError = 'Error del servidor. El productor no se puede eliminar porque tiene eventos asociados o hay un problema en el servidor.';
        }
      } else if (error.error && error.error.message) {
        mensajeError = error.error.message;
      }
      
      this.mensajeService.mostrarError(mensajeError);
    }
  }

  cerrarModalEdicion(): void {
    this.mostrarModalEdicion = false;
    this.formError = '';
    this.formSuccess = '';
    this.isSubmitting = false;
  }

    async guardarEdicion(): Promise<void> {
    if (!this.validarFormularioEdicion()) {
      return;
    }

    this.isSubmitting = true;
    this.formError = '';
    this.formSuccess = '';

    try {
      // Verificar autenticación
      if (!this.verificarAutenticacion()) {
        this.formError = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
        this.limpiarSesion();
        return;
      }

      // Obtener token
      const token = localStorage.getItem('idToken') || localStorage.getItem('accessToken');
      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      });

      // Preparar datos para enviar
      const rutLimpio = this.limpiarRut(this.productorEnEdicion.rutProductor || '');
      const datosActualizados = {
        id: this.productorEnEdicion.id,
        nombre: this.productorEnEdicion.nombre,
        email: this.productorEnEdicion.email,
        tipoUsuario: 'productor', // Siempre será productor
        activo: this.productorEnEdicion.activo,
        rutProductor: rutLimpio,
        usuarioId: this.usuarioActualId
      };



      // Realizar la petición PUT
      const response = await this.http.put<any>(
        `http://18.235.227.189:8085/usuario/actualizar/${this.productorEnEdicion.id}`,
        datosActualizados,
        { headers }
      ).toPromise();



      if (response) {
        this.formSuccess = 'Usuario actualizado exitosamente';
        
        // Actualizar la lista de productores
        await this.cargarProductores(false); // No limpiar sesión en caso de error
        
        // Cerrar modal después de un breve delay
        setTimeout(() => {
          this.cerrarModalEdicion();
        }, 2000);
      }

    } catch (error: any) {
      console.error('❌ Error al actualizar productor:', error);
      
      
      // Si hay respuesta del servidor, mostrarla
      if (error.error) {

      }
      
      let mensajeError = 'Error al actualizar el productor. Por favor, inténtalo de nuevo.';
      
      if (error.status === 401) {
        mensajeError = 'Sesión expirada. Por favor, inicia sesión nuevamente.';
        this.limpiarSesion();
      } else if (error.status === 403) {
        mensajeError = 'No tienes permisos para editar este productor.';
      } else if (error.status === 404) {
        mensajeError = 'Productor no encontrado.';
      } else if (error.status === 400) {
        // Error de validación del backend
        if (error.error && error.error.message) {
          mensajeError = error.error.message;
        } else if (error.error && typeof error.error === 'string') {
          mensajeError = error.error;
        } else {
          mensajeError = 'Los datos proporcionados no son válidos. Verifica la información ingresada.';
        }
      } else if (error.status >= 500) {
        mensajeError = 'Error del servidor. Por favor, inténtalo más tarde.';
      } else if (error.error && error.error.message) {
        mensajeError = error.error.message;
      }
      
      this.formError = mensajeError;
    } finally {
      this.isSubmitting = false;
  
    }
  }

  validarFormularioEdicion(): boolean {
    
    if (!this.productorEnEdicion.nombre?.trim()) {
      this.formError = 'El nombre del productor es obligatorio.';
      return false;
    }

    if (!this.productorEnEdicion.rutProductor?.trim()) {
      this.formError = 'El RUT es obligatorio.';
      return false;
    }

    // Validación básica: solo verificar que no esté vacío
    const rutParaValidar = this.productorEnEdicion.rutProductor.trim();
    if (!rutParaValidar) {
      this.formError = 'El RUT es obligatorio.';
      return false;
    }

    this.formError = '';
    return true;
  }
} 