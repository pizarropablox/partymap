import { environment } from '../../environments/environment';

export class ApiEndpoints {
  private static baseUrl = environment.apiUrl;

  // ===== ENDPOINTS DE RESERVAS =====
  static readonly RESERVA = {
    ESTADISTICAS: `${this.baseUrl}/reserva/estadisticas`,
    ESTADISTICAS_BASICAS: `${this.baseUrl}/reserva/estadisticas-basicas`,
    CANTIDAD_MINIMA: `${this.baseUrl}/reserva/cantidad-minima`,
    BUSCAR: `${this.baseUrl}/reserva/buscar`,
    POR_EVENTO: (eventoId: number) => `${this.baseUrl}/reserva/evento/${eventoId}/usuario`,
    CANCELAR: (reservaId: number) => `${this.baseUrl}/reserva/${reservaId}/cancelar`,
    USUARIO: `${this.baseUrl}/reserva/usuario`,
    ALL: `${this.baseUrl}/reserva/all`,
    CREAR: `${this.baseUrl}/reserva/crear`,
  };

  // ===== ENDPOINTS DE USUARIOS =====
  static readonly USUARIO = {
    CURRENT: `${this.baseUrl}/usuario/current`,
    ALL: `${this.baseUrl}/usuario/all`,
    ESTADISTICAS: `${this.baseUrl}/usuario/estadisticas`,
    PRODUCTOR: (usuarioId: string) => `${this.baseUrl}/usuario/productor/${usuarioId}`,
    CREAR_PRODUCTOR: `${this.baseUrl}/usuario/crear-productor`,
    ACTUALIZAR: (usuarioId: number) => `${this.baseUrl}/usuario/actualizar/${usuarioId}`,
    ELIMINAR: (usuarioId: number) => `${this.baseUrl}/usuario/eliminar/${usuarioId}`,
  };

  // ===== ENDPOINTS DE EVENTOS =====
  static readonly EVENTO = {
    ALL: `${this.baseUrl}/evento/all`,
    CREAR: `${this.baseUrl}/evento/crear`,
    ACTUALIZAR: (eventoId: number) => `${this.baseUrl}/evento/actualizar/${eventoId}`,
    ELIMINAR: (eventoId: number) => `${this.baseUrl}/evento/eliminar/${eventoId}`,
    POR_USUARIO: (productorId: number) => `${this.baseUrl}/evento/usuario/${productorId}`,
    MIS_ESTADISTICAS: `${this.baseUrl}/evento/mis-estadisticas`,
  };

  // ===== ENDPOINTS DE UBICACIONES =====
  static readonly UBICACION = {
    ALL: `${this.baseUrl}/ubicacion/all`,
    BASE: `${this.baseUrl}/ubicacion`,
  };

  // ===== MÉTODOS UTILITARIOS =====
  static buildUrl(path: string): string {
    return `${this.baseUrl}${path}`;
  }

  static getBaseUrl(): string {
    return this.baseUrl;
  }
} 