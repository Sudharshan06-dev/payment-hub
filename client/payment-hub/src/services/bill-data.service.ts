import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { BillsResponse, Bill } from '../app/models/bill.model';

@Injectable({
  providedIn: 'root'
})
export class BillDataService {
  // Subject for bills list
  private billsSubject = new BehaviorSubject<Bill[]>([]);
  public bills$ = this.billsSubject.asObservable();

  // Subject for selected bill
  private selectedBillSubject = new BehaviorSubject<Bill | null>(null);
  public selectedBill$ = this.selectedBillSubject.asObservable();

  // Subject for bill statistics
  private billStatsSubject = new BehaviorSubject<BillsResponse | null>(null);
  public billStats$ = this.billStatsSubject.asObservable();

  // Subject for total amount owed
  private totalAmountOwedSubject = new BehaviorSubject<number>(0);
  public totalAmountOwed$ = this.totalAmountOwedSubject.asObservable();

  // Subject for loading state
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor() { }

  /**
   * Update bills list
   */
  setBills(bills: Bill[]): void {
    this.billsSubject.next(bills);
  }

  /**
   * Get current bills list
   */
  getBills(): Bill[] {
    return this.billsSubject.value;
  }

  /**
   * Update selected bill
   */
  setSelectedBill(bill: Bill | null): void {
    this.selectedBillSubject.next(bill);
  }

  /**
   * Get selected bill
   */
  getSelectedBill(): Bill | null {
    return this.selectedBillSubject.value;
  }

  /**
   * Update bill statistics
   */
  setBillStats(stats: BillsResponse | null): void {
    this.billStatsSubject.next(stats);
  }

  /**
   * Get bill statistics
   */
  getBillStats(): BillsResponse | null {
    return this.billStatsSubject.value;
  }

  /**
   * Update total amount owed
   */
  setTotalAmountOwed(amount: number): void {
    this.totalAmountOwedSubject.next(amount);
  }

  /**
   * Get total amount owed
   */
  getTotalAmountOwed(): number {
    return this.totalAmountOwedSubject.value;
  }

  /**
   * Set loading state
   */
  setLoading(loading: boolean): void {
    this.loadingSubject.next(loading);
  }

  /**
   * Get loading state
   */
  isLoading(): boolean {
    return this.loadingSubject.value;
  }

  /**
   * Clear all data
   */
  clearData(): void {
    this.billsSubject.next([]);
    this.selectedBillSubject.next(null);
    this.billStatsSubject.next(null);
    this.totalAmountOwedSubject.next(0);
    this.loadingSubject.next(false);
  }
}
