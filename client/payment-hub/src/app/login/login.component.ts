import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../auth.service';
import { RequestService } from '../../services/request.service';
import { LocalStorageHelper } from '../../services/local-storage.service';
import { ToasterHelper } from '../../services/toast.service';
import { jwtDecode } from 'jwt-decode';
import { AUTH_PATH } from '../../environment';
import { SKIP_AUTH_TRUE } from '../../interceptors/auth.interceptor';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule]
})
export class LoginComponent implements OnInit, OnDestroy {

  loginForm: FormGroup;
  registerForm: FormGroup;

  isLoginLoading = false;
  isRegisterLoading = false;

  showLoginPassword = false;
  showRegisterPassword = false;

  activeTab: 'login' | 'register' = 'login';

  loginErrorMessage = '';
  registerErrorMessage = '';
  private destroy$ = new Subject<void>();

  // ==================== CONSTRUCTOR ====================

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private request: RequestService,
    private localStorage: LocalStorageHelper,
    private toastService: ToasterHelper
  ) {

    this.loginForm = this.createLoginForm();
    this.registerForm = this.createRegisterForm();
  }

  ngOnInit(): void {

    this.route.queryParams.subscribe(params => {

      if (params['token']) {

        const decoded: any = jwtDecode(params['token']);
        const userDetails = {
          email: decoded.sub,
          user_id: decoded.user_id,
          firstname: decoded.firstname,
          lastname: decoded.lastname
        };
        this.localStorage.storeItem('access_token', params['token'])
        this.localStorage.storeItem('user_details', userDetails)
        this.router.navigate(['/dashboard']);
      }
    });

    this.loadRememberedEmail();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  private createLoginForm(): FormGroup {
    return this.formBuilder.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.pattern(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)
        ]
      ],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6)
        ]
      ],
      rememberMe: [false]
    });
  }

  private createRegisterForm(): FormGroup {
    return this.formBuilder.group({
      firstName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(50)
        ]
      ],
      lastName: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(50)
        ]
      ],
      username: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(20),
          Validators.pattern(/^[a-zA-Z0-9_-]*$/)
        ]
      ],
      email: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.pattern(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)
        ]
      ],
      phoneNumber: [
        '',
        [
          Validators.pattern(/^[+]?[(]?[0-9]{3}[)]?[-\s.]?[0-9]{3}[-\s.]?[0-9]{4,6}$/),
        ]
      ],
      passwordHash: [
        '',
        [
          Validators.required,
          Validators.minLength(8),
          Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
        ]
      ],
      confirmPassword: [
        '',
        [Validators.required]
      ],
      agreeTerms: [false, [Validators.requiredTrue]]
    },
      {
        validators: this.passwordMatchValidator
      });
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('passwordHash');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  // ==================== LOGIN HANDLER ====================

  onLogin(): void {
    // Mark all fields as touched for validation display
    Object.keys(this.loginForm.controls).forEach(key => {
      this.loginForm.get(key)?.markAsTouched();
    });

    // Early exit if form is invalid
    if (this.loginForm.invalid) {
      this.loginErrorMessage = 'Please fill in all required fields correctly.';
      return;
    }

    this.isLoginLoading = true;
    this.loginErrorMessage = '';

    const { email, password, rememberMe } = this.loginForm.value;

    this.request.post(AUTH_PATH + '/login', this.loginForm.getRawValue(), [SKIP_AUTH_TRUE]).subscribe({
      next: (data: any) => {

        // Handle successful response here
        this.localStorage.storeItem('access_token', data?.access_token)
        this.localStorage.storeItem('user_details', data?.user_details)

        if (rememberMe) {
          this.saveRememberedEmail(email);
        }
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 1000);
      },

      error: (err: any) => {
        this.toastService.error(err?.error);
      }
    });
  }

  // ==================== REGISTRATION HANDLER ====================


  onRegister(): void {

    Object.keys(this.registerForm.controls).forEach(key => {
      this.registerForm.get(key)?.markAsTouched();
    });

    if (this.registerForm.invalid) {
      this.registerErrorMessage = 'Please fill in all required fields correctly.';
      return;
    }

    this.isRegisterLoading = true;
    this.registerErrorMessage = '';

    const formData = this.registerForm.value;


    this.request.post(AUTH_PATH + '/register', this.registerForm.getRawValue(), [SKIP_AUTH_TRUE]).subscribe({
      next: (data: any) => {

        this.toastService.success(data);
        this.loginForm.patchValue({ email: formData.email });
        this.registerForm.reset();

        setTimeout(() => {
          this.switchTab('login');
        }, 2000);
      },

      error: (err: any) => {
        this.toastService.error(err?.error);
      }
    });
  }

  togglePasswordVisibility(formType: 'login' | 'register'): void {
    if (formType === 'login') {
      this.showLoginPassword = !this.showLoginPassword;
    } else {
      this.showRegisterPassword = !this.showRegisterPassword;
    }
  }


  private saveRememberedEmail(email: string): void {
    try {
      localStorage.setItem('billhub_remembered_email', email);
    } catch (error) {
      this.toastService.warning(`Could not save email to localStorage:, ${error}`);
    }
  }

  private loadRememberedEmail(): void {
    try {
      const savedEmail = localStorage.getItem('billhub_remembered_email');
      if (savedEmail) {
        this.loginForm.patchValue({
          email: savedEmail,
          rememberMe: true
        });
      }
    } catch (error) {
      this.toastService.warning(`Could not load email to localStorage:, ${error}`);
    }
  }

  getRememberedEmail(): string {
    try {
      return localStorage.getItem('billhub_remembered_email') || '';
    } catch (error) {
      return '';
    }
  }

  public redirectToGoogleAuth() {
    window.location.href = AUTH_PATH + '/google/login';
  }

  // ==================== SOCIAL LOGIN ====================
  socialLogin(): void {
    this.isLoginLoading = true;
    this.loginErrorMessage = '';

    // TODO: Implement actual OAuth flow with backend
    console.log('Social login initiated with:', 'google');

    // Mock implementation
    setTimeout(() => {
      this.isLoginLoading = false;
      this.loginErrorMessage = `Google login is not yet configured.`;
    }, 1000);
  }

  // ==================== TAB NAVIGATION ====================
  switchTab(tab: 'login' | 'register'): void {
    this.activeTab = tab;
    this.loginErrorMessage = '';
    this.registerErrorMessage = '';
  }

  // ==================== FORM GETTERS (Optional - for template) ====================

  get loginEmailControl() {
    return this.loginForm.get('email');
  }

  get loginPasswordControl() {
    return this.loginForm.get('password');
  }


  get registerEmailControl() {
    return this.registerForm.get('email');
  }


  get registerPasswordControl() {
    return this.registerForm.get('passwordHash');
  }

  get registerConfirmPasswordControl() {
    return this.registerForm.get('confirmPassword');
  }
}