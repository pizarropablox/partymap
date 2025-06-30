import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Mensaje {
  texto: string;
  tipo: 'exito' | 'error' | 'advertencia' | 'info';
  titulo?: string;
  duracion?: number; // en milisegundos, 0 = no auto-cerrar
}

@Injectable({
  providedIn: 'root'
})
export class MensajeService {
  private mensajeSubject = new BehaviorSubject<Mensaje | null>(null);
  public mensaje$: Observable<Mensaje | null> = this.mensajeSubject.asObservable();

  constructor() { }

  /**
   * Muestra un mensaje de éxito
   */
  mostrarExito(texto: string, titulo?: string, duracion: number = 3000): void {
    this.mostrarMensaje({
      texto,
      tipo: 'exito',
      titulo: titulo || 'Éxito',
      duracion
    });
  }

  /**
   * Muestra un mensaje de error
   */
  mostrarError(texto: string, titulo?: string, duracion: number = 5000): void {
    this.mostrarMensaje({
      texto,
      tipo: 'error',
      titulo: titulo || 'Error',
      duracion
    });
  }

  /**
   * Muestra un mensaje de advertencia
   */
  mostrarAdvertencia(texto: string, titulo?: string, duracion: number = 4000): void {
    this.mostrarMensaje({
      texto,
      tipo: 'advertencia',
      titulo: titulo || 'Advertencia',
      duracion
    });
  }

  /**
   * Muestra un mensaje informativo
   */
  mostrarInfo(texto: string, titulo?: string, duracion: number = 3000): void {
    this.mostrarMensaje({
      texto,
      tipo: 'info',
      titulo: titulo || 'Información',
      duracion
    });
  }

  /**
   * Muestra un mensaje personalizado
   */
  mostrarMensaje(mensaje: Mensaje): void {
    this.mensajeSubject.next(mensaje);
  }

  /**
   * Cierra el mensaje actual
   */
  cerrarMensaje(): void {
    this.mensajeSubject.next(null);
  }

  /**
   * Muestra un mensaje de confirmación (para reemplazar confirm())
   */
  mostrarConfirmacion(texto: string, titulo?: string): Promise<boolean> {
    return new Promise((resolve) => {
      // Por ahora, usamos confirm nativo, pero se puede extender para usar un modal personalizado
      const resultado = confirm(texto);
      resolve(resultado);
    });
  }
} 