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

  accounts!: Account;
  readonly API_BASE_PATH = ACCOUNT_PATH;

  private userId: number = 0;
  accountForm!: FormGroup;
  isFormVisible = false;
  isEditMode = false;
  currentEditingAccount: Account | null = null;

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
  }

  ngOnInit(): void {
    if (!this.userId) {
      this.toastr.error({title: "Error!", message: 'User ID not found. Please login again.'});
      return;
    }
    this.loadAccounts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initForm(): void {
    this.accountForm = this.fb.group({
      accountNumber: ['', [Validators.required, Validators.pattern(/^\d{10,16}$/)]],
      accountHolderName: ['', [Validators.required, Validators.minLength(2)]],
      bankName: ['', [Validators.required]],
      routingNumber: ['', [Validators.required, Validators.pattern(/^\d{9}$/)]],
      accountType: ['CHECKING', Validators.required],
      balance: [0, [Validators.required, Validators.min(0)]],
      currency: ['USD', Validators.required],
      isActive: [true]
    });
  }

  loadAccounts(): void {
    const url = `${this.API_BASE_PATH}/${this.userId}`;

    this.accountService.get(url).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response: any) => {
        this.accounts = response.data || [];
      },
      error: (error) => {
        this.toastr.error({title: "Error!", message: error?.error?.message});
      }
    });
  }

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

  editAccount(account: Account): void {
    this.isEditMode = true;
    this.currentEditingAccount = account;
    this.accountForm.patchValue(account);
    this.isFormVisible = true;
  }

  cancelForm(): void {
    this.isFormVisible = false;
    this.accountForm.reset();
    this.isEditMode = false;
    this.currentEditingAccount = null;
  }

  saveAccount(): void {
    if (this.accountForm.invalid) {
      this.markFormGroupTouched(this.accountForm);
      this.toastr.error({title: "Error!", message: 'Please fill all required fields correctly'});
      return;
    }

    if (this.isEditMode && this.currentEditingAccount?.accountId) {
      this.updateAccount();
    } else {
      this.createAccount();
    }
  }

  private createAccount(): void {
    const url = `${this.API_BASE_PATH}/${this.userId}`;
    const accountData: Account = this.accountForm.value;

    this.accountService.post(url, accountData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          this.toastr.success({title: "Success", message: response?.message});
          this.loadAccounts();
          this.cancelForm();
        },
        error: (error: any) => {
          this.toastr.error({title: "Error!", message: error?.error?.message});
        }
      });
  }

  private updateAccount(): void {
    if (!this.currentEditingAccount?.accountId) return;

    const url = `${this.API_BASE_PATH}/${this.userId}/${this.currentEditingAccount.accountId}`;
    const accountData: Account = this.accountForm.value;

    this.accountService.put(url, accountData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          this.toastr.success({ title: "Success", message: response?.message });
          this.loadAccounts();
          this.cancelForm();
        },
        error: (error: any) => {
          this.toastr.error({title: "Error!", message: error?.error?.message});
        }
      });
  }

  deleteAccount(account: Account): void {
    if (!account.accountId) return;

    const confirmed = confirm(
      `Delete account ${this.maskAccountNumber(account.accountNumber)}? This action cannot be undone.`
    );

    if (!confirmed) return;

    const url = `${this.API_BASE_PATH}/${this.userId}/${account.accountId}`;

    this.accountService.delete(url)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          this.toastr.success({ title: "Success", message: response?.message }); 
          this.loadAccounts();
        },
        error: (error: any) => {
          this.toastr.error({title: "Error!", message: error?.error?.message});
        }
      });
  }

  maskAccountNumber(accountNumber: string): string {
    if (!accountNumber) return '';
    const lastFour = accountNumber.slice(-4);
    return `****${lastFour}`;
  }

  formatCurrency(amount: number, currency: string): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency
    }).format(amount);
  }

  getStatusBadgeClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'ACTIVE': 'bg-success',
      'INACTIVE': 'bg-warning text-dark',
      'FROZEN': 'bg-danger',
      'CLOSED': 'bg-secondary'
    };
    return statusMap[status] || 'bg-info';
  }

  get isFormValid(): boolean {
    return this.accountForm.valid;
  }

  get formButtonText(): string {
    return this.isEditMode ? 'Update Account' : 'Create Account';
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }
}