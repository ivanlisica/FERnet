import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="register-container">
      <div class="register-card">
        <h2>Registracija</h2>

        <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
          <div class="form-row">
            <div class="form-group">
              <label for="firstName">Ime:</label>
              <input
                type="text"
                id="firstName"
                formControlName="firstName"
                class="form-control"
                [class.error]="registerForm.get('firstName')?.invalid && registerForm.get('firstName')?.touched"
              >
              <div class="error-message" *ngIf="registerForm.get('firstName')?.invalid && registerForm.get('firstName')?.touched">
                Ime je obavezno
              </div>
            </div>

            <div class="form-group">
              <label for="lastName">Prezime:</label>
              <input
                type="text"
                id="lastName"
                formControlName="lastName"
                class="form-control"
                [class.error]="registerForm.get('lastName')?.invalid && registerForm.get('lastName')?.touched"
              >
              <div class="error-message" *ngIf="registerForm.get('lastName')?.invalid && registerForm.get('lastName')?.touched">
                Prezime je obavezno
              </div>
            </div>
          </div>

          <div class="form-group">
            <label for="username">Korisničko ime:</label>
            <input
              type="text"
              id="username"
              formControlName="username"
              class="form-control"
              [class.error]="registerForm.get('username')?.invalid && registerForm.get('username')?.touched"
            >
            <div class="error-message" *ngIf="registerForm.get('username')?.invalid && registerForm.get('username')?.touched">
              <span *ngIf="registerForm.get('username')?.errors?.['required']">Korisničko ime je obavezno</span>
              <span *ngIf="registerForm.get('username')?.errors?.['minlength']">Korisničko ime mora imati najmanje 3 karaktera</span>
            </div>
          </div>

          <div class="form-group">
            <label for="email">Email:</label>
            <input
              type="email"
              id="email"
              formControlName="email"
              class="form-control"
              [class.error]="registerForm.get('email')?.invalid && registerForm.get('email')?.touched"
            >
            <div class="error-message" *ngIf="registerForm.get('email')?.invalid && registerForm.get('email')?.touched">
              <span *ngIf="registerForm.get('email')?.errors?.['required']">Email je obavezan</span>
              <span *ngIf="registerForm.get('email')?.errors?.['email']">Email mora biti u ispravnom formatu</span>
            </div>
          </div>

          <div class="form-group">
            <label for="password">Lozinka:</label>
            <input
              type="password"
              id="password"
              formControlName="password"
              class="form-control"
              [class.error]="registerForm.get('password')?.invalid && registerForm.get('password')?.touched"
            >
            <div class="error-message" *ngIf="registerForm.get('password')?.invalid && registerForm.get('password')?.touched">
              <span *ngIf="registerForm.get('password')?.errors?.['required']">Lozinka je obavezna</span>
              <span *ngIf="registerForm.get('password')?.errors?.['minlength']">Lozinka mora imati najmanje 6 karaktera</span>
            </div>
          </div>

          <div class="form-group">
            <label for="confirmPassword">Potvrdi lozinku:</label>
            <input
              type="password"
              id="confirmPassword"
              formControlName="confirmPassword"
              class="form-control"
              [class.error]="registerForm.get('confirmPassword')?.invalid && registerForm.get('confirmPassword')?.touched"
            >
            <div class="error-message" *ngIf="registerForm.get('confirmPassword')?.invalid && registerForm.get('confirmPassword')?.touched">
              <span *ngIf="registerForm.get('confirmPassword')?.errors?.['required']">Potvrda lozinke je obavezna</span>
              <span *ngIf="registerForm.errors?.['passwordMismatch'] && registerForm.get('confirmPassword')?.touched">Lozinke se ne poklapaju</span>
            </div>
          </div>

          <div class="form-group">
            <label for="role">Uloga:</label>
            <select
              id="role"
              formControlName="role"
              class="form-control"
            >
              <option value="client">Klijent</option>
              <option value="admin">Administrator</option>
            </select>
          </div>

          <div class="error-message" *ngIf="errorMessage">
            {{ errorMessage }}
          </div>

          <button
            type="submit"
            class="btn-primary"
            [disabled]="registerForm.invalid || loading"
          >
            {{ loading ? 'Registriram...' : 'Registriraj se' }}
          </button>
        </form>

        <div class="login-link">
          <p>Već imate račun? <a routerLink="/login">Prijavite se</a></p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }

    .register-card {
      background: white;
      padding: 40px;
      border-radius: 10px;
      box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
      width: 100%;
      max-width: 500px;
    }

    h2 {
      text-align: center;
      margin-bottom: 30px;
      color: #333;
      font-weight: 600;
    }

    .form-row {
      display: flex;
      gap: 15px;
    }

    .form-row .form-group {
      flex: 1;
    }

    .form-group {
      margin-bottom: 20px;
    }

    label {
      display: block;
      margin-bottom: 5px;
      color: #555;
      font-weight: 500;
    }

    .form-control {
      width: 100%;
      padding: 12px 15px;
      border: 2px solid #e1e5e9;
      border-radius: 5px;
      font-size: 16px;
      transition: border-color 0.3s ease;
      box-sizing: border-box;
    }

    .form-control:focus {
      outline: none;
      border-color: #667eea;
    }

    .form-control.error {
      border-color: #e74c3c;
    }

    select.form-control {
      cursor: pointer;
    }

    .error-message {
      color: #e74c3c;
      font-size: 14px;
      margin-top: 5px;
    }

    .btn-primary {
      width: 100%;
      padding: 12px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 5px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: transform 0.2s ease;
      margin-top: 10px;
    }

    .btn-primary:hover:not(:disabled) {
      transform: translateY(-2px);
    }

    .btn-primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none;
    }

    .login-link {
      text-align: center;
      margin-top: 20px;
    }

    .login-link p {
      color: #666;
    }

    .login-link a {
      color: #667eea;
      text-decoration: none;
      font-weight: 500;
    }

    .login-link a:hover {
      text-decoration: underline;
    }

    @media (max-width: 480px) {
      .form-row {
        flex-direction: column;
        gap: 0;
      }
    }
  `]
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      role: ['client']
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      const { confirmPassword, ...userData } = this.registerForm.value;

      this.authService.register(userData).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = 'Greška pri registraciji. Pokušajte ponovo.';
          console.error('Registration error:', error);
        }
      });
    }
  }
}
