import { Injectable, Optional } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { BehaviorSubject, Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(@Optional() private msalService: MsalService) {
    // Si MSAL no está disponible, establecer como no autenticado
    if (!this.msalService) {
      console.warn('MSAL Service not available - running in fallback mode');
      this.isAuthenticatedSubject.next(false);
    } else {
      // Verificar si hay cuentas activas
      const accounts = this.msalService.instance.getAllAccounts();
      this.isAuthenticatedSubject.next(accounts.length > 0);
    }
  }

  // Métodos seguros que funcionan con o sin MSAL
  public isAuthenticated(): boolean {
    if (!this.msalService) {
      return false;
    }
    return this.msalService.instance.getAllAccounts().length > 0;
  }

  public getActiveAccount(): any {
    if (!this.msalService) {
      return null;
    }
    return this.msalService.instance.getActiveAccount();
  }

  public getAllAccounts(): any[] {
    if (!this.msalService) {
      return [];
    }
    return this.msalService.instance.getAllAccounts();
  }

  public login(): Observable<any> {
    if (!this.msalService) {
      console.warn('MSAL not available - cannot perform login');
      return of(null);
    }
    return this.msalService.loginRedirect();
  }

  public logout(): Observable<any> {
    if (!this.msalService) {
      console.warn('MSAL not available - cannot perform logout');
      return of(null);
    }
    return this.msalService.logoutPopup();
  }

  public acquireTokenSilent(request: any): Observable<any> {
    if (!this.msalService) {
      console.warn('MSAL not available - cannot acquire token');
      return of(null);
    }
    return this.msalService.acquireTokenSilent(request);
  }

  public acquireTokenPopup(request: any): Observable<any> {
    if (!this.msalService) {
      console.warn('MSAL not available - cannot acquire token');
      return of(null);
    }
    return this.msalService.acquireTokenPopup(request);
  }

  public handleRedirectPromise(): Promise<any> {
    if (!this.msalService) {
      return Promise.resolve(null);
    }
    return this.msalService.instance.handleRedirectPromise();
  }

  public setActiveAccount(account: any): void {
    if (!this.msalService) {
      return;
    }
    this.msalService.instance.setActiveAccount(account);
  }

  public clearCache(): void {
    if (!this.msalService) {
      return;
    }
    this.msalService.instance.clearCache();
  }

  public enableAccountStorageEvents(): void {
    if (!this.msalService) {
      return;
    }
    this.msalService.instance.enableAccountStorageEvents();
  }

  public initialize(): Promise<void> {
    if (!this.msalService) {
      console.warn('MSAL not available - skipping initialization');
      return Promise.resolve();
    }
    return this.msalService.initialize().toPromise();
  }
} 