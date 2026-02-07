import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RequestService } from '../../services/request.service';
import { CommonModule } from '@angular/common';
import { ACCOUNT_PATH, PAYMENT_PATH } from '../../environment';
import { ToasterHelper } from '../../services/toast.service';
import { Bill, PaymentInitiationRequest } from '../models/bill.model';
import { LocalStorageHelper } from '../../services/local-storage.service';

interface Account {
  accountId: number;
  accountNumber: string;
  accountHolderName: string;
  bankName: string;
  routingNumber: string;
  accountType: string;
  balance: number;
  currency: string;
  status: string;
  isActive: boolean;
}

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, FormsModule]
})
export class PaymentComponent implements OnInit {

  private _bill: Bill | null = null;
  private user_id: number = 0;

  @Input()
  set bill(value: Bill | null) {
    this._bill = value;
    if (value) {
      console.log('New bill set:', value);
    }
  }

  get bill(): Bill | null {
    return this._bill;
  }

  @Output() onClose = new EventEmitter<void>();
  @Output() onSuccess = new EventEmitter<number>();

  paymentForm!: FormGroup;
  isProcessing = false;
  error: string | null = null;
  success = false;
  paymentStep: 'review' | 'payment' | 'confirm' = 'review';

  // Payment method
  paymentMethod: 'account' | 'card' = 'account';
  accounts: Account[] = [];
  selectedAccount: Account | null = null;
  isLoadingAccounts = false;

  // Card validation
  cardNumberFormatted = '';
  cardTypeIcon: 'credit_card' | 'account_balance' = 'credit_card';
  agreeTerms: boolean = false;

  constructor(
    private fb: FormBuilder,
    private requestService: RequestService,
    private localStorageHelper: LocalStorageHelper,
    private toasterService: ToasterHelper
  ) {
    this.user_id = this.localStorageHelper.getItem('user_details')?.userId;
  }

  ngOnInit(): void {
    this.initializeForm();
    this.getAccountDetails();
  }

  initializeForm(): void {
    this.paymentForm = this.fb.group({
      // Account payment fields
      accountId: [''],

      // Card payment fields
      cardNumber: [''],
      cardHolderName: [''],
      expiry: [''],
      cvv: [''],

      amount: [
        { value: this.bill?.amount, disabled: true },
        Validators.required
      ]
    });
  }

  /**
   * Get user accounts
   */
  getAccountDetails(): void {
    this.isLoadingAccounts = true;
    const url = `${ACCOUNT_PATH}/${this.user_id}`;

    this.requestService.get(url).subscribe({
      next: (response: any) => {
        this.isLoadingAccounts = false;
        if (response.success && response.data && response.data) {
          this.accounts.push(response.data);
          this.selectedAccount = this.accounts[0];
          this.paymentForm.patchValue({
            accountId: this.selectedAccount.accountId
          });
        } else {
          // No accounts found, default to card payment
          this.paymentMethod = 'card';
        }
      },
      error: (err: any) => {
        this.isLoadingAccounts = false;
        this.toasterService.error(err?.error?.message || 'Failed to load accounts');
        this.paymentMethod = 'card';
      }
    });
  }

  /**
   * Switch payment method
   */
  switchPaymentMethod(method: 'account' | 'card'): void {
    this.paymentMethod = method;
    this.error = null;

    if (method === 'account') {
      // Set validators for account payment
      this.paymentForm.get('accountId')?.setValidators([Validators.required]);
      this.paymentForm.get('cardNumber')?.clearValidators();
      this.paymentForm.get('cardHolderName')?.clearValidators();
      this.paymentForm.get('expiry')?.clearValidators();
      this.paymentForm.get('cvv')?.clearValidators();
    } else {
      // Set validators for card payment
      this.paymentForm.get('accountId')?.clearValidators();
      this.paymentForm.get('cardNumber')?.setValidators([
        Validators.required,
        Validators.pattern(/^\d{13,19}$/)
      ]);
      this.paymentForm.get('cardHolderName')?.setValidators([
        Validators.required,
        Validators.minLength(3)
      ]);
      this.paymentForm.get('expiry')?.setValidators([
        Validators.required,
        Validators.pattern(/^\d{2}\/\d{2}$/)
      ]);
      this.paymentForm.get('cvv')?.setValidators([
        Validators.required,
        Validators.pattern(/^\d{3,4}$/)
      ]);
    }

    this.paymentForm.updateValueAndValidity();
  }

  /**
   * Select account
   */
  selectAccount(account: Account): void {
    this.selectedAccount = account;
    this.paymentForm.patchValue({
      accountId: account.accountId
    });
  }

  /**
   * Format card number as user types
   */
  onCardNumberChange(event: any): void {
    let value = event.target.value.replace(/\s+/g, '');
    value = value.replace(/\D/g, '');

    if (value.length > 19) {
      value = value.slice(0, 19);
    }

    const formatted = value.replace(/(\d{4})/g, '$1 ').trim();
    this.cardNumberFormatted = formatted;
    this.paymentForm.get('cardNumber')?.setValue(value, { emitEvent: false });
  }

  /**
   * Format expiry as MM/YY
   */
  onExpiryChange(event: any): void {
    let value = event.target.value.replace(/\D/g, '');

    if (value.length >= 2) {
      value = value.slice(0, 2) + '/' + value.slice(2, 4);
    }

    this.paymentForm.get('expiry')?.setValue(value, { emitEvent: false });
  }

  /**
   * Format CVV (numbers only)
   */
  onCVVChange(event: any): void {
    let value = event.target.value.replace(/\D/g, '');

    if (value.length > 4) {
      value = value.slice(0, 4);
    }

    this.paymentForm.get('cvv')?.setValue(value, { emitEvent: false });
  }

  /**
   * Mask account number
   */
  maskAccountNumber(accountNumber: string): string {
    if (!accountNumber) return '';
    const lastFour = accountNumber.slice(-4);
    return `****${lastFour}`;
  }

  /**
   * Format currency
   */
  formatCurrency(amount: any) {
    if (!amount) return '$0.00';

    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  /**
   * Move to payment step
   */
  proceedToPayment(): void {
    if (this.paymentStep === 'review') {
      this.paymentStep = 'payment';
      this.error = null;
    }
  }

  /**
   * Submit payment
   */
  submitPayment(): void {
    if (this.paymentMethod === 'account') {
      if (!this.selectedAccount) {
        this.error = 'Please select an account';
        return;
      }

      if (this.selectedAccount.balance < (this.bill?.amount || 0)) {
        this.error = 'Insufficient balance in selected account';
        return;
      }
    } else {
      if (this.paymentForm.invalid) {
        this.error = 'Please fill in all required fields correctly';
        return;
      }
    }

    this.isProcessing = true;
    this.error = null;

    const formValue = this.paymentForm.getRawValue();

    let paymentRequest: any;

    if (this.paymentMethod === 'account') {
      paymentRequest = {
        billId: this.bill?.bill_id,
        userId: this.user_id,
        paymentDetails: this.accounts,
        amount: this.bill?.amount,
        accountId: this.selectedAccount?.accountId,
        paymentMethod: 'BANK_ACCOUNT'
      };
    } else {
      paymentRequest = {
        billId: this.bill?.bill_id,
        userId: this.user_id,
        amount: this.bill?.amount,
        paymentDetails: [{
          cardNumber: formValue.cardNumber,
          cardHolderName: formValue.cardHolderName,
          cardExpiry: formValue.expiry,
          cardCVV: formValue.cvv,
        }],
        paymentMethod: 'CREDIT_CARD'
      };
    }

    this.requestService.post(PAYMENT_PATH, paymentRequest).subscribe({
      next: (response: any) => {
        this.isProcessing = false;
        if (response.success) {
          this.success = true;
          this.paymentStep = 'confirm';
          this.toasterService.success({title: 'Success', message: response?.message});
          setTimeout(() => {
            this.onSuccess.emit(this.bill?.bill_id);
          }, 2000);
        } else {
          this.error = response.message;
          this.toasterService.error(response.message);
        }
      },
      error: (err: any) => {
        this.isProcessing = false;
        this.error = err?.error?.message || 'Payment failed. Please try again.';
        this.toasterService.error({title: 'Error!', message: this.error});
      }
    });
  }

  /**
   * Close modal
   */
  closeModal(): void {
    if (!this.isProcessing) {
      this.onClose.emit();
    }
  }

  /**
   * Go back to previous step
   */
  goBack(): void {
    if (this.paymentStep === 'payment') {
      this.paymentStep = 'review';
      this.error = null;
    }
  }
}