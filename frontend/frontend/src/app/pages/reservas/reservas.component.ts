import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ReservaService, ReservaResponse } from '../../services/reserva.service';
import { ApiEndpoints } from '../../config/api-endpoints';

interface UsuarioActual {
  id: number;
  nombre: string;
  email: string;
  tipoUsuario: string;
  activo: boolean;
  fechaCreacion?: string;
}

@Component({
  selector: 'app-reservas',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './reservas.component.html',
  styleUrls: ['./reservas.component.css']
})
export class ReservasComponent implements OnInit {
  private reservaService = inject(ReservaService);
  private http = inject(HttpClient);

  // 📋 Lista de reservas
  reservas: ReservaResponse[] = [];
  
  // 👤 Información del usuario actual
  usuarioActual: UsuarioActual | null = null;
  
  // 📱 Estado de carga
  isLoading = true;
  errorMessage = '';
  
  // 🔄 Paginación
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;

  // 🎯 Modal de confirmación
  mostrarModalCancelacion = false;
  reservaACancelar: ReservaResponse | null = null;

  constructor() {}

  ngOnInit() {
    this.cargarUsuarioActual();
  }

  /**
   * Carga la información del usuario actual
   */
  cargarUsuarioActual() {

    
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    if (!token) {
      console.error('No hay token de autenticación disponible');
      this.errorMessage = 'No hay token de autenticación disponible';
      this.isLoading = false;
      return;
    }

    

    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };

    

    this.http.get<UsuarioActual>(ApiEndpoints.USUARIO.CURRENT, { headers })
      .subscribe({
        next: (usuario) => {
  
          this.usuarioActual = usuario;
          this.cargarReservas();
        },
        error: (error) => {
          console.error('Error al cargar usuario actual:', error);
          console.error('Status:', error.status);
          console.error('Message:', error.message);
          this.errorMessage = 'Error al cargar información del usuario';
          this.isLoading = false;
        }
      });
  }

  /**
   * Carga las reservas desde el backend según el rol del usuario
   */
  cargarReservas() {

    
    this.isLoading = true;
    this.errorMessage = '';

    // Usar el mismo endpoint para todos, pero filtrar según el rol
    this.reservaService.obtenerReservas().subscribe({
      next: (data) => {

        
        // Filtrar reservas según el rol del usuario
        const userRole = this.usuarioActual?.tipoUsuario?.toLowerCase() || '';
        
        
        if (userRole === 'administrador') {
          // Los administradores ven todas las reservas
          this.reservas = data;
          
        } else {
          // Los clientes solo ven sus propias reservas
          const userId = this.usuarioActual?.id;
          
          this.reservas = data.filter(reserva => reserva.usuario?.id === userId);
          
        }

        // Ordenar reservas de la más reciente a la más antigua por fecha de creación
        this.reservas = this.reservas.sort((a, b) => {
          const fechaA = new Date(a.fechaCreacion).getTime();
          const fechaB = new Date(b.fechaCreacion).getTime();
          return fechaB - fechaA; // Orden descendente (más reciente primero)
        });
        
        this.calcularPaginacion();
        this.isLoading = false;
  
      },
      error: (error) => {
        console.error('Error detallado al cargar reservas:', error);
        console.error('Status:', error.status);
        console.error('Message:', error.message);
        console.error('URL:', error.url);
        
        this.errorMessage = 'Error al cargar las reservas. Verifica tu conexión y permisos.';
        this.isLoading = false;
      }
    });
  }

  /**
   * Calcula la paginación
   */
  calcularPaginacion() {
    this.totalPages = Math.ceil(this.reservas.length / this.itemsPerPage);
  }

  /**
   * Obtiene las reservas paginadas
   */
  getReservasPaginadas(): ReservaResponse[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return this.reservas.slice(startIndex, endIndex);
  }

  /**
   * Cambia de página
   */
  cambiarPagina(pagina: number) {
    if (pagina >= 1 && pagina <= this.totalPages) {
      this.currentPage = pagina;
    }
  }

  /**
   * Formatea la fecha
   */
  formatearFecha(fecha: string): string {
    if (!fecha) return 'N/A';
    
    try {
      const date = new Date(fecha);
      return date.toLocaleDateString('es-CL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return fecha;
    }
  }

  /**
   * Formatea el precio
   */
  formatearPrecio(precio: number): string {
    if (!precio) return 'Gratis';
    return new Intl.NumberFormat('es-CL', {
      style: 'currency',
      currency: 'CLP'
    }).format(precio);
  }

  /**
   * Obtiene el color del estado
   */
  getEstadoColor(estado: string): string {
    switch (estado?.toLowerCase()) {
      case 'confirmada':
      case 'confirmed':
        return 'success';
      case 'pendiente':
      case 'pending':
        return 'warning';
      case 'cancelada':
      case 'cancelled':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  /**
   * Obtiene el texto del estado
   */
  getEstadoTexto(estado: string): string {
    switch (estado?.toLowerCase()) {
      case 'confirmada':
      case 'confirmed':
        return 'Confirmada';
      case 'pendiente':
      case 'pending':
        return 'Pendiente';
      case 'cancelada':
      case 'cancelled':
        return 'Cancelada';
      default:
        return estado || 'Desconocido';
    }
  }

  /**
   * Refresca la lista de reservas
   */
  refrescarReservas() {
    this.cargarUsuarioActual();
  }

  /**
   * Verifica si el usuario puede cancelar una reserva específica
   */
  puedeCancelarReserva(reserva: ReservaResponse): boolean {
    // Si no hay información del usuario actual, no puede cancelar
    if (!this.usuarioActual) {
      return false;
    }

    // Si la reserva ya está cancelada, no se puede cancelar
    if (reserva.estado === 'CANCELADA' || !reserva.activo) {
      return false;
    }

    // Administradores pueden cancelar cualquier reserva
    if (this.usuarioActual.tipoUsuario === 'ADMINISTRADOR' || this.usuarioActual.tipoUsuario === 'ADMIN') {
      return true;
    }

    // Usuarios normales solo pueden cancelar sus propias reservas
    if (this.usuarioActual.tipoUsuario === 'CLIENTE' || this.usuarioActual.tipoUsuario === 'USUARIO') {
      return this.usuarioActual.id === reserva.usuario.id;
    }

    return false;
  }

  /**
   * Cancela una reserva
   */
  cancelarReserva(reserva: ReservaResponse) {
    this.reservaACancelar = reserva;
    this.mostrarModalCancelacion = true;
  }

  /**
   * Confirma la cancelación de la reserva
   */
  confirmarCancelacion() {
    if (!this.reservaACancelar) return;

    this.reservaService.cancelarReserva(this.reservaACancelar.id).subscribe({
      next: () => {
        // Actualizar el estado de la reserva localmente
        const index = this.reservas.findIndex(r => r.id === this.reservaACancelar!.id);
        if (index !== -1) {
          this.reservas[index].estado = 'CANCELADA';
          this.reservas[index].activo = 0;
        }
        this.cerrarModalCancelacion();
      },
      error: (error) => {
        alert('Error al cancelar la reserva. Inténtalo de nuevo.');
        this.cerrarModalCancelacion();
      }
    });
  }

  /**
   * Cierra el modal de cancelación
   */
  cerrarModalCancelacion() {
    this.mostrarModalCancelacion = false;
    this.reservaACancelar = null;
  }
} 