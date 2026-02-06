import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RequestService } from '../../services/request.service';
import { CommonModule } from '@angular/common';
import { PAYMENT_PATH } from '../../environment';
import { ToasterHelper } from '../../services/toast.service';
import { Bill, PaymentInitiationRequest,  } from '../models/bill.model';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, FormsModule]
})
export class PaymentComponent implements OnInit {

  private _bill: Bill | null = null;

  @Input()
  set bill(value: Bill | null) {
    this._bill = value;
    // Perform actions when value changes
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

  // Card validation
  cardNumberFormatted = '';
  cardTypeIcon: 'cc_visa' | 'cc_mastercard' | 'cc_amex' | 'credit_card' = 'credit_card';
  agreeTerms:boolean =  false

  constructor(
    private fb: FormBuilder,
    private requestService: RequestService,
    private toasterService: ToasterHelper
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  /**
   * Initialize payment form
   */
  initializeForm(): void {
    this.paymentForm = this.fb.group({
      cardNumber: [
        '',
        [
          Validators.required,
          Validators.pattern(/^\d{13,19}$/)
        ]
      ],
      cardholderName: [
        '',
        [
          Validators.required,
          Validators.minLength(3)
        ]
      ],
      expiry: [
        '',
        [
          Validators.required,
          Validators.pattern(/^\d{2}\/\d{2}$/)
        ]
      ],
      cvv: [
        '',
        [
          Validators.required,
          Validators.pattern(/^\d{3,4}$/)
        ]
      ],
      amount: [
        { value: this.bill?.amount, disabled: true },
        Validators.required
      ]
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

    // Format with spaces
    const formatted = value.replace(/(\d{4})/g, '$1 ').trim();
    this.cardNumberFormatted = formatted;
    this.paymentForm.get('cardNumber')?.setValue(value, { emitEvent: false });

    // Detect card type
    this.detectCardType(value);
  }

  /**
   * Detect card type based on number
   */
  detectCardType(cardNumber: string): void {
    const patterns: { [key: string]: RegExp } = {
      visa: /^4[0-9]{12}(?:[0-9]{3})?$/,
      mastercard: /^5[1-5][0-9]{14}$/,
      amex: /^3[47][0-9]{13}$/
    };

    for (const [type, pattern] of Object.entries(patterns)) {
      if (pattern.test(cardNumber)) {
        this.cardTypeIcon = `cc_${type}` as any;
        return;
      }
    }

    this.cardTypeIcon = 'credit_card';
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
   * Mask card number for display
   */
  getMaskedCardNumber(): string {
    const cardNumber = this.paymentForm.get('cardNumber')?.value;
    if (!cardNumber || cardNumber.length < 4) {
      return '••••••••••••••••';
    }
    const lastFour = cardNumber.slice(-4);
    return '•••• •••• •••• ' + lastFour;
  }

  /**
   * Format currency
   */
  formatCurrency(amount: any) {

    if (!amount) {
      return;
    }

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
    if (this.paymentForm.invalid) {
      this.error = 'Please fill in all required fields correctly';
      return;
    }

    this.isProcessing = true;
    this.error = null;

    const formValue = this.paymentForm.getRawValue();
    const paymentRequest: PaymentInitiationRequest = {
      billId: this.bill?.bill_id,
      amount: this.bill?.amount,
      cardNumber: formValue.cardNumber,
      cardExpiry: formValue.expiry,
      cardCVV: formValue.cvv
    };

    this.requestService.post(PAYMENT_PATH, paymentRequest).subscribe({
      next: (response: any) => {

        this.isProcessing = false;
        if (response.success) {
          this.success = true;
          this.paymentStep = 'confirm';
          setTimeout(() => {
            this.onSuccess.emit(this.bill?.bill_id);
          }, 2000);

        } else {
          this.toasterService.error(response.message)
        }
      },
      error: (err: any) => {
        this.toasterService.error(err?.error)
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