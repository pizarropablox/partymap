import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { MensajeService, Mensaje } from './mensaje.service';

@Component({
  selector: 'app-mensaje-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="mensajeActual" class="modal-overlay" (click)="cerrarMensaje()">
      <div class="modal-content mensaje-modal" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <div class="modal-icon" [class]="mensajeActual.tipo">
            <svg *ngIf="mensajeActual.tipo === 'exito'" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 7L10 17L5 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
            </svg>
            <svg *ngIf="mensajeActual.tipo === 'error'" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="15" y1="9" x2="9" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="9" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg *ngIf="mensajeActual.tipo === 'advertencia'" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M10.29 3.86L1.82 18A2 2 0 0 0 3.54 21H20.46A2 2 0 0 0 22.18 18L13.71 3.86A2 2 0 0 0 10.29 3.86Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="12" y1="9" x2="12" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="12" y1="17" x2="12.01" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg *ngIf="mensajeActual.tipo === 'info'" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="12" y1="16" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="12" y1="8" x2="12.01" y2="8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3>{{ mensajeActual.titulo }}</h3>
          <button class="btn-cerrar" (click)="cerrarMensaje()">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="message-content">
            <p class="message-text">{{ mensajeActual.texto }}</p>
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn-aceptar" (click)="cerrarMensaje()">
            Aceptar
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
      animation: fadeIn 0.3s ease-out;
    }

    .modal-content {
      background: white;
      border-radius: 12px;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
      max-width: 450px;
      width: 90%;
      max-height: 90vh;
      overflow: hidden;
      animation: slideIn 0.3s ease-out;
    }

    .modal-header {
      display: flex;
      align-items: center;
      padding: 20px 24px;
      border-bottom: 1px solid #e5e7eb;
      gap: 12px;
    }

    .modal-icon {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid;
    }

    .modal-icon.exito {
      color: #059669;
      background: rgba(5, 150, 105, 0.1);
      border-color: #10b981;
    }

    .modal-icon.error {
      color: #dc2626;
      background: rgba(220, 38, 38, 0.1);
      border-color: #ef4444;
    }

    .modal-icon.advertencia {
      color: #d97706;
      background: rgba(217, 119, 6, 0.1);
      border-color: #f59e0b;
    }

    .modal-icon.info {
      color: #3b82f6;
      background: rgba(59, 130, 246, 0.1);
      border-color: #3b82f6;
    }

    .modal-header h3 {
      margin: 0;
      font-size: 1.25rem;
      font-weight: 600;
      color: #1f2937;
      flex: 1;
    }

    .btn-cerrar {
      background: none;
      border: none;
      color: #6b7280;
      cursor: pointer;
      padding: 8px;
      border-radius: 6px;
      transition: all 0.2s ease;
    }

    .btn-cerrar:hover {
      background: #f3f4f6;
      color: #374151;
    }

    .modal-body {
      padding: 24px;
    }

    .message-content {
      text-align: center;
    }

    .message-text {
      font-size: 1rem;
      line-height: 1.6;
      color: #374151;
      margin: 0;
      white-space: pre-line;
    }

    .modal-footer {
      padding: 20px 24px;
      border-top: 1px solid #e5e7eb;
      display: flex;
      justify-content: center;
    }

    .btn-aceptar {
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      color: white;
      border: none;
      padding: 12px 32px;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.2);
    }

    .btn-aceptar:hover {
      background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
      transform: translateY(-1px);
      box-shadow: 0 6px 12px -2px rgba(59, 130, 246, 0.3);
    }

    .btn-aceptar:active {
      transform: translateY(0);
    }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }

    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateY(-20px) scale(0.95);
      }
      to {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }

    @media (max-width: 768px) {
      .modal-content {
        margin: 20px;
        width: calc(100% - 40px);
      }
      
      .modal-header {
        padding: 16px 20px;
      }
      
      .modal-body {
        padding: 20px;
      }
      
      .modal-footer {
        padding: 16px 20px;
      }
      
      .btn-aceptar {
        width: 100%;
      }
    }
  `]
})
export class MensajeModalComponent implements OnInit, OnDestroy {
  mensajeActual: Mensaje | null = null;
  private subscription: Subscription | null = null;
  private timeoutId: any = null;

  constructor(private mensajeService: MensajeService) {}

  ngOnInit(): void {
    this.subscription = this.mensajeService.mensaje$.subscribe(mensaje => {
      this.mensajeActual = mensaje;
      
      // Limpiar timeout anterior si existe
      if (this.timeoutId) {
        clearTimeout(this.timeoutId);
        this.timeoutId = null;
      }
      
      // Auto-cerrar si tiene duración
      if (mensaje && mensaje.duracion && mensaje.duracion > 0) {
        this.timeoutId = setTimeout(() => {
          this.cerrarMensaje();
        }, mensaje.duracion);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
  }

  cerrarMensaje(): void {
    this.mensajeService.cerrarMensaje();
  }
} 