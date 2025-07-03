import { TestBed } from '@angular/core/testing';
import { EventNotificationService } from './event-notification.service';

describe('EventNotificationService', () => {
  let service: EventNotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EventNotificationService]
    });
    service = TestBed.inject(EventNotificationService);
  });

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });
});
