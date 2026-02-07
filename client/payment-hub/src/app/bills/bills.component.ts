import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RequestService } from '../../services/request.service';
import { BillDataService } from '../../services/bill-data.service';
import { CommonModule } from '@angular/common';
import { PaymentComponent } from "../payment/payment.component";
import { ToastrService } from 'ngx-toastr'; // Add toast service for notifications
import { BILL_PATH } from '../../environment';
import { ToasterHelper } from '../../services/toast.service';
import { LocalStorageHelper } from '../../services/local-storage.service';
import { BillStatus,  Bill, BillsResponse } from '../models/bill.model';

@Component({
  selector: 'app-bills',
  templateUrl: './bills.component.html',
  styleUrls: ['./bills.component.css'],
  standalone: true,
  imports: [CommonModule, PaymentComponent]
})
export class BillsComponent implements OnInit, OnDestroy {
  // API Constants
  private readonly API_BASE_PATH = BILL_PATH;
  private userId: number = 0; // Get from UserService or Auth service

  // Bills data
  bills: Bill[] = [];
  filteredBills: Bill[] = [];
  billStats: BillsResponse | null = null;
  totalAmountOwed: number = 0;

  // UI States
  isLoading = false;
  error: string | null = null;
  selectedBill: Bill | null = null;
  showPaymentModal = false;

  // Filters and sorting
  statusFilter: 'ALL' | BillStatus = 'ALL';
  sortBy: 'due_date' | 'amount' | 'biller_name' = 'due_date';
  searchQuery = '';
  daysFilter: number = 30;

  // Pagination
  pageSize = 10;
  currentPage = 0;
  paginatedBills: Bill[] = [];

  // Status badge styling
  statusBadgeClass = {
    [BillStatus.DUE]: 'badge-warning',
    [BillStatus.OVERDUE]: 'badge-danger',
    [BillStatus.PENDING]: 'badge-info',
    [BillStatus.PAID]: 'badge-success'
  };

  statusBadgeIcon = {
    [BillStatus.DUE]: 'schedule',
    [BillStatus.OVERDUE]: 'error',
    [BillStatus.PENDING]: 'hourglass_empty',
    [BillStatus.PAID]: 'check_circle'
  };

  private destroy$ = new Subject<void>();

  constructor(
    private requestService: RequestService,
    private router: Router,
    private localStorage: LocalStorageHelper,
    private toastService: ToasterHelper,
    private billDataService: BillDataService // Inject BillDataService
  ) {}

  ngOnInit(): void {
    // Get userId from UserService or Auth service
    // Example: this.userId = this.userService.getCurrentUserId();
    // For now, assuming userId is available (you'll need to inject UserService)
    this.userId = this.localStorage.getItem('user_details')?.userId;
    
    this.loadBills();
  }

  /**
   * Load all bills for the current user
   */
  loadBills(): void {
    this.isLoading = true;
    this.billDataService.setLoading(true);
    this.error = null;

    const url = `${this.API_BASE_PATH}/${this.userId}`;

    this.requestService.get(url).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success && response.data) {
          this.bills = response.data;
          this.billDataService.setBills(this.bills); // Emit to sidebar
          this.loadBillStatistics();
          this.getTotalAmountOwed();
          this.applyFiltersAndSort();
          this.updatePagination();
          this.isLoading = false;
          this.billDataService.setLoading(false);
        } else {
          this.error = response.message || 'Failed to load bills';
          this.isLoading = false;
          this.billDataService.setLoading(false);
        }
      },
      error: (err: any) => {
        console.error('Error loading bills:', err);
        this.error = err?.error?.message || 'Failed to load bills. Please try again.';
        this.toastService.error(this.error);
        this.isLoading = false;
        this.billDataService.setLoading(false);
      }
    });
  }

  /**
   * Load bill statistics (total, pending, due, overdue)
   */
  private loadBillStatistics(): void {
    const url = `${this.API_BASE_PATH}/${this.userId}/statistics`;

    this.requestService.get(url).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success && response.data) {
          const stats = response.data;
          this.billStats = {
            bills: this.bills,
            total: this.bills.length,
            pending: stats.pending || 0,
            due: stats.due || 0,
            overdue: stats.overdue || 0
          };
          this.billDataService.setBillStats(this.billStats); // Emit to sidebar
        }
      },
      error: (err: any) => {
        console.error('Error loading bill statistics:', err);
        // Fallback: calculate stats from bills
        this.calculateStatistics();
      }
    });
  }

  /**
   * Fallback: Calculate statistics from bills array
   */
  private calculateStatistics(): void {
    const total = this.bills.length;
    const pending = this.bills.filter(b => b.bill_status === BillStatus.PENDING).length;
    const due = this.bills.filter(b => b.bill_status === BillStatus.DUE).length;
    const overdue = this.bills.filter(b => b.bill_status === BillStatus.OVERDUE).length;

    this.billStats = {
      bills: this.bills,
      total,
      pending,
      due,
      overdue
    };
    this.billDataService.setBillStats(this.billStats); // Emit to sidebar
  }

  /**
   * Get total amount owed by the user
   */
  private getTotalAmountOwed(): void {
    const url = `${this.API_BASE_PATH}/${this.userId}/total-owed`;

    this.requestService.get(url).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.totalAmountOwed = response.data || 0;
        }
      },
      error: (err: any) => {
        console.error('Error loading total amount owed:', err);
        // Fallback: calculate from bills
        this.totalAmountOwed = this.bills
          .filter(b => b.bill_status !== BillStatus.PAID)
          .reduce((sum, bill : any) => sum + bill.amount, 0);
      }
    });
  }

  /**
   * Search bills by biller name or ID
   */
  onSearch(query: any): void {
    this.searchQuery = query?.value;
    this.currentPage = 0;

    console.log(this.searchQuery);

    if (!query) {
      this.applyFiltersAndSort();
      this.updatePagination();
      return;
    }

    this.isLoading = true;
    const url = `${this.API_BASE_PATH}/${this.userId}/search`;

    this.requestService.get(url, { term: query }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success && response.data) {
          this.bills = response.data;
          this.applyFiltersAndSort();
          this.updatePagination();
          this.isLoading = false;
          this.toastService.success(`Found ${response.count} bill(s)`);
        } else {
          this.isLoading = false;
        }
      },
      error: (err: any) => {
        console.error('Error searching bills:', err);
        this.toastService.error(err?.error?.message || 'Search failed');
        this.isLoading = false;
      }
    });
  }

  /**
   * Filter bills by status
   */
  onStatusFilterChange(status: string): void {
    this.statusFilter = status as any;
    this.currentPage = 0;

    if (status === 'ALL') {
      this.applyFiltersAndSort();
      this.updatePagination();
      return;
    }

    // Call API to get bills by status
    this.isLoading = true;
    const url = `${this.API_BASE_PATH}/${this.userId}/status/${status.toLowerCase()}`;

    this.requestService.get(url).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success && response.data) {
          this.bills = response.data;
          this.applyFiltersAndSort();
          this.updatePagination();
          this.isLoading = false;
        } else {
          this.isLoading = false;
        }
      },
      error: (err: any) => {
        console.error('Error filtering bills by status:', err);
        this.toastService.error(err?.error?.message || 'Failed to filter bills');
        this.isLoading = false;
      }
    });
  }

  /**
   * Get bills due within X days
   */
  onDaysFilterChange(days: number): void {
    this.daysFilter = days;
    this.currentPage = 0;
    this.isLoading = true;

    const url = `${this.API_BASE_PATH}/${this.userId}/due?days=${days}`;

    this.requestService.get(url).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success && response.data) {
          this.bills = response.data;
          this.applyFiltersAndSort();
          this.updatePagination();
          this.isLoading = false;
        } else {
          this.isLoading = false;
        }
      },
      error: (err: any) => {
        console.error('Error filtering bills by due date:', err);
        this.toastService.error(err?.error?.message || 'Failed to filter bills');
        this.isLoading = false;
      }
    });
  }

  /**
   * Get overdue bills
   */
  loadOverdueBills(): void {
    this.isLoading = true;
    const url = `${this.API_BASE_PATH}/${this.userId}/overdue`;

    this.requestService.get(url).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success && response.data) {
          this.bills = response.data;
          this.applyFiltersAndSort();
          this.updatePagination();
          this.isLoading = false;
          this.statusFilter = BillStatus.OVERDUE as any;
        } else {
          this.isLoading = false;
        }
      },
      error: (err: any) => {
        console.error('Error loading overdue bills:', err);
        this.toastService.error(err?.error?.message || 'Failed to load overdue bills');
        this.isLoading = false;
      }
    });
  }

  /**
   * Get a specific bill by ID
   */
  getBillById(billId: number): void {
    const url = `${this.API_BASE_PATH}/${this.userId}/bill/${billId}`;

    this.requestService.get(url).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success && response.data) {
          const bill = response.data;
          this.selectedBill = bill;
          console.log('Bill details:', bill);
        }
      },
      error: (err: any) => {
        console.error('Error loading bill details:', err);
        this.toastService.error(err?.error?.message || 'Failed to load bill details');
      }
    });
  }

  /**
   * Create a new bill
   */
  createBill(billData: any): void {
    const url = `${this.API_BASE_PATH}/${this.userId}`;

    this.requestService.post(url, billData).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.bills.push(response.data);
          this.applyFiltersAndSort();
          this.updatePagination();
          this.toastService.success('Bill created successfully');
          this.loadBillStatistics();
        }
      },
      error: (err: any) => {
        console.error('Error creating bill:', err);
        this.toastService.error(err?.error?.message || 'Failed to create bill');
      }
    });
  }

  /**
   * Update an existing bill
   */
  updateBill(billId: number, billData: any): void {
    const url = `${this.API_BASE_PATH}/${this.userId}/bill/${billId}`;

    this.requestService.put(url, billData).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success) {
          const index = this.bills.findIndex((b: any) => b.bill_id === billId);
          if (index !== -1) {
            this.bills[index] = response.data;
            this.applyFiltersAndSort();
            this.updatePagination();
          }
          this.toastService.success('Bill updated successfully');
        }
      },
      error: (err: any) => {
        console.error('Error updating bill:', err);
        this.toastService.error(err?.error?.message || 'Failed to update bill');
      }
    });
  }

  /**
   * Delete a bill (soft delete)
   */
  deleteBill(billId: number): void {
    if (!confirm('Are you sure you want to delete this bill?')) {
      return;
    }

    const url = `${this.API_BASE_PATH}/${this.userId}/bill/${billId}`;

    this.requestService.delete(url).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.bills = this.bills.filter((b: any) => b.bill_id !== billId);
          this.applyFiltersAndSort();
          this.updatePagination();
          this.loadBillStatistics();
          this.toastService.success('Bill deleted successfully');
        }
      },
      error: (err: any) => {
        console.error('Error deleting bill:', err);
        this.toastService.error(err?.error?.message || 'Failed to delete bill');
      }
    });
  }

  /**
   * Mark a bill as paid
   */
  markBillAsPaid(billId: number): void {
    const url = `${this.API_BASE_PATH}/${this.userId}/bill/${billId}/pay`;

    this.requestService.put(url, {}).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (response.success) {
          const bill = this.bills.find((b: any) => b.bill_id === billId);
          if (bill) {
            bill.bill_status = BillStatus.PAID;
            this.applyFiltersAndSort();
            this.updatePagination();
            this.loadBillStatistics();
            this.getTotalAmountOwed();
            this.toastService.success({title: "Success", message: response?.message});
          }
        }
      },
      error: (err: any) => {
        console.error('Error marking bill as paid:', err);
        this.toastService.error(err?.error?.message || 'Failed to mark bill as paid');
      }
    });
  }

  /**
   * Apply filters and sort
   */
  applyFiltersAndSort(): void {
    let result = [...this.bills];

    // Apply status filter if not ALL
    if (this.statusFilter !== 'ALL') {
      result = result.filter(bill => bill.bill_status === this.statusFilter);
    }

    // Apply search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      result = result.filter(bill =>
        bill.biller_name.toLowerCase().includes(query) ||
        bill.bill_id?.toString().toLowerCase().includes(query) ||
        bill.category?.toLowerCase().includes(query)
      );
    }

    // Apply sorting
    result.sort((a: any, b: any) => {
      switch (this.sortBy) {
        case 'due_date':
          return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
        case 'amount':
          return b.amount - a.amount;
        case 'biller_name':
          return a.billerName.localeCompare(b.billerName);
        default:
          return 0;
      }
    });

    this.filteredBills = result;
  }

  /**
   * Handle sort change
   */
  onSortChange(sortBy: any): void {
    this.sortBy = sortBy as any;
    this.currentPage = 0;
    this.applyFiltersAndSort();
    this.updatePagination();
  }

  /**
   * Update pagination
   */
  updatePagination(): void {
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    this.paginatedBills = this.filteredBills.slice(start, end);
  }

  /**
   * Handle page change
   */
  onPageChange(page: number): void {
    this.currentPage = page;
    this.updatePagination();
  }

  /**
   * Open payment modal
   */
  openPaymentModal(bill: Bill): void {
    this.selectedBill = bill;
    this.showPaymentModal = true;
  }

  /**
   * Close payment modal
   */
  closePaymentModal(): void {
    this.showPaymentModal = false;
    this.selectedBill = null;
  }

  /**
   * Handle payment success
   */
  onPaymentSuccess(billId: number): void {
    this.markBillAsPaid(billId);
    this.closePaymentModal();
  }

  /**
   * Refresh bills
   */
  refreshBills(): void {
    this.currentPage = 0;
    this.statusFilter = 'ALL';
    this.searchQuery = '';
    this.loadBills();
  }

  /**
   * Get total pages
   */
  getTotalPages(): number {
    return Math.ceil(this.filteredBills.length / this.pageSize);
  }

  /**
   * Get pagination array
   */
  getPaginationArray(): number[] {
    const totalPages = this.getTotalPages();
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  /**
   * Get days until due
   */
  getDaysUntilDue(dueDate: string | Date): number {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const due = new Date(dueDate);
    due.setHours(0, 0, 0, 0);

    const diff = due.getTime() - today.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  /**
   * Get days until due text
   */
  getDaysUntilDueText(dueDate: string | Date): string {
    const days = this.getDaysUntilDue(dueDate);

    if (days < 0) {
      return `${Math.abs(days)} day${Math.abs(days) > 1 ? 's' : ''} overdue`;
    } else if (days === 0) {
      return 'Due today';
    } else if (days === 1) {
      return 'Due tomorrow';
    } else {
      return `Due in ${days} day${days > 1 ? 's' : ''}`;
    }
  }

  /**
   * Format currency
   */
  formatCurrency(amount: any): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  /**
   * Cleanup
   */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}