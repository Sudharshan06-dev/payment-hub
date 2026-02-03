/**
 * Bill Model
 * Represents a bill in the Wells Fargo payment portal
 */

export enum BillStatus {
  PENDING = 'PENDING',
  DUE = 'DUE',
  OVERDUE = 'OVERDUE',
  PAID = 'PAID'
}

export interface Bill {
  bill_id: number;
  user_id: number;
  biller_name: string;
  account_number: string;
  amount: number;
  currency: string;
  due_date: string | Date;
  bill_status: BillStatus;
  bill_frequency: 'MONTHLY' | 'QUARTERLY' | 'ANNUALLY' | 'ONE_TIME';
  created_at: string | Date;
  updated_at: string | Date;
  biller_logo?: string;
  description?: string;
  category?: 'Utilities' | 'Insurance' | 'Subscription' | 'Medical' | 'Other';
  paid_date?: string | Date;
  paymentHistory?: Payment[];
}

export interface Payment {
  id: string;
  billId: string;
  amount: number;
  paymentDate: Date;
  status: 'PENDING' | 'SETTLED' | 'REJECTED';
  transactionId?: string;
}

export interface BillsResponse {
  bills: Bill[];
  total: number;
  pending: number;
  due: number;
  overdue: number;
}

export interface PaymentInitiationRequest {
  billId: any;
  amount: any;
  cardNumber: string;
  cardExpiry: string;
  cardCVV: string;
}

export interface PaymentInitiationResponse {
  success: boolean;
  paymentId: string;
  status: string;
  message?: string;
}