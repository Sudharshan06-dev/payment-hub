// src/app/models/account.model.ts

export enum AccountType {
  CHECKING = 'CHECKING',
  SAVINGS = 'SAVINGS',
  MONEY_MARKET = 'MONEY_MARKET'
}

export enum AccountStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  FROZEN = 'FROZEN',
  CLOSED = 'CLOSED'
}

export interface Account {
  accountId?: number;
  accountNumber: string;
  accountType: AccountType;
  balance: number;
  currency: string;
  status: AccountStatus;
  createdAt?: string;
  updatedAt?: string;
  isActive: boolean;
  isDeleted?: boolean;
  userId?: number;
}

export interface AccountResponse {
  success: boolean;
  message: string;
  data: Account;
}

export interface AccountsListResponse {
  success: boolean;
  message: string;
  data: Account[];
}

export interface CreateAccountRequest {
  accountNumber: string;
  accountType: AccountType;
  balance: number;
  currency: string;
}

export interface UpdateAccountRequest {
  accountNumber?: string;
  accountType?: AccountType;
  balance?: number;
  currency?: string;
  status?: AccountStatus;
  isActive?: boolean;
}