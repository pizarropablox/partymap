import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { FileStorageService } from './file-storage.service';

@Injectable({
  providedIn: 'root'
})
export class FileUploadService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  constructor(private fileStorageService: FileStorageService) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt') || localStorage.getItem('idToken');
    
    if (!token) {
      throw new Error('No hay token de autenticación disponible');
    }

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  /**
   * Guarda una imagen localmente y devuelve la ruta
   * @param file Archivo de imagen a guardar
   * @returns Observable con la ruta de la imagen guardada
   */
  saveImageLocally(file: File): Observable<{ imagePath: string }> {
    return new Observable(observer => {
      // Generar nombre único para el archivo
      const timestamp = new Date().getTime();
      const randomId = Math.random().toString(36).substring(2, 15);
      const fileExtension = file.name.split('.').pop();
      const fileName = `evento_${timestamp}_${randomId}.${fileExtension}`;
      const imagePath = `/images/eventos/${fileName}`;

      // Guardar en localStorage
      this.fileStorageService.saveImageToStorage(file, imagePath)
        .then(() => {
    
          observer.next({ imagePath });
          observer.complete();
        })
        .catch(error => {
          console.error('Error al guardar imagen:', error);
          observer.error(error);
        });
    });
  }

  /**
   * Convierte un archivo a base64 para preview
   * @param file Archivo a convertir
   * @returns Promise con el string base64
   */
  fileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = error => reject(error);
    });
  }

  /**
   * Valida si el archivo es una imagen válida
   * @param file Archivo a validar
   * @returns true si es válido, false en caso contrario
   */
  validateImageFile(file: File): boolean {
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    const maxSize = 5 * 1024 * 1024; // 5MB
    
    if (!allowedTypes.includes(file.type)) {
      return false;
    }
    
    if (file.size > maxSize) {
      return false;
    }
    
    return true;
  }

  /**
   * Método para subir imagen al servidor (mantenido para compatibilidad futura)
   * @param file Archivo de imagen a subir
   * @returns Observable con la ruta de la imagen subida
   */
  uploadImage(file: File): Observable<{ imagePath: string }> {
    // Por ahora, usar el método local
    return this.saveImageLocally(file);
  }

  /**
   * Obtiene la URL de una imagen almacenada
   * @param imagePath Ruta de la imagen
   * @returns URL de la imagen o null si no existe
   */
  getImageUrl(imagePath: string): string | null {
    return this.fileStorageService.getImageFromStorage(imagePath);
  }
} 