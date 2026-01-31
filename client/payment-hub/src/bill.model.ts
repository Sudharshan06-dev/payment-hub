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
  id: string;
  billerId: string;
  billerName: string;
  billerLogo?: string;
  amount: number | null;
  dueDate: Date;
  createdDate: Date;
  paidDate?: Date;
  status: BillStatus;
  description?: string;
  category?: 'Utilities' | 'Insurance' | 'Subscription' | 'Medical' | 'Other';
  recurring: boolean;
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