import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class FileStorageService {
  private readonly STORAGE_KEY = 'event_images';

  constructor() {}

  /**
   * Guarda una imagen en el localStorage como base64
   * @param file Archivo de imagen
   * @param imagePath Ruta donde se guardaría la imagen
   * @returns Promise con la ruta de la imagen
   */
  async saveImageToStorage(file: File, imagePath: string): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        try {
          const base64Data = reader.result as string;
          const imageData = {
            path: imagePath,
            data: base64Data,
            timestamp: new Date().toISOString()
          };

          // Obtener imágenes existentes
          const existingImages = this.getStoredImages();
          existingImages[imagePath] = imageData;

          // Guardar en localStorage
          localStorage.setItem(this.STORAGE_KEY, JSON.stringify(existingImages));
          
    
          resolve(imagePath);
        } catch (error) {
          reject(error);
        }
      };
      reader.onerror = () => reject(reader.error);
      reader.readAsDataURL(file);
    });
  }

  /**
   * Obtiene una imagen del localStorage
   * @param imagePath Ruta de la imagen
   * @returns Base64 de la imagen o null si no existe
   */
  getImageFromStorage(imagePath: string): string | null {
    const storedImages = this.getStoredImages();
    const imageData = storedImages[imagePath];
    return imageData ? imageData.data : null;
  }

  /**
   * Obtiene todas las imágenes almacenadas
   * @returns Objeto con todas las imágenes
   */
  getStoredImages(): { [key: string]: any } {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      return stored ? JSON.parse(stored) : {};
    } catch (error) {
      console.error('Error al obtener imágenes almacenadas:', error);
      return {};
    }
  }

  /**
   * Elimina una imagen del localStorage
   * @param imagePath Ruta de la imagen a eliminar
   */
  removeImageFromStorage(imagePath: string): void {
    const storedImages = this.getStoredImages();
    delete storedImages[imagePath];
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(storedImages));
    
  }

  /**
   * Limpia todas las imágenes del localStorage
   */
  clearAllImages(): void {
    localStorage.removeItem(this.STORAGE_KEY);

  }

  /**
   * Verifica si una imagen existe en el almacenamiento
   * @param imagePath Ruta de la imagen
   * @returns true si existe, false en caso contrario
   */
  imageExists(imagePath: string): boolean {
    const storedImages = this.getStoredImages();
    return imagePath in storedImages;
  }
} 