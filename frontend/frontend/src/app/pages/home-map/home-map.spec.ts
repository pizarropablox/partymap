import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeMap } from './home-map';

describe('HomeMap', () => {
  let component: HomeMap;
  let fixture: ComponentFixture<HomeMap>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeMap]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HomeMap);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
