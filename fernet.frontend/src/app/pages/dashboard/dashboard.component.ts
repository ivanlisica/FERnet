import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-container">
      <header class="header">
        <h1>Dashboard</h1>
        <button class="logout-btn" (click)="logout()">Odjavi se</button>
      </header>

      <main class="main-content">
        <div class="welcome-card">
          <h2>Dobrodošli!</h2>
          <p>Uspešno ste se prijavili u sistem.</p>
        </div>

        <div class="info-card">
          <h3>Informacije o sesiji</h3>
          <p><strong>Token:</strong> {{ token ? 'Aktivan' : 'Neaktivan' }}</p>
          <p><strong>Status:</strong> Prijavljen</p>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .dashboard-container {
      min-height: 100vh;
      background: #f5f5f5;
    }

    .header {
      background: white;
      padding: 20px 40px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .header h1 {
      color: #333;
      margin: 0;
    }

    .logout-btn {
      padding: 10px 20px;
      background: #e74c3c;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-weight: 500;
      transition: background 0.3s ease;
    }

    .logout-btn:hover {
      background: #c0392b;
    }

    .main-content {
      padding: 40px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .welcome-card, .info-card {
      background: white;
      padding: 30px;
      border-radius: 10px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      margin-bottom: 20px;
    }

    .welcome-card h2, .info-card h3 {
      color: #333;
      margin-bottom: 15px;
    }

    .welcome-card p, .info-card p {
      color: #666;
      line-height: 1.6;
    }

    .info-card p {
      margin-bottom: 10px;
    }
  `]
})
export class DashboardComponent {
  token: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.token = this.authService.getToken();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
