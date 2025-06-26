import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router'; // 👈 ESTA LÍNEA es la clave

@Component({
  selector: 'html-app',
  standalone: true,
  imports: [RouterOutlet], // 👈 IMPORTAMOS RouterOutlet AQUÍ
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {}
