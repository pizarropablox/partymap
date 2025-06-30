import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EventNotificationService {
  private eventCreatedSubject = new Subject<void>();
  public eventCreated$ = this.eventCreatedSubject.asObservable();

  constructor() {}

  /**
   * Notifica que se ha creado un nuevo evento
   */
  notifyEventCreated() {
    this.eventCreatedSubject.next();
  }
} 