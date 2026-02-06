// src/app/components/settings/settings.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RequestService } from '../../services/request.service';
import { ACCOUNT_PATH } from '../../environment';
import { LocalStorageHelper } from '../../services/local-storage.service';
import { Account, AccountStatus, AccountType } from '../models/account.model';
import { ToasterHelper } from '../../services/toast.service';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule]
})
export class SettingsComponent implements OnInit, OnDestroy {

  // Data
  accounts: Account[] = [];
  isLoadingAccounts = false;
  readonly API_BASE_PATH = ACCOUNT_PATH;

  // Form
  private userId: number = 0;
  accountForm!: FormGroup;
  isFormVisible = false;
  isEditMode = false;
  currentEditingAccount: Account | null = null;
  isSubmitting = false;

  // Enums for template
  AccountType = AccountType;
  AccountStatus = AccountStatus;

  private destroy$ = new Subject<void>();

  constructor(
    private accountService: RequestService,
    private fb: FormBuilder,
    private localStorage: LocalStorageHelper,
    private toastr: ToasterHelper
  ) {
    this.initForm();
    const userDetails = this.localStorage.getItem('user_details');
    this.userId = userDetails?.userId || 0;
    
    console.log('=== SETTINGS COMPONENT INIT ===');
    console.log('User ID:', this.userId);
    console.log('API Base Path:', this.API_BASE_PATH);
    console.log('===============================');
  }

  ngOnInit(): void {
    if (!this.userId) {
      this.toastr.error('User ID not found. Please login again.');
      return;
    }
    this.loadAccounts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Initialize form
   */
  private initForm(): void {
    this.accountForm = this.fb.group({
      accountNumber: ['', [Validators.required, Validators.minLength(8)]],
      accountType: ['CHECKING', Validators.required],
      balance: [0, [Validators.required, Validators.min(0)]],
      currency: ['USD', Validators.required],
      status: ['ACTIVE', Validators.required],
      isActive: [true]
    });
  }

  /**
   * Load all accounts
   * GET /api/v1/accounts/{userId}
   */
  loadAccounts(): void {
    this.isLoadingAccounts = true;
    const url = `http://localhost:8080/api/v1/users/${this.userId}`;
    
    console.log('Loading accounts from:', url);

     this.accountService.get(url).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
          console.log('Accounts loaded successfully:', response);
          this.accounts = response.data || [];
          this.isLoadingAccounts = false;
        },
        error: (error) => {
          console.error('Error loading accounts:', error);
          this.toastr.error(error?.error?.message || 'Failed to load accounts');
          this.isLoadingAccounts = false;
        }
      });
  }

  /**
   * Open form to add new account
   */
  addNewAccount(): void {
    this.isEditMode = false;
    this.currentEditingAccount = null;
    this.accountForm.reset({
      accountType: 'CHECKING',
      currency: 'USD',
      status: 'ACTIVE',
      isActive: true,
      balance: 0
    });
    this.isFormVisible = true;
  }

  /**
   * Open form to edit account
   */
  editAccount(account: Account): void {
    this.isEditMode = true;
    this.currentEditingAccount = account;
    this.accountForm.patchValue({
      accountNumber: account.accountNumber,
      accountType: account.accountType,
      balance: account.balance,
      currency: account.currency,
      status: account.status,
      isActive: account.isActive
    });
    this.isFormVisible = true;
  }

  /**
   * Cancel form
   */
  cancelForm(): void {
    this.isFormVisible = false;
    this.accountForm.reset();
    this.isEditMode = false;
    this.currentEditingAccount = null;
  }

  /**
   * Save account (create or update)
   */
  saveAccount(): void {
    if (this.accountForm.invalid) {
      this.toastr.error('Please fill all required fields');
      return;
    }

    this.isSubmitting = true;

    if (this.isEditMode && this.currentEditingAccount?.accountId) {
      this.updateAccount();
    } else {
      this.createAccount();
    }
  }

  /**
   * Create new account
   * POST /api/v1/accounts/{userId}
   */
  private createAccount(): void {
    const url = `${this.API_BASE_PATH}/${this.userId}`;
    const accountData: Account = this.accountForm.value;

    console.log('Creating account:', url, accountData);

    this.accountService.post(url, accountData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('Account created:', response);
          this.toastr.success(response.message || 'Account created successfully');
          this.loadAccounts();
          this.cancelForm();
          this.isSubmitting = false;
        },
        error: (error: any) => {
          console.error('Error creating account:', error);
          this.toastr.error(error?.error?.message || 'Failed to create account');
          this.isSubmitting = false;
        }
      });
  }

  /**
   * Update existing account
   * PUT /api/v1/accounts/{userId}/{accountId}
   */
  private updateAccount(): void {
    if (!this.currentEditingAccount?.accountId) return;

    const url = `${this.API_BASE_PATH}/${this.userId}/${this.currentEditingAccount.accountId}`;
    const accountData: Account = this.accountForm.value;

    console.log('Updating account:', url, accountData);

    this.accountService.put(url, accountData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('Account updated:', response);
          this.toastr.success(response.message || 'Account updated successfully');
          this.loadAccounts();
          this.cancelForm();
          this.isSubmitting = false;
        },
        error: (error: any) => {
          console.error('Error updating account:', error);
          this.toastr.error(error?.error?.message || 'Failed to update account');
          this.isSubmitting = false;
        }
      });
  }

  /**
   * Delete account
   * DELETE /api/v1/accounts/{userId}/{accountId}
   */
  deleteAccount(account: Account): void {
    if (!account.accountId) return;

    const confirmed = confirm(
      `Delete account ${this.maskAccountNumber(account.accountNumber)}? This cannot be undone.`
    );

    if (!confirmed) return;

    const url = `${this.API_BASE_PATH}/${this.userId}/${account.accountId}`;
    
    console.log('Deleting account:', url);

    this.accountService.delete(url)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('Account deleted:', response);
          this.toastr.success(response.message || 'Account deleted successfully');
          this.loadAccounts();
        },
        error: (error: any) => {
          console.error('Error deleting account:', error);
          this.toastr.error(error?.error?.message || 'Failed to delete account');
        }
      });
  }

  /**
   * Mask account number (show only last 4 digits)
   */
  maskAccountNumber(accountNumber: string): string {
    if (!accountNumber) return '';
    const lastFour = accountNumber.slice(-4);
    return `****${lastFour}`;
  }

  /**
   * Format currency
   */
  formatCurrency(amount: number, currency: string): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency
    }).format(amount);
  }

  /**
   * Get status badge color
   */
  getStatusBadgeClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'ACTIVE': 'bg-success',
      'INACTIVE': 'bg-warning',
      'FROZEN': 'bg-danger',
      'CLOSED': 'bg-secondary'
    };
    return statusMap[status] || 'bg-info';
  }

  /**
   * Check if form is valid and not submitting
   */
  get isFormValid(): boolean {
    return this.accountForm.valid && !this.isSubmitting;
  }

  /**
   * Get form button text
   */
  get formButtonText(): string {
    return this.isEditMode ? 'Update Account' : 'Create Account';
  }

  /**
   * Get account type icon
   */
  getAccountTypeIcon(type: string): string {
    const iconMap: { [key: string]: string } = {
      'CHECKING': 'account_balance',
      'SAVINGS': 'savings',
      'CREDIT_CARD': 'credit_card',
      'INVESTMENT': 'trending_up'
    };
    return iconMap[type] || 'account_balance_wallet';
  }

  /**
   * Get status icon
   */
  getStatusIcon(status: string): string {
    const iconMap: { [key: string]: string } = {
      'ACTIVE': 'check_circle',
      'INACTIVE': 'pause_circle',
      'FROZEN': 'lock',
      'CLOSED': 'cancel'
    };
    return iconMap[status] || 'help';
  }
}