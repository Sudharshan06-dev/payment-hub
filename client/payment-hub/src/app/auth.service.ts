import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap, map } from 'rxjs/operators';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    isActive: boolean;
    createdAt: string;
  };
}

export interface AuthUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  isActive: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  
  private apiUrl = 'http://localhost:8081/api/v1/auth'; // Adjust based on your backend
  private currentUserSubject: BehaviorSubject<AuthUser | null>;
  public currentUser$: Observable<AuthUser | null>;
  private tokenKey = 'authToken';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.currentUserSubject = new BehaviorSubject<AuthUser | null>(this.getUserFromStorage());
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  /**
   * Login user with email and password
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${this.apiUrl}/login`,
      credentials
    ).pipe(
      tap(response => {
        // Store token
        this.setToken(response.token);
        // Update current user
        this.currentUserSubject.next(response.user);
        localStorage.setItem('user', JSON.stringify(response.user));
      })
    );
  }

  /**
   * Logout user
   */
  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem('user');
    localStorage.removeItem('rememberMe');
    localStorage.removeItem('rememberedEmail');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  /**
   * Register new user
   */
  register(userData: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
  }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${this.apiUrl}/register`,
      userData
    ).pipe(
      tap(response => {
        this.setToken(response.token);
        this.currentUserSubject.next(response.user);
        localStorage.setItem('user', JSON.stringify(response.user));
      })
    );
  }

  /**
   * Request password reset
   */
  forgotPassword(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/forgot-password`,
      { email }
    );
  }

  /**
   * Reset password with token
   */
  resetPassword(token: string, newPassword: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/reset-password`,
      { token, newPassword }
    );
  }

  /**
   * Verify email address
   */
  verifyEmail(token: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/verify-email`,
      { token }
    );
  }

  /**
   * Refresh authentication token
   */
  refreshToken(): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(
      `${this.apiUrl}/refresh-token`,
      {}
    ).pipe(
      tap(response => {
        this.setToken(response.token);
      })
    );
  }

  /**
   * Get current user profile
   */
  getCurrentUserProfile(): Observable<AuthUser> {
    return this.http.get<AuthUser>(
      `${this.apiUrl}/profile`
    ).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
        localStorage.setItem('user', JSON.stringify(user));
      })
    );
  }

  /**
   * Update user profile
   */
  updateProfile(userData: Partial<AuthUser>): Observable<AuthUser> {
    return this.http.put<AuthUser>(
      `${this.apiUrl}/profile`,
      userData
    ).pipe(
      tap(user => {
        this.currentUserSubject.next(user);
        localStorage.setItem('user', JSON.stringify(user));
      })
    );
  }

  /**
   * Change password
   */
  changePassword(oldPassword: string, newPassword: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/change-password`,
      { oldPassword, newPassword }
    );
  }

  // ==================== HELPER METHODS ====================

  /**
   * Set authentication token
   */
  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  /**
   * Get authentication token
   */
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Get current user value
   */
  get currentUserValue(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get user from storage
   */
  private getUserFromStorage(): AuthUser | null {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  /**
   * Get authorization header for HTTP requests
   */
  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  /**
   * Decode JWT token (without verification - for client-side use only)
   * DO NOT use for security-critical operations
   */
  decodeToken(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  /**
   * Check if token is expired
   */
  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;

    const decoded = this.decodeToken(token);
    if (!decoded || !decoded.exp) return true;

    const expirationDate = new Date(decoded.exp * 1000);
    return expirationDate <= new Date();
  }

  /**
   * Get user full name
   */
  getUserFullName(): string {
    const user = this.currentUserValue;
    if (user) {
      return `${user.firstName} ${user.lastName}`;
    }
    return 'User';
  }

  /**
   * Check if user has specific role
   */
  hasRole(role: string): boolean {
    const token = this.getToken();
    if (!token) return false;

    const decoded = this.decodeToken(token);
    return decoded?.roles?.includes(role) || false;
  }

  /**
   * Check if user has all specified roles
   */
  hasRoles(roles: string[]): boolean {
    return roles.every(role => this.hasRole(role));
  }

  /**
   * Check if user has any of the specified roles
   */
  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }
}