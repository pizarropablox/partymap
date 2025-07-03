import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeMapComponent } from './home-map.component';
import { CommonModule } from '@angular/common';

describe('HomeMapComponent', () => {
  let component: HomeMapComponent;
  let fixture: ComponentFixture<HomeMapComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, HomeMapComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse correctamente', () => {
    expect(component).toBeTruthy();
  });
});
