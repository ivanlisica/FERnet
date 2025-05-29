import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { ErrorHandlingService } from './error-handling.service';
import jwtDecode from 'jwt-decode';

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  role: string;
  firstName: string;
  lastName: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  access_token: string;
  refresh_token: string;
  user?: UserInfo;
}

export interface UserInfo {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/auth';
  private tokenSubject = new BehaviorSubject<string | null>(this.getStoredToken());
  public token$ = this.tokenSubject.asObservable();

  private currentUserSubject = new BehaviorSubject<UserInfo | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private errorHandler: ErrorHandlingService,
    private router: Router
  ) {}

  register(userData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, userData)
      .pipe(
        tap(response => this.handleAuthResponse(response)),
        catchError(this.errorHandler.handleError.bind(this.errorHandler))
      );
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => this.handleAuthResponse(response)),
        catchError(this.errorHandler.handleError.bind(this.errorHandler))
      );
  }

  logout(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user_info');
    this.tokenSubject.next(null);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!this.getStoredToken();
  }

  getToken(): string | null {
    return this.getStoredToken();
  }

  getCurrentUser(): UserInfo | null {
    return this.currentUserSubject.value;
  }

  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user ? user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN') : false;
  }

  getCurrentUsername(): string | null {
    const user = this.getCurrentUser();
    return user ? user.username : null;
  }

  private handleAuthResponse(response: AuthResponse): void {
    localStorage.setItem('access_token', response.access_token);
    localStorage.setItem('refresh_token', response.refresh_token);
    this.tokenSubject.next(response.access_token);

    if (response.user) {
      localStorage.setItem('user_info', JSON.stringify(response.user));
      this.currentUserSubject.next(response.user);
    } else {

      this.loadUserInfo();
    }
  }


private loadUserInfo(): void {
  try {
    const token = this.getStoredToken();
    if (token) {
      const decoded: any = jwtDecode(token);
      const userInfo: UserInfo = {
        username: decoded.sub || '',
        firstName: decoded.firstName || '',
        lastName: decoded.lastName || '',
        email: decoded.email || '',
        roles: decoded.roles || []
      };
      localStorage.setItem('user_info', JSON.stringify(userInfo));
      this.currentUserSubject.next(userInfo);
    }
  } catch (error) {
    console.error('Error decoding token:', error);
  }
}

  private getStoredToken(): string | null {
    return localStorage.getItem('access_token');
  }

  private getStoredUser(): UserInfo | null {
    const userInfo = localStorage.getItem('user_info');
    return userInfo ? JSON.parse(userInfo) : null;
  }


  refreshToken(): Observable<AuthResponse> {
    const refreshToken = localStorage.getItem('refresh_token');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refresh_token: refreshToken })
      .pipe(
        tap(response => this.handleAuthResponse(response)),
        catchError(this.errorHandler.handleError.bind(this.errorHandler))
      );
  }
}
