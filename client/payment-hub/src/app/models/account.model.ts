export interface Account {
  accountId?: number;
  accountNumber: string;
  accountHolderName: string;
  bankName: string;
  routingNumber: string;
  accountType: AccountType | string;
  balance: number;
  currency: string;
  status: AccountStatus | string;
  isActive: boolean;
  isDeleted?: boolean;
  userId?: number;
  createdAt?: string;
  updatedAt?: string;
}

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